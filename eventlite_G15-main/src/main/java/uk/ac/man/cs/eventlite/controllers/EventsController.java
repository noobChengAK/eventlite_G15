package uk.ac.man.cs.eventlite.controllers;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.Status;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.dao.VenueService;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {
	
	private String KEY = "TrIp43T3c99GwcAzfvZUEG1XC";
	private String SECRET = "sGkI48RFYH1yfg2VOIT4senJ5Oj5u2kfsdsIoqcQVfhd9VxyE2";
	private String TOKEN = "1384498731735666694-U5O8cFeTSAU7X496N5SHbYkFm7KMeH";
	private String TOKEN_SECRET = "nwNhh5wKErkbyWG9Ir8lGiLdRNbnNKQtdrwa4Sp6FWDl0";
			

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllEvents(Model model) {
		try {
						
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey(KEY)
			  .setOAuthConsumerSecret(SECRET)
			  .setOAuthAccessToken(TOKEN)
			  .setOAuthAccessTokenSecret(TOKEN_SECRET);
			
			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter twitter = tf.getInstance();
			
			List<Status> twitterFeed = twitter.getHomeTimeline();
			TreeMap<String, List<String>> tweets = new TreeMap<String, List<String>>();
			
			
			for (Status t : twitterFeed) {
				List<String> linksAndContent = new ArrayList<String>();
				linksAndContent.add("https://twitter.com/" + t.getUser().getScreenName()+ "/status/" + t.getId());
				linksAndContent.add(t.getText());
				tweets.put(t.getCreatedAt().toString(), linksAndContent);
			}
			model.addAttribute("tweets", tweets);
		}
		catch (TwitterException e) {
			e.printStackTrace();
		}
		Iterable<Event> upcoming_events = eventService.findUpcomingEvents();
		Iterable<Venue> upcoming_events_venues = new ArrayList<Venue>();
		
		for(Event e : upcoming_events) {
			if(!((ArrayList<Venue>) upcoming_events_venues).contains(e.getVenue())) {
				((ArrayList<Venue>) upcoming_events_venues).add(e.getVenue());
			}			
		}
		
		model.addAttribute("events", upcoming_events);
		model.addAttribute("venues_mark", upcoming_events_venues);
		model.addAttribute("past_events", eventService.findPastEvents());
		
		return "events/index";
	}

	
	@GetMapping("/search")
	public String getByName(Model model,
			@RequestParam(value = "nameVal", required = false ,defaultValue = "")String searchRes) {
		
		
		if(!searchRes.isEmpty()) {
			model.addAttribute("events_searched", eventService.listEventsByName(searchRes));
			model.addAttribute("val", searchRes);
		}else {
			return "redirect:/events";
		}
		
		return "events/index";
	}
	
	


	@DeleteMapping("/event_detail/{id}")
	public String deleteEvent(@PathVariable("id") long id) {
		eventService.deleteById(id);		
		return "redirect:/events";
	}

	
	@DeleteMapping("/")
    public String deleteAllEvents() {
        eventService.deleteAll();
        return "redirect:/events";
    }
	
	
	@GetMapping
	@RequestMapping(value = "/event_detail/{id}")
	public String getEventDetail(Model model, @PathVariable("id") long id) {
		
		Event event = eventService.findById(id);
		if(event == null) {
			model.addAttribute("warning", "No event with this id exists");
		}else {
			model.addAttribute("event", event);	
		}		
		return "events/event_detail/index";
	}
	
	@PostMapping("/event_detail/{id}")
	public String tweet(Model model, @PathVariable("id") long id,
			@RequestParam(value = "tweetVal", required = true)String tweetVal) {
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(KEY)
		  .setOAuthConsumerSecret(SECRET)
		  .setOAuthAccessToken(TOKEN)
		  .setOAuthAccessTokenSecret(TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		try {
			if(tweetVal!= "TWITTER TESTING ONLY") {
				Status status = twitter.updateStatus(tweetVal);
				model.addAttribute("tweet", tweetVal);
			}			
		} catch (TwitterException e) {
			model.addAttribute("warning_twitter", "Twitter could not post this tweet :(");
			e.printStackTrace();
		}		
		
		Event event = eventService.findById(id);
		model.addAttribute("event", event);
		return "events/event_detail/index";
	}
	@GetMapping("/create_event")
	public String newEvent(Model model) {
		model.addAttribute("venues", venueService.findAll());
		return "events/create_event";
	}
//
	@PostMapping(path = "/create_event",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(Model model,
			@RequestParam(value = "nameVal", required = true)String name,
			@RequestParam(value = "venueVal", required = true)String venue,
			@RequestParam(value = "dateVal", required = true)String date,
			@RequestParam(value = "timeVal", required = false ,defaultValue = "00:00")String time,
			@RequestParam(value = "desVal", required = true)String description
			){
		
		Event e = new Event();
		e.setName(name);
		try {
			e.setVenue(venueService.findById(Long.parseLong(venue)));
		}catch(Exception ex) {
			e.setVenue(null);
		}
		
		try {
			e.setDate(LocalDate.parse(date));
		}catch(Exception ex) {
			e.setDate(null);
		}
		
		try {
			e.setTime(LocalTime.parse(time));
		}catch(Exception ex) {
			e.setTime(null);
		}
	
		e.setDescription(description);
		
		if (e.validate_event() == "") {
			eventService.save(e);
			return "redirect:/events";
		}else {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("warning", e.validate_event());
			return "events/create_event";
		}	
	}
	
	@GetMapping
	@RequestMapping(value = "/event_detail/{id}/update_event")
	public String getEventDetailUpdate(Model model, @PathVariable("id") long id) {
		
		Event event = eventService.findById(id);
		model.addAttribute("event", event);
		model.addAttribute("venues", venueService.findAll());
		
		return "events/event_detail/update_event";
	}
	
	@PostMapping("/event_detail/{id}/update_event")
	public String updateEvent(Model model, @PathVariable("id") long id,
										   @RequestParam("name") String name,
                                           @RequestParam("description") String description,
                                           @RequestParam("date") String date,
                                           @RequestParam("time") String time,
                                           @RequestParam("venue") String venueId) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
		
		Event event = eventService.findById(id);
		//event.setName(name);
		
		//event.setDate(date3);
		//event.setVenue(venueService.findById(Long.parseLong(venueId)));
		
		event.setName(name);
		event.setDescription(description);
		
		try {
			Date date2 = dateFormat.parse(date);
			LocalDate date3 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			event.setDate(date3);
		} catch (Exception e){
			event.setDate(null);
		}
		try {
			event.setVenue(venueService.findById(Long.parseLong(venueId)));
		} catch (Exception e){
			event.setVenue(null);
		}
			
		
		try {
			Date time2 = timeFormat.parse(time);
			LocalTime time3 = time2.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
			event.setTime(time3);
		} catch (Exception e) {
			event.setTime(null);
		}
		
		if (event.validate_event() == "") {
			eventService.update(event);
			return "redirect:/events/event_detail/{id}";
			//return "redirect:/events";
		}else {
			model.addAttribute("venues", venueService.findAll());
			model.addAttribute("warning", event.validate_event());
			model.addAttribute("event", event);
			return "events/event_detail/update_event";
		}	
		
		//return "events/event_detail/update_event";
		
	}

}
