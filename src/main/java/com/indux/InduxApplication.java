package com.indux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.indux")
@EnableMongoRepositories
@EnableJpaRepositories
@EnableScheduling
public class InduxApplication {

    public static void main(String[] args) {
        SpringApplication.run(InduxApplication.class, args);
    }

}
