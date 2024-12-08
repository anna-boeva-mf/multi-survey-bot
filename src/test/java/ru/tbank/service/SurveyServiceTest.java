package ru.tbank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;

import org.springframework.dao.DataIntegrityViolationException;
import ru.tbank.dto.SurveyDTO;
import ru.tbank.entity.Survey;
import ru.tbank.repository.SurveyGroupRepository;
import ru.tbank.repository.SurveyRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SurveyServiceTest {
    @Mock
    private SurveyRepository surveyRepository;
    @Mock
    private SurveyGroupRepository surveyGroupRepository;
    @InjectMocks
    private SurveyService surveyService;

    private Survey survey;
    private SurveyDTO surveyDTO;

    @BeforeEach
    void setUp() {
        survey = new Survey();
        survey.setSurveyId(1L);
        survey.setSurveyQuestion("Вопрос?");
        survey.setSurveyTypeId(1L);
        survey.setSurveyGroupId(1L);
        survey.setInsertDt(LocalDateTime.now());
        surveyDTO = new SurveyDTO();
        surveyDTO.setSurveyQuestion("Вопрос?");
        surveyDTO.setSurveyTypeId(1L);
    }

    @Test
    void testGetAllSurveysInGroup_OK() {
        Long surveyGroupId = 1L;
        when(surveyRepository.findBySurveyGroupId(surveyGroupId)).thenReturn(List.of(survey));
        List<Survey> surveys = surveyService.getAllSurveysInGroup(surveyGroupId);

        Assertions.assertAll(
                () -> Assertions.assertFalse(surveys.isEmpty(), "Список опросов не пуст"),
                () -> Assertions.assertEquals(1, surveys.size(), "Только 1 опрос, добавленный"),
                () -> Assertions.assertEquals(survey.getSurveyQuestion(), surveys.get(0).getSurveyQuestion(), "Добавляемый вопрос опроса и добавленный совпадают")
        );
    }

    @Test
    void testGetAllSurveysInGroup_Empty() {
        Long surveyGroupId = 1L;
        when(surveyRepository.findBySurveyGroupId(surveyGroupId)).thenReturn(Collections.emptyList());

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.getAllSurveysInGroup(surveyGroupId), "Список опросов пуст");
    }

    @Test
    void testGetAllSurveysInGroupWithAnswers_OK() {
        Long surveyGroupId = 1L;
        when(surveyRepository.findBySurveyGroupId(surveyGroupId)).thenReturn(List.of(survey));
        List<Survey> surveyResult = surveyService.getAllSurveysInGroupWithAnswers(surveyGroupId);

        Assertions.assertAll(
                () -> Assertions.assertFalse(surveyResult.isEmpty(), "Список опросов не пуст"),
                () -> Assertions.assertEquals(survey.getSurveyQuestion(), surveyResult.get(0).getSurveyQuestion(), "Добавляемый вопрос опроса и добавленный совпадают")
        );
    }

    @Test
    void testGetAllSurveysInGroupWithAnswers_Empty() {
        Long surveyGroupId = 1L;
        when(surveyRepository.findBySurveyGroupId(surveyGroupId)).thenReturn(Collections.emptyList());

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.getAllSurveysInGroupWithAnswers(surveyGroupId), "Список опросов пуст");
    }

    @Test
    void testGetSurveyById_OK() {
        when(surveyRepository.findBySurveyId(anyLong())).thenReturn(survey);
        Survey surveyResult = surveyService.getSurveyById(survey.getSurveyId());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyResult, "Был получен опрос по запросу"),
                () -> Assertions.assertEquals(survey.getSurveyId(), surveyResult.getSurveyId(), "Ид опроса один и тот же")
        );
    }

    @Test
    void testGetSurveyById_NotFound() {
        when(surveyRepository.findBySurveyId(anyLong())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.getSurveyById(survey.getSurveyId()), "Опрос не найден");
    }

    @Test
    void testCreateSurvey_OK() {
        Long surveyGroupId = 1L;
        when(surveyGroupRepository.existsBySurveyGroupId(surveyGroupId)).thenReturn(true);
        when(surveyRepository.findBySurveyGroupId(surveyGroupId)).thenReturn(Collections.emptyList());
        when(surveyRepository.save(any(Survey.class))).thenReturn(survey);
        Survey surveyResult = surveyService.createSurvey(surveyDTO, surveyGroupId);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyResult, "Опрос был создан"),
                () -> Assertions.assertEquals(survey.getSurveyQuestion(), surveyResult.getSurveyQuestion(), "Вопрос создаваемого опроса и созданного совпадают")
        );
    }

    @Test
    void testCreateSurvey_EmptyQuestion() {
        surveyDTO.setSurveyQuestion("");

        Assertions.assertThrows(IllegalArgumentException.class, () -> surveyService.createSurvey(surveyDTO, 1L), "Не получается создать опрос с пустым вопросом");
    }

    @Test
    void testCreateSurvey_GroupNotFound() {
        when(surveyGroupRepository.existsBySurveyGroupId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.createSurvey(surveyDTO, 1L), "Группа опросов не найдена");
    }

    @Test
    void testUpdateSurvey_OK() {
        Long surveyId = 1L;
        when(surveyRepository.existsById(surveyId)).thenReturn(true);
        when(surveyRepository.findBySurveyId(surveyId)).thenReturn(survey);
        when(surveyRepository.save(any(Survey.class))).thenReturn(survey);
        Survey surveyResult = surveyService.updateSurvey(surveyId, surveyDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyResult, "Результат апдейта не пуст"),
                () -> Assertions.assertEquals(survey.getSurveyQuestion(), surveyResult.getSurveyQuestion(), "Измененный и тот, на что меняли, опросы совпадают")
        );
    }

    @Test
    void testUpdateSurvey_NotFound() {
        when(surveyRepository.existsById(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.updateSurvey(1L, surveyDTO), "Изменяемый опрос не найден");
    }

    @Test
    void testDeleteSurvey_OK() {
        Long surveyId = 1L;
        when(surveyRepository.existsBySurveyId(surveyId)).thenReturn(true);
        boolean result = surveyService.deleteSurvey(surveyId);

        Assertions.assertTrue(result, "Опрос удален");
        verify(surveyRepository, times(1)).deleteById(surveyId);
    }

    @Test
    void testDeleteSurvey_NotFound() {
        when(surveyRepository.existsBySurveyId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyService.deleteSurvey(1L), "Опрос для удаления не существует");
    }

    @Test
    void testCreateSurvey_DataIntegrityViolationException() {
        Long surveyGroupId = 1L;
        when(surveyGroupRepository.existsBySurveyGroupId(surveyGroupId)).thenReturn(true);
        when(surveyRepository.save(any(Survey.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> surveyService.createSurvey(surveyDTO, 1L), "Ошибка рабты с базой");
    }

    @Test
    void testUpdateSurvey_EmptySurveyQuestion() {
        Long surveyId = 1L;
        SurveyDTO surveyDTO = new SurveyDTO();
        surveyDTO.setSurveyQuestion("");
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> surveyService.updateSurvey(surveyId, surveyDTO));
        Assertions.assertEquals("Вопрос для обновления должен быть не пуст", thrown.getMessage(), "Не получается проапдейтить опросом с пустым вопросом");
    }

    @Test
    void testUpdateSurvey_DataIntegrityViolation() {
        Long surveyId = 1L;
        SurveyDTO surveyDTO = new SurveyDTO();
        surveyDTO.setSurveyQuestion("Какой?");
        Survey existingSurvey = new Survey();
        existingSurvey.setSurveyId(surveyId);
        when(surveyRepository.existsById(surveyId)).thenReturn(true);
        when(surveyRepository.findBySurveyId(surveyId)).thenReturn(existingSurvey);
        when(surveyRepository.save(any(Survey.class))).thenThrow(new DataIntegrityViolationException("Ошибка вставки"));

        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> surveyService.updateSurvey(surveyId, surveyDTO));
        Assertions.assertEquals("Ошибка в данных запроса: Ошибка вставки", thrown.getMessage());
    }

    @Test
    void testUpdateSurvey_UnexpectedException() {
        Long surveyId = 1L;
        SurveyDTO surveyDTO = new SurveyDTO();
        surveyDTO.setSurveyQuestion("Какой?");
        Survey existingSurvey = new Survey();
        existingSurvey.setSurveyId(surveyId);
        when(surveyRepository.existsById(surveyId)).thenReturn(true);
        when(surveyRepository.findBySurveyId(surveyId)).thenReturn(existingSurvey);
        when(surveyRepository.save(any(Survey.class))).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> surveyService.updateSurvey(surveyId, surveyDTO));
        Assertions.assertEquals("Ошибка обновления опроса: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testDeleteSurvey_DataIntegrityViolation() {
        Long surveyId = 1L;
        when(surveyRepository.existsBySurveyId(surveyId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Ошибка удаления")).when(surveyRepository).deleteById(surveyId);

        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> surveyService.deleteSurvey(surveyId));
        Assertions.assertEquals("Ошибка в данных запроса: Ошибка удаления", thrown.getMessage());
    }

    @Test
    void testDeleteSurvey_UnexpectedException() {
        Long surveyId = 1L;
        when(surveyRepository.existsBySurveyId(surveyId)).thenReturn(true);
        doThrow(new RuntimeException("Неизвестная ошибка")).when(surveyRepository).deleteById(surveyId);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> surveyService.deleteSurvey(surveyId));
        Assertions.assertEquals("Ошибка удаления опроса: Неизвестная ошибка", thrown.getMessage());
    }
}
