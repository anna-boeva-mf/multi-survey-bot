package ru.tbank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDTO {
    private Long userId;
    private Long surveyId;
    private String userResult;

    public ResultDTO(Long userId, Long surveyId, String userResult) {
        this.userId = userId;
        this.surveyId = surveyId;
        this.userResult = userResult;
    }

    public ResultDTO() {
    }
}
