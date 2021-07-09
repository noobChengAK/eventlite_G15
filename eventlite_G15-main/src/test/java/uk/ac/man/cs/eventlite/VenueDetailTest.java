package uk.ac.man.cs.eventlite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;


import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class VenueDetailTest {

	private MockMvc mvc;
	long id;

	@Autowired
	private WebApplicationContext context;
	
	@Autowired
	private VenueService venueService;
	

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();

		Venue v = new Venue();
		v.setAddress("Blank address");
		v.setName("Blank Name");
		v.setCapacity(999);

	

		venueService.save(v);
		
		id = v.getId();
		}
	
	@Test
	public void getRoot() throws Exception {
		mvc.perform(get("/venues/venues_detail/"+ id).accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
	}

	@Test
	public void getJsonRoot() throws Exception {
		mvc.perform(get("/venues/venues_detail/" + id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotAcceptable());
	}
	
	@Test
	public void testPageContent() throws Exception {
		MvcResult page = mvc.perform(get("/venues/venues_detail/" + id).accept(MediaType.TEXT_HTML)).andReturn();
		assertThat(page.getResponse().getContentAsString()).contains("Blank Name");
		assertThat(page.getResponse().getContentAsString()).contains("Blank address");
		assertThat(page.getResponse().getContentAsString()).contains("999");
		
	}
}


