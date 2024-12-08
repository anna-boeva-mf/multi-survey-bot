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
import ru.tbank.repository.AnswerRepository;
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
class AnswerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static String accessToken;
    @Autowired
    private AnswerRepository answerRepository;
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
    void testGetAllAnswersInSurvey_Empty_AndCreateUserForTests() throws Exception {
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

        mockMvc.perform(get("/api/v1/answer?surveyId=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAnswersInSurvey_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/answer?surveyId=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAnswerById_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ2\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long answerId = answerRepository.findAll().stream().filter(a -> a.getAnswer().equalsIgnoreCase("Ответ2")).collect(Collectors.toList()).get(0).getAnswerId();

        mockMvc.perform(get("/api/v1/answer/" + answerId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateAnswer_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ3\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        Assertions.assertTrue(surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ3"))), "Ответ добавился в опрос");
    }

    @Test
    void testUpdateAnswer_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ4\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Boolean answerWasInSurvey = surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ4")));
        Long answerId = answerRepository.findAll().stream().filter(a -> a.getAnswer().equalsIgnoreCase("Ответ4")).collect(Collectors.toList()).get(0).getAnswerId();

        mockMvc.perform(put("/api/v1/answer/" + answerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ5\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        Boolean answerInSurvey = surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ4")));
        Boolean newAnswerInSurvey = surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ5")));

        Assertions.assertAll(
                () -> Assertions.assertTrue(answerWasInSurvey, "Ответ был обавлен в опрос"),
                () -> Assertions.assertFalse(answerInSurvey, "Больше нет такого ответа"),
                () -> Assertions.assertTrue(newAnswerInSurvey, "Ответ изменился")
        );
    }

    @Test
    void testUpdateAnswer_NotExists() throws Exception {
        mockMvc.perform(put("/api/v1/answer/454545454545")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ5\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testDeleteAnswer_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Ответ10\",\"correctFlg\": false," +
                                "    \"survey\":{\"surveyId\": 1,\n" +
                                "    \"surveyQuestion\": \"Вопрос?\",\"surveyTypeId\": 1,\"surveyGroupId\": 1}}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Boolean answerInSurvey = surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ10")));
        Long answerId = answerRepository.findAll().stream().filter(a -> a.getAnswer().equalsIgnoreCase("Ответ10")).collect(Collectors.toList()).get(0).getAnswerId();

        mockMvc.perform(delete("/api/v1/answer/" + answerId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(print());
        Boolean answerInSurveyAfterDelete = surveyRepository.findBySurveyId(1L).getAnswers().stream().anyMatch((a -> a.getAnswer().equalsIgnoreCase("Ответ10")));

        Assertions.assertAll(
                () -> Assertions.assertTrue(answerInSurvey, "Ответ был обавлен в опрос"),
                () -> Assertions.assertFalse(answerInSurveyAfterDelete, "Ответ был удален")
        );
    }

    @Test
    void testDeleteAnswer_Unauth() throws Exception {
        mockMvc.perform(delete("/api/v1/answer/" + 1))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
