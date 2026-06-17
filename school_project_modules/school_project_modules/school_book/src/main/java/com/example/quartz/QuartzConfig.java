package com.example.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail myJobDetail() {
        return JobBuilder.newJob(ImgQuartzJob.class)
                .withIdentity("imgJob", "imgGroup") //指定 Job 的名称和分组
                .storeDurably() // 即使没有 Trigger 也保留
                .build();
    }

    @Bean
    public Trigger myJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(myJobDetail())       // 绑定到上面那个 Job
                .withIdentity("imgTrigger", "imgGroup") // Trigger 的唯一标识
                .withSchedule(CronScheduleBuilder.cronSchedule("0/05 * * * * ?")) // 每20秒执行一次
                .build();
    }
}
