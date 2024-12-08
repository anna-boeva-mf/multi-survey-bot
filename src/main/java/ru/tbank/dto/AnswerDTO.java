package ru.tbank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerDTO {
    private String answer;
    private Boolean correctFlg;
    private Long surveyId;
    private LocalDateTime insertDt;
}
