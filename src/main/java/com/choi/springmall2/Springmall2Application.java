package com.choi.springmall2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class Springmall2Application {

    public static void main(String[] args) {
        SpringApplication.run(Springmall2Application.class, args);
    }

}
