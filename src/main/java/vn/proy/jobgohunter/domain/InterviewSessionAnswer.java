package vn.proy.jobgohunter.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interview_session_answer")
@Getter
@Setter
public class InterviewSessionAnswer {

    @EmbeddedId
    private SessionOrderId id = new SessionOrderId();

    @Column(name = "selected_index")
    private Integer selectedIndex;

    @Getter
    @Setter
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SessionOrderId implements Serializable {
        @Column(name = "session_id", length = 36)
        private String sessionId;

        @Column(name = "order_index")
        private Integer orderIndex;
    }
}
