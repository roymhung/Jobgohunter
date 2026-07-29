package vn.proy.jobgohunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewSessionAnswer;
import vn.proy.jobgohunter.domain.InterviewSessionAnswer.SessionOrderId;

public interface InterviewSessionAnswerRepository
        extends JpaRepository<InterviewSessionAnswer, SessionOrderId> {

    List<InterviewSessionAnswer> findByIdSessionIdOrderByIdOrderIndexAsc(String sessionId);
}
