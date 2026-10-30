package tn.transporttn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/** Active @Async avec un petit groupe de threads dédié aux envois push. */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executeur = new ThreadPoolTaskExecutor();
        executeur.setCorePoolSize(2);
        executeur.setMaxPoolSize(4);
        executeur.setQueueCapacity(500);
        executeur.setThreadNamePrefix("push-");
        executeur.initialize();
        return executeur;
    }
}