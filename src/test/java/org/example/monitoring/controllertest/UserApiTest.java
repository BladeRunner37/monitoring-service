package org.example.monitoring.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.monitoring.entity.Consumption;
import org.example.monitoring.entity.Measurement;
import org.example.monitoring.entity.User;
import org.example.monitoring.model.Error;
import org.example.monitoring.model.*;
import org.example.monitoring.repository.MeasurementRepository;
import org.example.monitoring.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserApiTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        prepareData();
    }

    @AfterEach
    public void cleanUp() {
        cleanUpData();
    }

    @Test
    public void getMeasurementsPositiveTest() throws Exception {
        String rawResponse = mockMvc.perform(get("/app/user/{login}/measurements", "user1")
                .param("offset", "0")
                .param("size", "2"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        MeasurementPage page = objectMapper.readValue(rawResponse, MeasurementPage.class);
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        MeasurementDto firstReceived = page.getPageContent().get(0);
        assertEquals("user1", firstReceived.getUserLogin());
        assertEquals(OffsetDateTime.parse("2022-03-01T10:15:30Z"), firstReceived.getDateSaved());
        assertConsumptions(firstReceived, new BigDecimal(300));
        MeasurementDto secondReceived = page.getPageContent().get(1);
        assertEquals("user1", firstReceived.getUserLogin());
        assertEquals(OffsetDateTime.parse("2022-02-01T10:15:30Z"), secondReceived.getDateSaved());
        assertConsumptions(secondReceived, new BigDecimal(200));
    }

    @Test
    public void getMeasurementsEmptyResultTest() throws Exception {
        String rawResponse = mockMvc.perform(get("/app/user/{login}/measurements", "user2")
                .param("offset", "0")
                .param("size", "2"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        MeasurementPage page = objectMapper.readValue(rawResponse, MeasurementPage.class);
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getTotalPages());
        assertTrue(page.getPageContent().isEmpty());
    }

    @Test
    public void getMeasurementsNotFoundTest() throws Exception {
        String rawResponse = mockMvc.perform(get("/app/user/{login}/measurements", "user3")
                .param("offset", "0")
                .param("size", "2"))
                .andExpect(status().is(404))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Error error = objectMapper.readValue(rawResponse, Error.class);
        assertEquals("User user3 not found", error.getErrorMessage());
    }

    @Test
    public void saveMeasurementTestPositive() throws Exception {
        BigDecimal value = new BigDecimal(1000);
        List<Consumptions> consumptions = Arrays.stream(ConsumptionType.values())
                .map(consumptionType -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(consumptionType);
                    consumptions1.setValue(value);
                    return consumptions1;
                }).collect(toList());
        MeasurementDto dto = new MeasurementDto();
        dto.setConsumptions(consumptions);

        String rawResponse = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String body = objectMapper.writeValueAsString(dto);

        MeasurementDto received = objectMapper.readValue(rawResponse, MeasurementDto.class);
        assertEquals("user1", received.getUserLogin());
        assertConsumptions(received, value);
    }

    @Test
    public void saveMeasuremetWrongValueTest() throws Exception {
        BigDecimal value = new BigDecimal(1000);
        List<Consumptions> consumptions = Arrays.stream(ConsumptionType.values())
                .map(consumptionType -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(consumptionType);
                    if (consumptionType != ConsumptionType.GAS) {
                        consumptions1.setValue(value);
                    } else {
                        consumptions1.setValue(new BigDecimal(200));
                    }
                    return consumptions1;
                }).collect(toList());
        MeasurementDto dto = new MeasurementDto();
        dto.setConsumptions(consumptions);

        String rawResponse = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Error error = objectMapper.readValue(rawResponse, Error.class);
        assertEquals("Consumption GAS value 200 less than last value 300, date 2022-03-01T11:15:30+01:00", error.getErrorMessage());
    }

    @Test
    public void saveMeasurementMultipleValueTest() throws Exception {
        BigDecimal value = new BigDecimal(1000);
        List<Consumptions> consumptions = Arrays.stream(ConsumptionType.values())
                .map(consumptionType -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(consumptionType);
                    consumptions1.setValue(value);
                    return consumptions1;
                }).collect(toList());
        Consumptions multiple = new Consumptions();
        multiple.setType(ConsumptionType.GAS);
        multiple.setValue(value);
        consumptions.add(multiple);
        MeasurementDto dto = new MeasurementDto();
        dto.setConsumptions(consumptions);

        String rawResponse = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Error error = objectMapper.readValue(rawResponse, Error.class);
        assertEquals("Multiple consumption GAS in request", error.getErrorMessage());
    }

    @Test
    public void saveMeasurementMissingValueTest() throws Exception {
        BigDecimal value = new BigDecimal(1000);
        List<Consumptions> consumptions = Arrays.stream(ConsumptionType.values())
                .map(consumptionType -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(consumptionType);
                    consumptions1.setValue(value);
                    return consumptions1;
                }).collect(toList());
        consumptions.removeIf(c -> ConsumptionType.GAS == c.getType());
        MeasurementDto dto = new MeasurementDto();
        dto.setConsumptions(consumptions);

        String rawResponse = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Error error = objectMapper.readValue(rawResponse, Error.class);
        assertEquals("Consumption GAS value in request is missing", error.getErrorMessage());
    }

    @Test
    public void saveMeasurementValidationErrorTest() throws Exception {
        MeasurementDto dto = new MeasurementDto();
        dto.setConsumptions(null);

        String rawResponse = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Error error = objectMapper.readValue(rawResponse, Error.class);
        assertTrue(error.getErrorMessage().contains("Field error in object 'measurementDto' on field 'consumptions': rejected value [null]"));

        BigDecimal value = new BigDecimal(1000);
        List<Consumptions> consumptions = Arrays.stream(ConsumptionType.values())
                .map(consumptionType -> {
                    Consumptions consumptions1 = new Consumptions();
                    consumptions1.setType(consumptionType);
                    if (consumptionType != ConsumptionType.GAS) {
                        consumptions1.setValue(value);
                    } else {
                        consumptions1.setValue(null);
                    }
                    return consumptions1;
                }).collect(toList());
        MeasurementDto dto2 = new MeasurementDto();
        dto2.setConsumptions(consumptions);

        String rawResponse2 = mockMvc.perform(post("/app/user/{login}/measurement", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Error error2 = objectMapper.readValue(rawResponse2, Error.class);
        assertTrue(error2.getErrorMessage().contains("Field error in object 'measurementDto' on field 'consumptions[0].value': rejected value [null]"));
    }

    private void assertConsumptions(MeasurementDto received, BigDecimal expectedValue) {
        Arrays.stream(ConsumptionType.values())
                .forEach(consumptionType -> {
                    BigDecimal value = received.getConsumptions().stream()
                            .filter(c -> consumptionType == c.getType())
                            .findFirst()
                            .map(Consumptions::getValue)
                            .orElseThrow(() -> new RuntimeException("Not found consumption with type " + consumptionType));
                    assertEquals(expectedValue, value);
                });
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
        measurement1.setDateSaved(OffsetDateTime.parse("2022-01-01T10:15:30Z"));
        Measurement measurement2 = new Measurement();
        measurement2.setUser(user1);
        measurement2.setConsumptions(createConsumptions(200, measurement2));
        measurement2.setDateSaved(OffsetDateTime.parse("2022-02-01T10:15:30Z"));
        Measurement measurement3 = new Measurement();
        measurement3.setUser(user1);
        measurement3.setConsumptions(createConsumptions(300, measurement3));
        measurement3.setDateSaved(OffsetDateTime.parse("2022-03-01T10:15:30Z"));
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
                }).collect(toSet());
    }

    private void cleanUpData() {
        userRepository.deleteAll();
    }
}
