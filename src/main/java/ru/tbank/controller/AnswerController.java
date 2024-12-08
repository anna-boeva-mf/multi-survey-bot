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
import ru.tbank.entity.Answer;
import ru.tbank.service.AnswerService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/v1/answer"})
public class AnswerController {
    @Autowired
    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @Operation(summary = "Получение всех ответов опроса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping
    public ResponseEntity<List<Answer>> getAllAnswersInSurvey(@RequestParam @NotNull Long surveyId) {
        List<Answer> answers = answerService.getAllAnswersInSurvey(surveyId);
        return new ResponseEntity<>(answers, HttpStatus.OK);
    }

    @Operation(summary = "Получение ответа в опросе по ид")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswerById(@PathVariable @NotNull Long id) {
        Answer answer = answerService.getAnswerById(id);
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @Operation(summary = "Добавление нового ответа в опрос")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping
    public ResponseEntity<Answer> createAnswer(@RequestBody Answer answer) {
        Answer answerFinal = answerService.createAnswer(answer);
        return new ResponseEntity<>(answerFinal, HttpStatus.CREATED);
    }

    @Operation(summary = "Изменение ответа в опросе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PutMapping("/{id}")
    public ResponseEntity<Answer> updateAnswer(@PathVariable @NotNull Long id, @RequestBody Answer answer) {
        Answer answerFinal = answerService.updateAnswer(id, answer);
        return new ResponseEntity<>(answerFinal, HttpStatus.CREATED);
    }

    @Operation(summary = "Удаление ответа в опросе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful deleting"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable @NotNull Long id) {
        boolean isDeleted = answerService.deleteAnswer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
