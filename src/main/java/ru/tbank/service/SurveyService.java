package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tbank.dto.SurveyDTO;
import ru.tbank.entity.Survey;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.SurveyGroupRepository;
import ru.tbank.repository.SurveyRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@LogExecutionTime
@Service
public class SurveyService {
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private SurveyGroupRepository surveyGroupRepository;

    public List<Survey> getAllSurveysInGroup(Long surveyGroupId) {
        log.info("Получение всех опросов группы");
        try {
            List<Survey> surveys = surveyRepository.findBySurveyGroupId(surveyGroupId);
            if (surveys.isEmpty()) {
                throw new EntityNotFoundException("Список опросов пуст, опросов нет");
            } else {
                List<Survey> surveysOnly = surveys.stream().map(survey -> new Survey(survey, false)).collect(Collectors.toList());
                return surveysOnly;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения всех опросов, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения всех опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех опросов: " + e.getMessage());
        }
    }

    public List<Survey> getAllSurveysInGroupWithAnswers(Long surveyGroupId) {
        log.info("Получение всех опросов группы вместе с ответами");
        try {
            List<Survey> surveys = surveyRepository.findBySurveyGroupId(surveyGroupId);
            if (surveys.isEmpty()) {
                throw new EntityNotFoundException("Список опросов пуст");
            } else {
                List<Survey> surveysOnly = surveys.stream().map(survey -> new Survey(survey, true)).collect(Collectors.toList());
                return surveysOnly;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения опросов, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения опросов: " + e.getMessage());
        }
    }

    public Survey getSurveyById(Long id) {
        log.info("Получение опроса по ид");
        try {
            Survey survey = this.surveyRepository.findBySurveyId(id);
            if (survey != null) {
                return survey;
            } else {
                throw new EntityNotFoundException("Опрос с таким и не найден");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка получения опроса по ид, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения опроса по ид: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения опроса по ид: " + e.getMessage());
        }
    }

    public Survey createSurvey(SurveyDTO surveyDTO, Long surveyGroupId) {
        log.info("Добавление нового опроса в группу");
        if (!StringUtils.hasText(surveyDTO.getSurveyQuestion())) {
            log.error("Пустое значение вопроса");
            throw new IllegalArgumentException("Вопрос должен быть не пуст");
        }
        try {
            if (!surveyGroupRepository.existsBySurveyGroupId(surveyGroupId)) {
                log.warn("Группа опросов с таким ид не существует");
                throw new EntityNotFoundException("Группа опросов с таким ид не существует");
            } else {
                List<Survey> surveys = surveyRepository.findBySurveyGroupId(surveyGroupId);
                if (surveys.stream().anyMatch(survey -> survey.getSurveyQuestion().equalsIgnoreCase(surveyDTO.getSurveyQuestion()))) {
                    log.warn("Опрос уже содержится в группе опросов");
                    throw new EntityAlreadyExistsException("Опрос уже содержится в группе опросов");
                } else {
                    Survey survey = new Survey();
                    survey.setSurveyQuestion(surveyDTO.getSurveyQuestion());
                    survey.setSurveyTypeId(surveyDTO.getSurveyTypeId());
                    survey.setSurveyGroupId(surveyGroupId);
                    survey.setInsertDt(LocalDateTime.now());
                    return surveyRepository.save(survey);
                }
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления нового опроса в группу, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка добавления нового опроса в группу: {}", e.getMessage());
            throw new RuntimeException("Ошибка добавления нового опроса в группу: " + e.getMessage());
        }
    }

    public Survey updateSurvey(Long surveyId, SurveyDTO surveyDTO) {
        log.info("Обновление опроса");
        if (!StringUtils.hasText(surveyDTO.getSurveyQuestion())) {
            throw new IllegalArgumentException("Вопрос для обновления должен быть не пуст");
        }
        try {
            if (!surveyRepository.existsById(surveyId)) {
                throw new EntityNotFoundException("Опрос не найден");
            } else {
                Survey survey = surveyRepository.findBySurveyId(surveyId);
                survey.setSurveyQuestion(surveyDTO.getSurveyQuestion());
                survey.setSurveyTypeId(surveyDTO.getSurveyTypeId());
                survey.setInsertDt(LocalDateTime.now());
                return surveyRepository.save(survey);
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновления опроса, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обновления опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка обновления опроса: " + e.getMessage());
        }
    }

    public boolean deleteSurvey(Long surveyId) {
        log.info("Удаление опроса");
        try {
            if (surveyRepository.existsBySurveyId(surveyId)) {
                surveyRepository.deleteById(surveyId);
                return true;
            } else {
                throw new EntityNotFoundException("Опрос не найден");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления опроса, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления опроса: " + e.getMessage());
        }
    }
}
