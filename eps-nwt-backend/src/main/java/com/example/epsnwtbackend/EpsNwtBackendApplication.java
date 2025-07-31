package com.example.epsnwtbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EpsNwtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpsNwtBackendApplication.class, args);
	}

}
