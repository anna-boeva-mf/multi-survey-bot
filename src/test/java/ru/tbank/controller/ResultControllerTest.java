package ru.tbank.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.repository.ResultRepository;
import ru.tbank.repository.SurveyRepository;

import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResultControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static String accessToken;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private SurveyRepository surveyRepository;

    @Container
    public static PostgreSQLContainer<?> pgDB = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("kudago_test")
            .withUsername("pguser_test")
            .withPassword("pgpwd_test");

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgDB::getJdbcUrl);
        registry.add("spring.datasource.username", pgDB::getUsername);
        registry.add("spring.datasource.password", pgDB::getPassword);
    }

    @Test
    @Order(0)
    void testGetAllResults_OK_AndCreateUserForTests() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserA\",\"password\": \"MypasswordA\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserA\",\"password\": \"MypasswordA\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        accessToken = jsonObject.get("accessToken").getAsString();

        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": 1,\"userResult\": \"[1]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/result?userId=1&surveyId=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllResults_NotExistentUser() throws Exception {
        mockMvc.perform(get("/api/v1/result?userId=111111&surveyId=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateResult_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Вопрос3?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ3\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос3?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long surveyId = surveyRepository.findAll().stream().filter(s -> s.getSurveyQuestion().equalsIgnoreCase("Вопрос3?")).collect(Collectors.toList()).get(0).getSurveyId();

        MvcResult result = mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": " + surveyId + ",\"userResult\": \"[1]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResult = new String(result.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
        String resultId = jsonObject.get("resultId").getAsString();

        Assertions.assertTrue(resultRepository.existsByResultId(Long.valueOf(resultId)), "Ответ был добавлен");
    }

    @Test
    void testCreateResult_BadRequest_WithoutUserId() throws Exception {
        mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyId\": 1,\"userResult\": \"[2]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testGetResultById_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Вопрос2?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ2\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос2?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long surveyId = surveyRepository.findAll().stream().filter(s -> s.getSurveyQuestion().equalsIgnoreCase("Вопрос2?")).collect(Collectors.toList()).get(0).getSurveyId();

        MvcResult result = mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": " + surveyId + ",\"userResult\": \"[1]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResult = new String(result.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
        String resultId = jsonObject.get("resultId").getAsString();

        mockMvc.perform(get("/api/v1/result/" + resultId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetResultById_NotAuth() throws Exception {
        mockMvc.perform(get("/api/v1/result/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteResult_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Вопрос4?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ4\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос4?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long surveyId = surveyRepository.findAll().stream().filter(s -> s.getSurveyQuestion().equalsIgnoreCase("Вопрос4?")).collect(Collectors.toList()).get(0).getSurveyId();

        MvcResult result = mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": " + surveyId + ",\"userResult\": \"[1]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResult = new String(result.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
        String resultId = jsonObject.get("resultId").getAsString();
        Boolean wasAdded = resultRepository.existsByResultId(Long.valueOf(resultId));

        mockMvc.perform(delete("/api/v1/result/" + resultId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(print());
        Boolean wasDeleted = resultRepository.existsByResultId(Long.valueOf(resultId));

        Assertions.assertAll(
                () -> Assertions.assertTrue(wasAdded, "Ответ был добавлен"),
                () -> Assertions.assertFalse(wasDeleted, "Ответ был удален")
        );
    }

    @Test
    void testDeleteResult_NotExistentResultId() throws Exception {
        mockMvc.perform(delete("/api/v1/result/14568")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testUpdateResult_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Вопрос5?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ5\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос5?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long surveyId = surveyRepository.findAll().stream().filter(s -> s.getSurveyQuestion().equalsIgnoreCase("Вопрос5?")).collect(Collectors.toList()).get(0).getSurveyId();

        MvcResult result = mockMvc.perform(post("/api/v1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": " + surveyId + ",\"userResult\": \"[2]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResult = new String(result.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
        String resultId = jsonObject.get("resultId").getAsString();
        Boolean wasAdded = resultRepository.existsByResultId(Long.valueOf(resultId));

        mockMvc.perform(put("/api/v1/result/" + resultId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1,\"surveyId\": " + surveyId + ",\"userResult\": \"[1]\"}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        String userResult = resultRepository.findByUserIdAndSurveyId(1L, surveyId).getUserResult();

        Assertions.assertAll(
                () -> Assertions.assertTrue(wasAdded, "Ответ был добавлен"),
                () -> Assertions.assertEquals("[1]", userResult, "Ответ был изменен")
        );
    }
}