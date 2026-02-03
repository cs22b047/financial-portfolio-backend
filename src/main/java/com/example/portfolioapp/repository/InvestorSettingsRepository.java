package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.InvestorSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvestorSettingsRepository extends JpaRepository<InvestorSettings, Long> {

    @Query("SELECT s FROM InvestorSettings s ORDER BY s.id ASC LIMIT 1")
    Optional<InvestorSettings> findSettings();
}
