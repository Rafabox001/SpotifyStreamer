package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements SpotifySearchFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private Boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (findViewById(R.id.songs_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.songs_detail_container, new TopTracksActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }


        } else {
            mTwoPane = false;
        }
        SpotifySearchFragment forecastFragment =  ((SpotifySearchFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment));

    }


    @Override
    public void onItemSelected(String artistId, String artistName) {
        if (mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString("artist", artistName);
            args.putString("artistId", artistId);

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.songs_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();


        }else{
            Intent i = new Intent(this, TopTracksActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, artistId);
            i.putExtra("artist", artistName);
            startActivity(i);
        }

    }
}
