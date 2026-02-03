package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Exchange;
import com.example.portfolioapp.entity.ExchangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByCode(String code);

    Optional<Exchange> findByMicCode(String micCode);

    List<Exchange> findByExchangeType(ExchangeType exchangeType);

    List<Exchange> findByIsActiveTrue();

    List<Exchange> findByCountry(String country);

    boolean existsByCode(String code);
}
