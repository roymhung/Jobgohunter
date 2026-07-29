package vn.proy.jobgohunter.domain.response.interview;

import java.time.Instant;

public class ResInterviewHistoryItemDTO {
    private String sessionId;
    private Instant submittedAt;
    private Integer scorePercent;
    private boolean passed;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
