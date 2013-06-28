package com.ticketflex.android;

import android.content.Context;
import android.widget.Toast;

import com.ticketflex.android.Exception.JSONResponseException;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Task.AutoAsyncTask;

/**
 * AsyncTask responsible for putting a ticket up for sale.
 * @author Baseer
 *
 */
public class SellTicketTask extends AutoAsyncTask<Ticket, Void, Ticket> {
	public SellTicketTask(Context context) {
		super(context);
	}
	@Override
	protected Ticket doInBackgroundAuto(Ticket... tickets) throws JSONResponseException {
		Ticket ticket = tickets[0];
		return ticket = Ticket.sell(ticket.getId(), ticket.getPrice());
	}
	@Override
	protected void onPostExecuteAuto(Ticket myTicket) {
		Toast.makeText(context, "Ticket put up for sale.", Toast.LENGTH_SHORT).show();
		if (Context.class.isInstance(this.context)){
			((EventActivity)context).updateViewWithMyTicket(myTicket);
		}
	}
}