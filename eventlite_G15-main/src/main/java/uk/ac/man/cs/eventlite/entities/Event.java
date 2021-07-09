package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.ac.man.cs.eventlite.dao.VenueService;


@Entity
@Table(name="events")
public class Event {

	@Id()
	@GeneratedValue
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	private String name;

	@JsonIgnore
	@ManyToOne
	private Venue venue;
	
	private String description;

	public Event() {
	}
	
	public Event(String name, long id, String date, String time, String description, long venueId, VenueService venueService) {
		//setId(id);
		setName(name);
		setDate(LocalDate.parse(date));
        setTime(LocalTime.parse(time));
        setDescription(description);
        setVenue(venueService.findById((long) venueId));
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String validate_event() {
		if(this.name == null || this.name == "" || this.name.length() == 0 || this.name.length() > 256) {
			return "Error: Name of this event is invalid";
		}
		if(this.venue == null) {
			return "Error: Venue of this event is invalid";
		}
		if(this.date == null || this.date.isBefore(LocalDate.now().plusDays(1))) {
			return "Error: Date of this event is invalid";
		}
		//if(this.time == null) {
		//	return "Error: Time of this event is invalid";
		//}
		if(this.description.length() > 500) {
			return "Error: Description of this event is invalid";
		}
		
		return "";
	}
}
