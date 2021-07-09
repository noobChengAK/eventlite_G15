package uk.ac.man.cs.eventlite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class ApiTest {


	private MockMvc mvc;
	private long id;
	private long venueId;
	
	@Autowired
	VenueService venueService;
	
	@Autowired
	EventService eventService;

	@Autowired
	private WebApplicationContext context;
	
	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		
		Venue venue = new Venue("Fake venue", "Oxford Rd, Manchester M13 9PP", -1, 1000, "000");
		venueService.save(venue);
		venueId = venue.getId();
		
		Event event = new Event();		
		event.setName("Blank Name");
		event.setDate(LocalDate.parse("2100-01-01"));
		event.setTime(LocalTime.parse("00:00"));
		event.setDescription("This is a mock description.");
		event.setVenue(venue);
		eventService.save(event);
		id = event.getId();
	}
	
	@AfterEach
	public void cleanUp() {
		eventService.deleteAll();
		venueService.deleteAll();
	}
	
	@Test
	public void homeApi() throws Exception {
		MvcResult result = mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = (JSONObject) obj.getJSONObject("_links");
		assertThat(links.get("venues")).isInstanceOf(JSONObject.class);
		assertThat(links.get("events")).isInstanceOf(JSONObject.class);
	}
	
	@Test
	public void eventsApi() throws Exception {
		MvcResult result = mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		
		assertThat(obj.get("_embedded")).isInstanceOf(JSONObject.class);
		JSONObject embedded = (JSONObject) obj.getJSONObject("_embedded");
		assertThat(embedded.get("events")).isInstanceOf(JSONArray.class);
		JSONArray events = (JSONArray) embedded.get("events");
		assertThat(events.get(0)).isInstanceOf(JSONObject.class);
		JSONObject event = events.getJSONObject(0);
		assertEvent(event);
		
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = (JSONObject) obj.getJSONObject("_links");
		assertThat(links.get("self")).isInstanceOf(JSONObject.class);
	}
	
	@Test 
	public void eventDetailApi() throws Exception {
		MvcResult result = mvc.perform(get("/api/events/" + id).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		assertEvent(obj);
		
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = obj.getJSONObject("_links");
		assertThat(links.get("self")).isInstanceOf(JSONObject.class);
		assertThat(links.get("venue")).isInstanceOf(JSONObject.class);
	}
	
	@Test
	public void venuesApi() throws Exception {
		MvcResult result = mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		
		assertThat(obj.get("_embedded")).isInstanceOf(JSONObject.class);
		JSONObject embedded = (JSONObject) obj.getJSONObject("_embedded");
		assertThat(embedded.get("venues")).isInstanceOf(JSONArray.class);
		JSONArray venues = (JSONArray) embedded.get("venues");
		assertThat(venues.get(0)).isInstanceOf(JSONObject.class);
		JSONObject venue = venues.getJSONObject(0);
		assertVenue(venue);
		
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = (JSONObject) obj.getJSONObject("_links");
		assertThat(links.get("self")).isInstanceOf(JSONObject.class);
	}
	
	@Test
	public void venueDetailApi() throws Exception {
		MvcResult result = mvc.perform(get("/api/venues/" + venueId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		assertVenue(obj);
		
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = obj.getJSONObject("_links");
		assertThat(links.get("self")).isInstanceOf(JSONObject.class);
		assertThat(links.get("venue")).isInstanceOf(JSONObject.class);
		assertThat(links.get("events")).isInstanceOf(JSONObject.class);
		assertThat(links.get("next3events")).isInstanceOf(JSONObject.class);
	}
	
	@Test
	public void next3EventsApi() throws Exception {
		MvcResult result = mvc.perform(get("/api/venues/" + venueId + "/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		
		String content = result.getResponse().getContentAsString();
		JSONObject obj = new JSONObject(content);
		assertThat(obj.get("_embedded")).isInstanceOf(JSONObject.class);
		JSONObject embedded = (JSONObject) obj.getJSONObject("_embedded");
		assertThat(embedded.get("events")).isInstanceOf(JSONArray.class);
		JSONArray events = (JSONArray) embedded.get("events");
		assertThat(events.get(0)).isInstanceOf(JSONObject.class);
		JSONObject event = events.getJSONObject(0);
		assertEvent(event);
		
		assertThat(obj.get("_links")).isInstanceOf(JSONObject.class);
		JSONObject links = (JSONObject) obj.getJSONObject("_links");
		assertThat(links.get("self")).isInstanceOf(JSONObject.class);
	}
	
	private void assertVenue(JSONObject venue) throws Exception {
		assertThat(venue.get("name")).isEqualTo("Fake venue");
		assertThat(venue.get("address")).isEqualTo("Oxford Rd, Manchester M13 9PP");
		assertThat(venue.get("capacity")).isEqualTo(1000);
		assertThat(venue.get("postcode")).isEqualTo("000");
	}
	
	private void assertEvent(JSONObject event) throws Exception {
		assertThat(event.get("name")).isEqualTo("Blank Name");
		assertThat(event.get("description")).isEqualTo("This is a mock description.");
		assertThat(event.get("date")).isEqualTo("2100-01-01");
		assertThat(event.get("time")).isEqualTo("00:00:00");
		assertThat(((Integer) event.get("id")).longValue()).isEqualTo(id);
	}
}
