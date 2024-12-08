package ru.tbank.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "answers", schema = "survey")
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "answer", unique = true, nullable = false)
    private String answer;

    @Column(name = "correct_flg")
    private Boolean correctFlg;

    @Column(name = "insert_dt")
    private LocalDateTime insertDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    @JsonBackReference
    private Survey survey;

    public Answer(Answer answer, boolean includeSurvey) {
        this.answerId = answer.getAnswerId();
        this.answer = answer.getAnswer();
        this.correctFlg = answer.getCorrectFlg();
        this.insertDt = answer.getInsertDt();
        if (includeSurvey) {
            this.survey = new Survey(answer.getSurvey(), false);
        }
    }
}
