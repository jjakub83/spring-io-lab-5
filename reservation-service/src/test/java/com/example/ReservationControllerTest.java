package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ReservationControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void listsReservations() throws Exception {
		mvc.perform(get("/custom/reservations/Krzysiek"))
				.andDo(print())
				.andExpect(jsonPath("@.name").value("Krzysiek"));
	}
}
