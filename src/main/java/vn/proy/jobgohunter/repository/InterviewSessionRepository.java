package vn.proy.jobgohunter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewSession;
import vn.proy.jobgohunter.util.enums.InterviewSessionStatusEnum;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, String> {

    long countByUserId(Long userId);

    Optional<InterviewSession> findByIdAndUserId(String id, Long userId);

    List<InterviewSession> findByUserIdAndStatusOrderBySubmittedAtDesc(
            Long userId, InterviewSessionStatusEnum status);
}
