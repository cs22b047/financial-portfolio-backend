package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Portfolio analytics and summary service.
 * Provides portfolio-level calculations and reports.
 */
@Service
@Transactional(readOnly = true)
public class PortfolioService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PortfolioTransactionRepository transactionRepository;

    // ==================== Portfolio Summary ====================

    public Map<String, Object> getPortfolioSummary() {
        List<Position> openPositions = positionRepository.findOpenPositions();
        List<Position> watchlist = positionRepository.findWatchlistPositions();

        Map<String, Object> summary = new HashMap<>();

        // Calculate totals
        BigDecimal totalMarketValue = openPositions.stream()
                .map(Position::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostBasis = openPositions.stream()
                .map(Position::getTotalCostBasis)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnrealizedGainLoss = openPositions.stream()
                .map(Position::getUnrealizedGainLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Percentages
        BigDecimal overallGainLossPercent = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            overallGainLossPercent = totalUnrealizedGainLoss
                    .divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        summary.put("totalMarketValue", totalMarketValue);
        summary.put("totalCostBasis", totalCostBasis);
        summary.put("totalUnrealizedGainLoss", totalUnrealizedGainLoss);
        summary.put("overallGainLossPercent", overallGainLossPercent);
        summary.put("openPositionsCount", openPositions.size());
        summary.put("watchlistCount", watchlist.size());

        return summary;
    }

    // ==================== Allocation Analysis ====================

    public Map<String, Object> getAllocationByType() {
        List<Position> positions = positionRepository.findOpenPositions();
        BigDecimal totalValue = positions.stream()
                .map(Position::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<InstrumentType, BigDecimal> valueByType = positions.stream()
                .filter(p -> p.getInstrument() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getInstrument().getInstrumentType(),
                        Collectors.reducing(BigDecimal.ZERO, Position::getMarketValue, BigDecimal::add)
                ));

        Map<String, Object> allocation = new HashMap<>();
        allocation.put("totalValue", totalValue);

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (Map.Entry<InstrumentType, BigDecimal> entry : valueByType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey().name());
            item.put("displayName", entry.getKey().getDisplayName());
            item.put("value", entry.getValue());
            item.put("percent", totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            breakdown.add(item);
        }

        allocation.put("breakdown", breakdown);
        return allocation;
    }

    public Map<String, Object> getAllocationBySector() {
        List<Position> positions = positionRepository.findOpenPositions();
        BigDecimal totalValue = positions.stream()
                .map(Position::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> valueBySector = positions.stream()
                .filter(p -> p.getInstrument() != null && p.getInstrument().getSector() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getInstrument().getSector().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Position::getMarketValue, BigDecimal::add)
                ));

        Map<String, Object> allocation = new HashMap<>();
        allocation.put("totalValue", totalValue);

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : valueBySector.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("sector", entry.getKey());
            item.put("value", entry.getValue());
            item.put("percent", totalValue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            breakdown.add(item);
        }

        allocation.put("breakdown", breakdown);
        return allocation;
    }

    // ==================== Top Performers ====================

    public List<Map<String, Object>> getTopPerformers(int limit) {
        return positionRepository.findOpenPositions().stream()
                .filter(p -> p.getUnrealizedGainLossPercent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getUnrealizedGainLossPercent().compareTo(a.getUnrealizedGainLossPercent()))
                .limit(limit)
                .map(this::positionToSummaryMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getWorstPerformers(int limit) {
        return positionRepository.findOpenPositions().stream()
                .filter(p -> p.getUnrealizedGainLossPercent().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(Position::getUnrealizedGainLossPercent))
                .limit(limit)
                .map(this::positionToSummaryMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLargestPositions(int limit) {
        return positionRepository.findOpenPositionsOrderByValue().stream()
                .limit(limit)
                .map(this::positionToSummaryMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> positionToSummaryMap(Position p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("symbol", p.getSymbol());
        map.put("name", p.getName());
        map.put("quantity", p.getTotalQuantity());
        map.put("marketValue", p.getMarketValue());
        map.put("costBasis", p.getTotalCostBasis());
        map.put("gainLoss", p.getUnrealizedGainLoss());
        map.put("gainLossPercent", p.getUnrealizedGainLossPercent());
        map.put("currentPrice", p.getCurrentPrice());
        map.put("avgCostBasis", p.getAverageCostBasis());
        return map;
    }



    // ==================== Realized Gains Summary ====================

    public Map<String, Object> getRealizedGainsSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        BigDecimal totalGains = transactionRepository.sumRealizedGainLoss(startDate, endDate);
        BigDecimal longTermGains = transactionRepository.sumLongTermGainLoss(startDate, endDate);
        BigDecimal shortTermGains = transactionRepository.sumShortTermGainLoss(startDate, endDate);
        BigDecimal totalFees = transactionRepository.sumFees(startDate, endDate);

        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalRealizedGainLoss", totalGains != null ? totalGains : BigDecimal.ZERO);
        summary.put("longTermGainLoss", longTermGains != null ? longTermGains : BigDecimal.ZERO);
        summary.put("shortTermGainLoss", shortTermGains != null ? shortTermGains : BigDecimal.ZERO);
        summary.put("totalFees", totalFees != null ? totalFees : BigDecimal.ZERO);

        return summary;
    }

    // ==================== Watchlist Alerts ====================

    public List<Map<String, Object>> getWatchlistAlerts() {
        return positionRepository.findWatchlistWithTargetPrice().stream()
                .filter(p -> p.isTargetBuyPriceReached())
                .map(p -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("id", p.getId());
                    alert.put("symbol", p.getSymbol());
                    alert.put("name", p.getName());
                    alert.put("currentPrice", p.getCurrentPrice());
                    alert.put("targetPrice", p.getTargetBuyPrice());
                    alert.put("addedDate", p.getAddedToWatchlistDate());
                    return alert;
                })
                .collect(Collectors.toList());
    }
}
