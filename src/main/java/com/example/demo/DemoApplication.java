package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// demo말고 navershopping 폴더도 도 가능하도록
@ComponentScan(basePackages = {"com.example.demo", "com.example.navershopping"})
@EnableJpaRepositories(basePackages = {"com.example.demo.repository", "com.example.navershopping.repository"})
@EntityScan(basePackages = {"com.example.demo.entity", "com.example.navershopping.entity"})
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
