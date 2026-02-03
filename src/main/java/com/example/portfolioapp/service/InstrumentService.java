package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing market instruments (stocks, bonds, crypto).
 * Handles instrument creation, updates, and market data management.
 */
@Service
@Transactional
public class InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    // ==================== CRUD Operations ====================

    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findByIsActiveTrue();
    }

    public Optional<Instrument> getById(Long id) {
        return instrumentRepository.findById(id);
    }

    public Optional<Instrument> getBySymbol(String symbol) {
        return instrumentRepository.findBySymbol(symbol.toUpperCase());
    }

    public List<Instrument> getByType(InstrumentType type) {
        return instrumentRepository.findByInstrumentType(type);
    }

    public List<Instrument> searchInstruments(String query) {
        return instrumentRepository.searchByNameOrSymbol(query);
    }

    // ==================== Create/Update Instrument ====================

    public Instrument createInstrument(String symbol, String name, InstrumentType type) {
        // Check if already exists
        Optional<Instrument> existing = instrumentRepository.findBySymbol(symbol.toUpperCase());
        if (existing.isPresent()) {
            return existing.get();
        }

        Instrument instrument = new Instrument(type, symbol.toUpperCase(), name);
        return instrumentRepository.save(instrument);
    }

    public Instrument createOrUpdateInstrument(String symbol, String name, InstrumentType type,
                                                BigDecimal currentPrice, String currencyCode) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElse(new Instrument(type, symbol.toUpperCase(), name));

        instrument.setName(name);
        instrument.setCurrentPrice(currentPrice);
        instrument.setCurrencyCode(currencyCode);
        instrument.setLastUpdated(LocalDateTime.now());

        return instrumentRepository.save(instrument);
    }

    public Instrument updatePrice(String symbol, BigDecimal price) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol));

        BigDecimal previousClose = instrument.getCurrentPrice();
        instrument.setCurrentPrice(price);
        instrument.setPreviousClose(previousClose);

        if (previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = price.subtract(previousClose);
            instrument.setDayChange(change);
            instrument.setDayChangePercent(change.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        }

        return instrumentRepository.save(instrument);
    }

    public Instrument updateMarketData(Long instrumentId, BigDecimal currentPrice, BigDecimal dayHigh,
                                        BigDecimal dayLow, Long volume, String marketStatus) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));

        instrument.setCurrentPrice(currentPrice);
        instrument.setDayHigh(dayHigh);
        instrument.setDayLow(dayLow);
        instrument.setVolume(volume);
        instrument.setMarketStatus(marketStatus);
        instrument.setLastUpdated(LocalDateTime.now());

        return instrumentRepository.save(instrument);
    }

    // ==================== Stock Metrics ====================

    public Instrument updateStockMetrics(String symbol, BigDecimal peRatio, BigDecimal dividendYield,
                                          BigDecimal beta, BigDecimal eps, Long marketCap) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol));

        StockMetrics metrics = instrument.getStockMetrics();
        if (metrics == null) {
            metrics = new StockMetrics(instrument);
            instrument.setStockMetrics(metrics);
        }

        metrics.setPeRatio(peRatio);
        metrics.setDividendYield(dividendYield);
        metrics.setBeta(beta);
        metrics.setEarningsPerShare(eps);
        instrument.setMarketCap(marketCap);

        return instrumentRepository.save(instrument);
    }

    // ==================== Reference Data ====================

    public Instrument setSector(String symbol, String sectorCode) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol));

        Sector sector = sectorRepository.findByCode(sectorCode)
                .orElseGet(() -> sectorRepository.save(new Sector(sectorCode, sectorCode)));

        instrument.setSector(sector);
        return instrumentRepository.save(instrument);
    }

    public Instrument setExchange(String symbol, String exchangeCode) {
        Instrument instrument = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Instrument not found: " + symbol));

        Exchange exchange = exchangeRepository.findByCode(exchangeCode)
                .orElseGet(() -> exchangeRepository.save(new Exchange(exchangeCode, exchangeCode, ExchangeType.STOCK)));

        instrument.setExchange(exchange);
        return instrumentRepository.save(instrument);
    }

    // ==================== Bulk Operations ====================

    public List<Instrument> getStaleInstruments(int maxAgeMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(maxAgeMinutes);
        return instrumentRepository.findStaleInstruments(cutoff);
    }

    public void deactivateInstrument(Long id) {
        instrumentRepository.findById(id).ifPresent(instrument -> {
            instrument.setIsActive(false);
            instrumentRepository.save(instrument);
        });
    }

    public boolean existsBySymbol(String symbol) {
        return instrumentRepository.existsBySymbol(symbol.toUpperCase());
    }
}
