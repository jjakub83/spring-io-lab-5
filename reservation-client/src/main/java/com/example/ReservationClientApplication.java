package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@EnableBinding(Source.class)
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
	public Resources<Reservation> listReservationsSafely() {
		return delegate.listReservations();
	}

	public Resources<Reservation> listReservationsFallback() {
		return new Resources(Arrays.asList("This,is,fallback".split(",")).stream().map(Reservation::new).collect(Collectors.toList()));
	}

}

@FeignClient("reservationservice")
interface ReservationsClient {

	@RequestMapping(path = "/reservations", method = GET)
	Resources<Reservation> listReservations();

	@RequestMapping(path = "/reservations/{id}", method = GET)
	Resource<Reservation> getReservation(@PathVariable("id") Long id);

}

@Slf4j
@RestController
@RequestMapping(path = "/reservations")
class ReservationNamesController {

	@Autowired
	private RestTemplate rest;

	@RequestMapping(path = "/names", method = GET)
	public List<String> names() {
		log.info("Calling names...");

		ParameterizedTypeReference<Resources<Reservation>> responseType =
				new ParameterizedTypeReference<Resources<Reservation>>() {};

		ResponseEntity<Resources<Reservation>> exchange =
				rest.exchange("http://reservationservice/reservations", HttpMethod.GET, null, responseType);

		return exchange.getBody().getContent().stream().map(Reservation::getName).collect(Collectors.toList());
	}

	@Autowired
	private ReservationsClient feignClient;

	@RequestMapping(path = "/feign-names", method = GET)
	public List<String> feignNames() {
		log.info("Calling feign-names...");
		return feignClient.listReservations().getContent().stream().map(Reservation::getName).collect(Collectors.toList());
	}

	@RequestMapping(path = "/{id}/feign-name", method = GET)
	public String feignName(@PathVariable("id") Long id) {
		return feignClient.getReservation(id).getContent().getName();
	}

	@Autowired
	private IntegrationClient safeClient;

	@RequestMapping(path = "/safe-names", method = GET)
	public List<String> safeNames() {
		log.info("Calling safe-names...");
		return safeClient.listReservationsSafely().getContent().stream().map(Reservation::getName).collect(Collectors.toList());
	}

	@Autowired
	@Output(Source.OUTPUT)
	private MessageChannel out;

	@RequestMapping(method = POST)
	public void createReservation(@RequestBody Reservation reservation) {
		Message<String> message = MessageBuilder.withPayload(reservation.getName()).build();
		out.send(message);
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
