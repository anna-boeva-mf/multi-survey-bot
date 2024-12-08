package ru.tbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.dto.RoleCreationDTO;
import ru.tbank.dto.RoleRequestDTO;
import ru.tbank.entity.User;
import ru.tbank.service.AdminService;
import ru.tbank.service.UserDetailsServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserDetailsServiceImpl UserService;

    public AdminController(AdminService adminService, UserDetailsServiceImpl userService) {
        this.adminService = adminService;
        UserService = userService;
    }

    @Operation(summary = "Добавление роли пользователю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping("/role/add")
    public ResponseEntity<Void> addRole(@RequestBody RoleRequestDTO request) {
        adminService.addRoleToUser(request.getUsername(), request.getRoleName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Удаление роли у пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping("/role/remove")
    public ResponseEntity<Void> removeRole(@RequestBody RoleRequestDTO request) {
        adminService.removeRoleFromUser(request.getUsername(), request.getRoleName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Содание новой роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping("/role/new")
    public ResponseEntity<String> addNewRole(@RequestBody RoleCreationDTO roleCreationDTO) {
        adminService.addNewRole(roleCreationDTO.getRoleName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Получение всех пользователей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping("/user")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = UserService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Получение пользователя по имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping("/user/{name}")
    public ResponseEntity<User> getUserByUsername(@PathVariable @NotBlank String name) {
        User user = (User) UserService.loadUserByUsername(name);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(summary = "Удаление пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @DeleteMapping("/user/delete/{name}")
    public ResponseEntity<Void> deleteUser(@PathVariable @NotBlank String name) {
        UserService.deleteUser(name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
