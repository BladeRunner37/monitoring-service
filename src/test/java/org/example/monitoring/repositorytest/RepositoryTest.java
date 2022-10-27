package org.example.monitoring.repositorytest;

import org.apache.commons.collections4.IterableUtils;
import org.example.monitoring.entity.Consumption;
import org.example.monitoring.entity.Measurement;
import org.example.monitoring.entity.User;
import org.example.monitoring.model.ConsumptionType;
import org.example.monitoring.repository.MeasurementRepository;
import org.example.monitoring.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
public class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @BeforeEach
    public void init() {
        prepareData();
    }

    @AfterEach
    public void cleanUp() {
        cleanUpData();
    }

    @Test
    public void userRepositoryTest() {
        List<User> users = IterableUtils.toList(userRepository.findAll());
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(users.stream().anyMatch(u -> "user1".equals(u.getLogin())));
        Assertions.assertTrue(users.stream().anyMatch(u -> "user2".equals(u.getLogin())));
    }

    @Test
    public void measurementRepositoryTest() {
        Optional<Measurement> measurementOptional = measurementRepository.findTopByUserLoginOrderByDateSavedDesc("user1");
        Assertions.assertTrue(measurementOptional.isPresent());
        Measurement measurement = measurementOptional.get();
        Assertions.assertEquals("user1", measurement.getUser().getLogin());
        BigDecimal expected = new BigDecimal(300);
        Arrays.stream(ConsumptionType.values())
                .forEach(ct -> {
                    Assertions.assertEquals(expected, findConsumptionValue(ct, measurement.getConsumptions()));
                });

        Optional<Measurement> measurementOptional2 = measurementRepository.findTopByUserLoginOrderByDateSavedDesc("user2");
        Assertions.assertFalse(measurementOptional2.isPresent());

        Page<Measurement> measurements = measurementRepository.findByUserLogin("user1", PageRequest.of(0, 2, Sort.by("dateSaved")));
        Assertions.assertEquals(3, measurements.getTotalElements());
        Assertions.assertEquals(2, measurements.getTotalPages());
        measurements.getContent()
                .forEach(m -> Assertions.assertEquals(ConsumptionType.values().length, m.getConsumptions().size()));
    }

    private BigDecimal findConsumptionValue(ConsumptionType consumptionType, Set<Consumption> consumptions) {
        return consumptions.stream()
                .filter(c -> consumptionType == c.getType())
                .findFirst()
                .map(Consumption::getValue)
                .orElseThrow(() -> new RuntimeException("Not found consumption with type " + consumptionType));
    }

    private void prepareData() {
        User user1 = new User();
        user1.setLogin("user1");
        userRepository.save(user1);
        User user2 = new User();
        user2.setLogin("user2");
        userRepository.save(user2);
        Measurement measurement1 = new Measurement();
        measurement1.setUser(user1);
        measurement1.setConsumptions(createConsumptions(100, measurement1));
        measurement1.setDateSaved(OffsetDateTime.parse("2022-01-01T10:15:30+01:00"));
        Measurement measurement2 = new Measurement();
        measurement2.setUser(user1);
        measurement2.setConsumptions(createConsumptions(200, measurement2));
        measurement2.setDateSaved(OffsetDateTime.parse("2022-02-01T10:15:30+01:00"));
        Measurement measurement3 = new Measurement();
        measurement3.setUser(user1);
        measurement3.setConsumptions(createConsumptions(300, measurement3));
        measurement3.setDateSaved(OffsetDateTime.parse("2022-03-01T10:15:30+01:00"));
        measurementRepository.save(measurement1);
        measurementRepository.save(measurement2);
        measurementRepository.save(measurement3);
    }

    private Set<Consumption> createConsumptions(int value, Measurement measurement) {
        return Arrays.stream(ConsumptionType.values())
                .map(ct -> {
                    Consumption consumption = new Consumption();
                    consumption.setType(ct);
                    consumption.setValue(new BigDecimal(value));
                    consumption.setMeasurement(measurement);
                    return consumption;
                }).collect(Collectors.toSet());
    }

    private void cleanUpData() {
        userRepository.deleteAll();
    }
}
