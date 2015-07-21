package com.example.rafael.spotifystreamer;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class TopTracksActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private static final String EXTRA_IMAGE = "com.example.rafael.spotifystreamer.extraImage";
    private static final String EXTRA_TITLE = "com.example.rafael.spotifystreamer.extraTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_IMAGE);
        supportPostponeEnterTransition();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String itemTitle = getIntent().getStringExtra(EXTRA_TITLE);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(itemTitle);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        final ImageView image = (ImageView)findViewById(R.id.image);
        Picasso.with(this).load(getIntent().getStringExtra(EXTRA_IMAGE)).into(image, new Callback() {

            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener(){
                   public void onGenerated(Palette palette){
                        applyPalette(palette);
                    }
                });
            }

            @Override
            public void onError() {

            }
        });



    }

    private void applyPalette(Palette palette){
        int primary_dark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(primary_dark));
        updateBackground((FloatingActionButton) findViewById(R.id.fab), palette);
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

}
