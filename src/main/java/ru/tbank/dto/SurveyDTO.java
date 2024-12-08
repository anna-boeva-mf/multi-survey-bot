package ru.tbank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurveyDTO {
    private String surveyQuestion;
    private Long surveyTypeId;
}
