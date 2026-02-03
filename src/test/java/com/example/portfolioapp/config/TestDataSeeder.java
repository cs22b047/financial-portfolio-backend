package com.example.portfolioapp.config;

import com.example.portfolioapp.entity.AssetType;
import com.example.portfolioapp.entity.MarketData;
import com.example.portfolioapp.repository.AssetTypeRepository;
import com.example.portfolioapp.repository.MarketDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Test data seeder for integration tests.
 * Seeds essential market data for testing portfolio operations.
 * Only runs in 'test' profile.
 */
@Component
@Profile("test")
public class TestDataSeeder implements CommandLineRunner {

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private MarketDataRepository marketDataRepository;

    @Override
    public void run(String... args) {
        seedAssetTypes();
        seedMarketData();
    }

    private void seedAssetTypes() {
        if (assetTypeRepository.count() == 0) {
            // Create STOCK asset type
            AssetType stock = new AssetType();
            stock.setCode("STOCK");
            stock.setName("STOCK");
            stock.setDescription("Common Stock");
            assetTypeRepository.save(stock);

            // Create other asset types if needed
            AssetType bond = new AssetType();
            bond.setCode("BOND");
            bond.setName("BOND");
            bond.setDescription("Fixed Income Bond");
            assetTypeRepository.save(bond);

            AssetType crypto = new AssetType();
            crypto.setCode("CRYPTO");
            crypto.setName("CRYPTO");
            crypto.setDescription("Cryptocurrency");
            assetTypeRepository.save(crypto);

            AssetType etf = new AssetType();
            etf.setCode("ETF");
            etf.setName("ETF");
            etf.setDescription("Exchange Traded Fund");
            assetTypeRepository.save(etf);
        }
    }

    private void seedMarketData() {
        if (marketDataRepository.count() == 0) {
            AssetType stock = assetTypeRepository.findByName("STOCK")
                .orElseThrow(() -> new RuntimeException("STOCK asset type not found"));

            // Apple Inc.
            MarketData apple = new MarketData();
            apple.setSymbol("AAPL");
            apple.setName("Apple Inc.");
            apple.setAssetType(stock);
            apple.setCurrentPrice(new BigDecimal("175.50"));
            apple.setCurrency("USD");
            apple.setExchange("NASDAQ");
            apple.setSector("Technology");
            apple.setIndustry("Consumer Electronics");
            marketDataRepository.save(apple);

            // Microsoft Corporation
            MarketData microsoft = new MarketData();
            microsoft.setSymbol("MSFT");
            microsoft.setName("Microsoft Corporation");
            microsoft.setAssetType(stock);
            microsoft.setCurrentPrice(new BigDecimal("380.00"));
            microsoft.setCurrency("USD");
            microsoft.setExchange("NASDAQ");
            microsoft.setSector("Technology");
            microsoft.setIndustry("Software");
            marketDataRepository.save(microsoft);

            // Google (Alphabet Inc.)
            MarketData google = new MarketData();
            google.setSymbol("GOOGL");
            google.setName("Alphabet Inc.");
            google.setAssetType(stock);
            google.setCurrentPrice(new BigDecimal("140.50"));
            google.setCurrency("USD");
            google.setExchange("NASDAQ");
            google.setSector("Technology");
            google.setIndustry("Internet Services");
            marketDataRepository.save(google);

            // Amazon.com Inc.
            MarketData amazon = new MarketData();
            amazon.setSymbol("AMZN");
            amazon.setName("Amazon.com Inc.");
            amazon.setAssetType(stock);
            amazon.setCurrentPrice(new BigDecimal("175.00"));
            amazon.setCurrency("USD");
            amazon.setExchange("NASDAQ");
            amazon.setSector("Consumer Cyclical");
            amazon.setIndustry("E-commerce");
            marketDataRepository.save(amazon);

            // Tesla Inc.
            MarketData tesla = new MarketData();
            tesla.setSymbol("TSLA");
            tesla.setName("Tesla Inc.");
            tesla.setAssetType(stock);
            tesla.setCurrentPrice(new BigDecimal("245.00"));
            tesla.setCurrency("USD");
            tesla.setExchange("NASDAQ");
            tesla.setSector("Consumer Cyclical");
            tesla.setIndustry("Auto Manufacturers");
            marketDataRepository.save(tesla);

            // NVIDIA Corporation
            MarketData nvidia = new MarketData();
            nvidia.setSymbol("NVDA");
            nvidia.setName("NVIDIA Corporation");
            nvidia.setAssetType(stock);
            nvidia.setCurrentPrice(new BigDecimal("850.00"));
            nvidia.setCurrency("USD");
            nvidia.setExchange("NASDAQ");
            nvidia.setSector("Technology");
            nvidia.setIndustry("Semiconductors");
            marketDataRepository.save(nvidia);

            // Meta Platforms Inc.
            MarketData meta = new MarketData();
            meta.setSymbol("META");
            meta.setName("Meta Platforms Inc.");
            meta.setAssetType(stock);
            meta.setCurrentPrice(new BigDecimal("475.00"));
            meta.setCurrency("USD");
            meta.setExchange("NASDAQ");
            meta.setSector("Technology");
            meta.setIndustry("Social Media");
            marketDataRepository.save(meta);

            // JPMorgan Chase & Co.
            MarketData jpmorgan = new MarketData();
            jpmorgan.setSymbol("JPM");
            jpmorgan.setName("JPMorgan Chase & Co.");
            jpmorgan.setAssetType(stock);
            jpmorgan.setCurrentPrice(new BigDecimal("185.00"));
            jpmorgan.setCurrency("USD");
            jpmorgan.setExchange("NYSE");
            jpmorgan.setSector("Financial Services");
            jpmorgan.setIndustry("Banks");
            marketDataRepository.save(jpmorgan);
        }
    }
}
