package uk.ac.man.cs.eventlite.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;



import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "36000")

public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	
	@Autowired
	private EventService eventService;
	
	private Event event;

	private WebTestClient client;
	private int currentRows;

	public static final String ADMIN_USER = "Mustafa";
	public static final String ADMIN_ROLE = uk.ac.man.cs.eventlite.config.Security.ADMIN_ROLE;

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("events");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}
	
	@Test
	public void testGetOneEvent() {
		client.get().uri("/events/event_detail/5").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("COMP23412 Showcase, group G"));
		});
	}
	
	
	@Test
	public void testGetNotExistingEvent() {
		client.get().uri("/events/event_detail/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("No event with this id exists"));
		});
	}
	
	@Test
	public void testGetBadEvent() {
		client.get().uri("/events/event_detail/invalidId").accept(MediaType.TEXT_HTML).exchange().expectStatus().isBadRequest();
	}
	
	@Test
	public void testMissingEvent() {
		client.get().uri("/events/event_detail/").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound();
	}
	
	@Test
	public void testSearchEvent() {
		event = eventService.findAll().iterator().next();
		assertNotNull(event);		
		String val = event.getName();
		
		client.get().uri("/events/search?nameVal=" + val).accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString(val));
		});
	}
	@Test
	public void testEmptySearchEvent() {		
		client.get().uri("/events/search?nameVal=").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound()
			.expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), not(containsString("Results for:")));
		});
	}
	
	@Test
	public void testGetCreatePage() {
		client.get().uri("/events/create_event").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Add Event"));
		});
	}
	@Test
	public void testPostCreatePageNoAUTH() {
		client.post().uri("/events/create_event").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isUnauthorized();		
	}
	
	//@Test
	//@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	//public void testPostCreatePageAUTH() {
	//	client.post().uri("/events/create_event").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isOk()
	//	.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED).expectBody(String.class).consumeWith(res-> {
	//		assertThat(res.getResponseBody(), containsString("Add Event"));
	//	});
	//}
	
	@Test
	public void testGetUpdatePage() {
		event = eventService.findAll().iterator().next();
		assertNotNull(event);		
		String val = event.getName();
		long id = event.getId();
		client.get().uri("/events/event_detail/"+id + "/update_event").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Update Details"));
		});
	}
	@Test
	public void testPostUpdatePageNoAUTH() {
		event = eventService.findAll().iterator().next();
		assertNotNull(event);		
		String val = event.getName();
		long id = event.getId();
		client.post().uri("/events/event_detail/"+id + "/update_event").accept(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus().isUnauthorized();		
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
		client.delete().uri("/events/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
	}
//	@Test
//	@DirtiesContext
//	public void testDeleteEventLogIN() throws Exception {
//		
//		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().delete().uri("/events/event_detail/5").accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("events"));
//		//verify(eventService).deleteById(1L);
//		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
//		//we use minus 1 here to detect wether we succcessfully delete the event
//	}
}
