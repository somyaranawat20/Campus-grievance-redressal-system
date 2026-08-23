package com.inn.cgrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CgrsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CgrsApplication.class, args);
	}

}
