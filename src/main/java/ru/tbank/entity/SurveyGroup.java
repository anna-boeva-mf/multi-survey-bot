package ru.tbank.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "survey_groups", schema = "survey")
@Entity
public class SurveyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_group_id")
    private Long surveyGroupId;

    @Column(name = "survey_group_name", unique = true, nullable = false)
    private String surveyGroupName;

    @Column(name = "survey_type_id")
    private Long surveyTypeId;

    @Column(name = "insert_dt")
    private LocalDateTime insertDt;
}
