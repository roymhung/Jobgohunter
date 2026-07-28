package vn.proy.jobgohunter.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.proy.jobgohunter.domain.Job;
import vn.proy.jobgohunter.domain.Skill;
import vn.proy.jobgohunter.domain.Subscriber;
import vn.proy.jobgohunter.domain.response.email.ResEmailJob;
import vn.proy.jobgohunter.repository.JobRepository;
import vn.proy.jobgohunter.repository.SkillRepository;
import vn.proy.jobgohunter.repository.SubscriberRepository;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;

    public SubscriberService(SubscriberRepository subscriberRepository,
            SkillRepository skillRepository, JobRepository jobRepository,
            EmailService emailService) {
        this.subscriberRepository = subscriberRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
    }

    public boolean isExistsByEmail(String email) {
        return this.subscriberRepository.existsByEmail(email);
    }

    public Subscriber create(Subscriber subs) {
        if (subs.getSkills() != null) {
            List<Long> reqSkills =
                    subs.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            subs.setSkills(dbSkills);
        }
        subs.setSubscribed(true);
        return this.subscriberRepository.save(subs);
    }

    public Subscriber update(Subscriber subsDB, Subscriber subsRequest) {
        if (subsRequest.getSkills() != null) {
            List<Long> reqSkills = subsRequest.getSkills().stream().map(x -> x.getId())
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            subsDB.setSkills(dbSkills);
        }
        subsDB.setSubscribed(true);
        return this.subscriberRepository.save(subsDB);
    }

    public Subscriber findById(long id) {
        Optional<Subscriber> subsOptional = this.subscriberRepository.findById(id);
        return subsOptional.orElse(null);
    }

    @Transactional
    public int sendSubscribersEmailJobs() {
        int sentCount = 0;
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs == null || listSubs.isEmpty()) {
            return sentCount;
        }
        for (Subscriber sub : listSubs) {
            if (sendNewJobsToSubscriber(sub) > 0) {
                sentCount++;
            }
        }
        return sentCount;
    }

    @Transactional
    public int sendSubscriberEmailJobsByEmail(String email) throws IdInvalidException {
        Subscriber sub = this.findByEmail(email);
        if (sub == null) {
            throw new IdInvalidException(
                    "Bạn chưa đăng ký nhận job qua email. Hãy chọn kỹ năng và bấm Cập nhật trước.");
        }
        if (!sub.isSubscribed()) {
            throw new IdInvalidException(
                    "Bạn đã hủy nhận email job. Chọn kỹ năng và bấm Cập nhật để đăng ký lại.");
        }
        return sendNewJobsToSubscriber(sub);
    }

    @Transactional
    public void unsubscribeByEmail(String email) throws IdInvalidException {
        Subscriber sub = this.findByEmail(email);
        if (sub == null) {
            throw new IdInvalidException("Bạn chưa đăng ký nhận job qua email.");
        }
        sub.setSubscribed(false);
        this.subscriberRepository.save(sub);
    }

    private int sendNewJobsToSubscriber(Subscriber sub) {
        if (!sub.isSubscribed()) {
            return 0;
        }
        List<Job> listJobs = findNewMatchingJobs(sub);
        if (listJobs.isEmpty()) {
            return 0;
        }
        List<ResEmailJob> arr = listJobs.stream().map(this::convertJobToSendEmail)
                .collect(Collectors.toList());
        this.emailService.sendEmailFromTemplateSync(sub.getEmail(),
                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay", "job", sub.getName(),
                arr);
        sub.setLastEmailSentAt(Instant.now());
        this.subscriberRepository.save(sub);
        return arr.size();
    }

    private List<Job> findNewMatchingJobs(Subscriber sub) {
        List<Skill> listSkills = sub.getSkills();
        if (listSkills == null || listSkills.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> skillIds =
                listSkills.stream().map(Skill::getId).collect(Collectors.toList());
        Instant since = sub.getLastEmailSentAt() != null ? sub.getLastEmailSentAt() : Instant.EPOCH;
        List<Job> listJobs =
                this.jobRepository.findDistinctBySkillIdsAndCreatedAtAfter(skillIds, since);
        return listJobs != null ? listJobs : Collections.emptyList();
    }

    public ResEmailJob convertJobToSendEmail(Job job) {
        ResEmailJob res = new ResEmailJob();
        res.setName(job.getName());
        res.setSalary(job.getSalary());
        res.setCompany(new ResEmailJob.CompanyEmail(job.getCompany().getName()));
        List<Skill> skills = job.getSkills();
        List<ResEmailJob.SkillEmail> s =
                skills.stream().map(skill -> new ResEmailJob.SkillEmail(skill.getName()))
                        .collect(Collectors.toList());
        res.setSkills(s);
        return res;
    }

    public Subscriber findByEmail(String email) {
        return this.subscriberRepository.findByEmail(email);
    }
}
