package ru.tbank.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.entity.Answer;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findAll(Specification<Answer> spec);

    Answer findByAnswerId(Long answerId);

    boolean existsByAnswerId(Long answerId);
}
