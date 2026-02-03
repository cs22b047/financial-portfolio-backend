package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Instrument;
import com.example.portfolioapp.entity.InstrumentType;
import com.example.portfolioapp.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for market instruments (stocks, bonds, crypto).
 */
@RestController
@RequestMapping("/api/instruments")
@CrossOrigin(origins = "*")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    // ==================== GET Endpoints ====================

    @GetMapping
    public List<Instrument> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrument> getById(@PathVariable Long id) {
        return instrumentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Instrument> getBySymbol(@PathVariable String symbol) {
        return instrumentService.getBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public List<Instrument> getByType(@PathVariable InstrumentType type) {
        return instrumentService.getByType(type);
    }

    @GetMapping("/search")
    public List<Instrument> search(@RequestParam String q) {
        return instrumentService.searchInstruments(q);
    }

    // ==================== POST Endpoints ====================

    @PostMapping
    public ResponseEntity<Instrument> createInstrument(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        String name = (String) request.get("name");
        InstrumentType type = InstrumentType.valueOf((String) request.get("type"));

        BigDecimal price = request.get("currentPrice") != null
                ? new BigDecimal(request.get("currentPrice").toString()) : null;
        String currency = (String) request.getOrDefault("currency", "USD");

        Instrument instrument = instrumentService.createOrUpdateInstrument(symbol, name, type, price, currency);
        return ResponseEntity.ok(instrument);
    }

    // ==================== PUT Endpoints ====================

    @PutMapping("/{symbol}/price")
    public ResponseEntity<Instrument> updatePrice(@PathVariable String symbol,
                                                   @RequestBody Map<String, Object> request) {
        BigDecimal price = new BigDecimal(request.get("price").toString());
        Instrument instrument = instrumentService.updatePrice(symbol, price);
        return ResponseEntity.ok(instrument);
    }

    @PutMapping("/{symbol}/market-data")
    public ResponseEntity<Instrument> updateMarketData(@PathVariable String symbol,
                                                        @RequestBody Map<String, Object> request) {
        Instrument existing = instrumentService.getBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));

        BigDecimal currentPrice = request.get("currentPrice") != null
                ? new BigDecimal(request.get("currentPrice").toString()) : null;
        BigDecimal dayHigh = request.get("dayHigh") != null
                ? new BigDecimal(request.get("dayHigh").toString()) : null;
        BigDecimal dayLow = request.get("dayLow") != null
                ? new BigDecimal(request.get("dayLow").toString()) : null;
        Long volume = request.get("volume") != null
                ? Long.parseLong(request.get("volume").toString()) : null;
        String marketStatus = (String) request.get("marketStatus");

        Instrument instrument = instrumentService.updateMarketData(
                existing.getId(), currentPrice, dayHigh, dayLow, volume, marketStatus);
        return ResponseEntity.ok(instrument);
    }

    @PutMapping("/{symbol}/stock-metrics")
    public ResponseEntity<Instrument> updateStockMetrics(@PathVariable String symbol,
                                                          @RequestBody Map<String, Object> request) {
        BigDecimal peRatio = request.get("peRatio") != null
                ? new BigDecimal(request.get("peRatio").toString()) : null;
        BigDecimal dividendYield = request.get("dividendYield") != null
                ? new BigDecimal(request.get("dividendYield").toString()) : null;
        BigDecimal beta = request.get("beta") != null
                ? new BigDecimal(request.get("beta").toString()) : null;
        BigDecimal eps = request.get("eps") != null
                ? new BigDecimal(request.get("eps").toString()) : null;
        Long marketCap = request.get("marketCap") != null
                ? Long.parseLong(request.get("marketCap").toString()) : null;

        Instrument instrument = instrumentService.updateStockMetrics(symbol, peRatio, dividendYield, beta, eps, marketCap);
        return ResponseEntity.ok(instrument);
    }

    // ==================== DELETE Endpoints ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateInstrument(@PathVariable Long id) {
        instrumentService.deactivateInstrument(id);
        return ResponseEntity.noContent().build();
    }
}
