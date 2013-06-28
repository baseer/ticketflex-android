package com.ticketflex.android;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Task.AutoAsyncTask;
import com.ticketflex.android.Task.CAsyncTask;
import com.ticketflex.android.Utils.Util;

/**
 * The Activity used by the Event Admin page, which can be reached by clicking on an event on the
 * Administer My Events screen.
 * @author Baseer
 *
 */
public class EventAdminActivity extends FacebookActivity {
	private Integer eventID = null;

	public Integer getEventId(){
		return this.eventID;
	}
	public void setEventId(Integer eventID){
		this.eventID = eventID;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_event_admin);

        findViewById(R.id.button_scan_ticket).setVisibility(View.GONE);
        findViewById(R.id.button_event_details).setVisibility(View.GONE);

        Integer eventID = getIntent().getIntExtra("id", 0);
        if (eventID == 0){
        	eventID = null;
        }
        setEventId(eventID);
        if (eventID != null){
        	setTitle(getIntent().getStringExtra("eventName") + " Admin");
        }       
      
        CAsyncTask<Integer, Void, Event> getEventTask = new GetEventTask(this);
        getEventTask.execute(eventID);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    	if (scanResult != null) {
			// The user just scanned a ticket's QR code, use the QR code to fetch the ticket details 
			String qrCode = scanResult.getContents();//data.getStringExtra("SCAN_RESULT");
			new GetTicketTask(this).execute(qrCode);
    	}
    };
    
    /**
     * AsyncTask responsible for fetching the event and enabling the buttons in the view.
     * @author Baseer
     *
     */
    public class GetEventTask extends AutoAsyncTask<Integer, Void, Event>{
    	public GetEventTask(Context context) {
			super(context);
		}
		@Override
    	protected Event doInBackgroundAuto(Integer... eventIDs) throws ClientProtocolException, JSONResponseException, URISyntaxException, IOException {
    		Event event = Event.getById(eventIDs[0]); // Get the event
    		// Get all of the tickets for the event, which will be used to populate the guest list. 
    		Ticket[] tickets = event.getTickets();
    		for (Ticket ticket:tickets){
    			Bitmap profilePic = ticket.getUser().getImageFromFacebook();
    			ticket.getUser().setImage(profilePic);
    		}
    		return event;
    	}
    	@Override
    	protected void onPostExecuteAuto(Event event) {
            Util util = new Util(context);
            
            // Initialize the Scan Ticket button.
            Button scanTicketButton = (Button)findViewById(R.id.button_scan_ticket);
            scanTicketButton.setVisibility(View.VISIBLE);
            scanTicketButton = util.setButtonIcon(scanTicketButton, R.drawable.icon_show_ticket, 40, 40);
            scanTicketButton.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View arg0) {
    				IntentIntegrator integrator = new IntentIntegrator(EventAdminActivity.this);
    				integrator.initiateScan();
    			}
    		});
            
            // Initialize the View Event button.            
            Button eventDetailsButton = (Button)findViewById(R.id.button_event_details);
            eventDetailsButton.setVisibility(View.VISIBLE);
            eventDetailsButton = util.setButtonIcon(eventDetailsButton, R.drawable.icon_event_details, 52, 52);
            eventDetailsButton.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View arg0) {
    				Integer eventID = EventAdminActivity.this.getEventId();
    				Intent eventDetailsIntent = new Intent(EventAdminActivity.this, EventActivity.class)
    											.putExtra("id", eventID)
    											.putExtra("eventName", getIntent().getStringExtra("eventName"));
    				startActivity(eventDetailsIntent);
    			}
    		});
            
    		ListView listView = (ListView) findViewById(R.id.event_admin_guest_list);
            // Initialize the array of tickets to be used in the guest list.
    		Ticket[] allTickets = event.getTickets();
    		ArrayList<Ticket> ticketsList = new ArrayList<Ticket>();
    		for (Ticket ticket:allTickets){
    			// Only display those tickets that are either active or admitted.
    			// We don't want to display "on sale" tickets on the guest list.
    			if (ticket.isActive() || ticket.isAdmitted()){
    				ticketsList.add(ticket);
    			}
    		}
    		final Ticket[] tickets = ticketsList.toArray(new Ticket[0]);
            // Populate the guest list view given an array of tickets.
    		ArrayAdapter<Ticket> guestListAdapter = new ArrayAdapter<Ticket>(context, 0, tickets){
    			@Override
    			public View getView(int position, View convertView,
    					ViewGroup parent) {
    				View row = convertView;
    				if (row == null){
    					LayoutInflater mInflater = LayoutInflater.from(context);
    					// Use the list_item_guest view to display each person on the guest list.
						row = mInflater.inflate(R.layout.list_item_guest, null);
						// Set the user's profile picture.
						((ImageView) row.findViewById(R.id.userThumbnail)).setImageBitmap(tickets[position].getUser().getImage());
						// Set the user's name
						((TextView) row.findViewById(R.id.name)).setText(tickets[position].getUser().getName());
						String displayStatus = "";
						TextView statusView = ((TextView) row.findViewById(R.id.status));
						// If the ticket is marked as active, it hasn't been scanned yet. Indicate that the attendee hasn't
						// arrived yet. 
						if (tickets[position].isActive()){
							displayStatus = "Not admitted yet.";
						}
						// If the ticket has been admitted, indicate so in green.
						else if (tickets[position].isAdmitted()){
							displayStatus = "Admitted";
							statusView.setTextColor(Color.rgb(0, 120, 0));
						}
						statusView.setText(displayStatus);
    				}
    				return row;
    			}
    		};
    		listView.setAdapter(guestListAdapter);
    	}
    }

    /**
     * This task will be called when a qr code is scanned from a ticket. It is responsible for fetching the full
     * details of a ticket given a qr code, determining whether the ticket is valid, and opening the admit/decline
     * ticket dialog box if necessary.
     * @author Baseer
     *
     */
    public class GetTicketTask extends AutoAsyncTask<String, Void, Ticket>{
    	public GetTicketTask(Context context) {
			super(context);
		}
    	@Override
    	protected Ticket doInBackgroundAuto(String... ticketQRCodes) throws JSONResponseException {
    		return Ticket.getByQrCode(ticketQRCodes[0]);
    	}
    	@Override
    	protected void onPostExecuteAuto(Ticket ticket) {
    		Integer ticketEventID = ticket.getEvent().getId();
    		// If the scanned ticket is valid, open a dialog that can be used to admit the attendee into the event.
    		if (ticket.isActive() && ticketEventID == EventAdminActivity.this.getEventId()){
    			DialogFragment dialog = new EventAdminActivity.TicketScanDialogFragment(context, ticket);
    			dialog.show(getSupportFragmentManager(), "TicketScanDialogFragment");
    		}
    		// If the ticket is not for this event, say so.
    		else if (ticketEventID != EventAdminActivity.this.getEventId()){
    			Toast.makeText(context, "That ticket is not for this event.", Toast.LENGTH_SHORT).show();
    		}
    		// If the ticket is not currently active, say so.
    		else {
    			if (ticket.isAdmitted()){
    				Toast.makeText(context, ticket.getUser().getName() + " has already been admitted.", Toast.LENGTH_SHORT).show();
    			}
    			else if (ticket.isOnSale()){
    				Toast.makeText(context, "Cannot admit that ticket because it is on sale.", Toast.LENGTH_SHORT).show();
    			}
    		}
    	}
    }
    
    /**
     * Class responsible for the admit/decline ticket dialog box that opens up when a ticket is scanned. 
     * @author Baseer
     *
     */
    public class TicketScanDialogFragment extends DialogFragment {
    	public Context context;
    	public Ticket ticket;
    	public TicketScanDialogFragment(Context context, Ticket ticket){
    		this.context = context;
    		this.ticket = ticket;
    	}
    	
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Build the dialog and set up the button click handlers
        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Admit "+ticket.getUser().getName()+"?");
			
			// Add the buttons
			builder.setPositiveButton(R.string.button_admit_ticket, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User clicked Admit button, notify server and update the guest list.
	        	   TicketAction ticketAction = new TicketAction(ticket.getId(), Ticket.ADMIT);
	        	   new TicketActionTask(context).execute(ticketAction);
	           }
	        });
			builder.setNegativeButton(R.string.button_decline_ticket, new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
				   // User pressed the Decline button, notify server and update the guest list.
		    	   TicketAction ticketAction = new TicketAction(ticket.getId(), Ticket.DECLINE);
		    	   new TicketActionTask(context).execute(ticketAction);
			   }
		    });
			
			return builder.create();
        }
    }
    
    /**
     * Represents an admit/decline action on a ticket. Gets passed in to the TicketActionTask to admit/decline a ticket.
     * @author Baseer
     *
     */    
    public class TicketAction {
    	private Integer ticketID;
    	private Integer action;
    	public TicketAction(Integer ticketID, Integer action){
    		this.ticketID = ticketID;
    		this.action = action;
    	}
    	public Integer getTicketID(){
    		return ticketID;
    	}
    	public Integer getAction(){
    		return action;
    	}
    }
    
    /**
     * Class responsible for admitting/declining a ticket, and updating the guest list after.
     * @author Baseer
     *
     */
    public class TicketActionTask extends CAsyncTask<TicketAction, Void, Ticket>{
    	public TicketActionTask(Context context) {
			super(context);
		}
    	@Override
    	protected Ticket doInBackground(TicketAction... ticketActions) {
    		TicketAction ticketAction = ticketActions[0];
    		Ticket ticket = null;
    		try {
    			ticket = Ticket.adminAction(ticketAction.getTicketID(), ticketAction.getAction());
    		}
    		catch (Exception e){
    			exception = e;
    		}
    		return ticket;
    	}
    	@Override
    	protected void onPostExecute(Ticket ticket) {
    		super.onPostExecute(ticket);
    		if (ticket != null && exception == null){
    			if (ticket.isAdmitted()){
    				Toast.makeText(context, ticket.getUser().getName() + " has been admitted.", Toast.LENGTH_SHORT).show();
    			}
    			new GetEventTask(context).execute(getEventId());
    		}
    		else {
    			handleException();
    		}
    	}
    }
}
