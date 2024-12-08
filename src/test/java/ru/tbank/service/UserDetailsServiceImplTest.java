package ru.tbank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import ru.tbank.dto.UserCreateDTO;
import ru.tbank.entity.User;
import ru.tbank.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testLoadUseByUsername_OK() {
        String username = "existingUser";
        User user = new User(username, true, "First", "Last", "tgUsername", "-1", LocalDateTime.now());
        when(userRepository.findByUsername(username)).thenReturn(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(userDetails, "Результат не пуст"),
                () -> Assertions.assertEquals(username, userDetails.getUsername(), "Имя найденного пользователя совпадает с именем для поиска")
        );
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String username = "nonExistingUser";
        when(userRepository.findByUsername(username)).thenReturn(null);

        EntityNotFoundException thrown = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });
        Assertions.assertEquals("Пользователь не найден: " + username, thrown.getMessage(), "Искомый пользователь не существует");
    }

    @Test
    void testCreateTgUser_OK() {
        UserCreateDTO userCreateDTO = new UserCreateDTO("newUser", "First", "Last", "tgUsername");
        User newUser = new User("newUser", true, "First", "Last", "tgUsername", "-1", LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        User createdUser = userDetailsService.createTgUser(userCreateDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(createdUser, "Пользователь создан"),
                () -> Assertions.assertEquals("newUser", createdUser.getUsername(), "Имя созданного пользователя как ожидаемое")
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateTgUser_EmptyUsername() {
        UserCreateDTO userCreateDTO = new UserCreateDTO("", "First", "Last", "tgUsername");

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userDetailsService.createTgUser(userCreateDTO);
        });
        Assertions.assertEquals("Имя пользователя должно быть не пусто", thrown.getMessage(), "Добавление пользователя с пустым именем");
    }

    @Test
    void testFindTgUserByUsername_OK() {
        UserCreateDTO userCreateDTO = new UserCreateDTO("existingUser ", "First", "Last", "tgUsername");
        User user = new User("existingUser", true, "First", "Last", "tgUsername", "-1", LocalDateTime.now());
        when(userRepository.findByUsername(userCreateDTO.getUsername())).thenReturn(user);
        User foundUser = userDetailsService.findTgUserByUsername(userCreateDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(foundUser, "Пользователь найден"),
                () -> Assertions.assertEquals("existingUser", foundUser.getUsername(), "Имя найенного пользователя как ожидаемое")
        );
    }

    @Test
    void testFindTgUserByUsername_BadRequestException() {
        UserCreateDTO userCreateDTO = new UserCreateDTO("newUser", "First", "Last", "tgUsername");
        when(userRepository.findByUsername(userCreateDTO.getUsername())).thenThrow(new BadRequestException("error"));
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            userDetailsService.findTgUserByUsername(userCreateDTO);
        });
        Assertions.assertEquals("Ошибка в данных запроса: error", thrown.getMessage(), "Ошибка в запросе");
    }

    @Test
    void testDeleteUser_OK() {
        String username = "existingUser";
        User user = new User(username, true, "First", "Last", "tgUsername", "-1", LocalDateTime.now());
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        boolean isDeleted = userDetailsService.deleteUser(username);

        Assertions.assertTrue(isDeleted, "Пользователь успешно удален");
        verify(userRepository, times(1)).deleteById(user.getUserId());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userDetailsService.deleteUser("User"), "Удаляемый пользователь не найден");
    }

    @Test
    void testDeleteUser_DataIntegrityViolationException() {
        String username = "existingUser1";
        User user = new User(username, true, "First", "Last", "tgUsername", "-1", LocalDateTime.now());
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        doThrow(new DataIntegrityViolationException("Ошибка")).when(userRepository).deleteById(anyLong());

        Assertions.assertThrows(RuntimeException.class, () -> userDetailsService.deleteUser("User"), "Ошибка работы с базой");
    }

    @Test
    void testGetAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        EntityNotFoundException thrown = Assertions.assertThrows(EntityNotFoundException.class, () -> userDetailsService.getAllUsers());
        Assertions.assertEquals("Список пользователей пуст", thrown.getMessage());
    }

    @Test
    void testGetAllUsers_Success() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("User2");
        User user3 = new User();
        user3.setUserId(3L);
        user3.setUsername("User2");
        List<User> users = List.of(user2, user3);
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userDetailsService.getAllUsers();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(user2, result.get(0));
        Assertions.assertEquals(user3, result.get(1));
    }

    @Test
    void testGetAllUsers_UnknownError() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            userDetailsService.getAllUsers();
        });
        Assertions.assertEquals("Ошибка получения всех пользователей: Неизвестная ошибка", thrown.getMessage());
    }
}
