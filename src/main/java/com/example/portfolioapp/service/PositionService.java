package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing portfolio positions (holdings and watchlist).
 * Handles buying, selling, and position tracking with tax lot support.
 */
@Service
@Transactional
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TaxLotRepository taxLotRepository;

    @Autowired
    private PortfolioTransactionRepository transactionRepository;

    // ==================== Query Methods ====================

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public List<Position> getOpenPositions() {
        return positionRepository.findOpenPositions();
    }

    public List<Position> getWatchlist() {
        return positionRepository.findWatchlistPositions();
    }

    public Optional<Position> getById(Long id) {
        return positionRepository.findById(id);
    }

    public Optional<Position> getBySymbol(String symbol) {
        return positionRepository.findBySymbol(symbol.toUpperCase());
    }

    public Optional<Position> getOpenPositionBySymbol(String symbol) {
        return positionRepository.findBySymbolAndStatus(symbol.toUpperCase(), PositionStatus.OPEN);
    }

    public List<Position> getOpenPositionsByType(InstrumentType type) {
        return positionRepository.findOpenPositionsByType(type);
    }

    // ==================== Buy Operations ====================

    /**
     * Record a purchase - creates/updates position and tax lot.
     */
    public Position buy(String symbol, BigDecimal quantity, BigDecimal pricePerUnit,
                        LocalDate purchaseDate, BigDecimal fees) {
        // Get or create instrument
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol + ". Create instrument first."));

        // Get or create position
        Position position = positionRepository.findBySymbolAndStatus(symbol.toUpperCase(), PositionStatus.OPEN)
                .orElseGet(() -> {
                    Position newPosition = new Position(instrument, PositionStatus.OPEN);
                    newPosition.setTotalQuantity(BigDecimal.ZERO);
                    newPosition.setTotalCostBasis(BigDecimal.ZERO);
                    newPosition.setCostBasisMethod(CostBasisMethod.FIFO);
                    return newPosition;
                });

        // Update position quantities
        BigDecimal newQuantity = position.getTotalQuantity().add(quantity);
        BigDecimal purchaseCost = quantity.multiply(pricePerUnit);
        if (fees != null) {
            purchaseCost = purchaseCost.add(fees);
        }
        BigDecimal newTotalCost = position.getTotalCostBasis().add(purchaseCost);

        position.setTotalQuantity(newQuantity);
        position.setTotalCostBasis(newTotalCost);
        position.setAverageCostBasis(newTotalCost.divide(newQuantity, 8, RoundingMode.HALF_UP));

        if (position.getFirstPurchaseDate() == null) {
            position.setFirstPurchaseDate(purchaseDate);
        }
        position.setLastPurchaseDate(purchaseDate);

        position = positionRepository.save(position);

        // Create tax lot
        TaxLot taxLot = new TaxLot(position, purchaseDate, pricePerUnit, quantity, fees);
        taxLot = taxLotRepository.save(taxLot);

        // Create transaction record
        PortfolioTransaction transaction = new PortfolioTransaction(
                position, SimpleTransactionType.BUY, purchaseDate, quantity, pricePerUnit);
        transaction.setFees(fees);
        transaction.setTotalAmount(purchaseCost);
        transactionRepository.save(transaction);

        return position;
    }

    // ==================== Sell Operations ====================

    /**
     * Record a sale - reduces position and closes tax lots based on cost basis method.
     */
    public Position sell(String symbol, BigDecimal quantity, BigDecimal pricePerUnit,
                         LocalDate sellDate, BigDecimal fees) {
        Position position = positionRepository.findBySymbolAndStatus(symbol.toUpperCase(), PositionStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("No open position for: " + symbol));

        if (quantity.compareTo(position.getTotalQuantity()) > 0) {
            throw new RuntimeException("Cannot sell more than owned. Owned: " + position.getTotalQuantity());
        }

        // Get tax lots based on cost basis method
        List<TaxLot> lots = getLotsByCostBasisMethod(position);

        BigDecimal remainingToSell = quantity;
        BigDecimal totalCostBasisSold = BigDecimal.ZERO;
        BigDecimal totalRealizedGainLoss = BigDecimal.ZERO;
        boolean isLongTerm = true;

        for (TaxLot lot : lots) {
            if (remainingToSell.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal lotRemaining = lot.getRemainingQuantity();
            BigDecimal sellFromLot = remainingToSell.min(lotRemaining);

            // Calculate gain/loss for this lot
            BigDecimal costBasis = lot.sellShares(sellFromLot);
            totalCostBasisSold = totalCostBasisSold.add(costBasis);

            BigDecimal proceeds = sellFromLot.multiply(pricePerUnit);
            BigDecimal gainLoss = proceeds.subtract(costBasis);
            totalRealizedGainLoss = totalRealizedGainLoss.add(gainLoss);

            // Check if short-term (any lot < 1 year makes it mixed)
            if (!lot.isLongTerm()) {
                isLongTerm = false;
            }

            taxLotRepository.save(lot);
            remainingToSell = remainingToSell.subtract(sellFromLot);
        }

        // Update position
        BigDecimal newQuantity = position.getTotalQuantity().subtract(quantity);
        BigDecimal newCostBasis = position.getTotalCostBasis().subtract(totalCostBasisSold);

        position.setTotalQuantity(newQuantity);
        position.setTotalCostBasis(newCostBasis);

        if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            position.setAverageCostBasis(newCostBasis.divide(newQuantity, 8, RoundingMode.HALF_UP));
        } else {
            position.setStatus(PositionStatus.CLOSED);
            position.setAverageCostBasis(BigDecimal.ZERO);
        }

        position = positionRepository.save(position);

        // Create sell transaction
        BigDecimal proceeds = quantity.multiply(pricePerUnit);
        if (fees != null) {
            proceeds = proceeds.subtract(fees);
            totalRealizedGainLoss = totalRealizedGainLoss.subtract(fees);
        }

        PortfolioTransaction transaction = new PortfolioTransaction(
                position, SimpleTransactionType.SELL, sellDate, quantity, pricePerUnit);
        transaction.setFees(fees);
        transaction.setTotalAmount(proceeds);
        transaction.setRealizedGainLoss(totalRealizedGainLoss);
        transaction.setIsLongTerm(isLongTerm);
        transactionRepository.save(transaction);

        return position;
    }

    private List<TaxLot> getLotsByCostBasisMethod(Position position) {
        CostBasisMethod method = position.getCostBasisMethod();
        if (method == null) method = CostBasisMethod.FIFO;

        return switch (method) {
            case FIFO -> taxLotRepository.findOpenLotsFIFO(position.getId());
            case LIFO -> taxLotRepository.findOpenLotsLIFO(position.getId());
            case HIFO -> taxLotRepository.findOpenLotsHIFO(position.getId());
            default -> taxLotRepository.findOpenLotsFIFO(position.getId());
        };
    }

    // ==================== Watchlist Operations ====================

    public Position addToWatchlist(String symbol, BigDecimal targetBuyPrice, String notes) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol));

        // Check if already on watchlist
        Optional<Position> existing = positionRepository.findBySymbolAndStatus(
                symbol.toUpperCase(), PositionStatus.WATCHLIST);
        if (existing.isPresent()) {
            Position pos = existing.get();
            pos.setTargetBuyPrice(targetBuyPrice);
            pos.setNotes(notes);
            return positionRepository.save(pos);
        }

        Position position = Position.createWatchlist(instrument, targetBuyPrice);
        position.setNotes(notes);
        return positionRepository.save(position);
    }

    public void removeFromWatchlist(Long positionId) {
        positionRepository.findById(positionId).ifPresent(position -> {
            if (position.getStatus().isWatching()) {
                positionRepository.delete(position);
            }
        });
    }

    // ==================== Position Updates ====================

    public Position updateNotes(Long positionId, String notes) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        position.setNotes(notes);
        return positionRepository.save(position);
    }

    public Position setTargetSellPrice(Long positionId, BigDecimal targetPrice) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        position.setTargetSellPrice(targetPrice);
        return positionRepository.save(position);
    }

    public Position setCostBasisMethod(Long positionId, CostBasisMethod method) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        position.setCostBasisMethod(method);
        return positionRepository.save(position);
    }

    // ==================== Tax Lots ====================

    public List<TaxLot> getTaxLots(Long positionId) {
        return taxLotRepository.findByPositionId(positionId);
    }

    public List<TaxLot> getOpenTaxLots(Long positionId) {
        return taxLotRepository.findOpenLots(positionId);
    }
}
