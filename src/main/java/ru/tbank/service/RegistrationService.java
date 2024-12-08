package ru.tbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tbank.dto.UserRegistrationDTO;
import ru.tbank.entity.Role;
import ru.tbank.entity.User;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.exception.RegistrationException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.RoleRepository;
import ru.tbank.repository.UserRepository;

import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@LogExecutionTime
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Transactional
    public User registerUser(UserRegistrationDTO userRegistrationDto){
        log.info("Регистрация пользователя");
        try {
            String userName = userRegistrationDto.getUsername();
            if (userExists(userName)) {
                throw new EntityAlreadyExistsException("Пользователь с именем " + userName + " уже существует");
            }
            String password = userRegistrationDto.getPassword();
            if (!(StringUtils.hasText(userName) && StringUtils.hasText(password))) {
                throw new BadRequestException("Имя пользователя и пароль не должны быть пусты");
            }
            User user = new User();
            user.setUsername(userName);
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            List<String> userRoles = new ArrayList<>();
            userRoles.add("USER");
            Set<Role> roles = userRoles.stream()
                    .map(roleName -> roleRepository.findByRoleName(roleName)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setRoleName(roleName);
                                return roleRepository.save(newRole);
                            }))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
            user.setInsertDt(LocalDateTime.now());
            return userRepository.save(user);
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (RegistrationException e) {
            log.error(e.getMessage());
            throw new RegistrationException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления пользователя в базу");
            throw new RegistrationException("Ошибка добавления пользователя в базу: " + e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка запроса");
            throw new BadRequestException("Ошибка запроса: " + e.getMessage());
        } catch (InternalServerErrorException e) {
            log.error("Ошибка работы сервиса");
            throw new InternalServerErrorException("Ошибка работы сервиса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка регистрации нового пользователя: {}", e.getMessage());
            throw new RuntimeException("Ошибка регистрации нового пользователя: " + e.getMessage());
        }
    }
}
