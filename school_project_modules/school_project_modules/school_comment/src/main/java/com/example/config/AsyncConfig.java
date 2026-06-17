package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync  //开启异步线程功能
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  //核心线程数
        executor.setMaxPoolSize(10);  //最大线程数
        executor.setQueueCapacity(100); //等待队列数
        executor.setKeepAliveSeconds(60); //等待的时间
        executor.setThreadNamePrefix("email-async-"); //线程名称

        // 拒绝策略：由调用者线程处理（即主线程执行）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
