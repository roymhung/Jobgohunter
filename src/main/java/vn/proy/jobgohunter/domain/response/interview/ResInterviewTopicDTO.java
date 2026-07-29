package vn.proy.jobgohunter.domain.response.interview;

public class ResInterviewTopicDTO {
    private String code;
    private String name;
    private String groupName;
    private long questionCount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public long getQuestionCount() { return questionCount; }
    public void setQuestionCount(long questionCount) { this.questionCount = questionCount; }
}
