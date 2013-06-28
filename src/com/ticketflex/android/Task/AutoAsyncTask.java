package com.ticketflex.android.Task;
import com.ticketflex.android.Exception.JSONResponseException;

import android.app.Activity;
import android.content.Context;

/**
 * Wrapper AsyncTask designed to automatically handle JSON errors thrown by the TicketFlex app server.
 * It also automatically shows a progress bar indicator while processing, and hides it when the task is complete.
 * This is the AsyncTask that should be used whenever possible when connecting to the TicketFlex app server.
 * @author Baseer
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AutoAsyncTask<Params, Progress, Result> extends CAsyncTask<Params, Progress, Result> {

	public AutoAsyncTask(Context context) {
		// Store the context, which is usually an Activity. A reference to the Activity is usually required for
		// many different tasks.
		super(context);
	}

	@Override
	protected void onPreExecute() {
		// If the context is an Activity, show the progress bar on the Activity, while this AsyncTask is doing it's
		// work.
		if (Activity.class.isInstance(context)){
			((Activity)context).setProgressBarIndeterminateVisibility(true);
		}
		onPreExecuteAuto();
	}
	
	/**
	 *  This method can be overridden when you create your AsyncTask. It allows you to do something on the UI thread
	 *  before you start the task in a background thread. You can also prematurely cancel the task from here as well.
	 *  For more info, search onPreExecute() on the Android developers page.
	 */
	protected void onPreExecuteAuto() {
	}
	
	/**
	 * This method should be overridden. It is executed in a background thread. Search doInBackground() on the
	 * Android developers page for more info on how you can use this method.
	 * @param params
	 * @return
	 * @throws JSONResponseException
	 * @throws Exception
	 */
	protected Result doInBackgroundAuto(Params... params) throws JSONResponseException, Exception {
		return null;
	}

	@Override
	protected Result doInBackground(Params... params) {
		Result result = null;
		try {
			result = doInBackgroundAuto(params);
		} /*catch (JSONResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */catch (Exception e) {
			exception = e;
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * This method should also be overridden. It is executed on the UI thread after doInBackground() has completed its
	 * work. Search onPostExecute() on the Android developers page for more info on how you can use this method. 
	 * @param result
	 */
	protected void onPostExecuteAuto(Result result){
	}
	
	@Override
	protected void onPostExecute(Result result){
		// Once the task has completed (meaning, once doInBackground() has completed), hide the progress bar indicator.
		if (Activity.class.isInstance(context)){
			((Activity)context).setProgressBarIndeterminateVisibility(false);
		}
		// If an exception hasn't been thrown by doInBackgroundAuto(), we can process the valid result by calling onPostExecuteAuto().
		if (exception == null){
			onPostExecuteAuto(result);
		}
		// Otherwise, handle the exception. This will show the "friendly_message" field from the server in a Toast, if
		// available.
		else {
			handleException();
		}
	}
}