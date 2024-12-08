package ru.tbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.entity.SurveyGroup;

@Repository
public interface SurveyGroupRepository extends JpaRepository<SurveyGroup, Long> {
    SurveyGroup findBySurveyGroupName(String surveyGroupName);

    boolean existsBySurveyGroupName(String surveyGroupName);

    boolean existsBySurveyGroupId(Long surveyGroupId);
}
