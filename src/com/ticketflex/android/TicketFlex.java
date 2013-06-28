package com.ticketflex.android;


import android.app.Application;
import android.content.Context;

/**
 * Hold an instance of the TicketFlex app for easy access.
 * @author Baseer
 *
 */
public class TicketFlex extends Application {

    private static TicketFlex instance;

    public TicketFlex() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}
