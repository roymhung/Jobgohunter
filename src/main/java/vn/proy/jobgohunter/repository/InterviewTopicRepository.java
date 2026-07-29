package vn.proy.jobgohunter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.proy.jobgohunter.domain.InterviewTopic;

public interface InterviewTopicRepository extends JpaRepository<InterviewTopic, Long> {

    List<InterviewTopic> findByActiveTrueOrderByGroupNameAscNameAsc();

    List<InterviewTopic> findByCodeInAndActiveTrue(Iterable<String> codes);

    Optional<InterviewTopic> findByCode(String code);
}
