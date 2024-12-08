package ru.tbank.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.tbank.entity.Answer;

public class AnswerSpecification {
    public static Specification<Answer> findBySurvey(Long surveyId) {
        return (root, query, criteriaBuilder) -> {
            if (surveyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("survey").get("surveyId"), surveyId);
        };
    }
}
