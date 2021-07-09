
package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import uk.ac.man.cs.eventlite.entities.Venue;


public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);
	
	public Venue findById(long id);

	public void deleteById(long id);
	
	public void deleteAll();

	public Iterable<Venue> findByName(String searchRes);
	
	public List<Venue> findMostUsedVenues();

	public void update(Venue venue);

}
