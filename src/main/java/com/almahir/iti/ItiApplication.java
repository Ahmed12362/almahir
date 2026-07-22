package com.almahir.iti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ItiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItiApplication.class, args);
	}

}
