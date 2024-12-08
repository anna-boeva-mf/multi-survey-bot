package ru.tbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.tbank.entity.Role;
import ru.tbank.entity.User;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.util.Optional;

@Slf4j
@Service
@LogExecutionTime
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void addRoleToUser(String username, String roleName) {
        log.info("Добавление роли пользователю");
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                log.error("Пользователь не найден");
                throw new EntityNotFoundException("Пользователь не найден: " + username);
            }
            Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new EntityNotFoundException("Роль не найдена: " + roleName));
            user.getRoles().add(role);
            userRepository.save(user);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления роли пользователю, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка добавления роли пользователю: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка добавления роли пользователю: {}", e.getMessage());
            throw new RuntimeException("Ошибка добавления роли пользователю: " + e.getMessage());
        }
    }

    public void removeRoleFromUser(String username, String roleName) {
        log.info("Удаление роли у пользователя");
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                log.error("Пользователь не найден");
                throw new EntityNotFoundException("Пользователь не найден: " + username);
            }
            Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new EntityNotFoundException("Роль не найдена: " + roleName));
            user.getRoles().remove(role);
            userRepository.save(user);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления роли у пользователя, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка удаления роли у пользователя: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления роли у пользователя: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления роли у пользователя: " + e.getMessage());
        }
    }

    public void addNewRole(String roleName) {
        log.info("Создание новой роли");
        try {
            Optional<Role> existingRole = roleRepository.findByRoleName(roleName);
            if (existingRole.isPresent()) {
                log.error("Роль уже существует");
                throw new EntityAlreadyExistsException("Роль с названием " + roleName + " уже существует.");
            }
            Role newRole = new Role();
            newRole.setRoleName(roleName);
            roleRepository.save(newRole);
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка создания новой роли, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка создания новой роли: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка создания новой роли: {}", e.getMessage());
            throw new RuntimeException("Ошибка создания новой роли: " + e.getMessage());
        }
    }
}
