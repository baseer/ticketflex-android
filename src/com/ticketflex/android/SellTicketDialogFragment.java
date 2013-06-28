package com.ticketflex.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ticketflex.android.Model.Event;
import com.ticketflex.android.Model.Ticket;
import com.ticketflex.android.Task.CAsyncTask;

/**
 * DialogFragment responsible for showing a dialog box that allows the user to put their ticket up for sale.
 * Within the dialog box, the user will need to select a price at which the ticket will be sold at.
 * @author Baseer
 *
 */
public class SellTicketDialogFragment extends DialogFragment {
	public Context context;
	public Ticket ticket;
	public Event event;
	//public Boolean wasCancelled = false;
	public static Boolean hasInputErrors = false;
	
	public SellTicketDialogFragment(Context context, Ticket ticket, Event event) {
		super();
		this.context = context;
		this.ticket = ticket;
		this.event = event;
	}
	public Ticket getTicket(){
		return this.ticket;
	}
	public Event getEvent(){
		return this.event;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Put ticket up for sale?");
        // Create a price field and add it to the dialog
        final EditText priceInput = new EditText(context);
        priceInput.setHint("$ Price");
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(priceInput);
        priceInput.requestFocus(); // Focus on the price field as soon as the dialog opens
        
        builder.setPositiveButton("Sell Ticket", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
    	   		Ticket ticket = new Ticket();
    	   		ticket.setId(getTicket().getId());
    	   		Boolean isValid = true;
    	   		Double price = null;
    	   		try {
    	   			price = Double.valueOf(priceInput.getText().toString());
    	   		}
    	   		catch (Exception e){
    	   			isValid = false;
    	   			Toast.makeText(context, "Enter a valid price.", Toast.LENGTH_SHORT).show();
    	   		}
    	   		if (isValid){
    	   			ticket.setPrice(price);
    	   			// Put the users ticket up for sale with the price they've chosen.
					CAsyncTask<Ticket, Void, Ticket> sellTicketTask = new SellTicketTask(context);
    	   			sellTicketTask.execute(ticket);
    	   		}
    	   		else {
    	   			// If there are errors, set hasInputErrors to true so that onDismiss() will show the dialog again
    	   			// so that the user can fix the errors.
    	   			SellTicketDialogFragment.hasInputErrors = true;
    	   		}
			}
		});
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Dialog will automatically be dismissed.
			}
		});
        
		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }
	/*@Override
	public void onCancel(DialogInterface dialog) {
		this.wasCancelled = true;
	};*/
	
	@Override
	public void onDismiss(DialogInterface dialogInterface) {
		if (hasInputErrors){
			this.getDialog().show();
			hasInputErrors = false;
		}			
	}
}