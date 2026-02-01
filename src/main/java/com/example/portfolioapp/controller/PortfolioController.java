package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {
    
    @Autowired
    private PortfolioService portfolioService;
    
    @GetMapping("/summary")
    public Map<String, Object> getPortfolioSummary() {
        return portfolioService.getPortfolioSummary();
    }
    
    @GetMapping("/top-performers")
    public List<Asset> getTopPerformers(@RequestParam(defaultValue = "5") int limit) {
        return portfolioService.getTopPerformers(limit);
    }
    
    @GetMapping("/price-alerts")
    public List<Asset> getTargetPriceAlerts() {
        return portfolioService.getTargetPriceAlerts();
    }
}