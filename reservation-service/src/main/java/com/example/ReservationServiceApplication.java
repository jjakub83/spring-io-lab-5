package com.example;

import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationController {

	@RequestMapping(method = GET)
	public List<Reservation> listReservations() {
		return asList("Krzysiek,Marcin,Kamil,Emil,Tomek,Krzysiek,Michał,Patryk,Kuba".split(","))
				.stream().map(Reservation::new).collect(Collectors.toList());
	}
}

@NoArgsConstructor
@AllArgsConstructor
class Reservation {

	@Getter
	String name;
}

