package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private TransactionService transactionService;

    private Transaction testTransaction;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        AssetType stockType = new AssetType();
        stockType.setId(1L);
        stockType.setCode("STOCK");
        stockType.setName("STOCK");

        MarketData appleStock = new MarketData();
        appleStock.setId(1L);
        appleStock.setSymbol("AAPL");
        appleStock.setName("Apple Inc.");
        appleStock.setAssetType(stockType);
        appleStock.setCurrency("USD");

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setMarketData(appleStock);
        testAsset.setQuantity(new BigDecimal("100"));
        testAsset.setStatus(AssetStatus.OWNED);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAsset(testAsset);
        testTransaction.setTransactionType(TransactionType.BUY);
        testTransaction.setQuantity(new BigDecimal("100"));
        testTransaction.setPrice(new BigDecimal("170.00"));
        testTransaction.setTransactionDate(LocalDate.of(2026, 1, 15).atStartOfDay());
    }

    @Test
    void getTransactionsBySymbol_Success() throws Exception {
        // Arrange
        when(transactionService.getTransactionsBySymbol("AAPL"))
            .thenReturn(Arrays.asList(testTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/symbol/AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionType").value("BUY"))
            .andExpect(jsonPath("$[0].quantity").value(100))
            .andExpect(jsonPath("$[0].price").value(170.00));

        verify(transactionService).getTransactionsBySymbol("AAPL");
    }

    @Test
    void getTransactionsByType_BuyTransactions() throws Exception {
        // Arrange
        when(transactionService.getTransactionsByType(TransactionType.BUY))
            .thenReturn(Arrays.asList(testTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/type/BUY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionType").value("BUY"));

        verify(transactionService).getTransactionsByType(TransactionType.BUY);
    }

    @Test
    void getTransactionsByType_SellTransactions() throws Exception {
        // Arrange
        Transaction sellTransaction = new Transaction();
        sellTransaction.setTransactionType(TransactionType.SELL);
        sellTransaction.setQuantity(new BigDecimal("50"));
        sellTransaction.setPrice(new BigDecimal("180.00"));

        when(transactionService.getTransactionsByType(TransactionType.SELL))
            .thenReturn(Arrays.asList(sellTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/type/SELL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionType").value("SELL"));
    }

    @Test
    void getTransactionsByType_DividendTransactions() throws Exception {
        // Arrange
        Transaction dividendTransaction = new Transaction();
        dividendTransaction.setTransactionType(TransactionType.DIVIDEND);
        dividendTransaction.setQuantity(new BigDecimal("100"));

        when(transactionService.getTransactionsByType(TransactionType.DIVIDEND))
            .thenReturn(Arrays.asList(dividendTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/type/DIVIDEND"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionType").value("DIVIDEND"));
    }

    @Test
    void getTransactionsByDateRange_Success() throws Exception {
        // Arrange
        when(transactionService.getTransactionsByDateRange(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(testTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/date-range")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].transactionDate").value("2026-01-15T00:00:00"));

        verify(transactionService).getTransactionsByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getTransactionsByDateRange_MissingParameters_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/transactions/date-range")
                .param("startDate", "2026-01-01"))
            .andExpect(status().isBadRequest());

        verify(transactionService, never()).getTransactionsByDateRange(any(), any());
    }

    @Test
    void getAllTransactions_Success() throws Exception {
        // Arrange
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setTransactionType(TransactionType.SELL);

        when(transactionService.getAllTransactions())
            .thenReturn(Arrays.asList(testTransaction, transaction2));

        // Act & Assert
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void getTotalInvested_Success() throws Exception {
        // Arrange
        when(transactionService.calculateTotalInvested())
            .thenReturn(new BigDecimal("17000.00"));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/total-invested"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(17000.00));

        verify(transactionService).calculateTotalInvested();
    }

    @Test
    void getRealizedGains_Success() throws Exception {
        // Arrange
        when(transactionService.calculateRealizedGains())
            .thenReturn(new BigDecimal("1500.00"));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/realized-gains"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(1500.00));

        verify(transactionService).calculateRealizedGains();
    }

    @Test
    void getRealizedGains_NoGains_ReturnsZero() throws Exception {
        // Arrange
        when(transactionService.calculateRealizedGains())
            .thenReturn(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/realized-gains"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(0));
    }

    @Test
    void getTransactionsBySymbol_EmptyResult() throws Exception {
        // Arrange
        when(transactionService.getTransactionsBySymbol("MSFT"))
            .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/transactions/symbol/MSFT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTransactionHistory_MultipleTypes() throws Exception {
        // Arrange
        Transaction buyTxn = new Transaction();
        buyTxn.setTransactionType(TransactionType.BUY);
        
        Transaction sellTxn = new Transaction();
        sellTxn.setTransactionType(TransactionType.SELL);
        
        Transaction divTxn = new Transaction();
        divTxn.setTransactionType(TransactionType.DIVIDEND);

        when(transactionService.getTransactionsBySymbol("AAPL"))
            .thenReturn(Arrays.asList(buyTxn, sellTxn, divTxn));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/symbol/AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }
}
