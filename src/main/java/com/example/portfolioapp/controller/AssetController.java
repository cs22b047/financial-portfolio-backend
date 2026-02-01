package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import com.example.portfolioapp.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {
    
    @Autowired
    private AssetService assetService;
    
    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/owned")
    public List<Asset> getOwnedAssets() {
        return assetService.getOwnedAssets();
    }
    
    @GetMapping("/watchlist")
    public List<Asset> getWatchlistAssets() {
        return assetService.getWatchlistAssets();
    }
    
    @GetMapping("/type/{assetTypeCode}")
    public List<Asset> getAssetsByType(@PathVariable String assetTypeCode) {
        return assetService.getAssetsByType(assetTypeCode);
    }
    
    @GetMapping("/status/{status}")
    public List<Asset> getAssetsByStatus(@PathVariable AssetStatus status) {
        return assetService.getAssetsByStatus(status);
    }
    
    @PostMapping
    public Asset createAsset(@RequestBody Asset asset) {
        return assetService.saveAsset(asset);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @RequestBody Asset assetDetails) {
        try {
            Asset updatedAsset = assetService.updateAsset(id, assetDetails);
            return ResponseEntity.ok(updatedAsset);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }
}