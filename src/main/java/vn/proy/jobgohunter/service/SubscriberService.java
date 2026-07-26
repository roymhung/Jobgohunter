package vn.proy.jobgohunter.service;

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

    // @Scheduled(fixedDelay = 1000)
    // public void testCron() {
    // System.out.println(">>> TEST CRON");s
    // }

    public boolean isExistsByEmail(String email) {
        return this.subscriberRepository.existsByEmail(email);
    }

    public Subscriber create(Subscriber subs) {
        // check skills
        if (subs.getSkills() != null) {
            List<Long> reqSkills =
                    subs.getSkills().stream().map(x -> x.getId()).collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            subs.setSkills(dbSkills);
        }

        return this.subscriberRepository.save(subs);
    }

    public Subscriber update(Subscriber subsDB, Subscriber subsRequest) {
        // check skills
        if (subsRequest.getSkills() != null) {
            List<Long> reqSkills = subsRequest.getSkills().stream().map(x -> x.getId())
                    .collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            subsDB.setSkills(dbSkills);
        }

        return this.subscriberRepository.save(subsDB);
    }

    // 1. Thêm hàm tìm kiếm theo ID
    public Subscriber findById(long id) {
        Optional<Subscriber> subsOptional = this.subscriberRepository.findById(id);
        if (subsOptional.isPresent()) {
            return subsOptional.get();
        }
        return this.subscriberRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public int sendSubscribersEmailJobs() {
        int sentCount = 0;
        List<Subscriber> listSubs = this.subscriberRepository.findAll();
        if (listSubs == null || listSubs.isEmpty()) {
            System.out.println(">>> EMAIL: Không có subscriber nào trong DB");
            return sentCount;
        }

        for (Subscriber sub : listSubs) {
            List<Skill> listSkills = sub.getSkills();
            if (listSkills == null || listSkills.isEmpty()) {
                System.out.println(">>> EMAIL: Subscriber " + sub.getEmail() + " chưa chọn skill");
                continue;
            }

            List<Long> skillIds =
                    listSkills.stream().map(Skill::getId).collect(Collectors.toList());
            List<Job> listJobs = this.jobRepository.findDistinctBySkillIds(skillIds);
            if (listJobs == null || listJobs.isEmpty()) {
                System.out.println(">>> EMAIL: Không có job khớp skill của " + sub.getEmail());
                continue;
            }

            List<ResEmailJob> arr = listJobs.stream().map(job -> this.convertJobToSendEmail(job))
                    .collect(Collectors.toList());

            System.out.println(">>> EMAIL: Gửi " + arr.size() + " job tới " + sub.getEmail());
            this.emailService.sendEmailFromTemplateSync(sub.getEmail(),
                    "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay", "job",
                    sub.getName(), arr);
            sentCount++;
        }
        return sentCount;
    }

    @Transactional(readOnly = true)
    public int sendSubscriberEmailJobsByEmail(String email) throws IdInvalidException {
        Subscriber sub = this.findByEmail(email);
        if (sub == null) {
            throw new IdInvalidException(
                    "Bạn chưa đăng ký nhận job qua email. Hãy chọn kỹ năng và bấm Cập nhật trước.");
        }

        List<Skill> listSkills = sub.getSkills();
        if (listSkills == null || listSkills.isEmpty()) {
            throw new IdInvalidException("Chưa chọn kỹ năng để nhận job qua email.");
        }

        List<Long> skillIds =
                listSkills.stream().map(Skill::getId).collect(Collectors.toList());
        List<Job> listJobs = this.jobRepository.findDistinctBySkillIds(skillIds);
        if (listJobs == null || listJobs.isEmpty()) {
            System.out.println(">>> EMAIL: Không có job khớp skill của " + email);
            return 0;
        }

        List<ResEmailJob> arr = listJobs.stream().map(job -> this.convertJobToSendEmail(job))
                .collect(Collectors.toList());

        System.out.println(">>> EMAIL: Gửi " + arr.size() + " job (skill ids " + skillIds
                + ") tới " + email);
        this.emailService.sendEmailFromTemplateSync(email,
                "Cơ hội việc làm hot đang chờ đón bạn, khám phá ngay", "job", sub.getName(),
                arr);
        return arr.size();
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
