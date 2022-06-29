package com.hanghae.degether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DegetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(DegetherApplication.class, args);
    }

}
