package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Dividend;
import com.example.portfolioapp.entity.DividendType;
import com.example.portfolioapp.service.DividendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dividends")
@CrossOrigin(origins = "*")
public class DividendController {
    
    @Autowired
    private DividendService dividendService;
    
    @GetMapping
    public List<Dividend> getAllDividends() {
        return dividendService.getAllDividends();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Dividend> getDividendById(@PathVariable Long id) {
        return dividendService.getDividendById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/asset/{assetId}")
    public List<Dividend> getDividendsByAsset(@PathVariable Long assetId) {
        return dividendService.getDividendsByAsset(assetId);
    }
    
    @GetMapping("/type/{dividendType}")
    public List<Dividend> getDividendsByType(@PathVariable DividendType dividendType) {
        return dividendService.getDividendsByType(dividendType);
    }
    
    @GetMapping("/date-range")
    public List<Dividend> getDividendsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dividendService.getDividendsByDateRange(startDate, endDate);
    }
    
    @PostMapping
    public Dividend createDividend(@RequestBody Dividend dividend) {
        return dividendService.saveDividend(dividend);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDividend(@PathVariable Long id) {
        dividendService.deleteDividend(id);
        return ResponseEntity.ok().build();
    }
}