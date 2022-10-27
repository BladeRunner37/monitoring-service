package org.example.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.monitoring.entity.Consumption;
import org.example.monitoring.entity.Measurement;
import org.example.monitoring.entity.User;
import org.example.monitoring.model.Consumptions;
import org.example.monitoring.model.MeasurementDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Converter {

    public Measurement convert(MeasurementDto dto, User user) {
        Measurement measurement = new Measurement();
        measurement.setUser(user);
        measurement.setDateSaved(OffsetDateTime.now());
        measurement.setConsumptions(mapConsuptions(dto.getConsumptions(), measurement));
        return measurement;
    }

    private Set<Consumption> mapConsuptions(List<Consumptions> consumptions, Measurement measurement) {
        return consumptions.stream()
                .map(c -> {
                    Consumption consumption = new Consumption();
                    consumption.setMeasurement(measurement);
                    consumption.setType(c.getType());
                    consumption.setValue(c.getValue());
                    return consumption;
                }).collect(Collectors.toSet());
    }

    public MeasurementDto convert(Measurement measurement) {
        MeasurementDto dto = new MeasurementDto();
        dto.setId(measurement.getId());
        dto.setUserLogin(measurement.getUser().getLogin());
        dto.setDateSaved(measurement.getDateSaved());
        dto.setConsumptions(mapConsumptions(measurement.getConsumptions()));
        return dto;
    }

    private List<Consumptions> mapConsumptions(Set<Consumption> consumptions) {
        return consumptions.stream()
                .map(c -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(c.getType());
                    consumptions1.setValue(c.getValue());
                    return consumptions1;
                })
                .collect(Collectors.toList());
    }
}
