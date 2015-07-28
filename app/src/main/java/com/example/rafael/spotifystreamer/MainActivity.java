package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.rafael.spotifystreamer.fragments.SpotifySearchFragment;
import com.example.rafael.spotifystreamer.fragments.TopTracksActivityFragment;


public class MainActivity extends AppCompatActivity implements SpotifySearchFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_IMAGE = "com.example.rafael.spotifystreamer.extraImage";
    private static final String EXTRA_TITLE = "com.example.rafael.spotifystreamer.extraTitle";

    private Boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();


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
        SpotifySearchFragment searchFragment =  ((SpotifySearchFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment));

    }

    private void initToolbar(){
        final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        toolbar.setTitleTextColor(getResources().getColor(R.color.actionBarText));

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }


    @Override
    public void onItemSelected(String artistId, String artistName, String artistArt) {
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
            i.putExtra(EXTRA_TITLE, artistName);
            i.putExtra(EXTRA_IMAGE, artistArt);
            startActivity(i);
        }

    }
}
