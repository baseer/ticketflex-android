package com.ticketflex.android;

import com.facebook.FacebookActivity;
import com.ticketflex.android.R;
import com.ticketflex.android.Model.Model;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.*;

/**
 * Activity used by the main screen.
 * @author Baseer
 *
 */
public class MainActivity extends FacebookActivity {
	/**
	 * Result code returned by the LoginActivity when the user logs in. You can implement the method
	 * MainActivity.onActivityResult() to perform actions once the user has logged in. See the Android docs for more
	 * info on how to use onActivityResult().
	 */
	public static Integer LOGIN_ACTIVITY_RESULT = 100;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize all buttons by setting their destination activities as well as the button background colours.
        final Button LoginButton = (Button)findViewById(R.id.button_login);
        if (Model.getCurrentUser() != null){
        	LoginButton.setText("Logout");
        }
		LoginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_ACTIVITY_RESULT);
			}
		});
		LoginButton.setBackgroundDrawable(menuItemStates());
		
		final Button myTicketsButton = (Button)findViewById(R.id.button_my_tickets);
		myTicketsButton.setOnClickListener(menuButtonListener(new Intent(this, MyTicketsActivity.class)));
		myTicketsButton.setBackgroundDrawable(menuItemStates());

		final Button eventsAdminButton = (Button)findViewById(R.id.button_events_admin);
		eventsAdminButton.setOnClickListener(menuButtonListener(new Intent(this, EventsAdminActivity.class)));
		eventsAdminButton.setBackgroundDrawable(menuItemStates());

		final Button eventsIndexButton = (Button)findViewById(R.id.button_events_index);
		eventsIndexButton.setOnClickListener(menuButtonListener(new Intent(this, EventsIndexActivity.class)));
		eventsIndexButton.setBackgroundDrawable(menuItemStates());
    }
    
    /**
     * Return a set of states that can be attached to the menu buttons.
     * These states have a normal state and an on-press state.
     * @return StateListDrawable
     */
    private StateListDrawable menuItemStates(){
    	StateListDrawable buttonStates = new StateListDrawable();
		buttonStates.addState(new int[] {android.R.attr.state_pressed},
				new ColorDrawable(Color.argb(204, 16, 75, 130)));
		buttonStates.addState(new int[] { },
				new ColorDrawable(Color.argb(204, 4, 110, 180)));
		return buttonStates;
    }
    
    
    private View.OnClickListener menuButtonListener(final Intent intent){
    	return new View.OnClickListener(){
    		public void onClick(View v){
    			startActivity(intent);
    		}
    	};
    }
}
