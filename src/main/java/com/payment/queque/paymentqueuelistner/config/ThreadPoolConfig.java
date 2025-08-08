package com.payment.queque.paymentqueuelistner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration class for thread pools using Java 21 virtual threads.
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${app.thread-pool.core-size:10}")
    private int corePoolSize;

    @Value("${app.thread-pool.max-size:20}")
    private int maxPoolSize;

    @Value("${app.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * Creates a thread pool task executor for asynchronous processing.
     * This is a traditional thread pool for compatibility with Spring's @Async.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("payment-processor-");
        executor.initialize();
        return executor;
    }

    /**
     * Creates a virtual thread per task executor using Java 21 virtual threads.
     * This is more efficient for I/O-bound tasks like SQS message processing.
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}