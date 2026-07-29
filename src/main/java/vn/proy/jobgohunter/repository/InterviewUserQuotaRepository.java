package vn.proy.jobgohunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewUserQuota;

public interface InterviewUserQuotaRepository extends JpaRepository<InterviewUserQuota, Long> {

    Optional<InterviewUserQuota> findByUserId(Long userId);
}
