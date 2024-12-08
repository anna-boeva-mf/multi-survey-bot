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
import ru.tbank.repository.SurveyGroupRepository;

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
class SurveyGroupControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private static String accessToken;
    @Autowired
    private SurveyGroupRepository surveyGroupRepository;

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
    void testGetSAllSurveyGroups_Empty_AndCreateUserForTests() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserSG\",\"password\": \"MypassworSG\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"UserSG\",\"password\": \"MypassworSG\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        accessToken = jsonObject.get("accessToken").getAsString();

        mockMvc.perform(get("/api/v1/survey-group")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllSurveyGroups_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test1\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/survey-group")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllSurveyGroups_Unauth() throws Exception {
        mockMvc.perform(get("/api/v1/survey-group"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetSurveyGroupById_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test2\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(get("/api/v1/survey-group/test2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSurveyGroupByName_NotExists() throws Exception {
        mockMvc.perform(get("/api/v1/survey-group/test2rrrrrrr")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSurveyGroup_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test3\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        boolean createdFlg = surveyGroupRepository.existsBySurveyGroupName("test3");
        Assertions.assertTrue(createdFlg, "Группа опросов добавлена");
    }

    @Test
    void testCreateSurveyGroup_NotCaseSensitive() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test4\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"TeSt4\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Группа опросов с таким названием уже существует"))
                .andDo(print());
    }

    @Test
    void testCreateSurveyGroup_BadRequestInput() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyTypeId\": 1111}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testCreateSurveyGroup_NotExistentSurveyTypeId() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test7\",\"surveyTypeId\": 1111}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testUpdateSurveyGroup_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test6\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(put("/api/v1/survey-group/Test6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test666\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testUpdateSurveyGroup_BadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test8\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(put("/api/v1/survey-group/Test8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    @Test
    void testUpdateSurveyGroup_NotFoundForUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/survey-group/Test9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test999\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Группа опросов с таким названием не существует"))
                .andDo(print());
    }

    @Test
    void testUpdateSurveyGroup_BadReq_NameAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test100\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test101\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(put("/api/v1/survey-group/Test101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"Test100\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Группа опросов с таким новым названием уже существует"))
                .andDo(print());
    }

    @Test
    void testDeleteSurveyGroup_OK() throws Exception {
        mockMvc.perform(post("/api/v1/survey-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"surveyGroupName\": \"test102\",\"surveyTypeId\": 1}")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andDo(print());

        boolean createdFlg = surveyGroupRepository.existsBySurveyGroupName("test102");

        mockMvc.perform(delete("/api/v1/survey-group/test102")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(print());

        boolean deletedFlg = !surveyGroupRepository.existsBySurveyGroupName("test102");

        Assertions.assertAll(
                () -> Assertions.assertTrue(createdFlg, "Группа опросов была добавлена"),
                () -> Assertions.assertTrue(deletedFlg, "Группа опросов была удалена")
        );
    }

    @Test
    void testDeleteSurveyGroup_DeleteUnExistGroup() throws Exception {
        mockMvc.perform(delete("/api/v1/survey-group/test103")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testDeleteSurveyGroup_EmptyInput() throws Exception {
        mockMvc.perform(delete("/api/v1/survey-group")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }
}