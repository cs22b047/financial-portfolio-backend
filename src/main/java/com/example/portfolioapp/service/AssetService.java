package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import com.example.portfolioapp.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    
    @Autowired
    private AssetRepository assetRepository;
    
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }
    
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }
    
    public List<Asset> getOwnedAssets() {
        return assetRepository.findOwnedAssets();
    }
    
    public List<Asset> getWatchlistAssets() {
        return assetRepository.findWatchlistAssets();
    }
    
    public List<Asset> getAssetsByType(String assetTypeCode) {
        return assetRepository.findByAssetTypeCode(assetTypeCode);
    }
    
    public List<Asset> getAssetsByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status);
    }
    
    public Asset saveAsset(Asset asset) {
        return assetRepository.save(asset);
    }
    
    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }
    
    public Asset updateAsset(Long id, Asset assetDetails) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found with id: " + id));
        
        asset.setSymbol(assetDetails.getSymbol());
        asset.setName(assetDetails.getName());
        asset.setQuantity(assetDetails.getQuantity());
        asset.setPurchasePrice(assetDetails.getPurchasePrice());
        asset.setCurrentPrice(assetDetails.getCurrentPrice());
        asset.setPurchaseDate(assetDetails.getPurchaseDate());
        asset.setStatus(assetDetails.getStatus());
        asset.setTargetPrice(assetDetails.getTargetPrice());
        asset.setNotes(assetDetails.getNotes());
        asset.setPriorityRank(assetDetails.getPriorityRank());
        
        return assetRepository.save(asset);
    }
}