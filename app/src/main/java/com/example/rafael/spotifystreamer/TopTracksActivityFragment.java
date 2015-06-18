package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    @InjectView(R.id.songsList) ListView songsList;
    private List<Track> trackList;
    private FancyAdapter fancyAdapter;
    private String mId;

    public TopTracksActivityFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                break;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.inject(this, rootView);

        //We get the intent of the previous activity to retrieve the id we´ll use to search the tracks
        Intent intent = getActivity().getIntent();
        Bundle extra = intent.getExtras();
        mId = intent.getStringExtra(Intent.EXTRA_TEXT);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.actionBarText), PorterDuff.Mode.SRC_ATOP);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(upArrow);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(extra.getString("artist"));


        getTracks(mId);

        return rootView;
    }

    private void getTracks(String id) {
        retrieveSpotifyData songsTask = new retrieveSpotifyData();
        songsTask.execute(id);
    }

    class FancyAdapter extends ArrayAdapter<Track> {
        FancyAdapter(){
            super(getActivity(), android.R.layout.simple_list_item_1, trackList);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;

            int resource;
            LayoutInflater inflater = getActivity().getLayoutInflater();


            if (convertView == null) {
                resource = R.layout.spotify_list_song;

                convertView = inflater.inflate(resource, parent, false);
                holder = new ViewHolder(convertView);

                if (position < 10){
                    holder.populateFrom(trackList.get(position));
                    convertView.setTag(holder);
                }


            }else {

                holder = (ViewHolder) convertView.getTag();
                holder.populateFrom(trackList.get(position));
            }

            return convertView;
        }
    }

    class ViewHolder{
        @InjectView(R.id.albumName) TextView album;
        @InjectView(R.id.songName) TextView song;
        @InjectView(R.id.songThumbnail) ImageView thumbnail;
        @InjectView(R.id.back) ImageView back;


        ViewHolder(View row){
            ButterKnife.inject(this, row);

        }

        void populateFrom(Track track){
            album.setText(track.album.name);
            song.setText(track.name);
            List<Image> img = track.album.images;
            int sizeOfList = img.size();
            Log.d("Size", String.valueOf(sizeOfList));
            if (sizeOfList > 0){
                Picasso.with(getActivity()).load(track.album.images.get(sizeOfList-1).url).into(thumbnail);
                Picasso.with(getActivity()).load(track.album.images.get(1).url).into(back);
            }



        }
    }


    public class retrieveSpotifyData extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected Tracks doInBackground(String... params) {

            // We pass the filter text and call spotify wrapper API to get the artist´s

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            //We use this map as a country parameter to get the top tracks of the desired artist
            Map<String, Object> map = new HashMap<>();
            map.put("country", Locale.getDefault().getCountry());
            Tracks tracks = spotify.getArtistTopTrack(params[0], map);



            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks result) {
            super.onPostExecute(result);

            //We dd the results to a list and call the adapter so the view get´s updated

            if (result != null) {
                if (trackList == null) {
                    trackList = new ArrayList<Track>();
                } else {
                    trackList.clear();
                }

                trackList = result.tracks;
                fancyAdapter = new FancyAdapter();
                songsList.setAdapter(fancyAdapter);
                fancyAdapter.notifyDataSetChanged();
            }


        }


    }
}
