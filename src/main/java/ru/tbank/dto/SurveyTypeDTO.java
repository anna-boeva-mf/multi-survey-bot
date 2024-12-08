package ru.tbank.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyTypeDTO {
    private String surveyTypeName;
    private boolean anonymousFlg;
    private boolean multipleChoiceFlg;
    private boolean quizFlg;
}
