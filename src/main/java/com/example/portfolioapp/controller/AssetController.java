package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import com.example.portfolioapp.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Assets (simplified assets table)
 * Returns data from the assets table directly
 */
@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    @Autowired
    private AssetService assetService;

    /**
     * Get all assets
     * GET /api/assets
     */
    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    /**
     * Get asset by ID
     * GET /api/assets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get asset by symbol
     * GET /api/assets/symbol/{symbol}
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Asset> getAssetBySymbol(@PathVariable String symbol) {
        try {
            Asset asset = assetService.getAssetBySymbol(symbol);
            return ResponseEntity.ok(asset);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get assets by status
     * GET /api/assets/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Asset>> getAssetsByStatus(@PathVariable AssetStatus status) {
        List<Asset> assets = assetService.getAssetsByStatus(status);
        return ResponseEntity.ok(assets);
    }

    /**
     * Get owned assets (OWNED status)
     * GET /api/assets/owned
     */
    @GetMapping("/owned")
    public ResponseEntity<List<Asset>> getOwnedAssets() {
        List<Asset> assets = assetService.getAssetsByStatus(AssetStatus.OWNED);
        return ResponseEntity.ok(assets);
    }

    /**
     * Get watchlist assets (WATCHLIST status)
     * GET /api/assets/watchlist
     */
    @GetMapping("/watchlist")
    public ResponseEntity<List<Asset>> getWatchlistAssets() {
        List<Asset> assets = assetService.getAssetsByStatus(AssetStatus.WATCHLIST);
        return ResponseEntity.ok(assets);
    }

    /**
     * Buy stock - creates or updates asset
     * POST /api/assets/buy
     */
    @PostMapping("/buy")
    public ResponseEntity<Asset> buyStock(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());
        LocalDate date = request.get("date") != null
                ? LocalDate.parse((String) request.get("date"))
                : LocalDate.now();

        Asset asset = assetService.buyStock(symbol, quantity, price, date);
        return new ResponseEntity<>(asset, HttpStatus.CREATED);
    }

    /**
     * Sell stock - updates asset
     * POST /api/assets/sell
     */
    @PostMapping("/sell")
    public ResponseEntity<Asset> sellStock(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        BigDecimal price = new BigDecimal(request.get("price").toString());
        LocalDate date = request.get("date") != null
                ? LocalDate.parse((String) request.get("date"))
                : LocalDate.now();

        Asset asset = assetService.sellStock(symbol, quantity, price, date);
        return ResponseEntity.ok(asset);
    }

    /**
     * Add to watchlist
     * POST /api/assets/watchlist
     */
    @PostMapping("/watchlist")
    public ResponseEntity<Asset> addToWatchlist(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        BigDecimal targetPrice = request.get("targetPrice") != null
                ? new BigDecimal(request.get("targetPrice").toString())
                : null;

        Asset asset = assetService.addToWatchlist(symbol, targetPrice);
        return new ResponseEntity<>(asset, HttpStatus.CREATED);
    }

    /**
     * Remove from watchlist
     * DELETE /api/assets/watchlist/{id}
     */
    @DeleteMapping("/watchlist/{id}")
    public ResponseEntity<Map<String, String>> removeFromWatchlist(@PathVariable Long id) {
        assetService.removeFromWatchlist(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Asset removed from watchlist successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update asset
     * PUT /api/assets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long id,
            @RequestBody Asset updates) {
        Asset updated = assetService.updateAsset(id, updates);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete asset
     * DELETE /api/assets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Asset deleted successfully");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }
}
