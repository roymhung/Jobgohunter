package vn.proy.jobgohunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.proy.jobgohunter.domain.InterviewSubscription;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

public interface InterviewSubscriptionRepository extends JpaRepository<InterviewSubscription, Long> {

    @Query("""
            SELECT s FROM InterviewSubscription s
            WHERE s.userId = :userId
              AND s.status = :status
              AND (s.endsAt IS NULL OR s.endsAt > CURRENT_TIMESTAMP)
            ORDER BY s.id DESC
            """)
    Optional<InterviewSubscription> findActiveForUser(
            @Param("userId") Long userId,
            @Param("status") InterviewSubscriptionStatusEnum status);
}
