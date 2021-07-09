
package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import uk.ac.man.cs.eventlite.entities.Venue;


@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private VenueRepository venueRepository;
	
	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll(Sort.by("name"));
	}
	
	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}

	@Override
	public Venue findById(long id) {
		return venueRepository.findById(id);
	}
	
	public void deleteById(long id) {
		venueRepository.deleteById(id);		
	}	
	
	@Override
	public void deleteAll() {
		venueRepository.deleteAll();
		
	}
	
	@Override
	public Iterable<Venue> findByName(String venue){
		return venueRepository.findByNameIgnoreCaseContaining(venue);
	}

	@Override
	public List<Venue> findMostUsedVenues() {
		return venueRepository.findMostUsedVenues();
	
	}

	@Override
	public void update(Venue venue) {
		venueRepository.save(venue);
	}
}
