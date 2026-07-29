package vn.proy.jobgohunter.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.proy.jobgohunter.util.enums.InterviewSessionStatusEnum;

@Entity
@Table(name = "interview_session")
@Getter
@Setter
public class InterviewSession {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewSessionStatusEnum status;

    @Column(name = "setup_json", nullable = false, columnDefinition = "json")
    private String setupJson;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "score_percent")
    private Integer scorePercent;
}
