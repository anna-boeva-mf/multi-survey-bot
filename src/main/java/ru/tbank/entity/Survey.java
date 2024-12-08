package ru.tbank.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "surveys", schema = "survey")
@Entity
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "survey_question", unique = true, nullable = false)
    private String surveyQuestion;

    @Column(name = "survey_type_id")
    private Long surveyTypeId;

    @Column(name = "survey_group_id")
    private Long surveyGroupId;

    @Column(name = "insert_dt")
    private LocalDateTime insertDt;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Answer> answers;

    public Survey(Survey survey, boolean includeAnswers) {
        this.surveyId = survey.surveyId;
        this.surveyQuestion = survey.surveyQuestion;
        this.surveyTypeId = survey.surveyTypeId;
        this.surveyGroupId = survey.surveyGroupId;
        this.insertDt = survey.insertDt;
        if (includeAnswers && survey.getAnswers() != null) {
            List<Answer> answers = survey.getAnswers().stream()
                    .map(answer -> new Answer(answer, false))
                    .collect(Collectors.toList());
            this.answers = answers;
        }
    }
}
