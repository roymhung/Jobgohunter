package vn.proy.jobgohunter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import vn.proy.jobgohunter.config.oauth.OAuthClientProperties;

// disable security
// @SpringBootApplication(exclude = {
// org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
// org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class})

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(OAuthClientProperties.class)
public class JobgohunterApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobgohunterApplication.class, args);
	}

}
