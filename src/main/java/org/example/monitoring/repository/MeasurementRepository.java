package org.example.monitoring.repository;

import org.example.monitoring.entity.Measurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    @EntityGraph(value = "Measurement.consumptions", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Measurement> findTopByUserLoginOrderByDateSavedDesc(String userLogin);

    @EntityGraph(value = "Measurement.consumptions", type = EntityGraph.EntityGraphType.LOAD)
    Page<Measurement> findByUserLogin(String userLogin, Pageable pageable);
}
