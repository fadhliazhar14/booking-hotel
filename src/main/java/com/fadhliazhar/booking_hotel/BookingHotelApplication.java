package com.fadhliazhar.booking_hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class BookingHotelApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BookingHotelApplication.class);

		ConfigurableEnvironment env = app.run(args).getEnvironment();

		System.out.println("================================================");
		System.out.println("Server port   : " + env.getProperty("server.port"));
		System.out.println("SSL enabled   : " + env.getProperty("server.ssl.enabled"));
		System.out.println("Active profile: " + String.join(", ", env.getActiveProfiles()));
		System.out.println("================================================");
	}

}
