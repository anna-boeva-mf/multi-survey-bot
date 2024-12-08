package ru.tbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.dto.UserRegistrationDTO;
import ru.tbank.entity.User;
import ru.tbank.service.RegistrationService;

@Slf4j
@RestController
@RequestMapping({"/api/v1/register"})
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful registration"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping
    public ResponseEntity<User> register(@RequestBody UserRegistrationDTO userRegistrationDto) {
        User user = registrationService.registerUser(userRegistrationDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
