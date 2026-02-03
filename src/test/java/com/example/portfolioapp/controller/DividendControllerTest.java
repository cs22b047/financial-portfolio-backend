package com.example.portfolioapp.controller;

import com.example.portfolioapp.entity.*;
import com.example.portfolioapp.service.DividendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class DividendControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private DividendService dividendService;

    private Dividend testDividend;
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

        testDividend = new Dividend();
        testDividend.setId(1L);
        testDividend.setAsset(testAsset);
        testDividend.setPaymentDate(LocalDate.of(2026, 1, 15));
        testDividend.setAmountPerShare(new BigDecimal("0.95"));
        testDividend.setSharesHeld(new BigDecimal("100"));
        testDividend.setTotalAmount(new BigDecimal("95.00"));
        testDividend.setCurrency("USD");
    }

    @Test
    void recordDividend_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("symbol", "AAPL");
        request.put("paymentDate", "2026-01-15");
        request.put("amountPerShare", 0.95);

        when(dividendService.recordDividend(eq("AAPL"), any(LocalDate.class), any(BigDecimal.class)))
            .thenReturn(testDividend);

        // Act & Assert
        mockMvc.perform(post("/api/dividends")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.asset.marketData.symbol").value("AAPL"))
            .andExpect(jsonPath("$.amountPerShare").value(0.95))
            .andExpect(jsonPath("$.sharesHeld").value(100))
            .andExpect(jsonPath("$.totalAmount").value(95.00));

        verify(dividendService).recordDividend(eq("AAPL"), any(LocalDate.class), any(BigDecimal.class));
    }

    @Test
    void recordDividend_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange - Missing required fields
        Map<String, Object> request = new HashMap<>();
        request.put("symbol", "AAPL");

        // Act & Assert
        mockMvc.perform(post("/api/dividends")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(dividendService, never()).recordDividend(any(), any(), any());
    }

    @Test
    void getDividendsBySymbol_Success() throws Exception {
        // Arrange
        Dividend div2 = new Dividend();
        div2.setId(2L);
        div2.setPaymentDate(LocalDate.of(2025, 10, 15));
        div2.setAmountPerShare(new BigDecimal("0.90"));

        when(dividendService.getDividendsBySymbol("AAPL"))
            .thenReturn(Arrays.asList(testDividend, div2));

        // Act & Assert
        mockMvc.perform(get("/api/dividends/symbol/AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].amountPerShare").value(0.95))
            .andExpect(jsonPath("$[1].amountPerShare").value(0.90));

        verify(dividendService).getDividendsBySymbol("AAPL");
    }

    @Test
    void getDividendsByDateRange_Success() throws Exception {
        // Arrange
        when(dividendService.getDividendsInDateRange(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(testDividend));

        // Act & Assert
        mockMvc.perform(get("/api/dividends/date-range")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].paymentDate").value("2026-01-15"));

        verify(dividendService).getDividendsInDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getDividendsByDateRange_MissingParameters_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/dividends/date-range")
                .param("startDate", "2026-01-01"))
            .andExpect(status().isBadRequest());

        verify(dividendService, never()).getDividendsInDateRange(any(), any());
    }

    @Test
    void getTotalDividendIncome_Success() throws Exception {
        // Arrange
        when(dividendService.calculateTotalDividendIncome(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new BigDecimal("1250.75"));

        // Act & Assert
        mockMvc.perform(get("/api/dividends/total-income")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(1250.75));

        verify(dividendService).calculateTotalDividendIncome(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getAllDividends_Success() throws Exception {
        // Arrange
        when(dividendService.getAllDividends()).thenReturn(Arrays.asList(testDividend));

        // Act & Assert
        mockMvc.perform(get("/api/dividends"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));

        verify(dividendService).getAllDividends();
    }

    @Test
    void recordDividend_CalculatesCorrectTotalAmount() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("symbol", "AAPL");
        request.put("paymentDate", "2026-02-01");
        request.put("amountPerShare", 2.50);

        Dividend largeDividend = new Dividend();
        largeDividend.setId(2L);
        largeDividend.setAsset(testAsset);
        largeDividend.setAmountPerShare(new BigDecimal("2.50"));
        largeDividend.setSharesHeld(new BigDecimal("100"));
        largeDividend.setTotalAmount(new BigDecimal("250.00")); // 2.50 * 100

        when(dividendService.recordDividend(eq("AAPL"), any(LocalDate.class), any(BigDecimal.class)))
            .thenReturn(largeDividend);

        // Act & Assert
        mockMvc.perform(post("/api/dividends")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalAmount").value(250.00));
    }

    @Test
    void getDividendsBySymbol_EmptyResult() throws Exception {
        // Arrange
        when(dividendService.getDividendsBySymbol("MSFT")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/dividends/symbol/MSFT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTotalDividendIncome_NoDividends_ReturnsZero() throws Exception {
        // Arrange
        when(dividendService.calculateTotalDividendIncome(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(get("/api/dividends/total-income")
                .param("startDate", "2020-01-01")
                .param("endDate", "2020-12-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(0));
    }
}
