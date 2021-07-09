package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	
	@Autowired
	private VenueService venueService;
	
	private Venue venue;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testGetOneVenue() {
		client.get().uri("/venues/venues_detail/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Kilburn, G23"));
		});
	}
	
	@Test
	public void testGetNotExistingVenue() {
		client.get().uri("/venues/venues_detail/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("No venue with this id exists"));
		});
	}
	
	@Test
	public void testGetBadVenue() {
		client.get().uri("/venues/venues_detail/invalidId").accept(MediaType.TEXT_HTML).exchange().expectStatus().isBadRequest();
	}
	
	@Test
	public void testSearchVenue() {
		venue = venueService.findAll().iterator().next();
		assertNotNull(venue);		
		String val = venue.getName();
		
		client.get().uri("/venues/search?nameVal=" + val).accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString(val));
		});
	}
	
	@Test
	public void testEmptySearchVenue() {		
		client.get().uri("/venues/search?nameVal=").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
			.expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), not(containsString("Results for:")));
		});
	}
	
	@Test
	public void testGetCreatePage() {
		client.get().uri("/venues/create_venue").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Add Venue"));
		});
	}
	@Test
	public void testPostCreatePageNoAUTH() {
		client.post().uri("/venues/create_venue").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isUnauthorized();		
	}
	
	//@Test
	//@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	//public void testPostCreatePageAUTH() {
	//	client.post().uri("/venues/create_venue").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isOk()
	//	.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED).expectBody(String.class).consumeWith(res-> {
	//		assertThat(res.getResponseBody(), containsString("Add Venue"));
	//	});
	//}
	
	@Test
	public void testGetUpdatePage() {
		venue = venueService.findAll().iterator().next();
		assertNotNull(venue);		
		long id = venue.getId();
		client.get().uri("/venues/venue_detail/" + id + "/update_venue").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Update Details"));
		});
	}
	@Test
	public void testPostUpdatePageNoAUTH() {
		venue = venueService.findAll().iterator().next();
		assertNotNull(venue);		
		long id = venue.getId();
		client.post().uri("/venues/venue_detail/" + id + "/update_venue").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isUnauthorized();		
	}
	
	//@Test
	//@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	//public void testPostUpdatePageAUTH() {
	//event = eventService.findAll().iterator().next();
	//assertNotNull(event);		
	//String val = event.getName();
	//long id = event.getId();
	//	client.post().uri("/events/event_detail/"+id + "/update_event").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isOk()
	//	.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED).expectBody(String.class).consumeWith(res-> {
	//		assertThat(res.getResponseBody(), containsString("Update Details"));
	//	});
	//}
	
	@Test
	public void testDeletenotSignIn() {
		client.delete().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
	}
//	@Test
//	@DirtiesContext
//	public void testDeleteVenueLogIN() throws Exception {
//		
//		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().delete().uri("/venues/venues_detail/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("venues"));
//		//verify(venueService).deleteById(1L);
//		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
//		//we use minus 1 here to detect wether we succcessfully delete the event
//	}
}
