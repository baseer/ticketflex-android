package com.ticketflex.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.LoggingBehaviors;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.ticketflex.android.Model.Model;
import com.ticketflex.android.Model.User;
import com.ticketflex.android.Task.FacebookLoginTask;

/**
 * Activity used to login via Facebook.
 * @author Baseer
 *
 */
public class LoginActivity extends Activity {
    static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
    TextView textInstructionsOrLink;
    Button buttonLoginLogout;
    Session.StatusCallback statusCallback = new SessionStatusCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);
        buttonLoginLogout = (Button)findViewById(R.id.buttonLoginLogout);
        textInstructionsOrLink = (TextView)findViewById(R.id.instructionsOrLink);

        Settings.addLoggingBehavior(LoggingBehaviors.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, null, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
        }

        updateView();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    private void updateView() {
		setProgressBarIndeterminateVisibility(false);
		// If the user is logged in, show the logout button.
    	if (Model.currentUser != null){
            buttonLoginLogout.setText("Logout");
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        }
    	// Otherwise, show the login button.
    	else {
            textInstructionsOrLink.setText("");
            buttonLoginLogout.setText("Login with Facebook");
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            //Toast.makeText(this, "opening for read", Toast.LENGTH_SHORT).show();
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            //Toast.makeText(this, "opening active session", Toast.LENGTH_SHORT).show();
            Session.openActiveSession(this, true, statusCallback);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        session.closeAndClearTokenInformation();
        User.logout();
        updateView();
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	String facebookToken = session.getAccessToken();
        	// If we have a facebookToken, send it to the TicketFlex app server to receive a TicketFlex access token.
        	if (facebookToken != null && facebookToken != ""){
	        	FacebookLoginTask task = new FacebookLoginTask(LoginActivity.this);
	        	task.execute(facebookToken);
        	}
        }
    }
}
