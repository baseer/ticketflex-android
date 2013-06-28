package com.ticketflex.android;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Model.Model;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Task.AutoAsyncTask;

/**
 * Activity used by the My Tickets screen.
 * @author Baseer
 *
 */
public class MyTicketsActivity extends Activity {
	private final class MyTicketsTask extends AutoAsyncTask<Void, Void, Ticket[]> {
		private MyTicketsTask(Context context) {
			super(context);
		}

		@Override
		protected void onPreExecuteAuto() {
			// If the user is not logged in, there's no way we can get their tickets, cancel the task and notify them
			// that they should login.
			if (Model.getCurrentUser() == null){
				Toast.makeText(context, "Please login to view your tickets.", Toast.LENGTH_SHORT).show();
				setProgressBarIndeterminateVisibility(false);
				cancel(true);
			}
		}

		@Override
		protected Ticket[] doInBackgroundAuto(Void... params) throws JSONResponseException, ClientProtocolException, IOException {
		    ArrayList<NameValuePair> conditions = new ArrayList<NameValuePair>();
		    // Get the user's tickets.
			conditions.add(new BasicNameValuePair("user_id", Model.getCurrentUser().getId().toString()));
			return Ticket.getAll(conditions);
		}

		@Override
		protected void onPostExecuteAuto(final Ticket[] tickets) {
			ListView listView = (ListView) findViewById(R.id.MyTicketsList);

	        // Use the EventsAdapter to show the tickets in a list view.
	        listView.setAdapter(new EventsAdapter(context, tickets)); 
			
	        // Bind the click handler on each ticket to view more details.
	        listView.setOnItemClickListener(new OnItemClickListener() {
	        	@Override
	        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
	        		Event event = tickets[position].getEvent();
	        		Intent intent = new Intent(context, EventActivity.class)
	        						.putExtra("id", event.getId())
	        						.putExtra("eventName", event.getName());
	        		startActivity(intent);
	        	}
			});
	        // Scroll down to show those events starting with todays date. 
			listView.setSelection(Event.findTodaysPosition(tickets));
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.activity_my_tickets);

        new MyTicketsTask(this).execute();
    }
}
