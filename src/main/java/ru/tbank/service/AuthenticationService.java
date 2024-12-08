package ru.tbank.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tbank.dto.AuthenticationRequestDTO;
import ru.tbank.dto.AuthenticationResponseDTO;
import ru.tbank.dto.ResetPassRequestDTO;
import ru.tbank.entity.User;
import ru.tbank.exception.RegistrationException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@LogExecutionTime
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private static final long ACCESS_TOKEN_EXPIRATION_TIME_MINUTES = 20;
    @Value("${spring.security.jwt.private-key}")
    private String privateKey;

    public AuthenticationResponseDTO login(AuthenticationRequestDTO request) {
        log.info("Логин пользователя");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = generateAccessToken(userDetails);
            AuthenticationResponseDTO response = new AuthenticationResponseDTO(accessToken);
            return response;
        } catch (BadRequestException e) {
            log.error("Ошибка обработки запроса на логин: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки запроса на логин: {}", e.getMessage());
            throw new RuntimeException("Ошибка ообработки запроса на логин: " + e.getMessage());
        }
    }

    public void logout(HttpServletRequest request) {
        log.info("Логаут пользователя");
        SecurityContextHolder.clearContext();
    }

    public void resetPassword(ResetPassRequestDTO resetPassRequestDTO) {
        log.info("Смена пароля пользователя");
        try {
            String token = resetPassRequestDTO.getToken();
            User user = extractUserByToken(token);
            if (user == null) {
                throw new EntityNotFoundException("Пользователь не найден");
            }
            if (!resetPassRequestDTO.getNewPassword().equals(resetPassRequestDTO.getConfirmPassword())) {
                throw new BadRequestException("Новый пароль и пароль для подтверждения не совпадают");
            }
            user.setPassword(passwordEncoder.encode(resetPassRequestDTO.getNewPassword()));
            userRepository.save(user);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка смены пароля у пользователя, ошибка в работе с базой");
            throw new RegistrationException("Ошибка смены пароля у пользователя: " + e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка запроса");
            throw new BadRequestException("Ошибка запроса: " + e.getMessage());
        } catch (InternalServerErrorException e) {
            log.error("Ошибка работы сервиса");
            throw new InternalServerErrorException("Ошибка работы сервиса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка смены пароля у пользователя: {}", e.getMessage());
            throw new RuntimeException("Ошибка смены пароля у пользователя: " + e.getMessage());
        }
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, ACCESS_TOKEN_EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
    }

    private String generateToken(UserDetails userDetails, long expirationTime, TimeUnit timeUnit) {
        long expirationMillis = timeUnit.toMillis(expirationTime);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS512, privateKey)
                .compact();
    }

    private Key codedKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(privateKey));
    }

    private Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(codedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public User extractUserByToken(String token) {
        Claims claims = getClaims(token);
        String username = claims.get("sub", String.class);
        User user = userRepository.findByUsername(username);
        return user;
    }

    public boolean isTokenValid(String token) {
        log.info("Проверка валидности токена");
        return getClaims(token).getExpiration().after(new Date());
    }
}
