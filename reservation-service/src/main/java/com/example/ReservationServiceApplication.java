package com.example;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

	@Bean
	CommandLineRunner init(ReservationRepository repository) {
		return args -> Arrays.asList("Krzysiek,Marcin,Kamil,Emil,Tomek,Krzysiek,Michał,Patryk,Kuba".split(","))
				.forEach(name -> repository.save(new Reservation(name)));
	}

}

@Component
class ReservationResourceProcessor implements ResourceProcessor<Resource<Reservation>> {

	@Override
	public Resource<Reservation> process(Resource<Reservation> resource) {
		Reservation reservation = resource.getContent();
		String url = format("https://www.google.pl/search?tbm=isch&q=%s", reservation.getName());
		resource.add(new Link(url, "photo"));
		return resource;
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name", rel = "find-by-name")
	List<Reservation> findByName(@Param("rn") String name);
}

@Slf4j
@RestController
@RequestMapping("/custom/reservations")
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
		Optional<ResponseEntity<Resource>> optional = storage.stream()
				.filter(r -> r.name.equals(name))
				.map(r -> new Resource(r, new Link(selfURI(r).toString(), "self")))
				.map(r -> ResponseEntity.ok(r))
				.findFirst();
		//.orElse(ResponseEntity.notFound().build());
		return optional.isPresent() ? optional.get() : ResponseEntity.notFound().build();
	}

	@RequestMapping(method = POST)
	public ResponseEntity<Void> create(@RequestBody Reservation reservation) {
		log.info("RESERVATION: {}", reservation);
		storage.add(reservation);
		return ResponseEntity.created(selfURI(reservation)).build();
	}

	URI selfURI(Reservation reservation) {
		return ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ReservationController.class).getReservation(reservation.name))
			.toUri();
	}

}

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
class Reservation {

	@Id
	@GeneratedValue
	Long id;

	String name;

	Reservation(String name) {
		this.name = name;
	}
}
