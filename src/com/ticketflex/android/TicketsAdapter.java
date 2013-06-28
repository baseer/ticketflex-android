package com.ticketflex.android;

import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Utils.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The Adapter responsible for displaying a list of tickets.
 * @author Baseer
 *
 */
public class TicketsAdapter extends ArrayAdapter<Ticket>{

	public TicketsAdapter(Context context, Ticket[] objects) {
		super(context, R.id.name, objects);
	}

	/**
	 * Return the View used to show a single ticket within a list of tickets.
	 */
	@Override
	public View getView(int position, View convertView,
			ViewGroup parent) {
		View row = convertView;
		if (row == null){
			LayoutInflater mInflater = LayoutInflater.from(getContext());
			// Use the list_item_ticket layout.
			row = mInflater.inflate(R.layout.list_item_ticket, null);
			Ticket ticket = getItem(position);
			Bitmap image = ticket.getUser().getImage();
			// Resize the seller's image.
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
			// Show the user's thumbnail, name, and ticket price.
			((ImageView) row.findViewById(R.id.ticketThumbnail)).setImageBitmap(scaledThumbnail);
			((TextView) row.findViewById(R.id.name)).setText(ticket.getUser().getName());
			((TextView) row.findViewById(R.id.price)).setText(Util.formatPrice(ticket.getPrice()));
			
		}
		return row;
	}	
}
