package com.ticketflex.android;

import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Task.AutoAsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * The Activity that is used by the See All Events screen.
 * @author Baseer
 *
 */
public class EventsIndexActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_events_index);
        
        AutoAsyncTask<Void, Void, Event[]> task = new AutoAsyncTask<Void, Void, Event[]>(this) {
        	@Override
        	protected Event[] doInBackgroundAuto(Void... params)
        			throws JSONResponseException, Exception {
        		// Get all events.
        		Event[] events = Event.getAll();
        		return events;
        	}
        	@Override
        	protected void onPostExecuteAuto(final Event[] events) {
        		ListView lv = (ListView)findViewById(R.id.EventsList);
        		
        		// Use the EventsAdapter to populate the list of events.
				lv.setAdapter(new EventsAdapter(context, events));
    			
				// Bind each event on the list to the EventActivity when clicked on.
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view,
							int position, long id) {
						Integer eventID = events[position].getId();
						String eventName = events[position].getName();
		        		Intent intent = new Intent(context, EventActivity.class)
		        							.putExtra("id", eventID)
		        							.putExtra("eventName", eventName);
		        		startActivity(intent);
					}					
				});
				// Scroll down to the events starting from today and onwards.
				lv.setSelection(Event.findTodaysPosition(events));
        	}
		};
        task.execute();
	}

}
