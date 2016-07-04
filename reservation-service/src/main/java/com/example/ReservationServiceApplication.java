package com.example;

import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Slf4j
@RestController
@RequestMapping("/reservations")
class ReservationController {

	List<Reservation> storage =
			asList("Krzysiek,Marcin,Kamil,Emil,Tomek,Krzysiek,Michał,Patryk,Kuba".split(","))
			.stream().map(Reservation::new).collect(Collectors.toList());

	@RequestMapping(method = GET)
	public List<Reservation> listReservations() {
		return storage;
	}

	@RequestMapping(path = "/{name}", method = GET)
	public ResponseEntity<?> getReservation(@PathVariable String name) {
		Optional<ResponseEntity<?>> optional = storage.stream()
				.filter(r -> r.name.equals(name))
				.findFirst()
				.map(r -> new Resource(r, new Link(selfURI(r).toString(), "self")))
				.map(r -> ResponseEntity.ok(r));
		return optional.isPresent() ? optional.get() : ResponseEntity.notFound().build();
	}

	@RequestMapping(method = POST)
	public ResponseEntity<Void> create(@RequestBody Reservation reservation) {
		log.info("RESERVATION: {}", reservation);
		storage.add(reservation);
		return ResponseEntity.created(selfURI(reservation)).build();
	}

	private URI selfURI(Reservation reservation) {
		return ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ReservationController.class).getReservation(reservation.name))
			.toUri();
	}


}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
}
