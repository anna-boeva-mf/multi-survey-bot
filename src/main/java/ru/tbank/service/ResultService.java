package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.tbank.dto.ResultDTO;
import ru.tbank.entity.Result;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.ResultRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;

@Slf4j
@LogExecutionTime
@Service
public class ResultService {
    @Autowired
    private ResultRepository resultRepository;

    public Result getResult(Long userId, Long surveyId) {
        log.info("Получение результата опроса юзера");
        try {
            Result result = resultRepository.findByUserIdAndSurveyId(userId, surveyId);
            if (result == null) {
                throw new EntityNotFoundException("Опрос не пройден юзером");
            } else {
                return result;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка обработки данных: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка поиска результата: {}", e.getMessage());
            throw new RuntimeException("Ошибка поиска результата: " + e.getMessage());
        }
    }

    public Result getResultById(Long resultId) {
        log.info("Получение результата опроса юзера по ид");
        try {
            Result result = resultRepository.findByResultId(resultId);
            if (result == null) {
                throw new EntityNotFoundException("Опрос не пройден юзером");
            } else {
                return result;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка обработки данных: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка поиска результата: {}", e.getMessage());
            throw new RuntimeException("Ошибка поиска результата: " + e.getMessage());
        }
    }

    public Result createResult(ResultDTO resultDTO) {
        log.info("Добавление результата опроса юзера");
        try {
            if (resultRepository.existsByUserIdAndSurveyId(resultDTO.getUserId(), resultDTO.getSurveyId())) {
                throw new EntityAlreadyExistsException("Результат пользователя по этому вопросу уже существует");
            } else {
                Result result = new Result();
                result.setUserId(resultDTO.getUserId());
                result.setSurveyId(resultDTO.getSurveyId());
                result.setUserResult(resultDTO.getUserResult());
                result.setInsertDt(LocalDateTime.now());
                return resultRepository.save(result);
            }
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления нового результата, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка добавления нового результат: {}", e.getMessage());
            throw new RuntimeException("Ошибка добавления нового результат: " + e.getMessage());
        }
    }

    public Result updateResult(Long resultId, ResultDTO resultDTO) {
        log.info("Обновление результата опроса юзера");
        try {
            if (!resultRepository.existsByResultId(resultId)) {
                throw new EntityNotFoundException("Результат не существует");
            } else {
                Result result = resultRepository.findByResultId(resultId);
                result.setUserResult(resultDTO.getUserResult());
                result.setInsertDt(LocalDateTime.now());
                return resultRepository.save(result);
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновления результат, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обновления результат: {}", e.getMessage());
            throw new RuntimeException("Ошибка обновления результат: " + e.getMessage());
        }
    }

    public boolean deleteResult(Long resultId) {
        log.info("Удаление результата опроса юзера");
        try {
            if (resultRepository.existsByResultId(resultId)) {
                resultRepository.deleteById(resultId);
                return true;
            } else {
                throw new EntityNotFoundException("Результат не найден");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления результата, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления результата: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления результата: " + e.getMessage());
        }
    }

    public boolean checkResultExists(ResultDTO resultDTO) {
        log.info("Проверка что результат опроса уже получен");
        try {
            if (resultRepository.existsByUserIdAndSurveyId(resultDTO.getUserId(), resultDTO.getSurveyId())) {
                log.warn("Результат пользователя по этому вопросу уже существует");
                return true;
            } else {
                return false;
            }
        } catch (BadRequestException e) {
            log.error("Ошибка проверки результата опроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка проверки результата опроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка проверки результата опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка проверки результата опроса: " + e.getMessage());
        }
    }
}
