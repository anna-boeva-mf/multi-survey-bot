package ru.tbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.entity.SurveyType;
import ru.tbank.service.SurveyTypeService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/survey-type"})
public class SurveyTypeController {
    @Autowired
    private final SurveyTypeService surveyTypeService;

    public SurveyTypeController(SurveyTypeService surveyTypeService) {
        this.surveyTypeService = surveyTypeService;
    }

    @Operation(summary = "Получение всех типов опросов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "500", description = "Application error")})
    @GetMapping
    public ResponseEntity<List<SurveyType>> getAllSurveyTypes() {
        List<SurveyType> surveyTypes = surveyTypeService.getAllSurveyTypes();
        return new ResponseEntity<>(surveyTypes, HttpStatus.OK);
    }
}
