package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.AssetType;
import com.example.portfolioapp.service.AssetTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-types")
@CrossOrigin(origins = "*")
public class AssetTypeController {
    
    @Autowired
    private AssetTypeService assetTypeService;
    
    @GetMapping
    public List<AssetType> getAllAssetTypes() {
        return assetTypeService.getAllAssetTypes();
    }
    
    @GetMapping("/active")
    public List<AssetType> getActiveAssetTypes() {
        return assetTypeService.getActiveAssetTypes();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AssetType> getAssetTypeById(@PathVariable Long id) {
        return assetTypeService.getAssetTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<AssetType> getAssetTypeByCode(@PathVariable String code) {
        return assetTypeService.getAssetTypeByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public AssetType createAssetType(@RequestBody AssetType assetType) {
        return assetTypeService.saveAssetType(assetType);
    }
}