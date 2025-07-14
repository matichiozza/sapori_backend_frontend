package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories("com.example.demo.datos")
@EntityScan("com.example.demo.modelo")
@EnableScheduling
public class SaporiApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaporiApiApplication.class, args);
	}

}
