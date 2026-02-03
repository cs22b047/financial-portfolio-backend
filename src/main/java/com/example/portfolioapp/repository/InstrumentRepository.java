package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Instrument;
import com.example.portfolioapp.entity.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbol(String symbol);

    Optional<Instrument> findBySymbolAndInstrumentType(String symbol, InstrumentType instrumentType);

    List<Instrument> findByInstrumentType(InstrumentType instrumentType);

    List<Instrument> findByIsActiveTrue();

    @Query("SELECT i FROM Instrument i WHERE i.instrumentType IN :types AND i.isActive = true")
    List<Instrument> findByInstrumentTypeIn(@Param("types") List<InstrumentType> types);

    @Query("SELECT i FROM Instrument i WHERE i.sector.code = :sectorCode")
    List<Instrument> findBySectorCode(@Param("sectorCode") String sectorCode);

    @Query("SELECT i FROM Instrument i WHERE i.exchange.code = :exchangeCode")
    List<Instrument> findByExchangeCode(@Param("exchangeCode") String exchangeCode);

    @Query("SELECT i FROM Instrument i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.symbol) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Instrument> searchByNameOrSymbol(@Param("query") String query);

    @Query("SELECT i FROM Instrument i WHERE i.lastUpdated < :cutoffTime")
    List<Instrument> findStaleInstruments(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    boolean existsBySymbol(String symbol);
}
