package org.example.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.monitoring.entity.Measurement;
import org.example.monitoring.entity.User;
import org.example.monitoring.model.MeasurementDto;
import org.example.monitoring.model.MeasurementPage;
import org.example.monitoring.repository.MeasurementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private static final String DATE_SAVED_SORT = "dateSaved";

    private final UserService userService;
    private final MeasurementRepository measurementRepository;
    private final MeasurementRequestValidatorService measurementRequestValidatorService;
    private final Converter converter;

    @Transactional(readOnly = true)
    public MeasurementPage findUserMeasurements(String login, int offset, int size) {
        userService.findUser(login);
        Page<Measurement> page =
                measurementRepository.findByUserLogin(login, PageRequest.of(offset, size, Sort.by(Sort.Order.desc(DATE_SAVED_SORT))));
        return convertPage(page);
    }

    private MeasurementPage convertPage(Page<Measurement> page) {
        MeasurementPage measurementPage = new MeasurementPage();
        measurementPage.setTotalElements(page.getTotalElements());
        measurementPage.setTotalPages(page.getTotalPages());
        measurementPage.setPageContent(page.getContent().stream()
                .map(converter::convert)
                .collect(Collectors.toList()));
        return measurementPage;
    }

    @Transactional
    public MeasurementDto saveMeasurement(String login, MeasurementDto measurementDto) {
        User user = userService.getOrCreateUser(login);
        Optional<Measurement> lastMeasurementOpt = measurementRepository.findTopByUserLoginOrderByDateSavedDesc(login);
        lastMeasurementOpt.ifPresent(measurement ->
                measurementRequestValidatorService.validateMeasurementRequest(measurement, measurementDto));
        return converter.convert(measurementRepository.save(converter.convert(measurementDto, user)));
    }
}
