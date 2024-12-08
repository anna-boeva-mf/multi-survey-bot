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
import ru.tbank.dto.SurveyDTO;
import ru.tbank.entity.Survey;
import ru.tbank.service.SurveyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/v1/survey"})
public class SurveyController {
    @Autowired
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @Operation(summary = "Получение всех опросов группы опросов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping
    public ResponseEntity<List<Survey>> getAllSurveysInGroup(@RequestParam @NotNull Long surveyGroupId, @RequestParam(defaultValue = "0") String withAnswers) {
        List<Survey> surveys;
        if (withAnswers.equals("1")) {
            surveys = surveyService.getAllSurveysInGroupWithAnswers(surveyGroupId);
        } else {
            surveys = surveyService.getAllSurveysInGroup(surveyGroupId);
        }
        return new ResponseEntity<>(surveys, HttpStatus.OK);
    }

    @Operation(summary = "Получение опроса по ид")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping("/{id}")
    public ResponseEntity<Survey> getSurveyById(@PathVariable @NotNull Long id) {
        Survey survey = surveyService.getSurveyById(id);
        return new ResponseEntity<>(survey, HttpStatus.OK);
    }

    @Operation(summary = "Добавление нового опроса в группу опросов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping
    public ResponseEntity<Survey> createSurvey(@RequestBody SurveyDTO surveyDTO, @RequestParam @NotNull Long surveyGroupId) {
        Survey survey = surveyService.createSurvey(surveyDTO, surveyGroupId);
        return new ResponseEntity<>(survey, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновление опроса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PutMapping("/{id}")
    public ResponseEntity<Survey> updateSurvey(@PathVariable @NotNull Long id, @RequestBody SurveyDTO surveyDTO) {
        Survey updatedSurvey = surveyService.updateSurvey(id, surveyDTO);
        return new ResponseEntity<>(updatedSurvey, HttpStatus.OK);
    }

    @Operation(summary = "Удаление опроса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful deleting"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable @NotNull Long id) {
        boolean isDeleted = surveyService.deleteSurvey(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
