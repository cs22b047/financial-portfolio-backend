package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.AssetType;
import com.example.portfolioapp.repository.AssetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetTypeService {
    
    @Autowired
    private AssetTypeRepository assetTypeRepository;
    
    public List<AssetType> getAllAssetTypes() {
        return assetTypeRepository.findAll();
    }
    
    public List<AssetType> getActiveAssetTypes() {
        return assetTypeRepository.findByIsActiveTrue();
    }
    
    public Optional<AssetType> getAssetTypeById(Long id) {
        return assetTypeRepository.findById(id);
    }
    
    public Optional<AssetType> getAssetTypeByCode(String code) {
        return assetTypeRepository.findByCode(code);
    }
    
    public AssetType saveAssetType(AssetType assetType) {
        return assetTypeRepository.save(assetType);
    }
}