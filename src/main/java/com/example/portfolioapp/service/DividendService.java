package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Dividend;
import com.example.portfolioapp.entity.DividendType;
import com.example.portfolioapp.repository.DividendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DividendService {
    
    @Autowired
    private DividendRepository dividendRepository;
    
    public List<Dividend> getAllDividends() {
        return dividendRepository.findAll();
    }
    
    public Optional<Dividend> getDividendById(Long id) {
        return dividendRepository.findById(id);
    }
    
    public List<Dividend> getDividendsByAsset(Long assetId) {
        return dividendRepository.findByAssetIdOrderByDateDesc(assetId);
    }
    
    public List<Dividend> getDividendsByType(DividendType dividendType) {
        return dividendRepository.findByDividendType(dividendType);
    }
    
    public List<Dividend> getDividendsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dividendRepository.findByPaymentDateBetween(startDate, endDate);
    }
    
    public Dividend saveDividend(Dividend dividend) {
        return dividendRepository.save(dividend);
    }
    
    public void deleteDividend(Long id) {
        dividendRepository.deleteById(id);
    }
}