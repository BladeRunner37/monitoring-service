package org.example.monitoring.service;

import org.example.monitoring.entity.Consumption;
import org.example.monitoring.entity.Measurement;
import org.example.monitoring.exception.BusinessException;
import org.example.monitoring.model.ConsumptionType;
import org.example.monitoring.model.Consumptions;
import org.example.monitoring.model.MeasurementDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;

@Service
public class MeasurementRequestValidatorService {

    public static final String WRONG_VALUE_FORMAT = "Consumption %s value %s less than last value %s, date %s";
    public static final String MISSING_VALUE_FORMAT = "Consumption %s value in request is missing";
    public static final String MULTIPLE_VALUE_FORMAT = "Multiple consumption %s in request";

    public void validateMeasurementRequest(Measurement lastMeasurement, MeasurementDto request) {
        Map<ConsumptionType, List<BigDecimal>> requestedMap = request.getConsumptions().stream()
                .collect(groupingBy(Consumptions::getType, mapping(Consumptions::getValue, toList())));
        // We are sure there's single value for type because of DB constraint
        Map<ConsumptionType, BigDecimal> lastValues = lastMeasurement.getConsumptions().stream()
                .collect(toMap(Consumption::getType, Consumption::getValue));
        Arrays.stream(ConsumptionType.values())
                .forEach(consumptionType -> {
                    List<BigDecimal> values = requestedMap.get(consumptionType);
                    if (Objects.isNull(values) || values.isEmpty()) {
                        throwMissingValue(consumptionType);
                    }
                    if (values.size() > 1) {
                        throwMultipleValue(consumptionType);
                    }
                    BigDecimal requestedValue = values.get(0);
                    BigDecimal lastValue = lastValues.get(consumptionType);
                    if (Objects.nonNull(lastValue)
                            && lastValue.compareTo(requestedValue) > 0) {
                        throwWrongValue(consumptionType, requestedValue, lastValue, lastMeasurement.getDateSaved());
                    }
                });
    }

    private void throwMultipleValue(ConsumptionType consumptionType) {
        throw new BusinessException(String.format(MULTIPLE_VALUE_FORMAT, consumptionType), HttpStatus.BAD_REQUEST);
    }

    private void throwMissingValue(ConsumptionType consumptionType) {
        throw new BusinessException(String.format(MISSING_VALUE_FORMAT, consumptionType), HttpStatus.BAD_REQUEST);
    }

    private void throwWrongValue(ConsumptionType requestedType, BigDecimal requestedValue, BigDecimal lastValue, OffsetDateTime dateSaved) {
        throw new BusinessException(String.format(WRONG_VALUE_FORMAT, requestedType, requestedValue, lastValue, dateSaved),
                HttpStatus.BAD_REQUEST);
    }
}
