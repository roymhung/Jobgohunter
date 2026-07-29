package vn.proy.jobgohunter.domain.request.interview;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ReqCreateInterviewSessionDTO {
    @NotEmpty
    private List<String> topics;
    @NotNull
    private String questionType;
    @NotNull
    private String level;

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
