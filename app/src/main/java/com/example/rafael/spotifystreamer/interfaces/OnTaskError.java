package com.example.rafael.spotifystreamer.interfaces;

import retrofit.RetrofitError;

/**
 * Created by Rafael on 10/08/2015.
 */
public interface OnTaskError {
    void onTaskError(RetrofitError error);
}
