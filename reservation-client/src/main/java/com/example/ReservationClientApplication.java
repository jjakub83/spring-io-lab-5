package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@Slf4j
@Component
class DiscoveryClientExample implements CommandLineRunner {

	private final DiscoveryClient discoveryClient;

	@Autowired
	public DiscoveryClientExample(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	@Override
	public void run(String... strings) throws Exception {
		try {
			log.info("------------------------------");
			log.info("DiscoveryClient Example");

			discoveryClient.getInstances("reservationservice").stream().forEach(instance -> {
				log.info("Reservation service: ");
				log.info("  ID: {}", instance.getServiceId());
				log.info("  URI: {}", instance.getUri());
				log.info("  Meta: {}", instance.getMetadata());
			});

			log.info("------------------------------");
		} catch (Exception e) {
			log.error("DiscoveryClient Example Error!", e);
		}
	}
}
