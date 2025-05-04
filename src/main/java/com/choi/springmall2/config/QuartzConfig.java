package com.choi.springmall2.config;

import com.choi.springmall2.job.TempFileCleanupJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail tempFileCleanupJobDetail() {
        return JobBuilder.newJob(TempFileCleanupJob.class)
                .withIdentity("tempFileCleanupJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger tempFileCleanupTrigger() {
        // 매일 새벽 3시 (CRON 표현식 사용)
        CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule("0 0 3 * * ?");

        return TriggerBuilder.newTrigger()
                .forJob(tempFileCleanupJobDetail())
                .withIdentity("tempFileCleanupTrigger")
                .withSchedule(schedule)
                .build();
    }
}
