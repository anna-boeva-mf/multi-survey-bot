package ru.tbank.bot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.entity.Answer;
import ru.tbank.entity.Survey;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.service.SurveyGroupService;
import ru.tbank.service.SurveyService;
import ru.tbank.service.SurveyTypeService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Data
public class BotSurvey {
    private String surveyName;
    private List<BotPoll> botPolls;

    public BotSurvey(BotSurveyBuilder botSurveyBuilder) {
        this.surveyName = botSurveyBuilder.surveyName;
        this.botPolls = botSurveyBuilder.botPolls;
    }

    public static class BotSurveyBuilder {
        private String surveyName;
        private List<BotPoll> botPolls;

        public BotSurveyBuilder(String surveyName, SurveyTypeService surveyTypeService, SurveyGroupService surveyGroupService, SurveyService surveyService) {
            this.surveyName = surveyName;
            SurveyGroup surveyGroup = surveyGroupService.getSurveyGroupByName(surveyName);
            if (surveyGroup == null) {
                this.botPolls = null;
                return;
            }
            List<Survey> surveys = surveyService.getAllSurveysInGroupWithAnswers(surveyGroup.getSurveyGroupId());
            if (surveys.isEmpty()) {
                this.botPolls = new ArrayList<>();
                return;
            }
            Long surveyTypeId = surveyGroup.getSurveyTypeId();
            boolean isMultipleChoiceFlg = surveyTypeService.getSurveyTypeById(surveyTypeId).isMultipleChoiceFlg();
            boolean isQuizFlg = surveyTypeService.getSurveyTypeById(surveyTypeId).isQuizFlg();
            log.info("Опрос {}, квиз: {}, множественный выбор: {}", surveyName, isQuizFlg, isMultipleChoiceFlg);
            List<BotPoll> botPolls = new ArrayList<>();
            if (isQuizFlg) {
                for (Survey survey : surveys) {
                    List<Answer> answers = survey.getAnswers();
                    if (answers.size() <= 1 || answers.size() > 10) {
                        log.warn("У вопроса неподходящее для телеграма количество ответов");
                        continue;
                    }
                    List<Answer> options = new ArrayList<>();
                    int correctAnswer = 0;
                    for (int i = 0; i < answers.size(); i++) {
                        Answer answer = answers.get(i);
                        options.add(answer);
                        if (answer.getCorrectFlg() == true) {
                            correctAnswer = i;
                        }
                    }
                    BotPoll botPoll = new BotPoll(survey.getSurveyId(), survey.getSurveyQuestion(), options, isMultipleChoiceFlg, isQuizFlg, correctAnswer);
                    botPolls.add(botPoll);
                }
            } else {
                for (Survey survey : surveys) {
                    List<Answer> answers = survey.getAnswers();
                    List<Answer> options = new ArrayList<>();
                    if (answers.size() <= 1 || answers.size() > 10) {
                        log.warn("У вопроса неподходящее для телеграма количество ответов");
                        continue;
                    }
                    for (Answer answer : answers) {
                        options.add(answer);
                    }
                    BotPoll botPoll = new BotPoll(survey.getSurveyId(), survey.getSurveyQuestion(), options, isMultipleChoiceFlg, isQuizFlg);
                    botPolls.add(botPoll);
                }
            }
            this.botPolls = botPolls;
            log.info("Подготовлен опрос " + surveyName);
        }

        public BotSurvey build() {
            return new BotSurvey(this);
        }
    }
}
