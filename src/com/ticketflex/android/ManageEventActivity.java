package com.ticketflex.android;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import com.ticketflex.android.R;
import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Task.AutoAsyncTask;
import com.ticketflex.android.Utils.Util;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Activity used by the Create Event and Edit Event screen.
 * If the intent thats passed in contains an event id, the event details will be filled in and you can edit the event.
 * @author Baseer
 *
 */
public class ManageEventActivity extends FragmentActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    public Bitmap eventImage = null;
    public Integer eventID = null;

    public void setEventID(Integer eventID){
    	this.eventID = eventID;
    }
    public Integer getEventID(){
    	return this.eventID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_manage_event);
        setProgressBarIndeterminateVisibility(false);
        
        Util util = new Util(this);
        // Initialize button icons and event detail icons.
        Button buttonLoadImage = (Button) findViewById(R.id.button_add_image);
        Intent intent = getIntent();
		Integer eventID = intent.getIntExtra("id", 0);

		final EditText nameView = ((EditText)findViewById(R.id.name));
		
		final EditText locationView = ((EditText)findViewById(R.id.location));
		util.setTextViewIcon(locationView, R.drawable.icon_location);
		
		final EditText priceView = (EditText)findViewById(R.id.price); 
		util.setTextViewIcon(priceView, R.drawable.icon_price);

		final EditText capacityView = ((EditText)findViewById(R.id.capacity));
		util.setTextViewIcon(capacityView, R.drawable.icon_capacity);

		final EditText startDateView = ((EditText)findViewById(R.id.start_date));
		util.setTextViewIcon(startDateView, R.drawable.icon_start_date);

		final EditText startTimeView = ((EditText)findViewById(R.id.start_time));
		util.setTextViewIcon(startTimeView, R.drawable.icon_start_time);

		final EditText descriptionView = ((EditText)findViewById(R.id.description));
		util.setTextViewIcon(descriptionView, R.drawable.icon_description);

		Button addImageButton = ((Button)findViewById(R.id.button_add_image));
		Button saveEventButton = ((Button)findViewById(R.id.button_create));

		// If we are editing an existing event, preset the input fields with the existing event details.
		if (eventID != 0){
			setEventID(eventID);
        	setTitle(intent.getStringExtra("eventName"));
        	addImageButton.setText("Change event image");
        	saveEventButton.setText("Save Event");
			AutoAsyncTask<Integer, Void, Event> getEventTask = new AutoAsyncTask<Integer, Void, Event>(this) {
				@Override
				protected Event doInBackgroundAuto(Integer... eventIDs)
						throws JSONResponseException, Exception {
					Event event = Event.getById(eventIDs[0]);
					event.setImage(event.getImageFromServer());
					return event;
				}
				@Override
				protected void onPostExecuteAuto(final Event event) {
					super.onPostExecuteAuto(event);
					((EditText)findViewById(R.id.name)).setText(event.getName());
					
					Date startDateTime = event.getStartTime();
					if (startDateTime != null){
						startDateView.setText(Event.friendlyDate(startDateTime));
						startTimeView.setText(Event.friendlyTime(startDateTime));
					}
					if (event.getLocation() != null){
						locationView.setText(event.getLocation());
					}
					if (event.getPrice() != null){
						priceView.setText(event.getPrice().toString());
					}
					if (event.getCapacity() != null){
						capacityView.setText(event.getCapacity().toString());
					}
					if (event.getDescription() != null){
						descriptionView.setText(event.getDescription().toString());
					}
					String imageUrl = event.getImageUrl(); 
					if (imageUrl != null){
						((ImageView) findViewById(R.id.image_view)).setImageBitmap(event.getImage());
					}
				}
			};
			getEventTask.execute(eventID);
		}
		// Otherwise, set the text on the buttons and the title to indicate that we're creating a new event. 
		else {
        	addImageButton.setText("Add an event image");
        	saveEventButton.setText("Create Event");
        	setTitle("Create Event");
		}
		
		// Initialize the load image button
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {             
            @Override
            public void onClick(View arg0) {                 
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        
        // Initialize the save event button
        final Button saveButton = (Button) findViewById(R.id.button_create);
        saveButton.setOnClickListener(new View.OnClickListener() {
        	
        	private Boolean formValid;
			
    		private void emptyFieldError(View fieldView, String fieldName){
    			// Only display the first error as a toast so we don't annoy the user with multiple errors.
    			if (formValid){
    				formValid = false;
    				Toast.makeText(ManageEventActivity.this, "Please enter a valid " + fieldName + ".", Toast.LENGTH_SHORT).show();
    				if (fieldView != null){
    					fieldView.requestFocus();
    				}
    			}
    		}
        	
        	@Override
            public void onClick(View v) {
                EditText nameField = (EditText)findViewById(R.id.name);
                EditText descriptionField = (EditText)findViewById(R.id.description);
                EditText locationField = (EditText)findViewById(R.id.location);
                EditText startDateField = (EditText)findViewById(R.id.start_date);
                EditText startTimeField = (EditText)findViewById(R.id.start_time);
                EditText priceField = (EditText)findViewById(R.id.price);
                EditText capacityField = (EditText)findViewById(R.id.capacity);

                String name = nameField.getText().toString();
                String description = descriptionField.getText().toString();
                String location = locationField.getText().toString();
                String startDate = startDateField.getText().toString();
                String startTime = startTimeField.getText().toString();
                String price = priceField.getText().toString();
                String capacity = capacityField.getText().toString();
                Event event = new Event();
        		if (getEventID() != null){
        			event.setId(getEventID());
        		}
        		
        		formValid = true; // Will be set to false by emptyFieldError if one of the fields are invalid.
        		
        		// Ensure all event fields are filled in before saving/creating an event.
        		
        		// Name
                event.setName(name);
                if (name.equals(""))		emptyFieldError(nameView, "event name");
                
                // Start Date/time
                try {
                	Date startDateTime = Event.friendlyDateTimeToDate(startDate,startTime);
					event.setStartTime(startDateTime);
				} catch (ParseException e) {
                	emptyFieldError(null, "start date/time");
				}
                
                // Location
                event.setLocation(location);
                if (location.equals(""))	emptyFieldError(locationView, "location");
                
                // Price
                try {               		event.setPrice(Double.valueOf(price)); }
                catch (Exception e)	{      	emptyFieldError(priceView, "price"); }
                
                // Capacity
                try	{						event.setCapacity(Integer.parseInt(capacity)); }
                catch (Exception e)	{		emptyFieldError(capacityView, "capacity"); }
                
                // Description
                event.setDescription(description);
                if (description.equals(""))	emptyFieldError(descriptionView, "description");                
                
                if (eventImage != null)		event.setImage(eventImage);
                
                if (formValid){
                	// If all of the fields are valid, create/edit the event. 
	                AutoAsyncTask<Event, Void, Event> task = new AutoAsyncTask<Event, Void, Event>(ManageEventActivity.this){
	                	@Override
	                	protected Event doInBackgroundAuto(Event... events)
	                			throws JSONResponseException, Exception {
	                		return events[0].save();
	                	}
	                	@Override
	                	protected void onPostExecuteAuto(Event event) {
	                		Toast.makeText(context, "Event saved.", Toast.LENGTH_SHORT).show();
	                		Intent intent = new Intent(context, EventActivity.class)
											.putExtra("id", event.getId())
											.putExtra("eventName", event.getName());
	                		startActivity(intent);
	                	}
	                };
	                task.execute(event);
                }
            }
        });
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
        Util util = new Util(this);
        // Set the icons for the Add image and Save event buttons
        Button buttonLoadImage = (Button) findViewById(R.id.button_add_image);
        buttonLoadImage = util.setButtonIcon(buttonLoadImage, R.drawable.icon_event_image, 60, 60);
        Button buttonSaveEvent = (Button) findViewById(R.id.button_create);
        buttonSaveEvent = util.setButtonIcon(buttonSaveEvent, R.drawable.icon_accept, 60, 60);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         
        // If we've received an image from the image selector, use it for the event.
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { Images.Media.DATA };
 
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
 
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String eventImagePath = cursor.getString(columnIndex);
            cursor.close();
             
            ImageView imageView = (ImageView) findViewById(R.id.image_view);
            
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(eventImagePath, options);

            // Calculate inSampleSize
            Integer reqWidth = 1024; 
            Integer reqHeight = 1024; 
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            eventImage = BitmapFactory.decodeFile(eventImagePath, options);
            
            imageView.setImageBitmap(eventImage);

        	((Button)findViewById(R.id.button_add_image)).setText("Change event image");
        }
     
     
    }
    
    public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
    
    public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
    
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    
    /**
     * DialogFragment responsible for selecting the event date.
     * @author Baseer
     *
     */
    public static class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			EditText startDateView = ((EditText)getActivity().findViewById(R.id.start_date));
			try {
				Date time = Event.friendlyDateToDate(startDateView.getText().toString());
				c.setTime(time);
			} catch (ParseException e) {
				// If there was a parse error, use the current date as the default date in the picker
				e.printStackTrace();
			}
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
		
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, day);
			((EditText)getActivity().findViewById(R.id.start_date)).setText(Event.friendlyDate(cal.getTime()));
		}
	}

    /**
     * DialogFragment responsible for selecting the event start time.
     * @author Baseer
     *
     */
    public static class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			EditText startTimeView = ((EditText)getActivity().findViewById(R.id.start_time));
			final Calendar c = Calendar.getInstance();
			try {
				Date time = Event.friendlyTimeToDate(startTimeView.getText().toString());
				c.setTime(time);
			} catch (ParseException e) {
				// If there was a parse error, use the current time as the default time in the picker
				e.printStackTrace();
			}
			int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
		
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hourOfDay, minute, false);
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			((EditText)getActivity().findViewById(R.id.start_time)).setText(Event.friendlyTime(cal.getTime()));
		}
	}
    
}
