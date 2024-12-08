package ru.tbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tbank.entity.Survey;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    @Query("SELECT l FROM Survey l LEFT JOIN FETCH l.answers WHERE l.surveyId = :id")
    Survey findByIdWithAnswers(@Param("id") Long id);

    Survey findBySurveyId(Long surveyId);

    List<Survey> findBySurveyGroupId(Long surveyGroupId);

    boolean existsBySurveyId(Long surveyId);
}
