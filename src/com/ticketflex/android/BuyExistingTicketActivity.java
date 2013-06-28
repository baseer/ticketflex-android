package com.ticketflex.android;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Model.User;
import com.ticketflex.android.Task.AutoAsyncTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity used to show the list of existing tickets that are put on sale.
 * This activity can be reached from the View Event screen.
 * @author Baseer
 *
 */
public class BuyExistingTicketActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.activity_buy_existing_ticket);
		Intent intent = getIntent();
		Integer eventID = intent.getIntExtra("id", 0);
		String eventName = intent.getStringExtra("eventName");
		setTitle("Purchase Ticket for " + eventName);
		
		AutoAsyncTask<Integer, Void, Ticket[]> getTickets = new AutoAsyncTask<Integer, Void, Ticket[]>(this) {
			@Override
			protected Ticket[] doInBackgroundAuto(Integer... eventIDs) throws JSONResponseException, ClientProtocolException, IOException, URISyntaxException {
				Ticket[] tickets = Ticket.getAllOnSale(eventIDs[0]);
				for (Ticket ticket:tickets){
					User user = ticket.getUser();
					// Fetch the ticket seller's profile pictures from Facebook.
					user.setImage(user.getImageFromFacebook());
				}
				return tickets;
			}
			@Override
			protected void onPostExecuteAuto(final Ticket[] tickets) {
				if (tickets.length == 0){
					Toast.makeText(context, "No one is selling tickets right now.", Toast.LENGTH_SHORT).show();
				}
				ListView lv = (ListView)findViewById(R.id.TicketsList);

				// Show the tickets in the list view.
				lv.setAdapter(new TicketsAdapter(context, tickets));
    			
				// If a ticket is clicked on, send the selected ticketID back to the calling activity. In this case,
				// the calling activity is EventActivity. EventActivity will then take care of purchasing the selected
				// ticket.
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view,
							int position, long id) {
						Integer ticketID = tickets[position].getId();
		        		Intent resultIntent = new Intent();
		        		resultIntent.putExtra("ticketID", ticketID);
		        		resultIntent.putExtra("sellerName", tickets[position].getUser().getName());
		        		setResult(Activity.RESULT_OK, resultIntent);
		        		finish();
					}					
				});
			}
		};
		getTickets.execute(eventID);
	}
}
