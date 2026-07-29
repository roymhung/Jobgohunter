package vn.proy.jobgohunter.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.proy.jobgohunter.domain.InterviewQuestion;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    long countByTopicCodeAndActiveTrue(String topicCode);

    /** Dùng {@link Pageable} (page size = số câu) — tránh LIMIT :param trên native MySQL. */
    @Query("""
            SELECT q FROM InterviewQuestion q
            WHERE q.active = true
              AND q.topicCode IN :topicCodes
              AND q.questionType = :questionType
              AND q.level = :level
            ORDER BY FUNCTION('RAND')
            """)
    List<InterviewQuestion> pickRandom(
            @Param("topicCodes") List<String> topicCodes,
            @Param("questionType") String questionType,
            @Param("level") String level,
            Pageable pageable);
}
