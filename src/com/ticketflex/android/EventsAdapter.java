package com.ticketflex.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Task.AutoAsyncTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The Adapter responsible for populating the list views in See All Events, My Tickets, and Administer Events.
 * @author Baseer
 *
 */
public class EventsAdapter extends ArrayAdapter<Event>{

	/**
	 * If a tickets array was passed in, convert it into an events array. 
	 * @param context
	 * @param tickets
	 */
	public EventsAdapter(Context context, Ticket[] tickets) {
		super(context, R.id.name);
		Event[] events = Event.ticketsToEvents(tickets);
		// Add the events to the ArrayAdapter and load the event thumbnail in the background.
		for (Event event:events){
			add(event);
			new EventThumbnailAsyncTask(context).execute(event);
		}
	}

	public EventsAdapter(Context context, Event[] objects) {
		super(context, R.id.name, new ArrayList<Event>(Arrays.asList(objects)));
		// Load the event thumbnails.
		for (Event event:objects){
			new EventThumbnailAsyncTask(context).execute(event);
		}
	}

	/**
	 * The AsyncTask responsible for fetching an event thumbnail. This is done in the background so that
	 * the event details are still visible and responsive before the thumbnail has finished loading.
	 * @author Baseer
	 *
	 */
	public class EventThumbnailAsyncTask extends AutoAsyncTask<Event, Void, Event>{
		public EventThumbnailAsyncTask(Context context) {
			super(context);
		}

		@Override
		protected Event doInBackgroundAuto(Event... events)
				throws JSONResponseException, Exception {
			Event event = events[0];
			event.setImageThumbnailSize(event.getImageFromServer(Event.thumbnailWidth, Event.thumbnailHeight));
			return event;
		}
		
		@Override
		protected void onPostExecuteAuto(Event event) {
			// Update the events list now that we have loaded the event thumbnail.
			EventsAdapter.this.notifyDataSetChanged();			
		}
	}
	
	/**
	 * Return the View of a particular Event given by the position in the events array.
	 * This is the method that determines what gets displayed for each event in the list view. 
	 */
	@Override
	public View getView(int position, View convertView,
			ViewGroup parent) {
		//View row = convertView;
		//if (row == null){
			LayoutInflater mInflater = LayoutInflater.from(getContext());
			// Use the list_item_event layout
			View row = mInflater.inflate(R.layout.list_item_event, null);
			Event event = getItem(position);
			Bitmap image = event.getImage();
			// If the image has been fetched from the server, scale it down to the correct size and attach it to
			// the list item.
			if (image != null){
				int minDimension = Math.min(image.getWidth(), image.getHeight());
				int x = 0; int y = 0;
				if (image.getWidth() > minDimension){
					x = (image.getWidth() - minDimension)/2;
				}
				else if (image.getHeight() > minDimension){
					y = (image.getHeight() - minDimension)/2;
				}
				Bitmap thumbnail = Bitmap.createBitmap(image, x, y, minDimension, minDimension);
				Bitmap scaledThumbnail = Bitmap.createScaledBitmap(thumbnail, 140, 140, true);
				((ImageView) row.findViewById(R.id.eventThumbnail)).setImageBitmap(scaledThumbnail);
			}
			// Add the event name, location, and start time.
			((TextView) row.findViewById(R.id.name)).setText(event.getName());
			String location = event.getLocation();
			((TextView) row.findViewById(R.id.location)).setText(location);
			Date startTime = event.getStartTime();
			if (startTime != null){
				((TextView) row.findViewById(R.id.startTime)).setText(Event.friendlyDateTime(startTime));
			}
			
		//}
		return row;
	}	
}
