//package uk.ac.man.cs.eventlite;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//import uk.ac.man.cs.eventlite.dao.EventService;
//import uk.ac.man.cs.eventlite.dao.VenueService;
//import uk.ac.man.cs.eventlite.entities.Event;
//import uk.ac.man.cs.eventlite.entities.Venue;
//
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = EventLite.class)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//
//public class EventDetailTest {
//
//	private MockMvc mvc;
//	long id;
//
//	@Autowired
//	private WebApplicationContext context;
//	
//	@Autowired
//	private EventService eventService;
//	
//	@Autowired
//	private VenueService venueService;
//
//	@BeforeEach
//	public void setup() {
//		mvc = MockMvcBuilders.webAppContextSetup(context).build();
//		
//		Venue venue = new Venue("fake Venue", "23 Manchester Road", 2L, 50,  "E14 3BD");
//		venueService.save(venue);
//		
//		Event event = new Event();		
//		event.setName("Blank Name");
//		event.setDate(LocalDate.parse("2000-01-01"));
//		event.setTime(LocalTime.parse("00:00"));
//		event.setDescription("This is a mock description.");
//		event.setVenue(venue);
//		eventService.save(event);
//		id = event.getId();
//	}
//	
//	@AfterEach
//	public void cleanup() {
//		eventService.deleteAll();
//		venueService.deleteAll();
//	}
//
//	@Test
//	public void getRoot() throws Exception {
//		mvc.perform(get("/events/event_detail/"+ id).accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
//	}
//
//	@Test
//	public void getJsonRoot() throws Exception {
//		mvc.perform(get("/events/event_detail/" + id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotAcceptable());
//	}
//
////	@Test
////	public void getApiRoot() throws Exception {
////		mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
////	}
//	
//	@Test
//	public void testPageContent() throws Exception {
//		MvcResult page = mvc.perform(get("/events/event_detail/" + id).accept(MediaType.TEXT_HTML)).andReturn();
//		assertThat(page.getResponse().getContentAsString()).contains("Blank Name");
//		assertThat(page.getResponse().getContentAsString()).contains("2000-01-01");
//		assertThat(page.getResponse().getContentAsString()).contains("00:00");
//		assertThat(page.getResponse().getContentAsString()).contains("This is a mock description.");
//	}
//}
//
//
