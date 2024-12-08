package ru.tbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tbank.dto.UserCreateDTO;
import ru.tbank.entity.User;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@LogExecutionTime
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new EntityNotFoundException("Пользователь не найден: " + username);
            }
            return user;
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения пользователя, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения пользователя: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения пользователя: " + e.getMessage());
        }
    }

    public User createTgUser(UserCreateDTO userCreateDTO) {
        log.info("Создание пользователя из телеграма");
        try {
            String username = userCreateDTO.getUsername();
            if (!StringUtils.hasText(username)) {
                throw new IllegalArgumentException("Имя пользователя должно быть не пусто");
            } else {
                User user = new User(userCreateDTO.getUsername(), true, userCreateDTO.getTgFirstname(), userCreateDTO.getTgLastname(), userCreateDTO.getTgUsername(), "-1", LocalDateTime.now());
                return userRepository.save(user);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка создания пользователя из телеграма, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка создания пользователя из телеграма: {}", e.getMessage());
            throw new RuntimeException("Ошибка создания пользователя из телеграма: " + e.getMessage());
        }
    }

    public User findTgUserByUsername(UserCreateDTO userCreateDTO) {
        log.info("Поиск пользователя из телеграма");
        try {
            User user = userRepository.findByUsername(userCreateDTO.getUsername());
            if (user == null) {
                user = createTgUser(userCreateDTO);
            }
            return user;
        } catch (BadRequestException e) {
            log.error("Ошибка поиска пользователя из телеграма, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка поиска пользователя из телеграма: {}", e.getMessage());
            throw new RuntimeException("Ошибка поиска пользователя из телеграма: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                throw new EntityNotFoundException("Список пользователей пуст");
            } else {
                return users;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения всех пользователей: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех пользователей: " + e.getMessage());
        }
    }

    public boolean deleteUser(String userName) {
        log.info("Удаление юзера");
        try {
            User user = userRepository.findByUsername(userName);
            if (user != null) {
                userRepository.deleteById(user.getUserId());
                return true;
            } else {
                throw new EntityNotFoundException("Пользователя с таким названием не существует");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления пользователя, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления пользователя: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления пользователя: " + e.getMessage());
        }
    }
}
