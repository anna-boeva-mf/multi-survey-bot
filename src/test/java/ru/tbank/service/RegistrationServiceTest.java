package ru.tbank.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.tbank.dto.UserRegistrationDTO;
import ru.tbank.entity.Role;
import ru.tbank.entity.User;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.exception.RegistrationException;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import javax.ws.rs.BadRequestException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void testUserExists_OK() {
        String username = "existingUser";
        User existingUser = new User();
        existingUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(existingUser);
        boolean exists = registrationService.userExists(username);

        Assertions.assertTrue(exists, "Пользователь существует");
    }

    @Test
    void testUserExists_UserDoesNotExist() {
        String username = "newUser";
        when(userRepository.findByUsername(username)).thenReturn(null);
        boolean exists = registrationService.userExists(username);

        Assertions.assertFalse(exists, "Пользователь не сущетвует");
    }

    @Test
    void testRegisterUser_OK() {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newUser");
        registrationDTO.setPassword("password123");
        when(userRepository.findByUsername("newUser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User registeredUser = registrationService.registerUser(registrationDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(registeredUser, "Пользователь зарегистрирован"),
                () -> Assertions.assertEquals("newUser", registeredUser.getUsername(), "Имя совпадает"),
                () -> Assertions.assertEquals("encodedPassword", registeredUser.getPassword(), "Пароль совпадает"),
                () -> Assertions.assertNotNull(registeredUser.getRoles(), "Роли совпадают"),
                () -> Assertions.assertFalse(registeredUser.getRoles().isEmpty(), "Роли не пусты")
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("existingUser");
        registrationDTO.setPassword("password123");
        User existingUser = new User();
        existingUser.setUsername("existingUser");
        when(userRepository.findByUsername("existingUser")).thenReturn(existingUser);

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> registrationService.registerUser(registrationDTO), "Регистрация уже существующего пользователя");
    }

    @Test
    void testRegisterUser_EmptyUsername() {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("");
        registrationDTO.setPassword("password123");

        Assertions.assertThrows(BadRequestException.class, () -> registrationService.registerUser(registrationDTO), "Пустое имя пользователя");
    }

    @Test
    void testRegisterUser_DataIntegrityViolationException() {
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newUser");
        registrationDTO.setPassword("password123");
        when(userRepository.findByUsername("newUser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenThrow(new DataIntegrityViolationException("Database error"));

        Assertions.assertThrows(RegistrationException.class, () -> registrationService.registerUser(registrationDTO), "Ошибка работы с базой");
    }
}
