package ru.tbank.bot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BotSurveySession {
    private Long chatId;
    private Long userId;
    private String surveyName;
    private BotSurvey botSurvey;
    private int currentQuestionIndex;

    public BotSurveySession(long chatId) {
        this.chatId = chatId;
    }

    public void incrementCurrentQuestionIndex() {
        this.currentQuestionIndex++;
    }
}
