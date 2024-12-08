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
@Table(name = "results", schema = "survey")
@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "user_result", nullable = false)
    private String userResult;

    @Column(name = "insert_dt")
    private LocalDateTime insertDt;
}
