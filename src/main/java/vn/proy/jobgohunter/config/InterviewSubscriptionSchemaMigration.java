package vn.proy.jobgohunter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL ENUM cũ (BANK_TRANSFER, MOMO) không nhận VNPAY — chuyển sang VARCHAR.
 */
@Component
public class InterviewSubscriptionSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(InterviewSubscriptionSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public InterviewSubscriptionSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migratePaymentMethodColumn() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE interview_subscription MODIFY COLUMN payment_method VARCHAR(20) NULL");
            log.info("interview_subscription.payment_method -> VARCHAR(20) OK");
        } catch (Exception e) {
            log.warn("Could not migrate payment_method column (may already be VARCHAR): {}", e.getMessage());
        }
    }
}
