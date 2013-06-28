package com.ticketflex.android;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Model.Model;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Model.User;
import com.ticketflex.android.Task.AutoAsyncTask;
import com.ticketflex.android.Task.CAsyncTask;
import com.ticketflex.android.Utils.Util;

/**
 * Activity used by the main event screen. This event screen also shows your ticket details if you have a ticket.
 * If you have created the event however, it also gives you the option to edit the event.
 * @author Baseer
 *
 */
public class EventActivity extends FacebookActivity {
	// This request code is used to identify the resulting intent that is returned from BuyExistingTicketActivity.
	// The intent contains the ticketID of the ticket that the user would like to purchase. 
	final protected static int BUY_EXISTING_TICKET_REQUEST_CODE = 100; 
	private Ticket ticket = null; // Holds the user's Ticket for the Event, if any.
	private Event event = null;

	public void setTicket(Ticket ticket){
		this.ticket = ticket;
	}
	public Ticket getTicket(){
		return this.ticket;
	}
	public void setEvent(Event event){
		this.event = event;
	}
	public Event getEvent(){
		return this.event;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);
		setContentView(R.layout.activity_ticket);
		Intent intent = getIntent();
		Integer eventID = intent.getIntExtra("id", 0);
		String eventName = intent.getStringExtra("eventName");
		setTitle(eventName);
		
		// Disable all buttons because we first need to fetch the event/ticket to determine which buttons should be
		// available.
		disableAllTicketActions();
		findViewById(R.id.buttons_container).setVisibility(View.GONE);
		
		final ImageView eventImage = (ImageView)findViewById(R.id.event_image);
		
		AutoAsyncTask<Integer, Void, Event> getEventTask = new AutoAsyncTask<Integer, Void, Event>(this) {		
			@Override
			protected Event doInBackgroundAuto(Integer... eventIDs) throws ClientProtocolException, JSONResponseException, URISyntaxException, IOException {
				event = Event.getById(eventIDs[0]);
				event.setImage(event.getImageFromServer());
				return event;
			}
			
			@Override
			protected void onPostExecuteAuto(final Event event) {
				setEvent(event);
				eventImage.setImageBitmap(event.getImage());
				Util util = new Util(context);
				
				// Set the button icons.
				Button sellTicketButton = ((Button)findViewById(R.id.button_sell_ticket));
				sellTicketButton = util.setButtonIcon(sellTicketButton, R.drawable.icon_sell_ticket, 40, 40);

				Button unsellTicketButton = ((Button)findViewById(R.id.button_unsell_ticket));
				unsellTicketButton = util.setButtonIcon(unsellTicketButton, R.drawable.icon_unsell_ticket, 40, 40);

				Button buyTicketButton = ((Button)findViewById(R.id.button_buy_ticket));
				buyTicketButton = util.setButtonIcon(buyTicketButton, R.drawable.icon_buy_ticket, 40, 40);
				
				Button buyExistingTicketButton = ((Button)findViewById(R.id.button_buy_existing_ticket));
				buyExistingTicketButton = util.setButtonIcon(buyExistingTicketButton, R.drawable.icon_buy_existing_ticket, 40, 40);

				Button showTicketButton = ((Button)findViewById(R.id.button_show_ticket));
				showTicketButton = util.setButtonIcon(showTicketButton, R.drawable.icon_show_ticket, 42, 42);
				
				Button editEventButton = ((Button)findViewById(R.id.button_edit_event));
				editEventButton = util.setButtonIcon(editEventButton, R.drawable.icon_edit_event, 42, 42);

				// Set the icons for the event detail fields
				TextView locationView = ((TextView)findViewById(R.id.location));
				locationView = util.setTextViewIcon(locationView, R.drawable.icon_location);
				locationView.setText(event.getLocation());
				// Add an onclick action so that when they click on the location, it opens Google Maps to look for it.
				locationView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(	android.content.Intent.ACTION_VIEW,
													Uri.parse("http://maps.google.com/maps?q="+event.getLocation()));
						intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
						startActivity(intent);
					}
				});
				
				TextView startTimeView = ((TextView)findViewById(R.id.start_time));
				startTimeView = util.setTextViewIcon(startTimeView, R.drawable.icon_start_date);
				startTimeView.setText(Event.friendlyDateTime(event.getStartTime()));
				
				TextView priceView = ((TextView)findViewById(R.id.price));
				priceView = util.setTextViewIcon(priceView, R.drawable.icon_price);
				priceView.setText(Util.formatPrice(event.getPrice()));
				
				TextView capacityView = ((TextView)findViewById(R.id.capacity));
				capacityView = util.setTextViewIcon(capacityView, R.drawable.icon_capacity);
				String capacityViewText = "";
				Integer numActiveTickets = event.getNumActiveTickets();
				capacityViewText += event.getNumRemainingTickets() + " tickets remaining";
				if (numActiveTickets != 0){
					capacityViewText += "\n" + numActiveTickets + " currently attending";
				}
				capacityView.setText(capacityViewText);
				
				String description = event.getDescription();
				if (description != null){
					TextView descriptionView = ((TextView)findViewById(R.id.description));
					descriptionView = util.setTextViewIcon(descriptionView, R.drawable.icon_description);
					descriptionView.setText(event.getDescription());
				}
				
				// Now that we have fetched the event, and potentially the user's ticket for the event,
				// enable the buttons that should be enabled.
				updateViewWithMyTicket(event.getMyTicket());
			}
		};
		getEventTask.execute(eventID);
	}
	
	/**
	 * Disable all buttons. Used in the initial creation of this Activity so that we can enable a few selected ones
	 * based on the server's response and the user's permissions.
	 */
	private void disableAllTicketActions(){
		Button BuyButton = ((Button)findViewById(R.id.button_buy_ticket));
		Button BuyExistingButton = ((Button)findViewById(R.id.button_buy_existing_ticket));
		Button SellButton = ((Button)findViewById(R.id.button_sell_ticket));
		Button UnsellButton = ((Button)findViewById(R.id.button_unsell_ticket));
		Button ShowTicketButton = ((Button)findViewById(R.id.button_show_ticket));
		Button EditEventButton = ((Button)findViewById(R.id.button_edit_event));
		SellButton.setEnabled(false);
		SellButton.setVisibility(View.GONE);
		UnsellButton.setEnabled(false);
		UnsellButton.setVisibility(View.GONE);
		ShowTicketButton.setEnabled(false);
		ShowTicketButton.setVisibility(View.GONE);
		BuyButton.setEnabled(false);
		BuyButton.setVisibility(View.GONE);
		BuyExistingButton.setEnabled(false);
		BuyExistingButton.setVisibility(View.GONE);
		EditEventButton.setEnabled(false);
		EditEventButton.setVisibility(View.GONE);
	}
	
	/**
	 * Show/hide specific action buttons based on the status of the user's ticket. If the user does not have a ticket
	 * for this event, then pass in myTicket = null.
	 * @param myTicket
	 */
	public void updateViewWithMyTicket(Ticket myTicket){
		setTicket(myTicket);
		Button BuyButton = ((Button)findViewById(R.id.button_buy_ticket));
		Button BuyExistingButton = ((Button)findViewById(R.id.button_buy_existing_ticket));
		Button SellButton = ((Button)findViewById(R.id.button_sell_ticket));
		Button UnsellButton = ((Button)findViewById(R.id.button_unsell_ticket));
		Button ShowTicketButton = ((Button)findViewById(R.id.button_show_ticket));
		Button EditEventButton = ((Button)findViewById(R.id.button_edit_event));
		//TextView statusView = ((TextView) findViewById(R.id.label_status));
		//statusView.setText("");

		disableAllTicketActions();
		findViewById(R.id.buttons_container).setVisibility(View.VISIBLE);
		
		if (myTicket != null){
			// If the user has an active ticket for the event, show the Sell button and the Show Ticket button. 
			if (myTicket.isActive()){
				SellButton.setEnabled(true);
				SellButton.setVisibility(View.VISIBLE);
				ShowTicketButton.setEnabled(true);
				ShowTicketButton.setVisibility(View.VISIBLE);
				SellButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
				        DialogFragment dialog = new SellTicketDialogFragment(EventActivity.this, getTicket(), getEvent());
				        dialog.show(getSupportFragmentManager(), "SellTicketDialogFragment");
					}
				});
				ShowTicketButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// Show the QR code of this ticket
						IntentIntegrator integrator = new IntentIntegrator(EventActivity.this);
						integrator.shareText(ticket.getQRCode(), "TEXT_TYPE");
					}
				});
			}
			// If the user has already been admitted, they can no longer perform any actions on the event.
			// Leave all buttons disabled.
			else if (myTicket.isAdmitted()){
				findViewById(R.id.buttons_container).setVisibility(View.GONE);
			}
			// If the user has put their ticket up for sale, show the Unsell Ticket button.
			else if (myTicket.isOnSale()){
				UnsellButton.setEnabled(true);
				UnsellButton.setVisibility(View.VISIBLE);
				UnsellButton.setText("Unsell "+Util.formatPrice(getTicket().getPrice())+" Ticket");
				UnsellButton.setOnClickListener(new View.OnClickListener() {							
					@Override
					public void onClick(View v) {
						AutoAsyncTask<Integer, Void, Ticket> unsellTicketTask = new AutoAsyncTask<Integer, Void, Ticket>(EventActivity.this) {
							@Override
							protected Ticket doInBackgroundAuto(
									Integer... ticketIDs)
									throws JSONResponseException, Exception {
								return Ticket.unsell(ticketIDs[0]);
							}
							@Override
							protected void onPostExecuteAuto(Ticket newTicket) {
								Toast.makeText(context, "You got your ticket back.", Toast.LENGTH_SHORT).show();
								updateViewWithMyTicket(newTicket);
							}
						};
						unsellTicketTask.execute(getTicket().getId());
					}
				});
			}
		}
		else {
			User currentUser = Model.getCurrentUser();
			// If the user does not have a ticket for the event, and the user is not the creator of the event,
			// show the buy ticket buttons.
			if (currentUser == null || currentUser.getId().intValue() != getEvent().getCreatorID().intValue()){
				BuyButton.setEnabled(true);
				BuyButton.setVisibility(View.VISIBLE);
				BuyButton.setOnClickListener(new View.OnClickListener() {							
					@Override
					public void onClick(View v) {
				        DialogFragment dialog = new BuyTicketDialogFragment(getEvent());
				        dialog.show(getSupportFragmentManager(), "BuyTicketDialogFragment");
					}
				});
				
				BuyExistingButton.setEnabled(true);
				BuyExistingButton.setVisibility(View.VISIBLE);
				BuyExistingButton.setOnClickListener(new View.OnClickListener() {							
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(EventActivity.this, BuyExistingTicketActivity.class)
										.putExtra("id", getEvent().getId())
										.putExtra("eventName", getEvent().getName());
						startActivityForResult(intent, BUY_EXISTING_TICKET_REQUEST_CODE);
					}
				});
			}
			// If the user is the creator of the event, show the Edit Event button since they have permission to
			// edit it.
			else {
				EditEventButton.setEnabled(true);
				EditEventButton.setVisibility(View.VISIBLE);
				EditEventButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(EventActivity.this, ManageEventActivity.class)
											.putExtra("id", getEvent().getId())
											.putExtra("eventName", getEvent().getName());
						startActivity(intent);
					}
				});
			}
		}
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
    	// If the BuyExistingTicketActivity returned an Intent containing a ticketID, it means that the user
    	// has selected an existing ticket that they'd like to purchase. Buy that selected ticket.
    	if (requestCode == BUY_EXISTING_TICKET_REQUEST_CODE){
    		if (resultCode == RESULT_OK) {
    			new AutoAsyncTask<Integer, Void, Ticket>(this) {
    				@Override
    				protected Ticket doInBackgroundAuto(Integer... ticketIDs) throws JSONResponseException {
    					return Ticket.buyExistingTicket(ticketIDs[0]);
    				}
    				@Override
    				protected void onPostExecuteAuto(Ticket ticket) {
    					String sellerName = data.getStringExtra("sellerName");
    					Toast.makeText(context, "Ticket purchased from "+sellerName+".", Toast.LENGTH_SHORT).show();  					
    					updateViewWithMyTicket(ticket);
    				}
    			}.execute(data.getIntExtra("ticketID", 0));
    		}
    	}
    };

    /**
     * DialogFragment responsible for showing a confirmation dialog box when buying a new ticket.
     * @author Baseer
     *
     */
	public class BuyTicketDialogFragment extends DialogFragment {
		public Event event;
		public BuyTicketDialogFragment(Event event) {
			super();
			this.event = event;
		}
		public Event getEvent(){
			return this.event;
		}
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("Buy this ticket for " + Util.formatPrice(getEvent().getPrice()) + "?")
	               .setPositiveButton(R.string.button_buy_ticket, new DialogInterface.OnClickListener() {
	            	   	public void onClick(DialogInterface dialog, int id) {
	            	   		// They've confirmed their new ticket purchase. Buy a new ticket. 
	            	   		CAsyncTask<Integer, Void, Ticket> buyTicketTask = new BuyTicketTask(EventActivity.this);
	            	   		buyTicketTask.execute(getEvent().getId());
	            	   	}
	               })
	               .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
		
		/**
		 * Async Task responsible for buying a ticket, given an event ID.
		 * @author Baseer
		 *
		 */
		public class BuyTicketTask extends AutoAsyncTask<Integer, Void, Ticket> {
			public BuyTicketTask(Context context) {
				super(context);
			}
			@Override
			protected Ticket doInBackgroundAuto(Integer... eventIDs) throws JSONResponseException {
				return Ticket.buy(eventIDs[0]);
			}
			@Override
			protected void onPostExecuteAuto(Ticket ticket) {
				Toast.makeText(context, "Ticket purchased.", Toast.LENGTH_SHORT).show();
				updateViewWithMyTicket(ticket);
			}
		}
	}
}
