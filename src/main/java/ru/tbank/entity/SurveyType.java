package ru.tbank.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Table(name = "survey_types", schema = "survey")
@Entity
public class SurveyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_type_id")
    private Long surveyTypeId;

    @Column(name = "survey_type_name", unique = true, nullable = false)
    private String surveyTypeName;

    @Column(name = "multiple_choice_flg")
    private boolean multipleChoiceFlg;

    @Column(name = "quiz_flg")
    private boolean quizFlg;

    @Column(name = "insert_dt")
    private LocalDateTime insertDt;
}
