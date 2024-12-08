package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tbank.entity.SurveyType;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.SurveyTypeRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.util.List;

@Slf4j
@LogExecutionTime
@Service
public class SurveyTypeService {
    @Autowired
    private SurveyTypeRepository surveyTypeRepository;

    public List<SurveyType> getAllSurveyTypes() {
        log.info("Получение всех типов опросов");
        try {
            List<SurveyType> surveyTypes = surveyTypeRepository.findAll();
            if (surveyTypes.isEmpty()) {
                throw new EntityNotFoundException("Список типов опросов пуст");
            } else {
                return surveyTypes;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения всех типов опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех типов опроса: " + e.getMessage());
        }
    }

    public SurveyType getSurveyTypeById(Long surveyTypeId) {
        log.info("Получение типа опроса");
        try {
            SurveyType surveyType = surveyTypeRepository.findBySurveyTypeId(surveyTypeId);
            if (surveyType == null) {
                throw new EntityNotFoundException("Такого типа опроса не существует");
            } else {
                return surveyType;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения типа опроса, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения типа опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех типов опроса: " + e.getMessage());
        }
    }

    public SurveyType getSurveyTypeByConfig(boolean multipleChoiceFlg, boolean quizFlg) {
        log.info("Получение типа опроса");
        try {
            SurveyType surveyType = surveyTypeRepository.findByMultipleChoiceFlgAndQuizFlg(multipleChoiceFlg, quizFlg);
            if (surveyType == null) {
                log.warn("Тип опроса с такими настройкми отсутствует");
                throw new EntityNotFoundException("Тип опроса с такими настройкми отсутствует");
            } else {
                return surveyType;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения типа опроса, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения типа опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех типов опроса: " + e.getMessage());
        }
    }
}
