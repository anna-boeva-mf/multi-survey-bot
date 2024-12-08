package ru.tbank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import ru.tbank.entity.Answer;
import ru.tbank.entity.Survey;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AnswerServiceTest {
    @Mock
    private AnswerRepository answerRepository;
    @InjectMocks
    private AnswerService answerService;

    private Answer answer;
    private Survey survey;

    @BeforeEach
    void setUp() {
        survey = new Survey();
        answer = new Answer();
        answer.setAnswerId(1L);
        answer.setAnswer("Answer1");
        answer.setCorrectFlg(false);
        answer.setInsertDt(LocalDateTime.now());
        answer.setSurvey(survey);
    }

    @Test
    void testGetAllAnswersInSurvey_OK() {
        Long surveyId = 1L;
        when(answerRepository.findAll(any(Specification.class))).thenReturn(List.of(answer));
        List<Answer> answers = answerService.getAllAnswersInSurvey(surveyId);

        Assertions.assertAll(
                () -> Assertions.assertFalse(answers.isEmpty(), "Список ответов не пуст"),
                () -> Assertions.assertEquals(1, answers.size(), "Добавлен только 1 ответ"),
                () -> Assertions.assertEquals(answer.getAnswer(), answers.get(0).getAnswer(), "Добавляемый ответ совпадает с добавленным")
        );
    }

    @Test
    void testGetAllAnswersInSurvey_Empty() {
        Long surveyId = 1L;
        when(answerRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        Assertions.assertThrows(EntityNotFoundException.class, () -> answerService.getAllAnswersInSurvey(surveyId), "Выдает ошибку, что ответов нет");
    }

    @Test
    void testGetAnswerById_OK() {
        when(answerRepository.findByAnswerId(anyLong())).thenReturn(answer);
        Answer answerResult = answerService.getAnswerById(answer.getAnswerId());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(answerResult, "Ответ получен"),
                () -> Assertions.assertEquals(answer.getAnswerId(), answerResult.getAnswerId(), "Ид искомого ответа совпадает с ид найденного")
        );
    }

    @Test
    void testGetAnswerById_NotFound() {
        when(answerRepository.findByAnswerId(anyLong())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> answerService.getAnswerById(answer.getAnswerId()), "Ответ с таким ид не существует");
    }

    @Test
    void testCreateAnswer_OK() {
        when(answerRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(answerRepository.save(any(Answer.class))).thenReturn(answer);
        Answer answerResult = answerService.createAnswer(answer);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(answerResult, "Ответ добавлен"),
                () -> Assertions.assertEquals(answer.getAnswer(), answerResult.getAnswer(), "Добавляемые и добавленный совпадают")
        );
    }

    @Test
    void testCreateAnswer_AlreadyExists() {
        when(answerRepository.findAll(any(Specification.class))).thenReturn(List.of(answer));

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> answerService.createAnswer(answer), "Ответ уже существует");
    }

    @Test
    void testUpdateAnswer_OK() {
        when(answerRepository.existsByAnswerId(anyLong())).thenReturn(true);
        when(answerRepository.save(any(Answer.class))).thenReturn(answer);
        Answer answerResult = answerService.updateAnswer(answer.getAnswerId(), answer);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(answerResult, "Ответ существует"),
                () -> Assertions.assertEquals(answer.getAnswerId(), answerResult.getAnswerId(), "Ответ был изменен")
        );
    }

    @Test
    void testUpdateAnswer_NotFound() {
        when(answerRepository.existsByAnswerId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> answerService.updateAnswer(answer.getAnswerId(), answer), "Ответ для изменения не существует");
    }

    @Test
    void testDeleteAnswer_OK() {
        when(answerRepository.existsByAnswerId(anyLong())).thenReturn(true);
        boolean result = answerService.deleteAnswer(answer.getAnswerId());

        Assertions.assertTrue(result, "Ответ был удален");
        verify(answerRepository, times(1)).deleteById(answer.getAnswerId());
    }

    @Test
    void testDeleteAnswer_NotFound() {
        when(answerRepository.existsByAnswerId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> answerService.deleteAnswer(answer.getAnswerId()), "Удаляемый ответ не найден");
    }

    @Test
    void testCreateAnswer_DataIntegrityViolationException() {
        when(answerRepository.save(any(Answer.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> answerService.createAnswer(answer), "Ошибка рабты с базой");
    }

    @Test
    void testDeleteAnswer_DataIntegrityViolation() {
        Long answerId = 1L;
        when(answerRepository.existsByAnswerId(answerId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Ошибка удаления")).when(answerRepository).deleteById(answerId);

        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            answerService.deleteAnswer(answerId);
        });
        Assertions.assertEquals("Ошибка в данных запроса: Ошибка удаления", thrown.getMessage());
    }

    @Test
    void testDeleteAnswer_UnexpectedException() {
        Long answerId = 1L;
        when(answerRepository.existsByAnswerId(answerId)).thenReturn(true);
        doThrow(new RuntimeException("Неизвестная ошибка")).when(answerRepository).deleteById(answerId);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            answerService.deleteAnswer(answerId);
        });
        Assertions.assertEquals("Ошибка удаления ответа: Неизвестная ошибка", thrown.getMessage());
    }
}
