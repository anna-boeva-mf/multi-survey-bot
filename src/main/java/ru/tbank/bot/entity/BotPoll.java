package ru.tbank.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.tbank.entity.Answer;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
public class BotPoll {
    private Long surveyId;
    private String question;
    private List<Answer> options;
    private boolean multipleChoiceFlg;
    private boolean quizFlg;
    private int correctAnswer;

    public BotPoll(Long surveyId, String question, List<Answer> options, boolean multipleChoiceFlg, boolean quizFlg) {
        this.surveyId = surveyId;
        this.question = question;
        this.options = options;
        this.multipleChoiceFlg = multipleChoiceFlg;
        this.quizFlg = quizFlg;
    }
}
