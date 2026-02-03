package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.CostBasisMethod;
import com.example.portfolioapp.entity.InstrumentType;
import com.example.portfolioapp.entity.Position;
import com.example.portfolioapp.entity.TaxLot;
import com.example.portfolioapp.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for portfolio positions (holdings and watchlist).
 */
@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
public class PositionController {

    @Autowired
    private PositionService positionService;

    // ==================== GET Endpoints ====================

    @GetMapping
    public List<Position> getAllPositions() {
        return positionService.getAllPositions();
    }

    @GetMapping("/open")
    public List<Position> getOpenPositions() {
        return positionService.getOpenPositions();
    }

    @GetMapping("/watchlist")
    public List<Position> getWatchlist() {
        return positionService.getWatchlist();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getById(@PathVariable Long id) {
        return positionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Position> getBySymbol(@PathVariable String symbol) {
        return positionService.getOpenPositionBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public List<Position> getByType(@PathVariable InstrumentType type) {
        return positionService.getOpenPositionsByType(type);
    }

    @GetMapping("/{id}/tax-lots")
    public List<TaxLot> getTaxLots(@PathVariable Long id) {
        return positionService.getTaxLots(id);
    }

    @GetMapping("/{id}/tax-lots/open")
    public List<TaxLot> getOpenTaxLots(@PathVariable Long id) {
        return positionService.getOpenTaxLots(id);
    }

    // ==================== POST Endpoints - Buy/Sell ====================

    @PostMapping("/buy")
    public ResponseEntity<Position> buy(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());
        LocalDate date = request.get("date") != null
                ? LocalDate.parse((String) request.get("date"))
                : LocalDate.now();
        BigDecimal fees = request.get("fees") != null
                ? new BigDecimal(request.get("fees").toString()) : null;

        Position position = positionService.buy(symbol, quantity, price, date, fees);
        return ResponseEntity.ok(position);
    }

    @PostMapping("/sell")
    public ResponseEntity<Position> sell(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());
        LocalDate date = request.get("date") != null
                ? LocalDate.parse((String) request.get("date"))
                : LocalDate.now();
        BigDecimal fees = request.get("fees") != null
                ? new BigDecimal(request.get("fees").toString()) : null;

        Position position = positionService.sell(symbol, quantity, price, date, fees);
        return ResponseEntity.ok(position);
    }

    // ==================== POST Endpoints - Watchlist ====================

    @PostMapping("/watchlist")
    public ResponseEntity<Position> addToWatchlist(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal targetPrice = request.get("targetPrice") != null
                ? new BigDecimal(request.get("targetPrice").toString()) : null;
        String notes = (String) request.get("notes");

        Position position = positionService.addToWatchlist(symbol, targetPrice, notes);
        return ResponseEntity.ok(position);
    }

    @DeleteMapping("/watchlist/{id}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long id) {
        positionService.removeFromWatchlist(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PUT Endpoints ====================

    @PutMapping("/{id}/notes")
    public ResponseEntity<Position> updateNotes(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request) {
        Position position = positionService.updateNotes(id, request.get("notes"));
        return ResponseEntity.ok(position);
    }

    @PutMapping("/{id}/target-sell-price")
    public ResponseEntity<Position> setTargetSellPrice(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> request) {
        BigDecimal price = new BigDecimal(request.get("price").toString());
        Position position = positionService.setTargetSellPrice(id, price);
        return ResponseEntity.ok(position);
    }

    @PutMapping("/{id}/cost-basis-method")
    public ResponseEntity<Position> setCostBasisMethod(@PathVariable Long id,
                                                        @RequestBody Map<String, String> request) {
        CostBasisMethod method = CostBasisMethod.valueOf(request.get("method"));
        Position position = positionService.setCostBasisMethod(id, method);
        return ResponseEntity.ok(position);
    }
}
