package vn.proy.jobgohunter.domain.response.interview;

import java.util.List;

public class ResInterviewQuestionDTO {
    private int orderIndex;
    private long questionId;
    private String topicCode;
    private String content;
    private List<String> options;
    private Integer selectedIndex;
    private Integer correctIndex;
    private String explanation;

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }
    public String getTopicCode() { return topicCode; }
    public void setTopicCode(String topicCode) { this.topicCode = topicCode; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public Integer getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(Integer selectedIndex) { this.selectedIndex = selectedIndex; }
    public Integer getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(Integer correctIndex) { this.correctIndex = correctIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
