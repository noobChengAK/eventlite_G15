package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService venueService;

	@GetMapping
	public String getAllVenues(Model model) {

		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}

	
	@GetMapping("/search")
	public String getByName(Model model,
			@RequestParam(value = "nameVal", required = false ,defaultValue = "")String searchRes) {		
		
		
		if(!searchRes.isEmpty()) {
			model.addAttribute("venues", venueService.findByName(searchRes));
			model.addAttribute("val", searchRes);
		}else {
			return "redirect:/venues";
		}		
		
		return "venues/index";
	}
	
	
	@DeleteMapping("/venues_detail/{id}")
	public String deleteVenue(Model model, @PathVariable("id") long id) {
		if(venueService.findById(id).getEvents().isEmpty()) {
			venueService.deleteById(id);		
			return "redirect:/venues";
		}else {
			model.addAttribute("venue", venueService.findById(id));
			model.addAttribute("warning", "This venue cannot be deleted, some events take place at this venue");
			return "venues/venues_detail/index";
		}		
	}
	
	@DeleteMapping("/")
    public String deleteAllVenues() {
        venueService.deleteAll();
        return "redirect:/venues";
    }

	@GetMapping
	@RequestMapping(value = "/venues_detail/{id}")
	public String getVenueDetail(Model model, @PathVariable("id") long id) {
		
		Venue venue = venueService.findById(id);
		
		if(venue == null) {
			model.addAttribute("error", "No venue with this id exists");
		}else {
			model.addAttribute("venue", venue);	
		}		
		return "venues/venues_detail/index";
	}
	
	@GetMapping("/create_venue")
	public String newEvent(Model model) {
		return "venues/create_venue";
	}
	@PostMapping(path = "/create_venue",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(Model model,
			@RequestParam(value = "nameVal", required = true)String name,
			@RequestParam(value = "capacityVal", required = true)String capacity,
			@RequestParam(value = "addressVal", required = true)String address,
			@RequestParam(value = "postcodeVal", required = true)String postcode) {
		
			Venue v = new Venue();
					
			v.setName(name);
			try {
				v.setCapacity(Integer.parseInt(capacity));
			}catch(Exception ex) {
				v.setCapacity(0);
			}
			
			v.setAddress(address);
			v.setPostcode(postcode);
			

			if(v.validate_venue() == "") {
				v.setLatLongFromAddress(address, postcode);
				venueService.save(v);			
				return "redirect:/venues";
			}else {
				model.addAttribute("warning", v.validate_venue());
				return "venues/create_venue";
			}
	}
	
	@GetMapping
	@RequestMapping(value = "/venue_detail/{id}/update_venue")
	public String getVenueDetailUpdate(Model model, @PathVariable("id") long id) {
		
		Venue venue = venueService.findById(id);
		model.addAttribute("venue", venue);
		
		return "venues/venues_detail/update_venue";
	}
	
	@PostMapping("/venue_detail/{id}/update_venue")
	public String updateVenue(Model model, @PathVariable("id") long id,
										   @RequestParam("name") String name,
                                           @RequestParam("address") String address,
                                           @RequestParam("postcode") String postcode,
                                           @RequestParam("capacity") String capacity) throws Exception {
		
		Venue venue = venueService.findById(id);
		
		venue.setName(name);
		venue.setAddress(address);
		venue.setPostcode(postcode);
		try {
			venue.setCapacity(Integer.parseInt(capacity));
		}catch (Exception e) {
			venue.setCapacity(0);
		}
		
//		venue.setName(name);
//		venue.setAddress(address);
//		venue.setPostcode(postcode);
//		venue.setCapacity(capacity);
		
		if(venue.validate_venue() == "") {
			venueService.update(venue);
			return "redirect:/venues/venues_detail/{id}";
		}
		else {
			model.addAttribute("warning", venue.validate_venue());
			model.addAttribute("venue", venue);
			return "venues/venues_detail/update_venue";
		}
			
		
		
	}



}
