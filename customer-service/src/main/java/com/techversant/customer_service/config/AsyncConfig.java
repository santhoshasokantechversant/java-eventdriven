/**
 * @file AsyncConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description This class configures a thread pool executor for asynchronous method execution in the application.
 */

package com.techversant.customer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configures a {@link ThreadPoolTaskExecutor} for asynchronous method execution.
     * This executor is used by Spring's @Async annotation to run methods asynchronously
     * using a dedicated thread pool.
     * Configuration details:
     * Core pool size: 5 threads always kept alive.
     * Maximum pool size: 10 threads for peak load handling.
     * Queue capacity: 100 tasks can wait in the queue before new threads are created.
     * Thread name prefix: "AsyncEvent-" to identify async threads in logs.
     *
     * @return a fully initialized {@link Executor} for asynchronous execution
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncEvent-");
        executor.initialize();
        return executor;
    }
}
