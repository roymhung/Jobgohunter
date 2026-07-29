package vn.proy.jobgohunter.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.InterviewQuestion;
import vn.proy.jobgohunter.domain.InterviewTopic;
import vn.proy.jobgohunter.repository.InterviewQuestionRepository;
import vn.proy.jobgohunter.repository.InterviewTopicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InterviewDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_TYPE = "Lý thuyết";
    private static final String DEFAULT_LEVEL = "Junior";

    private final InterviewTopicRepository topicRepository;
    private final InterviewQuestionRepository questionRepository;

    public InterviewDataInitializer(InterviewTopicRepository topicRepository,
            InterviewQuestionRepository questionRepository) {
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (topicRepository.count() > 0) {
            return;
        }
        Map<String, String[]> topics = Map.ofEntries(
                Map.entry("java", new String[] { "Backend", "Java" }),
                Map.entry("networking", new String[] { "Backend", "Networking" }),
                Map.entry("git", new String[] { "DevOps", "Git" }),
                Map.entry("linux", new String[] { "DevOps", "Linux" }),
                Map.entry("spring", new String[] { "Backend", "Spring Boot" }),
                Map.entry("security", new String[] { "Backend", "Security" }),
                Map.entry("testing", new String[] { "Backend", "Testing" }),
                Map.entry("api", new String[] { "Backend", "API Design" }),
                Map.entry("pattern", new String[] { "Backend", "Design Pattern" }),
                Map.entry("sql", new String[] { "Data", "SQL" }),
                Map.entry("docker", new String[] { "DevOps", "Docker" }),
                Map.entry("redis", new String[] { "Data", "Redis" }));

        List<InterviewTopic> topicEntities = new ArrayList<>();
        for (var e : topics.entrySet()) {
            InterviewTopic t = new InterviewTopic();
            t.setCode(e.getKey());
            t.setGroupName(e.getValue()[0]);
            t.setName(e.getValue()[1]);
            t.setActive(true);
            topicEntities.add(t);
        }
        topicRepository.saveAll(topicEntities);

        List<InterviewQuestion> questions = new ArrayList<>();
        int n = 0;
        for (InterviewTopic t : topicEntities) {
            for (int i = 1; i <= 5; i++) {
                n++;
                InterviewQuestion q = new InterviewQuestion();
                q.setTopicCode(t.getCode());
                q.setQuestionType(DEFAULT_TYPE);
                q.setLevel(DEFAULT_LEVEL);
                q.setContent("Câu hỏi demo #" + n + " — chủ đề " + t.getName() + "?");
                q.setOptionsJson(List.of("Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D"));
                q.setCorrectIndex(0);
                q.setExplanation("Giải thích demo cho câu " + n + ".");
                q.setActive(true);
                questions.add(q);
            }
        }
        questionRepository.saveAll(questions);
    }
}
