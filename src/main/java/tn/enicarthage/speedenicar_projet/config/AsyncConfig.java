package tn.enicarthage.speedenicar_projet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Active @Async dans tout le projet.
 * Sans cette annotation, @Async dans EmailService est ignoré
 * et l'email bloque le thread HTTP.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot utilise un thread pool par défaut.
    // Si vous voulez le personnaliser, décommentez ci-dessous :

    /*
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("speed-async-");
        exec.initialize();
        return exec;
    }
    */
}