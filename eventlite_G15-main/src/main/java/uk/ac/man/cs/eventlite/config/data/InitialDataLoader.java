package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

 
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0 && venueService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}

		Venue venue =  new Venue("Kilburn, G23", "Oxford Road, Manchester", 1L, 80,"M13 OHL");
		Venue venue1 = new Venue("Online", "23 Manchester Road", 2L, 50,  "E14 3BD");
		Venue venue2 = new Venue("Highland", "Highland Road", 2L, 1000,  "S43 2EZ");
		Venue venue3 = new Venue("Acacia Avenue", "19 Acacia Avenue", 1L, 10,  "WA15 8QY");
		
		venue.setLatLongFromAddress(venue.getAddress(), venue.getPostcode());
		venue1.setLatLongFromAddress(venue.getAddress(), venue.getPostcode());
		venue2.setLatLongFromAddress(venue.getAddress(), venue.getPostcode());
		venue3.setLatLongFromAddress(venue.getAddress(), venue.getPostcode());
        venueService.save(venue);

	    venueService.save(venue1);
	    
	    venueService.save(venue2);
	    venueService.save(venue3);
	    


	    eventService.save(new Event("COMP23412 Showcase, group G", 1, "2021-05-13", "16:00", "", 1L, venueService));
		
	    eventService.save(new Event("COMP23412 Showcase, group H", 2, "2021-05-11", "11:00", "", 2L, venueService));
		
	    eventService.save(new Event("COMP23412 Showcase, group F", 3, "2021-05-10", "16:00", "", 2L, venueService));
		
	    eventService.save(new Event("COMP23412 Showcase, group Q", 4, "2021-05-10", "15:00", "", 2L, venueService));

	}
	
	
}
