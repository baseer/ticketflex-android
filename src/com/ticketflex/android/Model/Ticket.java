package com.ticketflex.android.Model;

import java.text.ParseException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ticketflex.android.Exception.JSONResponseException;

public class Ticket extends Model {
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getQRCode() {
		return QRCode;
	}
	public void setQRCode(String qRCode) {
		QRCode = qRCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Double getPrice() {
		return this.price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Boolean isActive(){
		return (this.status != null) && this.status.equals("active");
	}
	public Boolean isAdmitted(){
		return (this.status != null) && this.status.equals("admitted");
	}
	public Boolean isOnSale() {
		return (this.status != null) && this.status.equals("on-sale");
	}
	private Integer id = null;
	private Event event = null;
	private User user = null;
	private String QRCode = null;
	private String status = null;
	private Double price = null;
	public static final Integer ADMIT = 1; // Use this Integer with Ticket.adminAction() to admit a ticket. 
	public static final Integer DECLINE = 2; // Use this Integer with Ticket.adminAction() to decline a ticket.

	/**
	 * Get a Ticket given the ticketID.
	 * @param ticketID
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket getById(Integer ticketID) throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/" + ticketID + "/");
			jsonResponse = Model.responseToJSON(response).getJSONObject("response");
			return jsonToTicket(jsonResponse);
		}
		catch (JSONResponseException e){
			throw e;
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Admit or decline the ticket given by ticketID.
	 * action should be one of Ticket.ADMIT or Ticket.DECLINE.
	 * @param ticketID
	 * @param action
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket adminAction(Integer ticketID, Integer action) throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			String actionUrl = "/tickets/";
			if (action == ADMIT){
				actionUrl += "admit";
			}
			else {
				actionUrl += "decline";
			}
			HttpResponse response = get(actionUrl + "/" + ticketID);
			jsonResponse = Model.responseToJSON(response).getJSONObject("response");
			return jsonToTicket(jsonResponse);
		}
		catch (JSONResponseException e){
			throw e;
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}		
	}
	
	private static String sanitizeQrCode(String ticketQrCode){
		return ticketQrCode.replaceAll("[^a-zA-Z0-9\\s]", "");
	}
	
	/**
	 * Get a Ticket given its QR code.
	 * @param ticketQrCode
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket getByQrCode(String ticketQrCode) throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			ticketQrCode = sanitizeQrCode(ticketQrCode);
			HttpResponse response = get("/tickets/qr-code/" + ticketQrCode);
			jsonResponse = Model.responseToJSON(response).getJSONObject("response");
			return jsonToTicket(jsonResponse);
		}
		catch (JSONResponseException e){
			throw e;
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	/**
	 * Parse the JSONObject containing ticket details, and create a Ticket object from it. 
	 * @param ticketRecord
	 * @return Ticket
	 * @throws JSONException
	 * @throws ParseException
	 */
	public static Ticket jsonToTicket(JSONObject ticketRecord) throws JSONException, ParseException{
		Ticket ticket = new Ticket();
		JSONObject ticketJson;
		if (ticketRecord.has("Ticket"))
			ticketJson = ticketRecord.getJSONObject("Ticket");
		else
			ticketJson = ticketRecord;
		if (!ticketJson.isNull("id")) ticket.setId(ticketJson.getInt("id"));
		if (!ticketJson.isNull("qr_code")) ticket.setQRCode(ticketJson.getString("qr_code"));
		if (!ticketJson.isNull("status")) ticket.setStatus(ticketJson.getString("status"));
		if (!ticketJson.isNull("price")) ticket.setPrice(ticketJson.getDouble("price"));
		
		// Set the ticket's associated event, if available.
		Event event = null;
		if (ticketRecord.has("Event")){
			event = Event.jsonToEvent(ticketRecord.getJSONObject("Event"));
		}
		ticket.setEvent(event);
		
		// Set the ticket's associated creator, if available.
		User user = null;
		if (ticketRecord.has("User")){
			user = User.jsonToUser(ticketRecord);
		}
		else if (ticketJson.has("User")){
			user = User.jsonToUser(ticketJson);
		}
		ticket.setUser(user);
		
		return ticket;
	}
	
	/**
	 * Get all tickets given the HTTP GET parameters in conditions.
	 * You can filter by "event_id", "user_id", and "status" by setting values for them in conditions.
	 * @param conditions
	 * @return Ticket[]
	 * @throws JSONResponseException
	 */
	public static Ticket[] getAll(ArrayList<NameValuePair> conditions) throws JSONResponseException {		
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/", conditions);
			jsonResponse = responseToJSON(response);
			JSONArray ticketRecords = jsonResponse.getJSONArray("response");
			int numTickets = ticketRecords.length();
			Ticket[] tickets = new Ticket[numTickets];
			JSONObject ticketJson = null;
			for (int i=0; i<numTickets; i++){
				ticketJson = ticketRecords.getJSONObject(i);
				tickets[i] = Ticket.jsonToTicket(ticketJson);
			}
			return tickets;
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Get all tickets for a given eventID.
	 * @param eventID
	 * @return Ticket[]
	 * @throws JSONResponseException
	 */
	public static Ticket[] getAllForEvent(Integer eventID) throws JSONResponseException{
		ArrayList<NameValuePair> conditions = new ArrayList<NameValuePair>();
		conditions.add(new BasicNameValuePair("event_id", eventID.toString()));
		return getAll(conditions);
	}
	
	/**
	 * Fetch all tickets up for sale for the given eventID.
	 * @param eventID
	 * @return Ticket[]
	 * @throws JSONResponseException
	 */
	public static Ticket[] getAllOnSale(Integer eventID) throws JSONResponseException {
		ArrayList<NameValuePair> conditions = new ArrayList<NameValuePair>();
		if (eventID != null){
			conditions.add(new BasicNameValuePair("event_id", eventID.toString()));
		}
		conditions.add(new BasicNameValuePair("status", "on-sale"));
		return getAll(conditions);
	}
	
	/**
	 * Put the ticket given by ticketID, up for sale.
	 * @param ticketID
	 * @param price
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket sell(Integer ticketID, Double price) throws JSONResponseException{		
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/sell/"+ticketID+"/"+price);
			return jsonToTicket(responseToJSON(response).getJSONObject("response"));
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Take back your ticket that you previously put up on sale, given the ticketID. This method will only
	 * succeed if the ticket has not already been purchased by someone else.
	 * @param ticketID
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket unsell(Integer ticketID) throws JSONResponseException{		
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/unsell/"+ticketID);
			return jsonToTicket(responseToJSON(response).getJSONObject("response"));
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Buy a new ticket from the event, given the eventID.
	 * @param eventID
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket buy(Integer eventID) throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/buy/"+eventID);
			return jsonToTicket(responseToJSON(response).getJSONObject("response"));
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
	
	/**
	 * Buy an existing ticket from a ticket holder, given by the ticketID.
	 * @param ticketID
	 * @return Ticket
	 * @throws JSONResponseException
	 */
	public static Ticket buyExistingTicket(Integer ticketID) throws JSONResponseException{
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/tickets/buy-existing/"+ticketID);
			return jsonToTicket(responseToJSON(response).getJSONObject("response"));
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}
	}
}
