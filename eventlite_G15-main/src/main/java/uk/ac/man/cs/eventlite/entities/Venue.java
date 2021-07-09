
package uk.ac.man.cs.eventlite.entities;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

@Entity
@Table(name="venues")
public class Venue {
   
	@Id
	@GeneratedValue
	private long id;

	private String name;

	private int capacity;
	
	private String address;
	
	@OneToMany(mappedBy="venue")
	private List<Event> events = new ArrayList<Event>();
	
	private String postcode;
	private double longitude;
	
	private double latitude;
	
	public Venue() {
	}
	
	public Venue(String name, String address, long id ,int capacity, String postcode) {
		setAddress(address);
		setName(name);
//		setId(id);
		setCapacity(capacity);
		setPostcode(postcode);
		if(address != "" && postcode != "none") {
			setLatLongFromAddress(address, postcode);
		} else {
			setLongitude(0);
			setLatitude(0);
		}
	}
	
	public void setLatLongFromAddress(String address, String postcode) {
		try {
			address += "," + postcode;
			String token = "pk.eyJ1IjoiamFuZ283MDcwNyIsImEiOiJja25nM3dwZ2EwbmdpMnhud2l3NnptdDB2In0.YEBs1Z8MUie4xPW-kDiIGg";
			String[] temp = address.split(" ");
			StringBuffer content = new StringBuffer();
			for(String word : temp) {
				word += ",";
				content.append(word);
			}
			URL url = new URL("https://api.mapbox.com/geocoding/v5/mapbox.places/" + content.toString() + ".json?access_token=" + token);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			int status = con.getResponseCode();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			
			in.close();
			con.disconnect();
			
			JSONObject obj = new JSONObject(content.toString());
			JSONArray features = (JSONArray) obj.get("features");
			JSONObject venue = (JSONObject) features.get(0);
			
			JSONArray position = (JSONArray) venue.get("center");
			setLongitude(position.getDouble(0));
			setLatitude(position.getDouble(1));
			
		} catch(JSONException e) {
			e.printStackTrace();
			setLongitude(0);
			setLatitude(0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public String validate_venue() {
		if(this.name == null || this.name == "" || this.name.length() == 0 || this.name.length() > 256) {
			return "Error: Name of this venue is invalid";
		}
		if(this.capacity < 1) {
			return "Error: Capacity of this venue is invalid";
		}
		if(this.address == null || this.address == "" || this.address.length() > 300 || this.address.length() == 0) {
			return "Error: Address of this venue is invalid";
		}
		if(this.postcode == null || this.postcode == "" || this.postcode.length() != 7) {
			return "Error: Postcode of this venue is invalid";
		}
		
		return "";
	}
}