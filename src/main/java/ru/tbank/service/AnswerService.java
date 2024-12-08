package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tbank.entity.Answer;
import ru.tbank.repository.AnswerSpecification;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.repository.AnswerRepository;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@LogExecutionTime
@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    public List<Answer> getAllAnswersInSurvey(Long surveyId) {
        log.info("Получение всех ответов опроса");
        try {
            Specification<Answer> spec = Specification.where(AnswerSpecification.findBySurvey(surveyId));
            List<Answer> answers = answerRepository.findAll(spec);
            if (answers.isEmpty()) {
                throw new EntityNotFoundException("Список ответов пуст");
            } else {
                List<Answer> answerOnly = answers.stream().map(answer -> new Answer(answer, false)).collect(Collectors.toList());
                return answerOnly;
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения всех ответов опроса, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения всех ответов опроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения всех ответов опроса: " + e.getMessage());
        }
    }

    public Answer getAnswerById(Long id) {
        log.info("Получение ответа по ид");
        try {
            Answer answer = this.answerRepository.findByAnswerId(id);
            if (answer != null) {
                return answer;
            } else {
                throw new EntityNotFoundException("Ответ с таким ид не найден");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            log.error("Ошибка получения ответа по ид, ошибка в данных запроса: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка получения ответа по ид: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения ответа по ид: " + e.getMessage());
        }
    }

    public Answer createAnswer(Answer answer) {
        log.info("Добавление нового ответа в опрос");
        if (!StringUtils.hasText(answer.getAnswer())) {
            throw new IllegalArgumentException("Ответ должен быть не пуст");
        }
        try {
            Specification<Answer> spec = Specification.where(AnswerSpecification.findBySurvey(answer.getSurvey().getSurveyId()));
            List<Answer> answers = answerRepository.findAll(spec);
            if (answers.stream().anyMatch(answerEx -> answerEx.getAnswer().equalsIgnoreCase(answer.getAnswer()))) {
                log.warn("Ответ уже содержится в опросе");
                throw new EntityAlreadyExistsException("Ответ уже содержится в опросе");
            } else {
                answer.setInsertDt(LocalDateTime.now());
                return answerRepository.save(answer);
            }
        } catch (EntityAlreadyExistsException e) {
            log.error(e.getMessage());
            throw new EntityAlreadyExistsException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка добавления нового ответа в опрос, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка добавления нового ответа в опрос: {}", e.getMessage());
            throw new RuntimeException("Ошибка добавления нового ответа в опрос: " + e.getMessage());
        }
    }

    public Answer updateAnswer(Long answerId, Answer answer) {
        log.info("Изменение ответа в опросе");
        try {
            if (!answerRepository.existsByAnswerId(answerId)) {
                log.warn("Ответ с таким ид не существует");
                throw new EntityNotFoundException("Ответ с таким ид не существует");
            } else {
                answer.setAnswerId(answerId);
                answer.setInsertDt(LocalDateTime.now());
                return answerRepository.save(answer);
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка обновления ответа в опрос, ошибка вставки данных в таблицу: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обновления ответа в опрос: {}", e.getMessage());
            throw new RuntimeException("Ошибка обновления ответа в опрос: " + e.getMessage());
        }
    }

    public boolean deleteAnswer(Long answerId) {
        log.info("Удаление ответа");
        try {
            if (answerRepository.existsByAnswerId(answerId)) {
                answerRepository.deleteById(answerId);
                return true;
            } else {
                throw new EntityNotFoundException("Ответ не найден");
            }
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new EntityNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка удаления ответа, ошибка работы с данными: {}", e.getMessage());
            throw new BadRequestException("Ошибка в данных запроса: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка удаления ответа: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления ответа: " + e.getMessage());
        }
    }
}
