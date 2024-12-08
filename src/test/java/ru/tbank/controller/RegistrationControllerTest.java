package ru.tbank.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @InjectMocks
    private RegistrationController registrationController;

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
    void testRegisterUser_OK() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User111\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        long userId = userRepository.findByUsername("User111").getUserId();

        String url = pgDB.getJdbcUrl();
        String user = pgDB.getUsername();
        String password = pgDB.getPassword();
        String value = "";
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "select roles.role_name as name1 from security.roles " +
                    "join security.user_roles " +
                    "on roles.role_id = user_roles.role_id " +
                    "where user_roles.user_id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, (int) userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    value = resultSet.getString("name1");
                }
            }
        }
        String finalValue = value;
        Assertions.assertAll(
                () -> Assertions.assertNotNull(userRepository.findByUsername("User111"), "Пользователь добавился"),
                () -> Assertions.assertEquals("USER", finalValue, "Для пользователя добавилась роль USER")
        );
    }

    @Test
    void testRegisterUser_UserAlredyExists() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User1\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User1\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void testRegisterUser_NoUserName() throws Exception {
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"Mypassword\"}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
