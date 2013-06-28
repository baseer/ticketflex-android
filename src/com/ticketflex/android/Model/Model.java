package com.ticketflex.android.Model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.ticketflex.android.Exception.JSONResponseException;

public class Model {
	public static String serverUrl = "http://ticketflex.baseersiddiqui.com"; // Url to the TicketFlex App Server
	private static HttpClient httpClient = null;
	public static User currentUser = null; // stores the current logged in user
	
	public static User getCurrentUser(){
		return Model.currentUser;
	}

	public static HttpClient getHttpClient(){
		return httpClient;
	}
	
	public static void setHttpClient(HttpClient httpClient){
		Model.httpClient = httpClient;
	}
	
	public static HttpResponse get(String relativeUrl) throws ClientProtocolException, URISyntaxException, IOException {
		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
		return get(relativeUrl, data);
	}
	
	/**
	 * Make an HTTP GET request with the GET parameters provided in data. relativeUrl should have the form
	 * "/events/5". This will make a GET request to http://ticketflex.baseersiddiqui.com/events/5.
	 * @param relativeUrl
	 * @param data
	 * @return HttpResponse
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse get(String relativeUrl, ArrayList<NameValuePair> data) throws URISyntaxException, ClientProtocolException, IOException{
		URI uri = createURI(relativeUrl, data);
		HttpGet httpGet = new HttpGet(uri);
		return request(httpGet);
	}
	public static HttpResponse post(String relativeUrl) throws ClientProtocolException, IOException, URISyntaxException{
		ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
		return post(relativeUrl, data);
	}
	/**
	 * Make an HTTP POST request with the POST parameters provided in data.
	 * @param relativeUrl
	 * @param data
	 * @return HttpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static HttpResponse post(String relativeUrl, ArrayList<NameValuePair> data) throws ClientProtocolException, IOException, URISyntaxException{
		URI uri = createURI(relativeUrl);
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
		return request(httpPost);
	}
	/**
	 * Make an HTTP multipart POST request with the POST parameters provided in mpEntity.
	 * This is useful for uploading images since image upload via POST is required to be a multipart POST request.
	 * @param relativeUrl
	 * @param mpEntity
	 * @return HttpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static HttpResponse post(String relativeUrl, MultipartEntity mpEntity) throws ClientProtocolException, IOException, URISyntaxException{
		HttpPost httpPost = new HttpPost(createURI(relativeUrl));
		httpPost.setEntity(mpEntity);
		return request(httpPost);
	}
	private static URI createURI(String relativeUrl) throws URISyntaxException{
		return createURI(relativeUrl, new ArrayList<NameValuePair>());
	}
	/**
	 * Generate a URI given the relativeUrl in the form of "/events/5", the GET parameters in getParams.
	 * The URI will contain the full url such as "http://ticketflex.baseersiddiqui.com/event/5?<GET parameters>". 
	 * @param relativeUrl
	 * @param getParams
	 * @return URI
	 * @throws URISyntaxException
	 */
	private static URI createURI(String relativeUrl, ArrayList<NameValuePair> getParams) throws URISyntaxException{
		getParams = addAccessToken(getParams);
		return URIUtils.createURI(null, serverUrl, -1, relativeUrl, URLEncodedUtils.format(getParams, "UTF-8"), null);
	}
	/**
	 * Given an ArrayList of name value pairs (typically used to store GET/POST parameters), add the access_token
	 * to this list of parameters.
	 * @param data
	 * @return ArrayList<NameValuePair>
	 */
	private static ArrayList<NameValuePair> addAccessToken(ArrayList<NameValuePair> data){
		if (currentUser != null){
			return addAccessToken(data, currentUser.getAccessToken());
		}
		return data;
	}
	private static ArrayList<NameValuePair> addAccessToken(ArrayList<NameValuePair> data, String accessToken){
		data.add(new BasicNameValuePair("access_token", accessToken));
		return data;
	}
	/**
	 * Make the actual HTTP request given an httpMethod (which is usually an HttpGet or HttpPost object).
	 * This method also initializes the httpClient with reasonable timeouts, if it hasn't already been initialized.
	 * @param httpMethod
	 * @return HttpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse request(HttpRequestBase httpMethod) throws ClientProtocolException, IOException {
		if (httpClient == null){
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			//httpClient = AndroidHttpClient.newInstance("com.ticketflex.android");
			httpClient = new DefaultHttpClient(httpParameters);
		}
		return httpClient.execute(httpMethod);
	}
	/**
	 * Convert an HttpResponse into a JSONObject. This method should be called if JSON is the expected response from
	 * the server. 
	 * @param response
	 * @return JSONObject
	 * @throws JSONResponseException
	 */
	public static JSONObject responseToJSON(HttpResponse response) throws JSONResponseException {
		JSONObject json = null;
		try {
			String rawResponse = EntityUtils.toString(response.getEntity());
			//Log.d("response", rawResponse);
			json = new JSONObject(rawResponse);
		}
		catch (Exception e) {
			throw new JSONResponseException(json, e);
		}
		if (JSONResponseException.isErrorResponse(json)){
			throw new JSONResponseException(json);
		}
		return json;
	}
	
	/**
	 * Parse a date string provided from the server (usually inside a JSON object), and convert it to a Date object.
	 * @param dateString
	 * @return Date
	 * @throws java.text.ParseException
	 */
	public static Date serverDateToDate(String dateString) throws java.text.ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.parse(dateString);
	}
}
