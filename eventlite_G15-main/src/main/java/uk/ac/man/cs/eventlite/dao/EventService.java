package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event event);

	public Iterable<Event> listEventsByName(String event);

	public void deleteById(long id);
	
	public void deleteAll();
	
	public Event findByName(String name);
	
	public Event findById(long id);
	
	public Iterable<Event> findUpcomingEvents();
	
	public Iterable<Event> findPastEvents();
	
	public Iterable<Event> findNextNEventsAtOneVenue(int n, long venueId);

	void update(Event event);

}
