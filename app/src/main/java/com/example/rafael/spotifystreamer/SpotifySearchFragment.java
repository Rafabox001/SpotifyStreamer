package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifySearchFragment extends Fragment {
    @InjectView(R.id.spotify_search_text) EditText spotifySearch;
    @InjectView(R.id.spotify_search_list) ListView spotifyList;
    private List<Artist> artistsList;
    private FancyAdapter fancyAdapter;

    public SpotifySearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        spotifySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                /**
                 *  We filter artist on each change of the EditText
                 *  I´m not sure if it is the right approach but it was the more friendly
                 *  I could think of
                 */
                if (!spotifySearch.getText().toString().contentEquals("")) {
                    searchArtist();
                }

            }
        });

        spotifyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistID = fancyAdapter.getItem(position).id;
                Log.d("ID", artistID);
                Intent i = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artistID);
                i.putExtra("artist", fancyAdapter.getItem(position).name);
                startActivity(i);
            }
        });

        return rootView;
    }



    private void searchArtist() {
        retrieveSpotifyData spotifyTask = new retrieveSpotifyData();
        spotifyTask.execute(spotifySearch.getText().toString());
    }

    class FancyAdapter extends ArrayAdapter<Artist> {
        FancyAdapter(){
            super(getActivity(), android.R.layout.simple_list_item_1, artistsList);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;

            int resource;
            LayoutInflater inflater = getActivity().getLayoutInflater();


            if (convertView == null) {
                resource = R.layout.spotify_list_artist;

                convertView = inflater.inflate(resource, parent, false);
                holder = new ViewHolder(convertView);

                holder.populateFrom(artistsList.get(position));
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
                holder.populateFrom(artistsList.get(position));
            }

            return convertView;
        }
    }

    class ViewHolder{
        @InjectView(R.id.artistName) TextView name;
        @InjectView(R.id.artistThumbnail) ImageView thumbnail;
        @InjectView(R.id.back) ImageView back;


        ViewHolder(View row){
            ButterKnife.inject(this, row);

        }

        void populateFrom(Artist artist){
            name.setText(artist.name);
            List<Image> img = artist.images;
            int sizeOfList = img.size();
            Log.d("Size", String.valueOf(sizeOfList));
            if (sizeOfList > 0){
                Picasso.with(getActivity()).load(artist.images.get(sizeOfList-1).url).into(thumbnail);
                Picasso.with(getActivity()).load(artist.images.get(1).url).into(back);
            }



        }
    }


    public class retrieveSpotifyData extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {

            // We pass the filter text and call spotify wrapper API to get the artist´s

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);


            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager result) {
            super.onPostExecute(result);

            //We dd the results to a list and call the adapter so the view get´s updated

            if (result != null) {
                if (artistsList == null) {
                    artistsList = new ArrayList<Artist>();
                } else {
                    artistsList.clear();
                }

                artistsList = result.artists.items;
                fancyAdapter = new FancyAdapter();
                spotifyList.setAdapter(fancyAdapter);
                fancyAdapter.notifyDataSetChanged();
            }


        }


    }
}
