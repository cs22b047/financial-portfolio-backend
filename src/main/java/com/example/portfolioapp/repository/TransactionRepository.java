package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAssetId(Long assetId);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.asset.id = :assetId ORDER BY t.transactionDate DESC")
    List<Transaction> findByAssetIdOrderByDateDesc(Long assetId);
}