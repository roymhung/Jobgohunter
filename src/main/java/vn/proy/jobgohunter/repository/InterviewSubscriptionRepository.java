package vn.proy.jobgohunter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewSubscription;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

public interface InterviewSubscriptionRepository extends JpaRepository<InterviewSubscription, Long> {

    @org.springframework.data.jpa.repository.Query("""
            SELECT s FROM InterviewSubscription s
            WHERE s.userId = :userId
              AND s.status = :status
              AND (s.endsAt IS NULL OR s.endsAt > CURRENT_TIMESTAMP)
            ORDER BY s.id DESC
            """)
    Optional<InterviewSubscription> findActiveForUser(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("status") InterviewSubscriptionStatusEnum status);

    Optional<InterviewSubscription> findFirstByUserIdAndStatusOrderByIdDesc(
            Long userId, InterviewSubscriptionStatusEnum status);

    Optional<InterviewSubscription> findByIdAndUserId(Long id, Long userId);

    List<InterviewSubscription> findByStatusOrderByIdDesc(InterviewSubscriptionStatusEnum status);
}
