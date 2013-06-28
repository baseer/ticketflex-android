package com.ticketflex.android;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;

import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Task.AutoAsyncTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * The Activity that is used by the Administer My Events screen.
 * @author Baseer
 *
 */
public class EventsAdminActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_events_admin);
        
        GetEventsTask task = new GetEventsTask(this);
        task.execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add the "+" button in the top right that allows the user to create a new event.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_events_admin, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	        	// If the "+" button was clicked on, send them to the ManageEventActivity to create a new event.
				startActivity(new Intent(EventsAdminActivity.this, ManageEventActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public class GetEventsTask extends AutoAsyncTask<Void, Void, Event[]> {
		public GetEventsTask(Context context) {
			super(context);
		}
		@Override
		protected Event[] doInBackgroundAuto(Void... params) throws JSONResponseException, ClientProtocolException, IOException {
			Event[] events = null;
			// Get only those events that the current user administers
			events = Event.getMine();
			return events;
		}
		@Override
		protected void onPostExecuteAuto(final Event[] events) {
			ListView listView = (ListView) findViewById(R.id.MyEventsList);
			
			// Populate the listView with the user's events.
	        listView.setAdapter(new EventsAdapter(context, events));
	        // If the user clicks on an event, take them to the Event Admin page for the selected event.
	        listView.setOnItemClickListener(new OnItemClickListener() {
	        	@Override
	        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
	        		Event event = events[position];
	        		Intent intent = new Intent(context, EventAdminActivity.class)
	        							.putExtra("id", event.getId())
	        							.putExtra("eventName", event.getName());
	        		startActivity(intent);
	        	}
			});
	        // Automatically scroll down to show only those events that are for today and onwards.
			listView.setSelection(Event.findTodaysPosition(events));
		}
	}
}
