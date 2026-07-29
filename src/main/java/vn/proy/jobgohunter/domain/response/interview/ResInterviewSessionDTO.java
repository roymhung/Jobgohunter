package vn.proy.jobgohunter.domain.response.interview;

import java.time.Instant;
import java.util.List;
import vn.proy.jobgohunter.util.enums.InterviewSessionStatusEnum;

public class ResInterviewSessionDTO {
    private String id;
    private InterviewSessionStatusEnum status;
    private List<String> topics;
    private String questionType;
    private String level;
    private Instant startedAt;
    private Instant endsAt;
    private Instant submittedAt;
    private Integer scorePercent;
    private boolean passed;
    private List<ResInterviewQuestionDTO> questions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public InterviewSessionStatusEnum getStatus() { return status; }
    public void setStatus(InterviewSessionStatusEnum status) { this.status = status; }
    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public List<ResInterviewQuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<ResInterviewQuestionDTO> questions) { this.questions = questions; }
}
