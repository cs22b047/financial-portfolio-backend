package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import com.example.portfolioapp.exception.DuplicateResourceException;
import com.example.portfolioapp.exception.InvalidOperationException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AssetRepository;
import com.example.portfolioapp.repository.MarketDataRepository;
import com.example.portfolioapp.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Asset operations (user's watchlist and owned stocks)
 * Primary data source: assets table, with market_data references for market info
 */
@Service
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MarketDataRepository marketDataRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Buy stock - creates new or updates existing owned asset
     * Queries assets table first, uses market_data for new asset creation
     */
    @Transactional
    public Asset buyStock(String symbol, BigDecimal quantity, BigDecimal price, LocalDate transactionDate) {
        logger.info("Buying stock: {}, quantity: {}, price: {}", symbol, quantity, price);

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Quantity must be greater than zero");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Price must be greater than zero");
        }

        // First check if asset already exists in assets table
        Optional<Asset> existingAsset = assetRepository.findBySymbol(symbol);

        Asset asset;
        if (existingAsset.isPresent() && existingAsset.get().getStatus() == AssetStatus.OWNED) {
            // Update existing owned asset with weighted average cost
            asset = existingAsset.get();
            BigDecimal oldQuantity = asset.getQuantity();
            BigDecimal oldCost = asset.getAverageCost() != null ? asset.getAverageCost() : price;
            BigDecimal newQuantity = oldQuantity.add(quantity);
            
            // Weighted average cost = (old_qty * old_cost + new_qty * new_price) / total_qty
            BigDecimal totalCost = oldQuantity.multiply(oldCost).add(quantity.multiply(price));
            BigDecimal avgCost = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
            
            asset.setQuantity(newQuantity);
            asset.setAverageCost(avgCost);
            asset.setCurrentPrice(price);
        } else if (existingAsset.isPresent()) {
            // Convert from WATCHLIST/RESEARCH to OWNED
            asset = existingAsset.get();
            asset.setStatus(AssetStatus.OWNED);
            asset.setQuantity(quantity);
            asset.setAverageCost(price);
            asset.setPurchasePrice(price);
            asset.setPurchaseDate(transactionDate);
            asset.setCurrentPrice(price);
        } else {
            // Asset doesn't exist in assets table - create from market_data
            MarketData marketData = marketDataRepository.findBySymbol(symbol)
                    .orElseThrow(() -> new ResourceNotFoundException("MarketData", "symbol", symbol));
            
            asset = new Asset(marketData, AssetStatus.OWNED);
            asset.setQuantity(quantity);
            asset.setAverageCost(price);
            asset.setPurchasePrice(price);
            asset.setPurchaseDate(transactionDate);
            asset.setCurrentPrice(price);
        }

        asset = assetRepository.save(asset);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setTransactionType(TransactionType.BUY);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTransactionDate(transactionDate.atStartOfDay());
        // Get currency from market_data reference
        transaction.setCurrency(asset.getMarketData() != null ? 
            asset.getMarketData().getCurrency() : "USD");
        transactionRepository.save(transaction);

        return asset;
    }

    /**
     * Sell stock - reduces quantity or deletes if all sold
     */
    @Transactional
    public Asset sellStock(String symbol, BigDecimal quantity, BigDecimal price, LocalDate transactionDate) {
        logger.info("Selling stock: {}, quantity: {}, price: {}", symbol, quantity, price);

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Quantity must be greater than zero");
        }

        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));

        if (asset.getStatus() != AssetStatus.OWNED) {
            throw new InvalidOperationException("Can only sell owned assets");
        }

        if (asset.getQuantity().compareTo(quantity) < 0) {
            throw new InvalidOperationException(
                String.format("Insufficient shares. Owned: %s, Trying to sell: %s", 
                    asset.getQuantity(), quantity));
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setTransactionType(TransactionType.SELL);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setTransactionDate(transactionDate.atStartOfDay());
        // Get currency from market_data reference
        transaction.setCurrency(asset.getMarketData() != null ? 
            asset.getMarketData().getCurrency() : "USD");
        transactionRepository.save(transaction);

        // Update or delete asset
        BigDecimal newQuantity = asset.getQuantity().subtract(quantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Sold all shares - delete asset
            assetRepository.delete(asset);
            return null;
        } else {
            // Partial sell - update quantity
            asset.setQuantity(newQuantity);
            return assetRepository.save(asset);
        }
    }

    /**
     * Add stock to watchlist
     * Queries assets table first, creates from market_data if needed
     */
    @Transactional
    public Asset addToWatchlist(String symbol) {
        logger.info("Adding symbol to watchlist: {}", symbol);

        // Check if asset already exists in assets table
        Optional<Asset> existing = assetRepository.findBySymbol(symbol);
        if (existing.isPresent()) {
            Asset asset = existing.get();
            if (asset.getStatus() == AssetStatus.WATCHLIST) {
                throw new DuplicateResourceException("Watchlist asset already exists for symbol: " + symbol);
            } else if (asset.getStatus() == AssetStatus.OWNED) {
                throw new InvalidOperationException("Cannot add owned asset to watchlist: " + symbol);
            }
            // Convert RESEARCH/SOLD to WATCHLIST
            asset.setStatus(AssetStatus.WATCHLIST);
            asset.setAddedToWatchlistDate(LocalDate.now());
            return assetRepository.save(asset);
        }

        // Asset doesn't exist - create from market_data
        MarketData marketData = marketDataRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("MarketData", "symbol", symbol));

        Asset asset = new Asset(marketData, AssetStatus.WATCHLIST);
        asset.setAddedToWatchlistDate(LocalDate.now());
        asset.setQuantity(BigDecimal.ZERO);
        asset.setAverageCost(BigDecimal.ZERO);
        
        return assetRepository.save(asset);
    }

    /**
     * Mark asset as owned (convert from watchlist or create new)
     * Queries assets table, updates current_price from market_data
     */
    @Transactional
    public Asset markAsOwned(String symbol, BigDecimal quantity, BigDecimal purchasePrice, LocalDate purchaseDate) {
        logger.info("Marking symbol as owned: {}, quantity: {}, price: {}", symbol, quantity, purchasePrice);

        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));
        
        if (asset.getStatus() == AssetStatus.OWNED) {
            throw new InvalidOperationException("Asset is already owned: " + symbol);
        }

        // Set ownership details
        asset.setStatus(AssetStatus.OWNED);
        asset.setQuantity(quantity);
        asset.setPurchasePrice(purchasePrice);
        asset.setPurchaseDate(purchaseDate);
        asset.setAverageCost(purchasePrice);
        
        // Update current price from market_data reference
        if (asset.getMarketData() != null) {
            asset.setCurrentPrice(asset.getMarketData().getCurrentPrice());
        } else {
            asset.setCurrentPrice(purchasePrice);
        }

        return assetRepository.save(asset);
    }

    /**
     * Update asset quantity (for buy/sell transactions)
     */
    @Transactional
    public Asset updateQuantity(Long assetId, BigDecimal newQuantity) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        if (asset.getStatus() != AssetStatus.OWNED) {
            throw new InvalidOperationException("Can only update quantity for owned assets");
        }

        asset.setQuantity(newQuantity);
        
        // If quantity becomes zero, mark as sold
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            asset.setStatus(AssetStatus.SOLD);
        }

        return assetRepository.save(asset);
    }

    /**
     * Update current prices for all owned assets from market_data references
     * Syncs asset.current_price with market_data.current_price
     */
    @Transactional
    public void updateAllCurrentPrices() {
        List<Asset> ownedAssets = assetRepository.findAllOwned();
        
        for (Asset asset : ownedAssets) {
            if (asset.getMarketData() != null) {
                asset.setCurrentPrice(asset.getMarketData().getCurrentPrice());
            }
        }
        
        assetRepository.saveAll(ownedAssets);
        logger.info("Updated current prices for {} owned assets from market_data", ownedAssets.size());
    }

    /**
     * Get all owned assets
     */
    public List<Asset> getAllOwnedAssets() {
        return assetRepository.findAllOwned();
    }

    /**
     * Get all watchlist assets
     */
    public List<Asset> getAllWatchlistAssets() {
        return assetRepository.findAllWatchlist();
    }

    /**
     * Get asset by symbol
     */
    public Asset getBySymbol(String symbol) {
        return assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));
    }

    /**
     * Get asset by ID
     */
    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    /**
     * Calculate total portfolio value
     */
    public BigDecimal getTotalPortfolioValue() {
        BigDecimal total = assetRepository.calculateTotalPortfolioValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total investment cost
     */
    public BigDecimal getTotalInvestmentCost() {
        BigDecimal total = assetRepository.calculateTotalInvestmentCost();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get top performing assets
     */
    public List<Asset> getTopPerformers(int limit) {
        List<Asset> performers = assetRepository.findTopPerformers();
        return performers.stream().limit(limit).toList();
    }

    /**
     * Delete asset (remove from watchlist or portfolio)
     */
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        
        assetRepository.delete(asset);
        logger.info("Deleted asset: {}", asset.getSymbol());
    }

    /**
     * Remove from watchlist by symbol
     */
    @Transactional
    public void removeFromWatchlist(String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));
        
        if (asset.getStatus() != AssetStatus.WATCHLIST) {
            throw new InvalidOperationException("Can only remove WATCHLIST assets. Asset status: " + asset.getStatus());
        }
        
        assetRepository.delete(asset);
        logger.info("Removed from watchlist: {}", symbol);
    }

    /**
     * Get owned assets (alias for getAllOwnedAssets)
     */
    public List<Asset> getOwnedAssets() {
        return getAllOwnedAssets();
    }

    /**
     * Get watchlist assets (alias for getAllWatchlistAssets)
     */
    public List<Asset> getWatchlistAssets() {
        return getAllWatchlistAssets();
    }

    /**
     * Get asset by symbol (alias for getBySymbol)
     */
    public Asset getAssetBySymbol(String symbol) {
        return getBySymbol(symbol);
    }

    /**
     * Calculate total portfolio value (alias for getTotalPortfolioValue)
     */
    public BigDecimal calculateTotalPortfolioValue() {
        return getTotalPortfolioValue();
    }

    /**
     * Calculate total cost (alias for getTotalInvestmentCost)
     */
    public BigDecimal calculateTotalCost() {
        return getTotalInvestmentCost();
    }

    /**
     * Calculate unrealized gain/loss
     */
    public BigDecimal calculateUnrealizedGainLoss() {
        BigDecimal totalValue = getTotalPortfolioValue();
        BigDecimal totalCost = getTotalInvestmentCost();
        return totalValue.subtract(totalCost);
    }

    /**
     * Get all assets (all statuses)
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    /**
     * Get asset by ID (returns Optional)
     */
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    /**
     * Get assets by status
     */
    public List<Asset> getAssetsByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    /**
     * Add to watchlist with optional target price
     * Queries assets table first, creates from market_data if needed
     */
    @Transactional
    public Asset addToWatchlist(String symbol, BigDecimal targetPrice) {
        logger.info("Adding symbol to watchlist: {} with target price: {}", symbol, targetPrice);

        // Check if asset already exists in assets table
        Optional<Asset> existing = assetRepository.findBySymbol(symbol);
        if (existing.isPresent()) {
            Asset asset = existing.get();
            if (asset.getStatus() == AssetStatus.WATCHLIST) {
                throw new DuplicateResourceException("Watchlist asset already exists for symbol: " + symbol);
            } else if (asset.getStatus() == AssetStatus.OWNED) {
                throw new InvalidOperationException("Cannot add owned asset to watchlist: " + symbol);
            }
            // Convert RESEARCH/SOLD to WATCHLIST
            asset.setStatus(AssetStatus.WATCHLIST);
            asset.setAddedToWatchlistDate(LocalDate.now());
            asset.setTargetPrice(targetPrice);
            return assetRepository.save(asset);
        }

        // Asset doesn't exist - create from market_data
        MarketData marketData = marketDataRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("MarketData", "symbol", symbol));

        Asset asset = new Asset(marketData, AssetStatus.WATCHLIST);
        asset.setAddedToWatchlistDate(LocalDate.now());
        asset.setTargetPrice(targetPrice);
        asset.setQuantity(BigDecimal.ZERO);
        asset.setAverageCost(BigDecimal.ZERO);
        
        return assetRepository.save(asset);
    }

    /**
     * Remove from watchlist by ID
     */
    @Transactional
    public void removeFromWatchlist(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
        
        if (asset.getStatus() != AssetStatus.WATCHLIST) {
            throw new InvalidOperationException("Can only remove WATCHLIST assets. Asset status: " + asset.getStatus());
        }
        
        assetRepository.delete(asset);
        logger.info("Removed from watchlist: {}", asset.getSymbol());
    }

    /**
     * Update asset
     */
    @Transactional
    public Asset updateAsset(Long id, Asset updates) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));

        // Update allowed fields
        if (updates.getTargetPrice() != null) {
            asset.setTargetPrice(updates.getTargetPrice());
        }
        if (updates.getNotes() != null) {
            asset.setNotes(updates.getNotes());
        }
        if (updates.getPriorityRank() != null) {
            asset.setPriorityRank(updates.getPriorityRank());
        }
        if (updates.getPriceAlertsEnabled() != null) {
            asset.setPriceAlertsEnabled(updates.getPriceAlertsEnabled());
        }

        asset.setUpdatedDate(LocalDateTime.now());
        return assetRepository.save(asset);
    }
}
