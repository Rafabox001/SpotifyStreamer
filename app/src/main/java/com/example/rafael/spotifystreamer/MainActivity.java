package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.rafael.spotifystreamer.fragments.SpotifySearchFragment;
import com.example.rafael.spotifystreamer.fragments.TopTracksActivityFragment;


public class MainActivity extends AppCompatActivity implements SpotifySearchFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_IMAGE = "com.example.rafael.spotifystreamer.extraImage";
    private static final String EXTRA_TITLE = "com.example.rafael.spotifystreamer.extraTitle";

    public Boolean mTwoPane = false;

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
        searchFragment.isTwoPane(mTwoPane);

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
            args.putBoolean("mTwoPane", mTwoPane);

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
            i.putExtra("mTwoPane", mTwoPane);
            startActivity(i);
        }

    }

    @Override
    public void onBackPressed() {
        Log.d("back press", "BACK");
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
