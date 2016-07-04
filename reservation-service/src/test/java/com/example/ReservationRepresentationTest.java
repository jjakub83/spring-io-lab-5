package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@JsonTest
public class ReservationRepresentationTest {

	JacksonTester<Resource<Reservation>> json;

	@Test
	public void representsReservation() throws Exception {
		Reservation reservation = new Reservation("John");
		Resource<Reservation> resource = new Resource<>(reservation);
		resource = new ReservationResourceProcessor().process(resource);

		JsonContent<Resource<Reservation>> result = json.write(resource);

		assertThat(result).extractingJsonPathStringValue("@.name").isEqualTo("John");
//		assertThat(result).extractingJsonPathStringValue("@.links.photo.href")
//			.isEqualTo("https://www.google.pl/search?tbm=isch&q=John");
	}
}
