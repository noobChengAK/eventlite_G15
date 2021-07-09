package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

public interface VenueRepository extends CrudRepository<Venue, Long>{
	
	public Iterable<Venue> findByNameIgnoreCaseContaining(String name);
	
	Venue findById(long id);
	
	@Query("select v from Venue v, Event e where v.id = e.venue group by v.id order by count(e.id) desc")
	public List<Venue> findMostUsedVenues();
	public Iterable<Venue> findAll(Sort sort);
	
     
	
	
}
