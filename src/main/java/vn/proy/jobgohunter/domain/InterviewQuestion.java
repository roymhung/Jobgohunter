package vn.proy.jobgohunter.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.proy.jobgohunter.util.converter.StringListJsonConverter;

@Entity
@Table(name = "interview_question")
@Getter
@Setter
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_code", nullable = false, length = 50)
    private String topicCode;

    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(nullable = false, length = 20)
    private String level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "options_json", nullable = false, columnDefinition = "json")
    private List<String> optionsJson;

    @Column(name = "correct_index", nullable = false)
    private Integer correctIndex;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private Boolean active = true;
}
