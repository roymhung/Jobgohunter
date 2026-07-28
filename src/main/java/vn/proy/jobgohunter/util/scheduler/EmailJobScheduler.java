package vn.proy.jobgohunter.util.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import vn.proy.jobgohunter.service.SubscriberService;

@Component
public class EmailJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailJobScheduler.class);

    private final SubscriberService subscriberService;

    public EmailJobScheduler(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /** 8:00 sáng mỗi ngày (giờ VN) — chỉ gửi job mới kể từ lần email trước. */
    @Scheduled(cron = "${app.email.cron:0 0 8 * * *}", zone = "${app.email.zone:Asia/Ho_Chi_Minh}")
    public void sendDailyJobEmails() {
        log.info(">>> CRON email job: bắt đầu gửi cho subscribers");
        int sent = this.subscriberService.sendSubscribersEmailJobs();
        log.info(">>> CRON email job: đã gửi cho {} subscriber có job mới", sent);
    }
}
