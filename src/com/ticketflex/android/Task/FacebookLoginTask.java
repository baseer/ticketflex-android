package com.ticketflex.android.Task;

import com.ticketflex.android.MainActivity;
import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * The AsyncTask that is responsible for logging in with Facebook. The user is redirected to the MainActivity on
 * successful login.
 * @author Baseer
 *
 */
public class FacebookLoginTask extends AutoAsyncTask <String, Void, Void> {
	public FacebookLoginTask(Context context) {
		super(context);
	}

	@Override
	protected Void doInBackgroundAuto(String... facebookToken) throws JSONResponseException {
    	User.login(facebookToken[0]);
		return null;
	}
	
	@Override
	protected void onPostExecuteAuto(Void param){
		// Once they've logged in with Facebook, redirect them to the MainActivity.
		Intent intent = new Intent(this.context, MainActivity.class);
		this.context.startActivity(intent);
		((Activity) context).finish();
		Toast.makeText(this.context, "Logged in with Facebook", Toast.LENGTH_SHORT).show();
	}
}
