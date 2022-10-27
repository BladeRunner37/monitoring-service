package org.example.monitoring.rest.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.monitoring.api.UserApiDelegate;
import org.example.monitoring.model.MeasurementDto;
import org.example.monitoring.model.MeasurementPage;
import org.example.monitoring.service.MeasurementService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApiDelegateImpl implements UserApiDelegate {

    private final MeasurementService measurementService;

    @Override
    public ResponseEntity<MeasurementPage> getMeasurements(String login, Integer offset, Integer size) {
        log.info("getMeasurements request, login {}, offset {}, size {}", login, offset, size);
        MeasurementPage page = measurementService.findUserMeasurements(login, offset, size);
        log.info("getMeasurements response {}", page);
        return ResponseEntity.ok(page);
    }

    @Override
    public ResponseEntity<MeasurementDto> saveMeasurement(String login, MeasurementDto measurementDto) {
        log.info("saveMeasurement request, login {}, body {}", login, measurementDto);
        MeasurementDto saved = measurementService.saveMeasurement(login, measurementDto);
        log.info("saveMeasurement response {}", saved);
        return ResponseEntity.ok(saved);
    }
}
