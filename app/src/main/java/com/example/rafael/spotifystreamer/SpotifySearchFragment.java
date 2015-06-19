package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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
import android.widget.Toast;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifySearchFragment extends Fragment {
    @InjectView(R.id.search_artist) SearchView spotifySearch;
    @InjectView(R.id.spotify_search_list) ListView spotifyList;
    private List<MyArtist> storedList;
    private FancyAdapter fancyAdapter;
    private String recoveredFilter = "";

    public SpotifySearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);


        spotifySearch.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // Start searching on submit
                        // ...
                        /**
                         *  We filter artist on each change of the EditText
                         *  I´m not sure if it is the right approach but it was the more friendly
                         *  I could think of
                         */
                        if (!query.contentEquals("") && !query.contentEquals(recoveredFilter)) {
                            searchArtist();
                        }
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        /**
                         *  We filter artist on each change of the EditText
                         *  I´m not sure if it is the right approach but it was the more friendly
                         *  I could think of
                         */
                        if (!newText.contentEquals("") && !newText.contentEquals(recoveredFilter)) {
                            searchArtist();
                        }
                        return true;
                    }
                });



        spotifyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistID = fancyAdapter.getItem(position).artistId;
                Log.d("ID", artistID);
                Intent i = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artistID);
                i.putExtra("artist", fancyAdapter.getItem(position).artistName);
                startActivity(i);
            }
        });

        return rootView;
    }



    private void searchArtist() {
        retrieveSpotifyData spotifyTask = new retrieveSpotifyData();
        spotifyTask.execute(spotifySearch.getQuery().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get saved datasource if present
        if (savedInstanceState != null) {
            storedList = savedInstanceState.getParcelableArrayList("artist");
            recoveredFilter = savedInstanceState.getString("filter");

            fancyAdapter = new FancyAdapter();
            spotifyList.setAdapter(fancyAdapter);
            fancyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("artist", (ArrayList<? extends Parcelable>) storedList);
        outState.putString("filter", spotifySearch.getQuery().toString());
    }

    class FancyAdapter extends ArrayAdapter<MyArtist> {
        FancyAdapter(){
            super(getActivity(), android.R.layout.simple_list_item_1, storedList);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;

            int resource;
            LayoutInflater inflater = getActivity().getLayoutInflater();


            if (convertView == null) {
                resource = R.layout.spotify_list_artist;

                convertView = inflater.inflate(resource, parent, false);
                holder = new ViewHolder(convertView);

                holder.populateFrom(storedList.get(position));
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
                holder.populateFrom(storedList.get(position));
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

        void populateFrom(MyArtist artist){
            name.setText(artist.artistName);
            if (artist.artistImage != null){
                Picasso.with(getActivity()).load(artist.artistImage).into(thumbnail);
            }
            if (artist.backImage != null){
                Picasso.with(getActivity()).load(artist.backImage).into(back);
            }
        }
    }


    public class retrieveSpotifyData extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {

            ArtistsPager results = null;

            // We pass the filter text and call spotify wrapper API to get the artist´s
            try{
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                results = spotify.searchArtists(params[0]);
            }catch (RetrofitError e){
                ToastText(getActivity().getResources().getString(R.string.spotifyError) + " " + e.toString());
            }


            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager result) {
            super.onPostExecute(result);


            //We dd the results to a list and call the adapter so the view get´s updated

            if (result != null) {
                if (storedList == null) {
                    storedList = new ArrayList<MyArtist>();
                } else {
                    storedList.clear();
                }

                for(Artist artist : result.artists.items){
                    storedList.add(new MyArtist(artist));
                }

                if (storedList.size() == 0){
                    ToastText(getActivity().getResources().getString(R.string.notFound));
                }else{
                    fancyAdapter = new FancyAdapter();
                    spotifyList.setAdapter(fancyAdapter);
                    fancyAdapter.notifyDataSetChanged();
                }

            }


        }

        void ToastText(String s){
            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        }


    }
}
