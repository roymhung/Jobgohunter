package vn.proy.jobgohunter.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.InterviewQuestion;
import vn.proy.jobgohunter.domain.InterviewTopic;
import vn.proy.jobgohunter.repository.InterviewQuestionRepository;
import vn.proy.jobgohunter.repository.InterviewTopicRepository;

@Service
public class InterviewDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_TYPE = "Lý thuyết";
    private static final String DEFAULT_LEVEL = "Junior";
    private static final int MIN_QUESTIONS_PER_TOPIC = 12;

    private static final Map<String, String[]> TOPIC_CATALOG = catalog();

    private final InterviewTopicRepository topicRepository;
    private final InterviewQuestionRepository questionRepository;

    public InterviewDataInitializer(InterviewTopicRepository topicRepository,
            InterviewQuestionRepository questionRepository) {
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    private static Map<String, String[]> catalog() {
        Map<String, String[]> m = new LinkedHashMap<>();
        put(m, "java", "Nền tảng", "Java");
        put(m, "networking", "Nền tảng", "Networking");
        put(m, "git", "Nền tảng", "Git");
        put(m, "linux", "Nền tảng", "Linux");
        put(m, "spring", "Backend", "Spring Boot");
        put(m, "security", "Backend", "Security");
        put(m, "testing", "Backend", "Testing");
        put(m, "api", "Backend", "API Design");
        put(m, "pattern", "Kiến trúc", "Design Pattern");
        put(m, "ddd", "Kiến trúc", "DDD");
        put(m, "micro", "Kiến trúc", "Microservices");
        put(m, "sysdesign", "Kiến trúc", "System Design");
        put(m, "sql", "Dữ liệu", "SQL");
        put(m, "nosql", "Dữ liệu", "NoSQL");
        put(m, "redis", "Dữ liệu", "Redis");
        put(m, "messaging", "Dữ liệu", "Messaging");
        put(m, "docker", "DevOps & Cloud", "Docker");
        put(m, "k8s", "DevOps & Cloud", "Kubernetes");
        put(m, "aws", "DevOps & Cloud", "AWS");
        put(m, "cicd", "DevOps & Cloud", "CI/CD");
        put(m, "monitor", "DevOps & Cloud", "Monitoring");
        put(m, "nginx", "DevOps & Cloud", "Nginx");
        return m;
    }

    private static void put(Map<String, String[]> m, String code, String group, String name) {
        m.put(code, new String[] { group, name });
    }

    @Override
    public void run(String... args) {
        int globalQ = (int) questionRepository.count();
        for (var e : TOPIC_CATALOG.entrySet()) {
            String code = e.getKey();
            String group = e.getValue()[0];
            String name = e.getValue()[1];

            InterviewTopic topic = topicRepository.findByCode(code).orElseGet(() -> {
                InterviewTopic t = new InterviewTopic();
                t.setCode(code);
                t.setGroupName(group);
                t.setName(name);
                t.setActive(true);
                return topicRepository.save(t);
            });

            long existing = questionRepository.countByTopicCodeAndActiveTrue(code);
            int need = (int) Math.max(0, MIN_QUESTIONS_PER_TOPIC - existing);
            if (need == 0) {
                continue;
            }
            List<InterviewQuestion> batch = new ArrayList<>();
            for (int i = 0; i < need; i++) {
                globalQ++;
                InterviewQuestion q = new InterviewQuestion();
                q.setTopicCode(topic.getCode());
                q.setQuestionType(DEFAULT_TYPE);
                q.setLevel(DEFAULT_LEVEL);
                q.setContent("Câu hỏi #" + globalQ + " — " + name + " (" + DEFAULT_TYPE + ", " + DEFAULT_LEVEL + ")?");
                q.setOptionsJson(List.of("Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"));
                q.setCorrectIndex(0);
                q.setExplanation("Giải thích demo cho câu " + globalQ + " — chủ đề " + name + ".");
                q.setActive(true);
                batch.add(q);
            }
            questionRepository.saveAll(batch);
        }
    }
}
