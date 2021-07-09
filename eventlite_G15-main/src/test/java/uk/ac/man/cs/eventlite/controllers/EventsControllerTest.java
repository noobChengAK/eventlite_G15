package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	public static final String ADMIN_USER = "Mustafa";
	public static final String ADMIN_ROLE = uk.ac.man.cs.eventlite.config.Security.ADMIN_ROLE;
	
	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		//verify(eventService).findAll();
		//verifyNoInteractions(event);
		//verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		Venue v = new Venue();
		v.setId(1);
		when(event.getVenue()).thenReturn(v);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		//verify(eventService).findAll();
	}
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testGetCreateEventPage() throws Exception {
		mvc.perform(get("/events/create_event/")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
			.andExpect(model().attributeExists("venues"))
				.andExpect(status().is(200))
				.andExpect(view().name("events/create_event"));
	}
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testGetUpdateEventPage() throws Exception {
		when(eventService.findById(1)).thenReturn(event);
		mvc.perform(get("/events/event_detail/1/update_event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attributeExists("venues"))
				.andExpect(model().attributeExists("event"))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"));
	}
	@Test
    public void getAllEvents() throws Exception {
    	List<Event> events = new ArrayList<Event>();
    	when(eventService.findUpcomingEvents()).thenReturn(events);
    	when(eventService.findPastEvents()).thenReturn(events);
    	mvc.perform(get("/events")
    			.accept(MediaType.TEXT_HTML))
    			.andExpect(status().isOk())
    			.andExpect(view().name("events/index"));
    }
	
	@Test
    public void getOneEvent() throws Exception {
		event = new Event();
		venue = new Venue();
		event.setVenue(venue);
    	when(eventService.findById(10)).thenReturn(event);
    	mvc.perform(get("/events/event_detail/10")
    			.accept(MediaType.TEXT_HTML))
    			.andExpect(status().isOk())
    			.andExpect(model().attribute("event", equalTo(event)))
    			.andExpect(view().name("events/event_detail/index"));
    }
	@Test
    public void postTweetOnEventNoAUTH() throws Exception {
		event = new Event();
		venue = new Venue();
		event.setVenue(venue);
    	when(eventService.findById(10)).thenReturn(event);
    	mvc.perform(post("/events/event_detail/10")
    			.accept(MediaType.TEXT_HTML))
    			.andExpect(status().isForbidden());
    }
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
    public void postTweetOnEvent() throws Exception {
		event = new Event();
		venue = new Venue();
		event.setVenue(venue);
    	when(eventService.findById(10)).thenReturn(event);
    	mvc.perform(post("/events/event_detail/10")
    			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    			.param("tweetVal", "TWITTER TESTING ONLY")
    			.with(csrf()))
    			.andExpect(status().isOk())
    			.andExpect(model().attribute("event", equalTo(event)))
    			.andExpect(view().name("events/event_detail/index"));
    }
	
	@Test
	@WithMockUser(username = "user", roles = {""})
	public void testAddWithNoAUTH() throws Exception {

		mvc.perform(post("/events/create_event/")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isForbidden());	 
		}
	
	@Test
	@WithMockUser(username = "user", roles = {""})
	public void testUpdateWithNoAUTH() throws Exception {

		mvc.perform(post("/events/update_event/")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isForbidden());	 
		}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithCorrectValues() throws Exception {
		when(venueService.findById(1)).thenReturn(venue);
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		mvc.perform(post("/events/create_event/")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("nameVal", "Test event")
		        .param("venueVal", "1")
		        .param("dateVal",  "2022-05-13")
		        .param("timeVal", "12:00")
		        .param("desVal", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().hasNoErrors())
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/events"))					
				.andExpect(handler().methodName("createEvent"));

		verify(eventService).save(arg.capture());
		assertThat("Test event", equalTo(arg.getValue().getName()));
	 }
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithCorrectValues() throws Exception {
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "Test event")
		        .param("venue", "1")
		        .param("date",  "2022-05-13")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().hasNoErrors())
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/events/event_detail/{id}"))					
				.andExpect(handler().methodName("updateEvent"))
				;
		verify(eventService).update(arg.capture());
		assertThat("Test event", equalTo(arg.getValue().getName()));
		assertThat(id, equalTo(arg.getValue().getVenue().getId()));
		assertThat("2022-05-13", equalTo(arg.getValue().getDate().toString()));
		assertThat("06:00", equalTo(arg.getValue().getTime().toString()));
		assertThat("This is a test event", equalTo(arg.getValue().getDescription()));
	 }
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingValues() throws Exception {		
		//all missing
		//event = new Event();
		mvc.perform(post("/events/create_event/")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("nameVal", "")
				.param("venueVal", "")
		        .param("dateVal",  "")
		        .param("timeVal", "")
		        .param("desVal", "")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
				.andExpect(status().is(200)) //did not add event, stay on page
				.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingValues() throws Exception {		
		//all missing
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "")
		        .param("venue", "")
		        .param("date",  "")
		        .param("time", "")
		        .param("description", "")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingName() throws Exception {	
		//missing name
		when(venueService.findById(1)).thenReturn(venue);
		mvc.perform(post("/events/create_event/")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("nameVal", "")
				.param("venueVal", "1")
		        .param("dateVal",  "2022-05-13")
		        .param("timeVal", "12:00")
		        .param("desVal", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
				.andExpect(status().is(200)) //did not add event, stay on page
				.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingName() throws Exception {	
		//missing name
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "")
		        .param("venue", "1")
		        .param("date",  "2022-05-13")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingVenue() throws Exception {	
		//missing venue
		mvc.perform(post("/events/create_event/")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("nameVal", "Test event")
				.param("venueVal", "")
		        .param("dateVal",  "2022-05-13")
		        .param("timeVal", "12:00")
		        .param("desVal", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
				.andExpect(status().is(200)) //did not add event, stay on page
				.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingVenue() throws Exception {	
		//missing venue
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "ALA")
		        .param("venue", "")
		        .param("date",  "2022-05-13")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingDate() throws Exception {	
		when(venueService.findById(1)).thenReturn(venue);
		//missing date
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "1")
			        .param("dateVal",  "")
			        .param("timeVal", "12:00")
			        .param("desVal", "This is a test event")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Date of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingDate() throws Exception {	
		//missing venue
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "ALA")
		        .param("venue", "1")
		        .param("date",  "")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Date of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingTime() throws Exception {	
			//missing time -- this should work
		when(venueService.findById(1)).thenReturn(venue);
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "1")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "")
			        .param("desVal", "This is a test event")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().hasNoErrors())
					.andExpect(status().is3xxRedirection())
					.andExpect(view().name("redirect:/events"))					
					.andExpect(handler().methodName("createEvent"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingTime() throws Exception {	
		//missing venue
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "ALA")
		        .param("venue", "1")
		        .param("date",  "2022-05-13")
		        .param("time", "")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().hasNoErrors())
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/events/event_detail/{id}"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithMissingDescription() throws Exception {	
		when(venueService.findById(1)).thenReturn(venue);
		//missing date
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "1")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "12:00")
			        .param("desVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().is(302)) //added event, redirect
					.andExpect(view().name("redirect:/events"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithMissingDescription() throws Exception {	
		//missing venue
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "ALA")
		        .param("venue", "1")
		        .param("date",  "2022-05-13")
		        .param("time", "10:34")
		        .param("description", "")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().hasNoErrors())
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/events/event_detail/{id}"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithBadName() throws Exception {	
		when(venueService.findById(1)).thenReturn(venue);
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Bad name with too many characters 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
					.param("venueVal", "1")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "12:00")
			        .param("desVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithBadName() throws Exception {	
		//missing name
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "Bad name with too many characters 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
		        .param("venue", "1")
		        .param("date",  "2022-05-13")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Name of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithBadVenue() throws Exception {	
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "-1.5")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "12:00")
			        .param("desVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testUpdateWithBadVenue() throws Exception {	
		event = new Event();
		venue = new Venue();
		venue.setId(1);
		venueService.save(venue);
		when(venueService.findById(1)).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(event);
		long id = 1L;
		
		mvc.perform(post("/events/event_detail/{id}/update_event",id)
				.param("name", "A")
		        .param("venue", "-1.34")
		        .param("date",  "2022-05-13")
		        .param("time", "06:00")
		        .param("description", "This is a test event")
		        .accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
				.andExpect(status().is(200))
				.andExpect(view().name("events/event_detail/update_event"))					
				.andExpect(handler().methodName("updateEvent"))
				;
	}
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithBadTime() throws Exception {	
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "-1.5")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "25:62")
			        .param("desVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test event")
					.param("venueVal", "-1.5")
			        .param("dateVal",  "2022-05-13")
			        .param("timeVal", "Not a valid time")
			        .param("desVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Venue of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithBadDate() throws Exception {	
		//wrong date format
		when(venueService.findById(1)).thenReturn(venue);
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "test event")
					.param("venueVal", "1")
			        .param("dateVal",  "bad date here")
			        .param("timeVal", "12:00")
			        .param("desVal", "test")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Date of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
		//past date, not in future
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "test event")
					.param("venueVal", "1")
			        .param("dateVal",  "1990-05-05")
			        .param("timeVal", "12:00")
			        .param("desVal", "test")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Date of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
	public void testAddWithBadDescription() throws Exception {	
		//wrong date format
		when(venueService.findById(1)).thenReturn(venue);
			mvc.perform(post("/events/create_event/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "test event")
					.param("venueVal", "1")
			        .param("dateVal",  "2022-05-05")
			        .param("timeVal", "12:00")
			        .param("desVal", "This description is way too long 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Description of this event is invalid")))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("events/create_event"));
	}
	
	@Test
	public void testSearch() throws Exception {
		List<Event> events = new ArrayList<Event>();
		when(venueService.findById(11)).thenReturn(venue);
		events.add(new Event("test event", 1, "2022-05-13", "16:00", "", 11, venueService));
		when(eventService.listEventsByName("test")).thenReturn(events);
		
		mvc.perform(get("/events/search?nameVal=test")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("val", "test"))
				.andExpect(model().attribute("events_searched", events))
				.andExpect(handler().methodName("getByName"))
				.andExpect(status().is(200)) //did not add event, stay on page
				.andExpect(view().name("events/index"));
		
		verify(eventService).listEventsByName("test");
	}
	
	@Test
	public void testEmptySearch() throws Exception {		
		mvc.perform(get("/events/search?nameVal=")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(handler().methodName("getByName"))
				.andExpect(view().name("redirect:/events"));	
	}
	
	@Test
	public void testNotExistingSearch() throws Exception {	
		when(eventService.listEventsByName("ThisEventNameDoesNotExist")).thenReturn(Collections.<Event>emptyList());
		
		mvc.perform(get("/events/search?nameVal=ThisEventNameDoesNotExist")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(model().attribute("val", "ThisEventNameDoesNotExist"))
				.andExpect(model().size(2))
				.andExpect(model().attributeExists("events_searched")) 
				.andExpect(handler().methodName("getByName"))
				.andExpect(status().is(200)) //did not add event, stay on page
				.andExpect(view().name("events/index"));
		
		verify(eventService).listEventsByName("ThisEventNameDoesNotExist");	
		assertThat(eventService.listEventsByName("ThisEventNameDoesNotExist"), equalTo(new ArrayList<Event>())) ;
	}
    
	@Test
    @WithMockUser(username = "user", roles = {""})
    public void testDeleteIsForbiddenWithoutUserAuth() throws Exception {
        long id = 1L;

        mvc.perform(delete("/events/{id}", id)
                .accept(MediaType.TEXT_HTML)
                .with(csrf()))
                .andExpect(status().isForbidden());
        

		verify(eventService, times(0)).deleteById(id);
    }
    
    @Test
	@WithMockUser(username = "user", roles = {""})
	public void testDeleteAllIsForbiddenWithoutUserAuth() throws Exception {
		mvc.perform(delete("/events/")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isForbidden());    
    }
    
    @Test
	@WithMockUser(username = "user", roles = {"ADMINISTRATOR"})
	public void testDeleteAll() throws Exception {
		mvc.perform(delete("/events/")
				.accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/events"))
				.andExpect(handler().methodName("deleteAllEvents"));
	}
   
    
    @Test
    @WithMockUser(username = "user", roles = {"ADMINISTRATOR"})
    public void testDelete() throws Exception {
        long id = 1L;

        mvc.perform(delete("/events/event_detail/{id}", id)
                .accept(MediaType.TEXT_HTML)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/events"))
                .andExpect(handler().methodName("deleteEvent"));
    
        verify(eventService).deleteById(id);
    }
    @Test
	public void getEventPageWhenNoEvent() throws Exception {
    	when(venueService.findById(1)).thenReturn(venue);

		mvc.perform(get("/events/1").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());

		
	}

}
