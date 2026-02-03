package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Asset;
import com.example.portfolioapp.entity.AssetStatus;
import com.example.portfolioapp.entity.Dividend;
import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import com.example.portfolioapp.exception.DuplicateResourceException;
import com.example.portfolioapp.exception.InvalidOperationException;
import com.example.portfolioapp.exception.ResourceNotFoundException;
import com.example.portfolioapp.repository.AssetRepository;
import com.example.portfolioapp.repository.DividendRepository;
import com.example.portfolioapp.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for Dividend operations.
 * 
 * CRITICAL CALCULATION LOGIC IMPLEMENTATION:
 * ✅ shares_held - Retrieved from assets.quantity (matching Python script)
 * ✅ total_amount - Calculated as amount_per_share × shares_held (matching Python script)
 * 
 * This service implements the SAME calculation logic as unified_finance_extractor.py:
 * 
 * Python logic (lines 389-456):
 *   cursor.execute("SELECT a.id, a.quantity FROM assets a WHERE ...")
 *   asset_id, shares_held = result
 *   total_amount = amount_per_share * shares_held
 *   INSERT INTO dividends (asset_id, payment_date, amount_per_share, shares_held, total_amount, currency)
 * 
 * Java equivalent:
 *   Asset asset = assetRepository.findBySymbol(symbol)
 *   BigDecimal sharesHeld = asset.getQuantity()
 *   BigDecimal totalAmount = amountPerShare.multiply(sharesHeld)
 *   dividend.calculateTotalAmount(sharesHeld)
 */
@Service
public class DividendService {

    private static final Logger logger = LoggerFactory.getLogger(DividendService.class);

    @Autowired
    private DividendRepository dividendRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Record a dividend payment for an owned asset.
     * 
     * This is the PRIMARY method that implements the calculation logic.
     * It mirrors the Python script's insert_dividend() method.
     * 
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param paymentDate Dividend payment date (from Yahoo Finance)
     * @param amountPerShare Dividend amount per share (from Yahoo Finance)
     * @return The created Dividend with calculated shares_held and total_amount
     * @throws ResourceNotFoundException if asset not found or not owned
     * @throws DuplicateResourceException if dividend already exists for this date
     * @throws InvalidOperationException if asset has no quantity
     */
    @Transactional
    public Dividend recordDividend(String symbol, LocalDate paymentDate, BigDecimal amountPerShare) {
        logger.info("Recording dividend for symbol: {}, date: {}, amount per share: {}", 
                   symbol, paymentDate, amountPerShare);

        // Step 1: Find the asset (equivalent to Python's SELECT a.id, a.quantity FROM assets)
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));

        // Step 2: Verify asset is owned (Python checks a.status = 'OWNED')
        if (asset.getStatus() != AssetStatus.OWNED) {
            throw new InvalidOperationException(
                String.format("Cannot record dividend for non-owned asset. Symbol: %s, Status: %s", 
                             symbol, asset.getStatus())
            );
        }

        // Step 3: Check for duplicate (Python checks: SELECT id FROM dividends WHERE asset_id AND payment_date)
        if (dividendRepository.existsByAssetIdAndPaymentDate(asset.getId(), paymentDate)) {
            throw new DuplicateResourceException(
                String.format("Dividend already exists for symbol: %s on date: %s", symbol, paymentDate)
            );
        }

        // Step 4: Get shares held (Python: shares_held = result[1])
        BigDecimal sharesHeld = asset.getQuantity();
        if (sharesHeld == null || sharesHeld.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(
                String.format("Asset %s has invalid quantity: %s. Cannot calculate dividend.", 
                             symbol, sharesHeld)
            );
        }

        // Step 5: Create and calculate dividend (Python: total_amount = amount_per_share * shares_held)
        Dividend dividend = Dividend.create(asset, paymentDate, amountPerShare);
        
        logger.info("Calculated dividend - Shares held: {}, Total amount: {}", 
                   dividend.getSharesHeld(), dividend.getTotalAmount());

        // Step 6: Save dividend to database
        Dividend savedDividend = dividendRepository.save(dividend);
        logger.info("Dividend saved with ID: {}", savedDividend.getId());

        // Step 7: Create corresponding DIVIDEND transaction (Python also creates transaction)
        createDividendTransaction(asset, savedDividend);

        return savedDividend;
    }

    /**
     * Create a DIVIDEND transaction record (matches Python script behavior)
     */
    private void createDividendTransaction(Asset asset, Dividend dividend) {
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setTransactionType(TransactionType.DIVIDEND);
        transaction.setPrice(dividend.getTotalAmount()); // Store total dividend in price field
        transaction.setTransactionDate(dividend.getPaymentDate().atStartOfDay());
        transaction.setCurrency(dividend.getCurrency());
        transaction.setNotes(String.format("Dividend payment: %s shares × $%s = $%s", 
                                          dividend.getSharesHeld(), 
                                          dividend.getAmountPerShare(),
                                          dividend.getTotalAmount()));
        
        transactionRepository.save(transaction);
        logger.info("Created dividend transaction for asset: {}", asset.getSymbol());
    }

    /**
     * Record a dividend with explicit shares (for historical data or adjustments)
     */
    @Transactional
    public Dividend recordDividendWithShares(String symbol, LocalDate paymentDate, 
                                            BigDecimal amountPerShare, BigDecimal sharesAtPayment) {
        logger.info("Recording dividend with explicit shares - Symbol: {}, Shares: {}", 
                   symbol, sharesAtPayment);

        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "symbol", symbol));

        if (dividendRepository.existsByAssetIdAndPaymentDate(asset.getId(), paymentDate)) {
            throw new DuplicateResourceException(
                String.format("Dividend already exists for symbol: %s on date: %s", symbol, paymentDate)
            );
        }

        Dividend dividend = new Dividend(asset, paymentDate, amountPerShare);
        dividend.setCurrency(asset.getMarketData().getCurrency());
        dividend.calculateTotalAmount(sharesAtPayment);
        dividend.validate();

        Dividend savedDividend = dividendRepository.save(dividend);
        createDividendTransaction(asset, savedDividend);

        return savedDividend;
    }

    /**
     * Get all dividends for a specific symbol
     */
    public List<Dividend> getDividendsBySymbol(String symbol) {
        return dividendRepository.findBySymbol(symbol);
    }

    /**
     * Get all dividends for an asset
     */
    public List<Dividend> getDividendsByAssetId(Long assetId) {
        return dividendRepository.findByAssetId(assetId);
    }

    /**
     * Get dividends within a date range
     */
    public List<Dividend> getDividendsInDateRange(LocalDate startDate, LocalDate endDate) {
        return dividendRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Calculate total dividend income for a date range
     */
    public BigDecimal calculateTotalDividendIncome(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = dividendRepository.sumTotalAmountInDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total dividends for a specific asset
     */
    public BigDecimal calculateTotalDividendsByAsset(Long assetId) {
        BigDecimal total = dividendRepository.sumTotalAmountByAssetId(assetId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get recent dividends (last N months)
     */
    public List<Dividend> getRecentDividends(int months) {
        LocalDate sinceDate = LocalDate.now().minusMonths(months);
        return dividendRepository.findRecentDividends(sinceDate);
    }

    /**
     * Get all dividends for owned assets
     */
    public List<Dividend> getAllDividendsForOwnedAssets() {
        return dividendRepository.findAllForOwnedAssets();
    }

    /**
     * Delete a dividend (cascade will delete associated transaction)
     */
    @Transactional
    public void deleteDividend(Long dividendId) {
        Dividend dividend = dividendRepository.findById(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend", dividendId));
        
        dividendRepository.delete(dividend);
        logger.info("Deleted dividend ID: {} for asset: {}", dividendId, dividend.getAsset().getSymbol());
    }

    /**
     * Get dividend by ID
     */
    public Dividend getDividendById(Long id) {
        return dividendRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend", id));
    }

    /**
     * Get dividends by date range
     */
    public List<Dividend> getDividendsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dividendRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(startDate, endDate);
    }

    /**
     * Get total dividend income for a date range
     */
    public BigDecimal getTotalDividendIncome(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = dividendRepository.calculateTotalDividendIncome(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get all dividends
     */
    public List<Dividend> getAllDividends() {
        return dividendRepository.findAllByOrderByPaymentDateDesc();
    }
}
