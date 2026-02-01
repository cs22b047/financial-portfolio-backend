package com.example.portfolioapp.service;

import com.example.portfolioapp.entity.Transaction;
import com.example.portfolioapp.entity.TransactionType;
import com.example.portfolioapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> getTransactionsByAsset(Long assetId) {
        return transactionRepository.findByAssetIdOrderByDateDesc(assetId);
    }
    
    public List<Transaction> getTransactionsByType(TransactionType transactionType) {
        return transactionRepository.findByTransactionType(transactionType);
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }
    
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}