package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReservationPersistenceTest {

	@Autowired TestEntityManager entityManager;
	@Autowired ReservationRepository repository;

	@Test
	public void persistsReservation() {
		Reservation reservation = new Reservation("John");

		Reservation expected = repository.save(reservation);
		entityManager.flush();
		entityManager.clear();

		Reservation actual = entityManager.find(Reservation.class, expected.id);
		assertThat(actual.id).isEqualTo(expected.id);
	}
}
