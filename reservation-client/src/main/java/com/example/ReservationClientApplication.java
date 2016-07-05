package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

	@Bean @LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@Component
class IntegrationClient {

	private final ReservationsClient delegate;

	@Autowired
	public IntegrationClient(ReservationsClient delegate) {
		this.delegate = delegate;
	}

	@HystrixCommand(fallbackMethod = "listReservationsFallback")
	public List<Reservation> listReservationsSafely() {
		return delegate.listReservations();
	}

	public List<Reservation> listReservationsFallback() {
		return Arrays.asList("This,is,fallback".split(",")).stream().map(Reservation::new).collect(Collectors.toList());
	}

}

@FeignClient("reservationservice")
interface ReservationsClient {

	@RequestMapping(path = "/custom/reservations", method = GET)
	List<Reservation> listReservations();

}

@RestController
class ReservationNamesController {

	private final RestTemplate rest;
	private final ReservationsClient feignClient;
	private final IntegrationClient safeClient;

	@Autowired
	public ReservationNamesController(RestTemplate rest, ReservationsClient feignClient, IntegrationClient safeClient) {
		this.rest = rest;
		this.feignClient = feignClient;
		this.safeClient = safeClient;
	}

	@RequestMapping(path = "/names", method = GET)
	public List<String> names() {

		ParameterizedTypeReference<List<Reservation>> responseType =
				new ParameterizedTypeReference<List<Reservation>>() {};

		ResponseEntity<List<Reservation>> exchange =
				rest.exchange("http://reservationservice/custom/reservations", HttpMethod.GET, null, responseType);

		return exchange.getBody().stream().map(Reservation::getName).collect(Collectors.toList());
	}

	@RequestMapping(path = "/feign-names", method = GET)
	public List<String> feignNames() {
		return feignClient.listReservations().stream().map(Reservation::getName).collect(Collectors.toList());
	}

	@RequestMapping(path = "/safe-names", method = GET)
	public List<String> safeNames() {
		return safeClient.listReservationsSafely().stream().map(Reservation::getName).collect(Collectors.toList());
	}

}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
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
