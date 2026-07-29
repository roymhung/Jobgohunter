package vn.proy.jobgohunter.domain.response.interview;

public class ResInterviewConfigDTO {
    private int freeSessions;
    private int freeQuestionsPerSession;
    private int proQuestionsPerSession;
    private int durationMinutes;
    private int passPercent;
    private int maxTopics;

    public int getFreeSessions() { return freeSessions; }
    public void setFreeSessions(int freeSessions) { this.freeSessions = freeSessions; }
    public int getFreeQuestionsPerSession() { return freeQuestionsPerSession; }
    public void setFreeQuestionsPerSession(int v) { this.freeQuestionsPerSession = v; }
    public int getProQuestionsPerSession() { return proQuestionsPerSession; }
    public void setProQuestionsPerSession(int v) { this.proQuestionsPerSession = v; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getPassPercent() { return passPercent; }
    public void setPassPercent(int passPercent) { this.passPercent = passPercent; }
    public int getMaxTopics() { return maxTopics; }
    public void setMaxTopics(int maxTopics) { this.maxTopics = maxTopics; }
}
