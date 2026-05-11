package com.indux.core.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    /**
     * Configuração personalizada do executor de threads para métodos assíncronos.
     * Limita o número de threads para evitar sobrecarga do servidor SMTP.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configurações do pool de threads
        executor.setCorePoolSize(5);          // Número mínimo de threads
        executor.setMaxPoolSize(10);          // Número máximo de threads
        executor.setQueueCapacity(25);        // Capacidade da fila de espera
        executor.setThreadNamePrefix("Async-"); // Prefixo dos nomes das threads
        
        // Política de rejeição quando a fila está cheia
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Aguarda finalização das threads ao encerrar a aplicação
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
