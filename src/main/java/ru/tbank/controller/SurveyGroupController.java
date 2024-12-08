package ru.tbank.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.dto.SurveyGroupDTO;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.service.SurveyGroupService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/survey-group"})
public class SurveyGroupController {
    @Autowired
    private final SurveyGroupService surveyGroupService;

    public SurveyGroupController(SurveyGroupService surveyGroupService) {
        this.surveyGroupService = surveyGroupService;
    }

    @Operation(summary = "Получение всех групп опросов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping
    public ResponseEntity<List<SurveyGroup>> getAllSurveyGroups() {
        List<SurveyGroup> surveyGroups = surveyGroupService.getAllSurveyGroups();
        return new ResponseEntity<>(surveyGroups, HttpStatus.OK);
    }

    @Operation(summary = "Получение группы опросов по названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping({"/{name}"})
    public ResponseEntity<SurveyGroup> getSurveyGroupById(@PathVariable @NotBlank String name) {
        SurveyGroup surveyGroup = surveyGroupService.getSurveyGroupByName(name);
        return new ResponseEntity<>(surveyGroup, HttpStatus.OK);
    }

    @Operation(summary = "Добавление новой группы опросов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PostMapping
    public ResponseEntity<SurveyGroup> createSurveyGroup(@RequestBody SurveyGroupDTO surveyGroupDTO) {
        SurveyGroup surveyGroup = surveyGroupService.createSurveyGroup(surveyGroupDTO);
        return new ResponseEntity<>(surveyGroup, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновление опроса по названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful updating"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @PutMapping("/{name}")
    public ResponseEntity<SurveyGroup> updateSurveyGroup(@PathVariable @NotBlank String name, @RequestBody SurveyGroupDTO surveyGroupDTO) {
        SurveyGroup updatedSurveyGroup = surveyGroupService.updateSurveyGroup(name, surveyGroupDTO);
        return new ResponseEntity<>(updatedSurveyGroup, HttpStatus.OK);
    }

    @Operation(summary = "Удаление группы опросов по названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful deleting"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteSurveyGroup(@PathVariable @NotBlank String name) {
        boolean isDeleted = surveyGroupService.deleteSurveyGroup(name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
