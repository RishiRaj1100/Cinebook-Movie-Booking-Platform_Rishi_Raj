package com.cinebook.domain.repository;

import com.cinebook.domain.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, UUID> {
    List<Theater> findByCityIgnoreCaseOrderByName(String city);
    List<Theater> findAllByOrderByNameAsc();
    boolean existsByCityIgnoreCase(String city);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT t.city FROM Theater t WHERE t.city IS NOT NULL AND t.city != '' ORDER BY t.city")
    List<String> findDistinctCities();
}
