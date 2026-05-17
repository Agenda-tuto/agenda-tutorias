package com.example.agenda_tutoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendaTutoriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendaTutoriaApplication.class, args);
    }
}