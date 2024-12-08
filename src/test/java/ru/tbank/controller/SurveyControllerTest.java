package ru.tbank.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import ru.tbank.repository.SurveyRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static String accessToken;
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
    @Order(1)
    void testGetAllSurveysInGroup_Empty_AndCreateUserForTests() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserS\",\"password\": \"MypassworS\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserS\",\"password\": \"MypassworS\"}"))
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

        mockMvc.perform(get("/api/v1/survey?surveyGroupId=1&withAnswers=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllSurveysInGroup_Ok() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Как?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/survey?surveyGroupId=1&withAnswers=1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllSurveysInGroup_Ok_WithoutAnswers() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Как1?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/survey?surveyGroupId=1&withAnswers=0")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllSurveysInGroup_NotExists() throws Exception {
        mockMvc.perform(get("/api/v1/survey?surveyGroupId=1111")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetSurvey_Ok() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Как должно быть?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonSecondResult = new String(result.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonSecondResult).getAsJsonObject();
        String surveyId = jsonObject.get("surveyId").getAsString();

        mockMvc.perform(get("/api/v1/survey/" + surveyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateSurvey_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Как дела?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void testCreateSurvey_NotCaseSensitive() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Точно?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"ТОЧНО?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Опрос уже содержится в группе опросов"))
                .andDo(print());
    }

    @Test
    void testCreateSurvey_NotExistentSurveyGroupId() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1121212")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Точно?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testUpdateSurvey_OK() throws Exception {
        MvcResult secondResult = mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Понравилось?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonSecondResult = new String(secondResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonSecondResult).getAsJsonObject();
        String surveyId = jsonObject.get("surveyId").getAsString();

        mockMvc.perform(put("/api/v1/survey/" + surveyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Получилось?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testUpdateSurvey_NotFoundForUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/survey/4444")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Рано?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Опрос не найден"))
                .andDo(print());
    }

    @Test
    void testDeleteSurvey_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey?surveyGroupId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyQuestion\": \"Что делать?\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());
        Long surveyId = surveyRepository.findBySurveyGroupId(1L).get(0).getSurveyId();
        //surveyService.getAllSurveysInGroup(1L).get(0).getSurveyId();

        mockMvc.perform(delete("/api/v1/survey/" + surveyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void testDeleteSurvey_DeleteUnExistSurvey() throws Exception {
        mockMvc.perform(delete("/api/v1/survey/555555")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testDeleteSurvey_EmptyInput() throws Exception {
        mockMvc.perform(delete("/api/v1/survey")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }
}