package ru.tbank.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
import ru.tbank.entity.Role;
import ru.tbank.entity.User;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerTest {
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
    void testAddRole_RoleDoesntExists() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("AdminNNNN");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //адмир логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        //пытается добавить роль (себе же), но такой роли нет
        mockMvc.perform(post("/api/v1/admin/role/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN\",\"roleName\": \"SUPER_ADMIN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Роль не найдена: SUPER_ADMIN"))
                .andDo(print());
    }

    @Test
    void testAddRole_OK() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin1\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("Admin1");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //админ логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin1\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();
        //админ создает новую роль
        mockMvc.perform(post("/api/v1/admin/role/new")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\": \"GUEST2\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        //создать еще какого-то пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User5\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //админ добавляет ему новую роль
        mockMvc.perform(post("/api/v1/admin/role/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User5\",\"roleName\": \"GUEST2\"}"))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    void testRemoveRole_OK_ByAdmin() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin12\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("Admin12");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //админ логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin12\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        //создать еще какого-то пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User7\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //админ удаляет у него его единственную поль USER
        mockMvc.perform(post("/api/v1/admin/role/remove")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User7\",\"roleName\": \"USER\"}"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testRemoveRole_FromNotExistentUser() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin3\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("Admin3");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //админ логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin3\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        //админ удаляет у несуществующего пользователя роль
        mockMvc.perform(post("/api/v1/admin/role/remove")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User454545\",\"roleName\": \"USER\"}"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void testAddNewRole_ByNotAdmin() throws Exception {
        //создать пользователя, он просто USER
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User4\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //юзер логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User4\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();
        //обычный пользователь пытается создать новую роль
        mockMvc.perform(post("/api/v1/admin/role/new")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\": \"GUEST3\"}"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    void testDeleteUser() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin389\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("Admin389");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //админ логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Admin389\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        //созать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"User4534\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(delete("/api/v1/admin/user/delete/" + "User4534")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andDo(print());

        User deletedUser = userRepository.findByUsername("User4534");
        Assertions.assertNull(deletedUser);
    }

    @Test
    void testGetAllUsers_OK() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN4\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("AdminNNNN4");
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //адмир логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN4\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        mockMvc.perform(get("/api/v1/admin/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testGetUser_OK() throws Exception {
        //создать пользователя
        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN3\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isCreated())
                .andDo(print());
        //сделать его админом
        User user = userRepository.findByUsername("AdminNNNN3");
        String userName = user.getUsername();
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        Set<Role> roles = userRoles.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setRoleName(roleName);
                            return roleRepository.save(newRole);
                        }))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userRepository.save(user);
        //адмир логинится
        MvcResult afterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"AdminNNNN3\",\"password\": \"Mypassword\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonAfterResult = new String(afterResult.getResponse().getContentAsByteArray());
        JsonObject jsonObject = JsonParser.parseString(jsonAfterResult).getAsJsonObject();
        String accessToken = jsonObject.get("accessToken").getAsString();

        mockMvc.perform(get("/api/v1/admin/user/" + userName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
