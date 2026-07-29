package vn.proy.jobgohunter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "interview_user_quota")
@Getter
@Setter
public class InterviewUserQuota {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "free_sessions_left", nullable = false)
    private Integer freeSessionsLeft = 5;
}
