package com.ticketflex.android.Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import com.ticketflex.android.Exception.JSONResponseException;

public class Event extends Model {
	private Integer id = null;
	private String name = null;
	private String description = null;
	private String location = null;
	private Date startTime = null;
	private Date endTime = null;
	private Double price = null;
	private Integer capacity = null;
	private String imageUrl = null;
	private Bitmap image = null;
	private Ticket myTicket = null; // Holds the current user's ticket for this event (if they have one).
	private Ticket[] tickets = null; // Holds all tickets for this event.
	private Integer creatorID = null;
	// Number of tickets that are active for this event - tickets that haven't been admitted nor are put up on sale.
	private Integer numActiveTickets = null;
	// Number of tickets that are put up for sale + the amount of new tickets that can be created.
	private Integer numRemainingTickets = null;
	
	// Default thumbnail width and height used when resizing full size images down to thumbnail sized images.
	// These thumbnails will get displayed on any screen that lists events, such as the See All Events screen.
	public static int thumbnailWidth = 150; 
	public static int thumbnailHeight = 150;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public void setStartTime(String startTime) throws ParseException {
		this.startTime = Model.serverDateToDate(startTime);
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setEndTime(String endTime) throws ParseException {
		this.endTime = Model.serverDateToDate(endTime);
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public String getImageUrl(){
		return this.imageUrl;
	}
	
	public void setImageUrl(String imageUrl){
		this.imageUrl = imageUrl;
	}
	
	/**
	 * Wrapper method to simply fetch the full size event image.
	 * @return Bitmap
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Bitmap getImageFromServer() throws ClientProtocolException, IOException{
		/*String imageUrl = getImageUrl();
		if (imageUrl != null){
			HttpResponse response = request(new HttpGet(imageUrl));
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200){
				byte[] bytes = EntityUtils.toByteArray(response.getEntity());
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
		}
		return null;*/
		return getImageFromServer(null, null);
	}

	/**
	 * Fetch the event image from the server. You can provide width and height resizing parameters to the server as
	 * well.
	 * You can set the resulting image to this Event by using setImage(getImageFromServer()).
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Bitmap getImageFromServer(Integer width, Integer height) throws ClientProtocolException, IOException{
		String imageUrl = getImageUrl();
		if (imageUrl != null){
			if ((width != null && width > 0) || (height != null && height > 0)){
				String separator = "?";
				if (width != null && width > 0){
					imageUrl += separator + "width="+width;
					separator = "&";
				}
				if (height != null && height > 0){
					imageUrl += separator + "height="+height;
				}
			}
			HttpResponse response = request(new HttpGet(imageUrl));
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200){
				byte[] bytes = EntityUtils.toByteArray(response.getEntity());
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
		}
		return null;
	}

	public Bitmap getImage() {
		return this.image;
	}
	
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	public void setImageThumbnailSize(Bitmap image){
		setImageThumbnailSize(image, thumbnailWidth, thumbnailHeight);
	}
	
	/**
	 * Given a Bitmap image, resize it and set it to this event.
	 * @param image
	 * @param maxWidth
	 * @param maxHeight
	 */
	public void setImageThumbnailSize(Bitmap image, int maxWidth, int maxHeight){
		int currentWidth = image.getWidth();
		int currentHeight = image.getHeight();
		if (currentWidth > maxWidth || currentHeight > maxHeight){
			int newWidth;
			int newHeight;
			float widthHeightRatio = (float)currentWidth/(float)currentHeight;
			if (widthHeightRatio >= 1){
				newHeight = maxHeight;
				newWidth = (int) (newHeight*widthHeightRatio);
			}
			else {
				newWidth = maxWidth;
				newHeight = (int) (newWidth/widthHeightRatio);
			}
			image = Bitmap.createScaledBitmap(image, newWidth, newHeight, false);			
		}
		setImage(image);
	}
	
	/**
	 * Convert a Bitmap to byte[] so that it can be used for uploading to the server.
	 * @param bitmap
	 * @param format
	 * @return
	 */
	private byte[] bitmapToBytes(Bitmap bitmap, CompressFormat format){ 
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bitmap.compress(format, 100, baos);
        return baos.toByteArray();
	}

	public Ticket getMyTicket(){
		return myTicket;
	}
	
	public void setMyTicket(Ticket myTicket){
		this.myTicket = myTicket;
	}
	
	public Ticket[] getTickets(){
		return tickets;
	}
	
	public void setTickets(Ticket[] tickets){
		this.tickets = tickets;
	}

	public Integer getNumActiveTickets() {
		return numActiveTickets;
	}
	
	public Integer getNumRemainingTickets() {
		return numRemainingTickets;
	}

	public Integer getCreatorID() {
		return this.creatorID;
	}

	public void setCreatorID(Integer creatorID) {
		this.creatorID = creatorID;
	}

	public void setNumActiveTickets(Integer numActiveTickets) {
		this.numActiveTickets = numActiveTickets;
	}
	
	public void setNumRemainingTickets(Integer numRemainingTickets) {
		this.numRemainingTickets = numRemainingTickets;
	}

	/**
	 * @param date
	 * @return A date and time string that can be easily read by the user, such as "Tomorrow at 10PM"
	 */
	public static String friendlyDateTime(Date date) {
		return friendlyDate(date) + " at " + friendlyTime(date);
	}
	
	/**
	 * Return a date-only string that can be easily read by the user, such as "Tomorrow" or "December 13 2012".
	 * @param date
	 * @return String
	 */
	public static String friendlyDate(Date date){
		Calendar nowCal = Calendar.getInstance();
		Calendar eventCal = Calendar.getInstance();
		eventCal.setTime(date);	
		int eventYear = eventCal.get(Calendar.YEAR);
		int nowYear = nowCal.get(Calendar.YEAR);
		int eventDayOfYear = eventCal.get(Calendar.DAY_OF_YEAR);
		int nowDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR);
		Locale locale = Locale.ENGLISH;
		String friendlyDate = "";
		if (eventYear != nowYear ||
			eventDayOfYear - nowDayOfYear > 1 ||
			eventDayOfYear - nowDayOfYear < -1){
			friendlyDate += eventCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " " +
							eventCal.get(Calendar.DAY_OF_MONTH);
			if (eventYear != nowYear){
				friendlyDate += " " + eventYear;
			}
		}
		else {
			if (eventDayOfYear == nowDayOfYear){
				friendlyDate += "Today";
			}
			else {
				if (nowDayOfYear < eventDayOfYear){
					friendlyDate += "Tomorrow";
				}
				else {
					friendlyDate += "Yesterday";
				}
			}
		}
		return friendlyDate;
	}
	
	/**
	 * The inverse function of friendlyDate. It parses the String friendlyDate and returns a Date object.
	 * @param friendlyDate
	 * @return a Date object containing the date parameters parsed from friendlyDate.
	 * @throws ParseException
	 */
	public static Date friendlyDateToDate(String friendlyDate) throws ParseException{
		Calendar cal = Calendar.getInstance();
		if (friendlyDate.equals("Today")){
		}
		else if (friendlyDate.equals("Yesterday")){
			cal.add(Calendar.DAY_OF_YEAR, -1);
		}
		else if (friendlyDate.equals("Tomorrow")){
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		else {
			String format = "MMMMM d yyyy";
			// If friendlyDate does not include the year, add it.
			// We check this by counting the number of spaces in the date.
			// If there's only one space, that means the year was not included.
			if (friendlyDate.indexOf(" ") == friendlyDate.lastIndexOf(" ")){
				friendlyDate += " " + cal.get(Calendar.YEAR);
			}
			return new SimpleDateFormat(format).parse(friendlyDate);
		}
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	/**
	 * @param date
	 * @return a time-only string that can be easily read by the user, such as "2PM" or "11:30PM"
	 */
	public static String friendlyTime(Date time){
		Calendar eventCal = Calendar.getInstance();
		eventCal.setTime(time);
		String friendlyTime = "";
		int hour = eventCal.get(Calendar.HOUR);
		if (hour == 0){
			friendlyTime += "12";
		}
		else {
			friendlyTime += hour;
		}
		Integer minute = eventCal.get(Calendar.MINUTE); 
		if (minute != 0){
			friendlyTime += ":";
			if (minute < 10){
				friendlyTime += "0";
			}
			friendlyTime += minute;
		}
		friendlyTime += eventCal.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.ENGLISH);
		return friendlyTime;
	}
	
	/**
	 * The inverse function of friendlyTime. It parses the String friendlyTime and returns a Date object.
	 * @param friendlyTime
	 * @return a Date object containing the time parameters parsed from friendlyTime.
	 * @throws ParseException
	 */	
	public static Date friendlyTimeToDate(String friendlyTime) throws ParseException{
		String format = "hh";
		if (friendlyTime.contains(":")){
			format += ":mm";
		}
		format += "a";
		return new SimpleDateFormat(format).parse(friendlyTime);
	}
	
	/**
	 * Given both a date and time in string formats, parse them and return a resulting Date object that is set
	 * to the given date and time.
	 * @param startDate
	 * @param startTime
	 * @return
	 * @throws ParseException
	 */	
	public static Date friendlyDateTimeToDate(String startDate, String startTime) throws ParseException {
		Date date = friendlyDateToDate(startDate);
		Date time = friendlyTimeToDate(startTime);
		Calendar dateTimeCal = Calendar.getInstance();
		dateTimeCal.setTime(date);
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		dateTimeCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		dateTimeCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		dateTimeCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
		return dateTimeCal.getTime();
	}
	
	/**
	 * Parse the JSON object containing event details and return an Event object.
	 * Note, the resulting Event object will not contain the event image, so we can save an HTTP request.
	 * To set the event image, simply do event.setImage(event.getImageFromServer());
	 * @param eventRecord
	 * @return Event
	 * @throws JSONException
	 * @throws ParseException
	 */	
	public static Event jsonToEvent(JSONObject eventRecord) throws JSONException, ParseException{
		Event event = new Event();
		JSONObject eventJson;
		if (eventRecord.has("Event"))
			eventJson = eventRecord.getJSONObject("Event");
		else
			eventJson = eventRecord;
		if (!eventJson.isNull("id")) event.setId(eventJson.getInt("id"));
		if (!eventJson.isNull("name")) event.setName(eventJson.getString("name"));
		if (!eventJson.isNull("description")) event.setDescription(eventJson.getString("description"));
		if (!eventJson.isNull("location")) event.setLocation(eventJson.getString("location"));
		if (!eventJson.isNull("start_time")) event.setStartTime(eventJson.getString("start_time"));
		if (!eventJson.isNull("end_time")) event.setEndTime(eventJson.getString("end_time"));
		if (!eventJson.isNull("price")) event.setPrice(eventJson.getDouble("price"));
		if (!eventJson.isNull("capacity")) event.setCapacity(eventJson.getInt("capacity"));
		if (!eventJson.isNull("creator_id")) event.setCreatorID(eventJson.getInt("creator_id"));
		if (!eventJson.isNull("num_active_tickets")) event.setNumActiveTickets(eventJson.getInt("num_active_tickets"));
		if (!eventJson.isNull("num_remaining_tickets")) event.setNumRemainingTickets(eventJson.getInt("num_remaining_tickets"));
		
		// Set the associated event image url, if available.
		if (!eventRecord.isNull("Image")){
			JSONObject imageObject = eventRecord.getJSONObject("Image");
			if (!imageObject.isNull("url")){
				event.setImageUrl(imageObject.getString("url"));
			}
		}

		// Set the user's ticket for this event, if available.
		if (!eventRecord.isNull("MyTicket")){
			Ticket myTicket = Ticket.jsonToTicket(eventRecord.getJSONObject("MyTicket"));
			myTicket.setEvent(event);
			event.setMyTicket(myTicket);
		}
		
		// Set the associated tickets to this event, if available.
		if (!eventRecord.isNull("Ticket")){
			JSONArray jsonTickets = eventRecord.getJSONArray("Ticket"); 
			Integer numTickets = jsonTickets.length();
			Ticket[] tickets = new Ticket[numTickets];
			for (int i=0; i<numTickets; i++){
				tickets[i] = Ticket.jsonToTicket(jsonTickets.getJSONObject(i));
			}
			event.setTickets(tickets);
		}
		return event;
	}
	
	/**
	 * Fetch the event given by eventID from the server, and return it in the form of an Event object.
	 * @param eventID
	 * @return Event
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws JSONResponseException
	 */
	public static Event getById(Integer eventID) throws ClientProtocolException, URISyntaxException, IOException, JSONResponseException {
		HttpResponse response = get("/events/" + eventID + "/");
		JSONObject jsonResponse = null;
		try {
			jsonResponse = Model.responseToJSON(response);
			return jsonToEvent(jsonResponse.getJSONObject("response"));
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Wrapper function to simply get all events. See getAll(conditions) for more details.
	 * @return Event[]
	 * @throws ClientProtocolException
	 * @throws JSONResponseException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Event[] getAll() throws ClientProtocolException, JSONResponseException, URISyntaxException, IOException{
		ArrayList<NameValuePair> conditions = new ArrayList<NameValuePair>();
		return getAll(conditions);
	}
	
	private static Event[] jsonToEvents(JSONObject response) throws JSONResponseException, JSONException, ParseException{
		JSONArray eventsJson = response.getJSONArray("response");
		int numEvents = eventsJson.length();
		Event[] events = new Event[numEvents];
		JSONObject eventJson = null;
		for (int i=0; i<numEvents; i++){
			eventJson = eventsJson.getJSONObject(i);
			events[i] = Event.jsonToEvent(eventJson);
		}
		return events;
	}
	
	/**
	 * Fetch many events from the server filtered by the optional conditions parameter.
	 * @param conditions
	 * @return Event[]
	 * @throws JSONResponseException
	 */
	public static Event[] getAll(ArrayList<NameValuePair> conditions) throws JSONResponseException {
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/events/", conditions);
			jsonResponse = responseToJSON(response);
			return jsonToEvents(jsonResponse);
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Fetch those events that the current logged in user can administer. You can administer an event iff you've created it. 
	 * @return Event[]
	 * @throws JSONResponseException
	 */
	public static Event[] getMine() throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/events/admin");
			jsonResponse = responseToJSON(response);
			return jsonToEvents(jsonResponse);
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Save the current Event object on the server.
	 * If the id is null, a new Event will be created. If the id is not null, the event given by id will be updated.
	 * @return Event
	 * @throws JSONResponseException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws org.apache.http.ParseException
	 * @throws JSONException
	 * @throws URISyntaxException
	 * @throws ParseException
	 */
	public Event save() throws JSONResponseException, ClientProtocolException, IOException, org.apache.http.ParseException, JSONException, URISyntaxException, ParseException {
		String url = "/events/edit/";
		Integer id = getId();
		if (id != null){
			url += Integer.toString(id) + "/";
		}
		MultipartEntity data = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		data.addPart("name", new StringBody(this.getName()));
		data.addPart("description", new StringBody(this.getDescription()));
		data.addPart("location", new StringBody(this.getLocation()));
		data.addPart("capacity", new StringBody(this.getCapacity().toString()));
		data.addPart("price", new StringBody(this.getPrice().toString()));
		data.addPart("start_time", new StringBody(this.getStartTime().toString()));
		Bitmap eventImage = this.getImage();
		if (eventImage != null){
			byte[] eventImageBytes = bitmapToBytes(eventImage, Bitmap.CompressFormat.JPEG);
			data.addPart("image", new ByteArrayBody(eventImageBytes, "image/jpeg", "eventImage.jpeg"));
		}
		
		return jsonToEvent(Model.responseToJSON(Model.post(url, data)).getJSONObject("response"));
	}

	/**
	 * Given an array of events, return the position at which all events from that point onwards happen after the current time.
	 * This is used to show a list of events and automatically scroll down to the position that is most relevant to today onwawrds.
	 * @param events
	 * @return int
	 */
	public static int findTodaysPosition(Event[] events) {
		int i = 0;
		Calendar todayCal = Calendar.getInstance();		
		Calendar eventCal = Calendar.getInstance();
		
		for (Event event:events){
			eventCal.setTime(event.getStartTime());
			if (eventCal.get(Calendar.DAY_OF_YEAR) >= todayCal.get(Calendar.DAY_OF_YEAR) &&
				eventCal.get(Calendar.YEAR) >= todayCal.get(Calendar.YEAR)){
				return i;
			}
			i++;
		}
		return 0;
	}
	
	/**
	 * Wrapper method to findTodaysPosition(Event[]) given tickets rather than events.
	 * @param tickets
	 * @return int
	 */
	public static int findTodaysPosition(Ticket[] tickets){
		return findTodaysPosition(ticketsToEvents(tickets));
	}

	/**
	 * Each Ticket has an associated Event. Given a list of tickets, return a list of their corresponding events.
	 * @param tickets
	 * @return Event[]
	 */
	public static Event[] ticketsToEvents(Ticket[] tickets){
		Event[] events = new Event[tickets.length];
		int i = 0;
		for (Ticket ticket:tickets){
			events[i] = ticket.getEvent();
			i++;
		}
		return events;
	}
}
