package com.ticketflex.android.Task;

import com.ticketflex.android.Exception.JSONResponseException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * For background on how AsyncTask's work, search the Android Developer page.
 * 
 * This is a wrapper AsyncTask designed to store the context of the caller, which is usually an Activity.
 * It also allows the doInBackground() method to store an exception in the AsyncTask, which can later be handled
 * by the onPostExecute() method.
 * It also provides a user-friendly way of handling an exception - by displaying the "friendly_message" field
 * received from the server in the form of a Toast.
 * @author Baseer
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class CAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public Context context;
    public Exception exception = null;

    public CAsyncTask<Params, Progress, Result> setContext(Context context){
        this.context = context;
        return this;
    }
    public CAsyncTask(Context context){
        setContext(context);
    }
    public void handleException(){
    	handleException("Sorry, that last action could not be completed.");
    }
    
    public void handleException(String fallbackMessage){
    	// If there has been a JSONResponseException, this means we can read the "friendly_message" field provided
    	// by the server. Use it to display a friendly message to the user.
    	if (exception != null && exception.getClass() == JSONResponseException.class){
			((JSONResponseException) exception).autoHandle(context);
		}
    	// Otherwise, just display the fallback message provided.
		else {
			if (exception != null)
				exception.printStackTrace();
			Toast.makeText(context, fallbackMessage, Toast.LENGTH_SHORT).show();	
		}
    }
}