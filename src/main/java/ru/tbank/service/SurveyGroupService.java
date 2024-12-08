package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tbank.dto.SurveyGroupDTO;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.SurveyGroupRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@LogExecutionTime
@Service
public class SurveyGroupService {
    @Autowired
    private SurveyGroupRepository surveyGroupRepository;

    public List<SurveyGroup> getAllSurveyGroups() {
        log.info("Получение всех групп опросов");
        try {
            List<SurveyGroup> surveyGroups = surveyGroupRepository.findAll();
            if (surveyGroups.isEmpty()) {
                throw new EntityNotFoundException("Список групп опросов пуст");
            } else {
                return surveyGroups;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения всех групп опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех групп опросов: " + e.getMessage());
        }
    }

    public SurveyGroup getSurveyGroupByName(String name) {
        log.info("Получение группы опросов по названию");
        try {
            SurveyGroup surveyGroup = this.surveyGroupRepository.findBySurveyGroupName(name.toLowerCase());
            if (surveyGroup != null) {
                return surveyGroup;
            } else {
                throw new EntityNotFoundException("Группа опросов с таким названием не найдена");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения группы опросов по названию, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения группы опросов по названию: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения группы опросов по названию: " + e.getMessage());
        }
    }

    public SurveyGroup createSurveyGroup(SurveyGroupDTO surveyGroupDTO) {
        log.info("Добавление новой группы опросов");
        if (!StringUtils.hasText(surveyGroupDTO.getSurveyGroupName())) {
            log.error("Пустое название группы запросов");
            throw new IllegalArgumentException("Название должно быть не пусто");
        }
        try {
            if (surveyGroupRepository.existsBySurveyGroupName(surveyGroupDTO.getSurveyGroupName().toLowerCase())) {
                throw new EntityAlreadyExistsException("Группа опросов с таким названием уже существует");
            } else {
                SurveyGroup surveyGroup = new SurveyGroup();
                surveyGroup.setSurveyGroupName(surveyGroupDTO.getSurveyGroupName().toLowerCase());
                surveyGroup.setSurveyTypeId(surveyGroupDTO.getSurveyTypeId());
                surveyGroup.setInsertDt(LocalDateTime.now());
                return surveyGroupRepository.save(surveyGroup);
            }
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления новой группы опросов, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка добавления новой группы опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка добавления новой группы опросов: " + e.getMessage());
        }
    }

    public SurveyGroup updateSurveyGroup(String name, SurveyGroupDTO surveyGroupDTO) {
        log.info("Обновление группы опросов");
        try {
            if (!surveyGroupRepository.existsBySurveyGroupName(name.toLowerCase())) {
                throw new EntityNotFoundException("Группа опросов с таким названием не существует");
            } else {
                if (!name.equalsIgnoreCase(surveyGroupDTO.getSurveyGroupName()) && surveyGroupRepository.existsBySurveyGroupName(surveyGroupDTO.getSurveyGroupName().toLowerCase())) {
                    throw new EntityAlreadyExistsException("Группа опросов с таким новым названием уже существует");
                } else {
                    SurveyGroup surveyGroup = surveyGroupRepository.findBySurveyGroupName(name.toLowerCase());
                    surveyGroup.setSurveyGroupName(surveyGroupDTO.getSurveyGroupName().toLowerCase());
                    surveyGroup.setSurveyTypeId(surveyGroupDTO.getSurveyTypeId());
                    surveyGroup.setInsertDt(LocalDateTime.now());
                    return surveyGroupRepository.save(surveyGroup);
                }
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновления группы опросов, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обновления группы опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка обновления группы опросов: " + e.getMessage());
        }
    }

    public boolean deleteSurveyGroup(String surveyGroupName) {
        log.info("Удаление группы опросов по названию");
        try {
            if (surveyGroupRepository.existsBySurveyGroupName(surveyGroupName.toLowerCase())) {
                surveyGroupRepository.deleteById(surveyGroupRepository.findBySurveyGroupName(surveyGroupName).getSurveyGroupId());
                return true;
            } else {
                throw new EntityNotFoundException("Группа опросов с таким названием не существует");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления группы опросов, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления группы опросов: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления группы опросов: " + e.getMessage());
        }
    }
}
