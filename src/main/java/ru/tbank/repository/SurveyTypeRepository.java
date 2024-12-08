package ru.tbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.entity.SurveyType;

@Repository
public interface SurveyTypeRepository extends JpaRepository<SurveyType, Long> {
    SurveyType findBySurveyTypeId(Long surveyTypeId);

    SurveyType findByMultipleChoiceFlgAndQuizFlg(boolean multipleChoiceFlg, boolean quizFlg);
}
