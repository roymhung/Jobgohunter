package vn.proy.jobgohunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jobgohunter.interview")
public class InterviewProperties {

    private int freeSessions = 5;
    private int freeQuestionsPerSession = 10;
    private int proQuestionsPerSession = 30;
    private int durationMinutes = 45;
    private int passPercent = 80;
    private int maxTopics = 3;
}
