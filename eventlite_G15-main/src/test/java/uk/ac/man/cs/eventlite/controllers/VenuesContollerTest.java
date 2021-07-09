package uk.ac.man.cs.eventlite.controllers;

import com.mapbox.geojson.Point;
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
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)

public class VenuesContollerTest {
	
	public static final String ADMIN_USER = "Mustafa";
	public static final String ADMIN_ROLE = uk.ac.man.cs.eventlite.config.Security.ADMIN_ROLE;
	
	
	 @Autowired
	    private MockMvc mvc;

	    @Mock
	    private Venue venue;

	    @MockBean
	    private VenueService venueService;
	    
	    @Test
	    public void getAllVenues() throws Exception {
	    	List<Venue> venues = new ArrayList<Venue>();
	    	when(venueService.findAll()).thenReturn(venues);
	    	mvc.perform(get("/venues")
	    			.accept(MediaType.TEXT_HTML))
	    			.andExpect(status().isOk())
	    			.andExpect(view().name("venues/index"));
	    }
	    @Test
	    public void getOneVenue() throws Exception {
	    	when(venueService.findById(9)).thenReturn(venue);
	    	mvc.perform(get("/venues/venues_detail/9")
	    			.accept(MediaType.TEXT_HTML))
	    			.andExpect(status().isOk())
	    			.andExpect(view().name("venues/venues_detail/index"));
	    }
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testGetCreateEventPage() throws Exception {
			mvc.perform(get("/venues/create_venue")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().is(200))
					.andExpect(view().name("venues/create_venue"));
		}
		@Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testGetUpdateVenuePage() throws Exception {
			when(venueService.findById(1)).thenReturn(venue);
			mvc.perform(get("/venues/venue_detail/1/update_venue")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().is(200))
					.andExpect(view().name("venues/venues_detail/update_venue"));
		}
	    @Test
		@WithMockUser(username = "user", roles = {""})
		public void testAddWithNoAUTH() throws Exception {

			mvc.perform(post("/venues/create_venue/")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().isForbidden());	 
			}
	    
	    @Test
		@WithMockUser(username = "user", roles = {""})
		public void testUpdateWithNoAUTH() throws Exception {

	    	long id = 1L;
	    	mvc.perform(post("/venues/venues_detail/{id}/update_venue", id)
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().isForbidden());	 
			}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithCorrectValues() throws Exception {
			ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);

			mvc.perform(post("/venues/create_venue/")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13 OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(view().name("redirect:/venues"))					
					.andExpect(handler().methodName("createVenue"));

			verify(venueService).save(arg.capture());
			assertThat("Test venue", equalTo(arg.getValue().getName()));
		 }
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithCorrectValues() throws Exception {
			venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "Test venue")
			        .param("capacity", "100")
			        .param("address",  "Oxford Road, Manchester")
			        .param("postcode", "M13 OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().hasNoErrors())
					.andExpect(status().is3xxRedirection())
					.andExpect(view().name("redirect:/venues/venues_detail/{id}"))					
					.andExpect(handler().methodName("updateVenue"))
					;
			verify(venueService).update(arg.capture());
			assertThat("Test venue", equalTo(arg.getValue().getName()));
			assertThat(100, equalTo(arg.getValue().getCapacity()));
			assertThat("Oxford Road, Manchester", equalTo(arg.getValue().getAddress()));
			assertThat("M13 OHL", equalTo(arg.getValue().getPostcode()));
			
		 }
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithMissingValues() throws Exception {		
			//all missing
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "")
			        .param("capacityVal", "")
			        .param("addressVal",  "")
			        .param("postcodeVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(status().is(200)) 
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithMissingValues() throws Exception {		
			//all missing
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "")
			        .param("capacity", "")
			        .param("address",  "")
			        .param("postcode", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
	    }
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithMissingName() throws Exception {		
			//name missing
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithMissingName() throws Exception {		
			//name missing
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "")
			        .param("capacity", "100")
			        .param("address",  "Oxford Road, Manchester")
			        .param("postcode", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithMissingCapacity() throws Exception {		
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test Venue")
			        .param("capacityVal", "")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithMissingCapacity() throws Exception {		
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "asd")
			        .param("capacity", "")
			        .param("address",  "sad")
			        .param("postcode", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithMissingAddress() throws Exception {	
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Address of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithMissingAddress() throws Exception {	
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "asd")
			        .param("capacity", "23")
			        .param("address",  "")
			        .param("postcode", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Address of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithMissingPostcode() throws Exception {		
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithMissingPostcode() throws Exception {		
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "asd")
			        .param("capacity", "23")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithBadName() throws Exception {	
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "This name is way too long 8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13 OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithBadName() throws Exception {	
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name is way too long 88888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
			        .param("capacity", "23")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "M15 6AZ")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Name of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithBadCapacity() throws Exception {	
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "0")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
			
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "-1000")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "1.5")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithBadCapacity() throws Exception {	
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "-1")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "123456")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"));
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "0")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "123456")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"));
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "1.5")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "123456")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Capacity of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithBadAddress() throws Exception {	
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "This address is way too long 888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888")
			        .param("postcodeVal", "M13OHL")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Address of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithBadAddress() throws Exception {	
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "23")
			        .param("address",  "Vasile Aaron 30aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
			        .param("postcode", "123456")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Address of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    @Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testAddWithBadPostCode() throws Exception {	
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "M")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
			mvc.perform(post("/venues/create_venue")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("nameVal", "Test venue")
			        .param("capacityVal", "100")
			        .param("addressVal",  "Oxford Road, Manchester")
			        .param("postcodeVal", "0123456789")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(status().is(200)) //did not add venue, stay on page
					.andExpect(view().name("venues/create_venue"));
		}
	    
	    @Test
		public void testSearch() throws Exception {
			List<Venue> venues = new ArrayList<Venue>();
			venues.add(new Venue("test Venue", "Oxford Road, Manchester", 1L, 80,"M13 OHL"));
			when(venueService.findByName("test")).thenReturn(venues);
			
			mvc.perform(get("/venues/search?nameVal=test")
					.accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("val", "test"))
					.andExpect(model().attribute("venues", venues))
					.andExpect(handler().methodName("getByName"))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("venues/index"));
			
			verify(venueService).findByName("test");
		}
		
		@Test
		public void testEmptySearch() throws Exception {		
			mvc.perform(get("/venues/search?nameVal=")
					.accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(status().is3xxRedirection())
					.andExpect(handler().methodName("getByName"))
					.andExpect(view().name("redirect:/venues"));	
		}
		
		@Test
		public void testNotExistingSearch() throws Exception {	
			when(venueService.findByName("ThisVenueNameDoesNotExist")).thenReturn(Collections.<Venue>emptyList());
			
			mvc.perform(get("/venues/search?nameVal=ThisVenueNameDoesNotExist")
					.accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("val", "ThisVenueNameDoesNotExist"))
					.andExpect(model().size(2))
					.andExpect(model().attributeExists("venues")) 
					.andExpect(handler().methodName("getByName"))
					.andExpect(status().is(200)) //did not add event, stay on page
					.andExpect(view().name("venues/index"));
			
			verify(venueService).findByName("ThisVenueNameDoesNotExist");	
			assertThat(venueService.findByName("ThisVenueNameDoesNotExist"), equalTo(new ArrayList<Venue>())) ;
		}
		
		@Test
		@WithMockUser(username = ADMIN_USER, roles = ADMIN_ROLE)
		public void testUpdateWithBadPostCode() throws Exception {	
	    	venue = new Venue();
			when(venueService.findById(1)).thenReturn(venue);
			long id = 1L;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "23")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "1234")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
			
			mvc.perform(post("/venues/venue_detail/{id}/update_venue",id)
					.param("name", "This name")
			        .param("capacity", "23")
			        .param("address",  "Vasile Aaron 30")
			        .param("postcode", "12345126")
			        .accept(MediaType.TEXT_HTML)
					.with(csrf()))
					.andExpect(model().attribute("warning", equalTo("Error: Postcode of this venue is invalid")))
					.andExpect(view().name("venues/venues_detail/update_venue"))					
					.andExpect(handler().methodName("updateVenue"))
					;
		}
	    
	    
	    @Test
	    @WithMockUser(username = "user", roles = {""})
	    public void testDeleteAllIsForbiddenWithoutUserAuth() throws Exception {
	        mvc.perform(delete("/venues/")
	                .accept(MediaType.TEXT_HTML)
	                .with(csrf()))
	                .andExpect(status().isForbidden());
	        
	    }
	    
	    @Test
	    @WithMockUser(username = "user", roles = {""})
	    public void testDeleteIsForbiddenWithoutUserAuth() throws Exception {
	        long id = 1L;

	        mvc.perform(delete("/venues/venues_detail/{id}", id)
	                .accept(MediaType.TEXT_HTML)
	                .with(csrf()))
	                .andExpect(status().isForbidden());
	        verify(venueService, times(0)).deleteById(1L);
	    }    

	    @Test
	    @WithMockUser(username = "user", roles = {"ADMINISTRATOR"})
	    public void testDeleteFreeVenue() throws Exception {
	    	when(venueService.findById(10)).thenReturn(venue);

	    	//delete a mock venue that is not connected to an event
	    	mvc.perform(delete("/venues/venues_detail/10")	    		
	    		.accept(MediaType.TEXT_HTML)
	    		.with(csrf()))
            	.andExpect(status().is3xxRedirection())
            	.andExpect(view().name("redirect:/venues"))
            	.andExpect(handler().methodName("deleteVenue"));
	    	
	    	verify(venueService).deleteById(10);
	    }
	    @Test
	    @WithMockUser(username = "user", roles = {"ADMINISTRATOR"})
	    public void testDeleteBusyVenue() throws Exception {	    	
	    	//delete a venue that has an event --this is not allowed
	    	when(venueService.findById(11)).thenReturn(venue);	
	    	List<Event> events = new ArrayList<Event>();
	    	events.add(new Event("COMP23412 Showcase, group G", 1, "2021-05-13", "16:00", "", 11L, venueService));
	    	when(venue.getEvents()).thenReturn(events);
	    	
	    	mvc.perform(delete("/venues/venues_detail/11")		    		
	    		.accept(MediaType.TEXT_HTML)
	    		.with(csrf()))
            	.andExpect(status().is2xxSuccessful())
            	.andExpect(view().name("venues/venues_detail/index"))
            	.andExpect(model().attribute("warning", "This venue cannot be deleted, some events take place at this venue"))
            	.andExpect(handler().methodName("deleteVenue"));
	    	
	    	verify(venueService, times(0)).deleteById(11);
        }
	    
	    
	    @Test
	    @WithMockUser(username = "user", roles = {"ADMINISTRATOR"})
	    public void testDeleteAll() throws Exception {
	        mvc.perform(delete("/venues/")
	                .accept(MediaType.TEXT_HTML)
	                .with(csrf()))
	                .andExpect(status().is3xxRedirection())
	                .andExpect(view().name("redirect:/venues"))
	                .andExpect(handler().methodName("deleteAllVenues"));	    
	    }
	    
	   
	  
	    @Test
	    public void testCreateVenueNoAuthFails() throws Exception {
	        when(venueService.findById(1)).thenReturn(venue);

	        mvc.perform(post("/venues/create_venue").contentType(MediaType.APPLICATION_FORM_URLENCODED)
	                .param("venue Name", "10", "Road Name", "M13 0HL").accept(MediaType.TEXT_HTML).with(csrf()))
	                .andExpect(status().isFound()).andExpect(header().string("Location", endsWith("/sign-in")));

	        
	    }
	   
}
