package ru.tbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.dto.ResultDTO;
import ru.tbank.entity.Result;
import ru.tbank.service.ResultService;

@Slf4j
@RestController
@RequestMapping({"/api/v1/result"})
public class ResultController {
    @Autowired
    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @Operation(summary = "Получение результата опроса юзера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping
    public ResponseEntity<Result> getResult(@RequestParam @NotNull Long userId, @RequestParam @NotNull Long surveyId) {
        Result result = resultService.getResult(userId, surveyId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Получение результата опроса юзера по ид")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping("/{id}")
    public ResponseEntity<Result> getResultById(@PathVariable @NotNull Long id) {
        Result result = resultService.getResultById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Добавление результата опроса юзера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping
    public ResponseEntity<Result> createResult(@RequestBody ResultDTO resultDTO) {
        Result result = resultService.createResult(resultDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновление результата опроса юзера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PutMapping("/{id}")
    public ResponseEntity<Result> updateResult(@PathVariable @NotNull Long id, @RequestBody ResultDTO resultDTO) {
        Result result = resultService.updateResult(id, resultDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Удаление результата опроса юзера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful deleting"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable @NotNull Long id) {
        boolean isDeleted = resultService.deleteResult(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
