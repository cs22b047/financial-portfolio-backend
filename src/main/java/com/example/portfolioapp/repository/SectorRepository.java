package com.example.portfolioapp.repository;

import com.example.portfolioapp.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    Optional<Sector> findByCode(String code);

    Optional<Sector> findByName(String name);

    List<Sector> findByIsActiveTrue();

    boolean existsByCode(String code);
}
