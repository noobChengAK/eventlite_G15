package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAndTimeAsc();
	}
	

	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	

	@Override
	public Iterable<Event> listEventsByName(String event){
		return eventRepository.findByNameIgnoreCaseContainingOrderByDateAscNameAsc(event);
	}

    @Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}
	
	
	@Override
	public void deleteAll() {
		eventRepository.deleteAll();
		
	}

	@Override
	public Event findByName(String name) {
		return eventRepository.findByName(name);
	}

	@Override
	public Event findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override
	public Iterable<Event> findUpcomingEvents() {
		LocalDate date = LocalDate.now();
		return eventRepository.findAllByDateAfterOrderByDateAscTimeAsc(date);
	}
	
	@Override
	public Iterable<Event> findPastEvents() {
		LocalDate date = LocalDate.now();
		return eventRepository.findAllByDateBeforeOrderByDateAscTimeAsc(date);
	}
	
	@Override
	public Iterable<Event> findNextNEventsAtOneVenue(int n, long venueId) {
		LocalDate date = LocalDate.now();
		Pageable pageable = PageRequest.of(0, n);
		return eventRepository.findTopEventsAtOneVenue(venueId, date, pageable);
	} 

	public void update(Event event) {
		eventRepository.save(event);
	}

}
