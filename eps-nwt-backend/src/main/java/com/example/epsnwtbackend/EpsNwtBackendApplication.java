package com.example.epsnwtbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EpsNwtBackendApplication {
	private static final Logger log = LoggerFactory.getLogger(EpsNwtBackendApplication.class);

	public static void main(String[] args) {
		log.info("➡️ Starting app...");
		try {
			SpringApplication.run(EpsNwtBackendApplication.class, args);
			log.info("✅ Application started successfully");
		} catch (Exception e) {
			log.error("❌ Application failed to start", e);
		}
	}
}
