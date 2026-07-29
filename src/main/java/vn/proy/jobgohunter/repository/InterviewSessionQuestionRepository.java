package vn.proy.jobgohunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewSessionQuestion;
import vn.proy.jobgohunter.domain.InterviewSessionQuestion.SessionOrderId;

public interface InterviewSessionQuestionRepository
        extends JpaRepository<InterviewSessionQuestion, SessionOrderId> {

    List<InterviewSessionQuestion> findByIdSessionIdOrderByIdOrderIndexAsc(String sessionId);
}
