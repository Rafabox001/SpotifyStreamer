package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.rafael.spotifystreamer.fragments.MediaPlayerFragment;
import com.example.rafael.spotifystreamer.fragments.TopTracksActivityFragment;
import com.example.rafael.spotifystreamer.utils.ControllableAppBarLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class TopTracksActivity extends AppCompatActivity implements MediaPlayerFragment.OnFragmentInteractionListener{

    private static final String EXTRA_IMAGE = "com.example.rafael.spotifystreamer.extraImage";
    private static final String EXTRA_TITLE = "com.example.rafael.spotifystreamer.extraTitle";
    @InjectView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @InjectView(R.id.app_bar_layout) ControllableAppBarLayout appBarLayout;
    @InjectView(R.id.image) ImageView image;
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.fragment_songs_container) FrameLayout container;

    public static final String URL_GIF = "http://www.picgifs.com/mini-graphics/mini-graphics/music/mini-graphics-music-124435.gif";

    public static final String TAG_TOP_FRAGMENT = "TopTracksActivityFragment";
    public static final String TAG_MEDIAPLAYER_FRAGMENT = "TopTracksActivityFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_IMAGE);
        supportPostponeEnterTransition();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        String itemTitle = getIntent().getStringExtra(EXTRA_TITLE);
        collapsingToolbarLayout.setTitle(itemTitle);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        Picasso.with(this).load(getIntent().getStringExtra(EXTRA_IMAGE)).into(image, new Callback() {

            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {
                        applyPalette(palette);
                    }
                });
            }

            @Override
            public void onError() {

            }        });

        //Glide.with(this).load(URL_GIF).asGif().placeholder(R.drawable.music).crossFade().into(fab);


        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_songs_container, new TopTracksActivityFragment(), TAG_TOP_FRAGMENT)
                .addToBackStack(TAG_TOP_FRAGMENT)
                .commit();

        appBarLayout.setOnStateChangeListener(new ControllableAppBarLayout.OnStateChangeListener() {
            @Override
            public void onStateChange(ControllableAppBarLayout.State toolbarChange) {
                switch (toolbarChange) {
                    case COLLAPSED:
                        break;
                    case EXPANDED:
                        break;
                    case IDLE: // Just fired once between switching states
                        break;
                }
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Resume", "TopTRacksActivity");

        if (getIntent().getAction() != null){
            String action = getIntent().getAction();
            Log.d("PendingIntent", action);
        }
    }

    public void collapseToolbar(){
        if (appBarLayout.getState() != ControllableAppBarLayout.State.COLLAPSED){
            appBarLayout.collapseToolbar(true);
            //fab.setVisibility(View.GONE);
        }
    }

    private void applyPalette(Palette palette){
        int primary_dark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(primary_dark));
        //updateBackground(fab, palette);
        supportPostponeEnterTransition();

    }

    private void updateBackground(FloatingActionButton fab, Palette palette){
        int ligthVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.accent));

        fab.setRippleColor(ligthVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        try{
            MediaPlayerFragment mediaPlayerFragment = (MediaPlayerFragment)getSupportFragmentManager().findFragmentByTag(TAG_MEDIAPLAYER_FRAGMENT);
            if (mediaPlayerFragment != null && mediaPlayerFragment.isVisible()){
                super.onBackPressed();
            }
        }catch (ClassCastException e){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }




    }


}
