package ru.tbank.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.bot.entity.BotPoll;
import ru.tbank.bot.entity.BotSurvey;
import ru.tbank.entity.Answer;
import ru.tbank.entity.Survey;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.entity.SurveyType;
import ru.tbank.service.SurveyGroupService;
import ru.tbank.service.SurveyService;
import ru.tbank.service.SurveyTypeService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotSurveyTest {

    private SurveyTypeService surveyTypeService;
    private SurveyGroupService surveyGroupService;
    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
        surveyTypeService = mock(SurveyTypeService.class);
        surveyGroupService = mock(SurveyGroupService.class);
        surveyService = mock(SurveyService.class);
    }

    @Test
    void testBotSurveyBuilder_NoSurveysInGroup() {
        SurveyGroup surveyGroup = new SurveyGroup();
        surveyGroup.setSurveyGroupId(1L);
        when(surveyGroupService.getSurveyGroupByName("TestSurvey")).thenReturn(surveyGroup);
        when(surveyService.getAllSurveysInGroupWithAnswers(1L)).thenReturn(Collections.emptyList());

        BotSurvey botSurvey = new BotSurvey.BotSurveyBuilder("TestSurvey", surveyTypeService, surveyGroupService, surveyService).build();

        assertNotNull(botSurvey.getBotPolls());
        assertTrue(botSurvey.getBotPolls().isEmpty());
    }

    @Test
    void testBotSurveyBuilder_QuizWithValidAnswers() {
        SurveyGroup surveyGroup = new SurveyGroup();
        surveyGroup.setSurveyGroupId(1L);
        surveyGroup.setSurveyTypeId(2L);
        when(surveyGroupService.getSurveyGroupByName("TestSurvey")).thenReturn(surveyGroup);

        Survey survey = mock(Survey.class);
        when(survey.getSurveyId()).thenReturn(1L);
        when(survey.getSurveyQuestion()).thenReturn("Question?");

        Answer answer1 = new Answer();
        Answer answer2 = new Answer();
        List<Answer> answers = List.of(answer1, answer2);
        when(survey.getAnswers()).thenReturn(answers);

        when(surveyService.getAllSurveysInGroupWithAnswers(1L)).thenReturn(List.of(survey));
        when(surveyTypeService.getSurveyTypeById(2L)).thenReturn(new SurveyType());

        BotSurvey botSurvey = new BotSurvey.BotSurveyBuilder("TestSurvey", surveyTypeService, surveyGroupService, surveyService).build();

        assertNotNull(botSurvey.getBotPolls());
        assertEquals(1, botSurvey.getBotPolls().size());
        BotPoll botPoll = botSurvey.getBotPolls().get(0);
        assertEquals("Question?", botPoll.getQuestion());
        assertEquals(2, botPoll.getOptions().size());
    }

    @Test
    void testBotSurveyBuilder_QuizWithInvalidAnswers() {
        SurveyGroup surveyGroup = new SurveyGroup();
        surveyGroup.setSurveyGroupId(1L);
        surveyGroup.setSurveyTypeId(2L);
        when(surveyGroupService.getSurveyGroupByName("TestSurvey")).thenReturn(surveyGroup);

        Survey survey = mock(Survey.class);
        when(survey.getSurveyId()).thenReturn(1L);
        when(survey.getSurveyQuestion()).thenReturn("Question?");

        Answer answer = new Answer();
        List<Answer> answers = List.of(answer);
        when(survey.getAnswers()).thenReturn(answers);

        when(surveyService.getAllSurveysInGroupWithAnswers(1L)).thenReturn(List.of(survey));
        when(surveyTypeService.getSurveyTypeById(2L)).thenReturn(new SurveyType());

        BotSurvey botSurvey = new BotSurvey.BotSurveyBuilder("TestSurvey", surveyTypeService, surveyGroupService, surveyService).build();

        assertNotNull(botSurvey.getBotPolls());
        assertTrue(botSurvey.getBotPolls().isEmpty());
    }
}
