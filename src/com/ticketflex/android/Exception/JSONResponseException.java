package com.ticketflex.android.Exception;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;
/**
 * This exception is thrown when either:
 * 1. A malformed response was received from the server, or
 * 2. A valid response was received from the server that indicates failure to complete the requested action.
 *    In this case, the server will provide a JSON Error Response that looks like:
 *    {
 *    	error: {
 *    		message: "Some technical message describing what went wrong or why the action could not be completed.",
 *    		friendly_message: "A friendly message that is intended to be displayed to the user."
 *   	}
 *    }
 *    
 * @author Baseer
 *
 */
public class JSONResponseException extends ResponseException {
	private static final long serialVersionUID = -978492844034696982L;
	JSONObject response = null;
	
	public JSONObject getResponse(){
		return this.response;
	}
	
	public void setResponse(JSONObject response){
		this.response = response;
	}

	public JSONResponseException(JSONObject response, Throwable cause){
		initCause(cause);
		
		if (response == null && getClass().isInstance(cause)){
			response = ((JSONResponseException) cause).getResponse();
		}
		setResponse(response);
	}

	public JSONResponseException(Throwable cause){
		initCause(cause);
		if (cause.getClass().isInstance(JSONResponseException.class)){
			setResponse(((JSONResponseException) cause).getResponse());
		}
	}
	
	public JSONResponseException(JSONObject response){
		setResponse(response);
	}

	private JSONObject getErrorObject(JSONObject response) throws JSONException{
		return response.getJSONObject("error");
	}

	/**
	 * Get the friendly message that is intended to be displayed to the user. It is normally displayed via a Toast.
	 */
	public String getFriendlyMessage() {
		if (getResponse() == null){
			return super.getFriendlyMessage();
		}
		try {
			JSONObject error = getErrorObject(getResponse());
			return error.getString("friendly_message");
		} catch (JSONException e) {
			e.printStackTrace();
			return super.getFriendlyMessage();
		}
	}
	/**
	 * Get the message filled with technical details, intended to appear in the stack trace.
	 */
	public String getMessage() {
		if (getResponse() == null){
			return super.getMessage();
		}
		try {
			JSONObject error = getErrorObject(getResponse());
			return error.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
			return super.getMessage();
		}
	}
	public void autoHandle(Context context){
		Toast.makeText(context, this.getFriendlyMessage(), Toast.LENGTH_SHORT).show();
	}

	public static Boolean isErrorResponse(JSONObject json){
		return json == null || json.has("error");
	}
}
