package ru.tbank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.tbank.entity.Role;
import ru.tbank.entity.User;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AdminService adminService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("User");
        role = new Role();
        role.setRoleName("Role");
    }

    @Test
    void testAddRoleToUser_OK() {
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        adminService.addRoleToUser("User", "Role");

        verify(userRepository, times(1)).save(user);
        Assertions.assertTrue(user.getRoles().contains(role), "Роль добавлена");
    }

    @Test
    void testAddRoleToUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> adminService.addRoleToUser("User ", "Role"), "Добавление роли несуществующему юзеру");
    }

    @Test
    void testAddRoleToUser_RoleNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> adminService.addRoleToUser("User ", "Role"), "Добавляемая роль не существует");
    }

    @Test
    void testAddRoleToUser_DataIntegrityViolationException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Ошибка"));

        Assertions.assertThrows(BadRequestException.class, () -> adminService.addRoleToUser("User ", "Role"), "Ошибка работы с базой");
    }

    @Test
    void testRemoveRoleFromUser_OK() {
        user.getRoles().add(role);
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        adminService.removeRoleFromUser("User ", "Role");

        verify(userRepository, times(1)).save(user);
        Assertions.assertFalse(user.getRoles().contains(role), "Успешное удаление роли у пользователя");
    }

    @Test
    void testRemoveRoleFromUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> adminService.removeRoleFromUser("User ", "Role"), "Пользователь дл добавления роли не найден");
    }

    @Test
    void testRemoveRoleFromUser_RoleNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> adminService.removeRoleFromUser("User ", "Role"), "Удаляемая роль не найдена");
    }

    @Test
    void testRemoveRoleFromUser_DataIntegrityViolationException() {
        user.getRoles().add(role);
        when(userRepository.findByUsername(anyString())).thenReturn(user);
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Ошибка"));

        Assertions.assertThrows(BadRequestException.class, () -> adminService.removeRoleFromUser("User ", "Role"), "Ошибка работы с базой");
    }

    @Test
    void testAddNewRole_OK() {
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());
        adminService.addNewRole("newRole");

        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testAddNewRole_RoleAlreadyExists() {
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> adminService.addNewRole("Role"), "Добавляемая роль уже существует");
    }

    @Test
    void testAddNewRole_DataIntegrityViolationException() {
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("Ошибка")).when(roleRepository).save(any(Role.class));

        Assertions.assertThrows(BadRequestException.class, () -> adminService.addNewRole("newRole"), "Ошибка работы с базой");
    }
}
