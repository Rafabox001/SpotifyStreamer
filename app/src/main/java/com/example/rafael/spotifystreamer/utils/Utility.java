package com.example.rafael.spotifystreamer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.rafael.spotifystreamer.R;

/**
 * Created by Rafael on 10/08/2015.
 */
public class Utility {

    /** This class would be called just to retieve preference values that would be used
     * for making the request to the spotify API (location), and in the second instance to decide
     * if notification would be shown in task bar and lock screen.
     */

    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key), "US");
    }

    public static boolean isNotificationEnabled(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notification_key), true);
    }
}
