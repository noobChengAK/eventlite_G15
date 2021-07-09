package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@GetMapping
	public CollectionModel<Venue> getAllVenues() {

		return venueCollection(venueService.findAll());
	}
	@GetMapping("/{id}")
	public EntityModel<Venue> getSingleVenue(@PathVariable("id") long id) {

		return singleVenue(venueService.findById(id));
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<Event> getNext3EventsForSingle(@PathVariable("id") long id) {
		return eventCollection(eventService.findNextNEventsAtOneVenue(3, id));
	}
	
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id) {
		venueService.deleteById(id);

		return "/venues";
	}

	private EntityModel<Venue> singleVenue(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withRel("venue");
		Link eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("events").withRel("events");
		Link nextEventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("next3events").withRel("next3events");

		return EntityModel.of(venue, selfLink, venueLink, eventsLink, nextEventsLink);
	}

	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();
		Link profileLink = linkTo(methodOn(VenuesControllerApi.class)).slash("../").slash("profile").withRel("profile");
		return CollectionModel.of(venues, selfLink, profileLink);
	}
	private CollectionModel<Event> eventCollection(Iterable<Event> events) {
		Link selfLink = linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel();

		return CollectionModel.of(events, selfLink);
	}
}
