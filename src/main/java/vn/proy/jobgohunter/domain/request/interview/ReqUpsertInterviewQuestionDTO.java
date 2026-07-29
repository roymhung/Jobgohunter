package vn.proy.jobgohunter.domain.request.interview;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReqUpsertInterviewQuestionDTO {

    @NotBlank
    @Size(max = 50)
    private String topicCode;

    @NotBlank
    @Size(max = 30)
    private String questionType;

    @NotBlank
    @Size(max = 20)
    private String level;

    @NotBlank
    private String content;

    @NotEmpty
    @Size(min = 2, max = 8)
    private List<@NotBlank String> options;

    @NotNull
    @Min(0)
    @Max(7)
    private Integer correctIndex;

    private String explanation;

    private Boolean active = true;

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Integer getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(Integer correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
