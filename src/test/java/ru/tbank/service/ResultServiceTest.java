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
import ru.tbank.dto.ResultDTO;
import ru.tbank.entity.Result;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.repository.ResultRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class ResultServiceTest {
    @Mock
    private ResultRepository resultRepository;
    @InjectMocks
    private ResultService resultService;

    private Result result;
    private ResultDTO resultDTO;

    @BeforeEach
    void setUp() {
        result = new Result();
        result.setResultId(1L);
        result.setUserId(1L);
        result.setSurveyId(1L);
        result.setUserResult("[1]");
        result.setInsertDt(LocalDateTime.now());
        resultDTO = new ResultDTO();
        resultDTO.setUserId(1L);
        resultDTO.setSurveyId(1L);
        resultDTO.setUserResult("[1]");
    }

    @Test
    void testGetResult_OK() {
        when(resultRepository.findByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(result);
        Result foundResult = resultService.getResult(1L, 1L);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(foundResult, "Результат не пуст"),
                () -> Assertions.assertEquals(result.getUserResult(), foundResult.getUserResult(), "Результат прохождения опроса юзера тот, что ожидается")
        );
    }

    @Test
    void testGetResult_EntityNotFound() {
        when(resultRepository.findByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> resultService.getResult(1L, 1L), "Результат не найден");
    }

    @Test
    void testGetResultById_OK() {
        when(resultRepository.findByResultId(anyLong())).thenReturn(result);
        Result foundResult = resultService.getResultById(1L);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(foundResult, "Результат не пуст"),
                () -> Assertions.assertEquals(result.getUserResult(), foundResult.getUserResult(), "Результат опроса польователя тот, что ожидается")
        );
    }

    @Test
    void testGetResultById_EntityNotFound() {
        when(resultRepository.findByResultId(anyLong())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> resultService.getResultById(1L), "Реультат не найден");
    }

    @Test
    void testCreateResult_OK() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(false);
        when(resultRepository.save(any(Result.class))).thenReturn(result);
        Result createdResult = resultService.createResult(resultDTO);

        Assertions.assertNotNull(createdResult, "Результат опроса добавлен");
        Assertions.assertEquals(result.getUserResult(), createdResult.getUserResult(), "Добавляемый и добавленный результат совпали");
    }

    @Test
    void testCreateResult_EntityAlreadyExists() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(true);

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> resultService.createResult(resultDTO), "Результат опроса юзера уже существует");
    }

    @Test
    void testUpdateResult_OK() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(true);
        when(resultRepository.findByResultId(anyLong())).thenReturn(result);
        when(resultRepository.save(any(Result.class))).thenReturn(result);
        Result updatedResult = resultService.updateResult(result.getResultId(), resultDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(updatedResult, "Результат не пуст"),
                () -> Assertions.assertEquals(result.getUserResult(), updatedResult.getUserResult(), "После апдейта ожидаемый результат")
        );
    }

    @Test
    void testUpdateResult_EntityNotFound() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> resultService.updateResult(result.getResultId(), resultDTO), "Обновляемый результат не найден");
    }

    @Test
    void testDeleteResult_OK() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(true);
        boolean isDeleted = resultService.deleteResult(result.getResultId());

        Assertions.assertTrue(isDeleted, "Результат удален");
        verify(resultRepository, times(1)).deleteById(result.getResultId());
    }

    @Test
    void testDeleteResult_EntityNotFound() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> resultService.deleteResult(result.getResultId()), "Удаляемый результат не найден");
    }

    @Test
    void testCheckResultExists_OK() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(true);
        boolean exists = resultService.checkResultExists(resultDTO);

        Assertions.assertTrue(exists, "Результат существует");
    }

    @Test
    void testCheckResultExists_NotExists() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(false);
        boolean exists = resultService.checkResultExists(resultDTO);

        Assertions.assertFalse(exists, "Результат не существует");
    }

    @Test
    void testGetResult_BadRequestException() {
        when(resultRepository.findByUserIdAndSurveyId(anyLong(), anyLong())).thenThrow(new BadRequestException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> resultService.getResult(1L, 1L), "Ошибка рабты с базой");
    }

    @Test
    void testGetResultById_BadRequestException() {
        when(resultRepository.findByResultId(anyLong())).thenThrow(new BadRequestException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> resultService.getResultById(1L), "Ошибка рабты с базой");
    }

    @Test
    void testCreateResult_DataIntegrityViolationException() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenReturn(false);
        when(resultRepository.save(any(Result.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> resultService.createResult(resultDTO), "Ошибка рабты с базой");
    }

    @Test
    void testUpdateResult_DataIntegrityViolationException() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(true);
        when(resultRepository.findByResultId(anyLong())).thenReturn(result);
        when(resultRepository.save(any(Result.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> resultService.updateResult(result.getResultId(), resultDTO), "Ошибка рабты с базой");
    }

    @Test
    void testDeleteResult_DataIntegrityViolationException() {
        when(resultRepository.existsByResultId(anyLong())).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Data integrity violation")).when(resultRepository).deleteById(anyLong());

        Assertions.assertThrows(BadRequestException.class, () -> resultService.deleteResult(result.getResultId()), "Ошибка рабты с базой");
    }

    @Test
    void testCheckResultExists_BadRequestException() {
        when(resultRepository.existsByUserIdAndSurveyId(anyLong(), anyLong())).thenThrow(new BadRequestException("Data integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> resultService.checkResultExists(resultDTO), "Ошибка рабты с базой");
    }

    @Test
    void testGetResult_UnknownError() {
        Long userId = 1L;
        Long surveyId = 1L;
        when(resultRepository.findByUserIdAndSurveyId(userId, surveyId)).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            resultService.getResult(userId, surveyId);
        });
        Assertions.assertEquals("Ошибка поиска результата: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testGetResultById_UnknownError() {
        Long resultId = 1L;
        when(resultRepository.findByResultId(resultId)).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> resultService.getResultById(resultId));
        Assertions.assertEquals("Ошибка поиска результата: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testCreateResult_UnknownError() {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setUserId(1L);
        resultDTO.setSurveyId(1L);
        when(resultRepository.existsByUserIdAndSurveyId(resultDTO.getUserId(), resultDTO.getSurveyId())).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> resultService.createResult(resultDTO));
        Assertions.assertEquals("Ошибка добавления нового результат: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testUpdateResult_UnknownError() {
        Long resultId = 1L;
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setUserResult("Новый ответ");
        when(resultRepository.existsByResultId(resultId)).thenReturn(true);
        when(resultRepository.findByResultId(resultId)).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> resultService.updateResult(resultId, resultDTO));
        Assertions.assertEquals("Ошибка обновления результат: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testDeleteResult_UnknownError() {
        Long resultId = 1L;
        when(resultRepository.existsByResultId(resultId)).thenReturn(true);
        doThrow(new RuntimeException("Неизвестная ошибка")).when(resultRepository).deleteById(resultId);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> resultService.deleteResult(resultId));
        Assertions.assertEquals("Ошибка удаления результата: Неизвестная ошибка", thrown.getMessage());
    }

    @Test
    void testCheckResultExists_UnknownError() {
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setUserId(1L);
        resultDTO.setSurveyId(1L);
        when(resultRepository.existsByUserIdAndSurveyId(resultDTO.getUserId(), resultDTO.getSurveyId())).thenThrow(new RuntimeException("Неизвестная ошибка"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> resultService.checkResultExists(resultDTO));
        Assertions.assertEquals("Ошибка проверки результата опроса: Неизвестная ошибка", thrown.getMessage());
    }
}
