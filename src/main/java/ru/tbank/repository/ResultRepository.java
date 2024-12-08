package ru.tbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.entity.Result;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    Result findByUserIdAndSurveyId(Long userId, Long surveyId);

    boolean existsByUserIdAndSurveyId(Long userId, Long surveyId);

    boolean existsByResultId(Long resultId);

    Result findByResultId(Long resultId);
}
