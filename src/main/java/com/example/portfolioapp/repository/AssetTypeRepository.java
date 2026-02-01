package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {
    
    Optional<AssetType> findByCode(String code);
    
    List<AssetType> findByIsActiveTrue();
}