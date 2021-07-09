
package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.ac.man.cs.eventlite.entities.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
   
	
	@Query(value="select e from Event e order by date, time")
	Iterable<Event> findAllByOrderByDateAndTimeAsc();
	Iterable<Event> findByNameIgnoreCaseContainingOrderByDateAscNameAsc(String event);
	//public Event createEvent(Event event);
    @Query(value = "select e from Event e where LOWER(e.name) like LOWER(concat('%', keyword, '%'))", nativeQuery = true)
    Iterable<Event> findByKeyword(@Param("keyword") String keyword);
	
    @Query(value = "SELECT e FROM Event e WHERE e.venue.id = :venueId AND e.date >= :currDate ORDER BY date, time")
    Page<Event> findTopEventsAtOneVenue(@Param("venueId") long venueId, @Param("currDate") LocalDate currDate, Pageable pageable);
    
    Event findByName(String name);
	
	Event findById(long id);
	
	public Iterable<Event> findAllByDateAfterOrderByDateAscTimeAsc(LocalDate date);
	public Iterable<Event> findAllByDateBeforeOrderByDateAscTimeAsc(LocalDate date);
	

}
