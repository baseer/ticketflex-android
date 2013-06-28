package com.ticketflex.android.Model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ticketflex.android.Exception.JSONResponseException;

public class User extends Model {
	private Integer id;
	private Integer facebookId;
	private String accessToken;
	private String facebookAccessToken;
	private String name;
	private Bitmap image;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getFacebookId() {
		return facebookId;
	}
	public void setFacebookId(Integer facebookId) {
		this.facebookId = facebookId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getFacebookAccessToken() {
		return facebookAccessToken;
	}
	public void setFacebookAccessToken(String facebookAccessToken) {
		this.facebookAccessToken = facebookAccessToken;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the user's profile picture from Facebook. If you want to set this as this User's picture, use
	 * setImage(getImageFromFacebook()).
	 * @return Bitmap
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public Bitmap getImageFromFacebook() throws ClientProtocolException, IOException, URISyntaxException {
		Integer facebookId = getFacebookId();
		if (facebookId != null){
			ArrayList<NameValuePair> getParams = new ArrayList<NameValuePair>();
			getParams.add(new BasicNameValuePair("width", "140"));
			getParams.add(new BasicNameValuePair("height", "140"));
			URI uri = URIUtils.createURI(
					null,
					"http://graph.facebook.com",
					-1,
					facebookId+"/picture",
					URLEncodedUtils.format(getParams, "UTF-8"),
					null
				);
			HttpResponse response = request(new HttpGet(uri));
			Header locationHeader = response.getFirstHeader("location");
			if (locationHeader != null){
				response = request(new HttpGet(locationHeader.getValue()));
			}
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode < 400){
				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(response.getEntity());
				return BitmapFactory.decodeStream(bufHttpEntity.getContent(), null, new BitmapFactory.Options());
			}
		}
		return null;
	}
	
	/**
	 * Get the current image for the user. This will be null unless you have fetched and set the user's picture from
	 * another location, such as getImageFromFacebook().  
	 * @return Bitmap
	 */
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	/**
	 * Login to TicketFlex by sending the Facebook access token to the server. Then, grab the JSON user object from
	 * the server's response, convert it to a User object, and store in the session. Then, given that the User object
	 * contains an access token, we can use it to make requests to the server.
	 * @param facebookToken
	 * @return User
	 * @throws JSONResponseException
	 */
	public static User login(String facebookToken) throws JSONResponseException {
		User user = null;
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = get("/facebook/login/"+facebookToken);
			//System.out.println(EntityUtils.toString(response.getEntity()));
			jsonResponse = responseToJSON(response);
			if (jsonResponse != null){
				user = jsonToUser(jsonResponse.getJSONObject("response"));
				Model.currentUser = user;
			}
		}
		catch (Exception e){
			throw new JSONResponseException(jsonResponse, e);
		}

		return user;
	}
	
	/**
	 * To logout, we simply remove the User from the session.
	 */
	public static void logout() {
		Model.currentUser = null;
	}
	
	/**
	 * Parse the JSON object containing user details, and return a User object.
	 */
	public static User jsonToUser(JSONObject userRecord) throws JSONException{
		User user = new User();
		JSONObject userJson = userRecord.getJSONObject("User");
		if (!userJson.isNull("id")) user.setId(userJson.getInt("id"));
		if (!userJson.isNull("facebook_id")) user.setFacebookId(userJson.getInt("facebook_id"));
		if (!userJson.isNull("access_token")) user.setAccessToken(userJson.getString("access_token"));
		if (!userJson.isNull("facebook_access_token")) user.setFacebookAccessToken(userJson.getString("facebook_access_token"));
		if (!userJson.isNull("name")) user.setName(userJson.getString("name"));
		return user;
	}
}
