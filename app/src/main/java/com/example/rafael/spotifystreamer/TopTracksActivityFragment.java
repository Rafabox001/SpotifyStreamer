package com.example.rafael.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    @InjectView(R.id.songsList) RecyclerView songsList;
    private List<MyTrack> myTrackList;
    private FancyAdapter fancyAdapter;
    private String mId;
    private String recoveredId = "";

    public TopTracksActivityFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
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

        //We get the intent of the previous activity to retrieve the id we�ll use to search the tracks
        Bundle arguments = getArguments();
        songsList.setHasFixedSize(true);

        if (arguments != null){
            mId = arguments.getString("artistId");
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
            songsList.setLayoutManager(glm);


        }else{
            Intent intent = getActivity().getIntent();
            mId = intent.getStringExtra(Intent.EXTRA_TEXT);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            songsList.setLayoutManager(llm);
        }


        if (savedInstanceState != null) {
            myTrackList = savedInstanceState.getParcelableArrayList("tracks");
            recoveredId = savedInstanceState.getString("id");

            fancyAdapter = new FancyAdapter(myTrackList);
            songsList.setAdapter(fancyAdapter);
            fancyAdapter.notifyDataSetChanged();

        }
        if (mId!=null){
            if (recoveredId.contentEquals("") && !recoveredId.contentEquals(mId)){
                getTracks(mId);
            }
        }



        return rootView;
    }

    private void getTracks(String id) {
        retrieveSpotifyData songsTask = new retrieveSpotifyData();
        songsTask.execute(id);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get saved datasource if present
        if (savedInstanceState != null) {
            myTrackList = savedInstanceState.getParcelableArrayList("tracks");
            recoveredId = savedInstanceState.getString("id");

            fancyAdapter = new FancyAdapter(myTrackList);
            songsList.setAdapter(fancyAdapter);
            fancyAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) myTrackList);
        outState.putString("id", mId);
    }

    public class FancyAdapter extends RecyclerView.Adapter<TracksViewHolder> {

        private List<MyTrack> trackList;
        public FancyAdapter(List<MyTrack> trackList){
            this.trackList = trackList;
        }

        @Override
        public int getItemCount() {
            return trackList.size();
        }

        @Override
        public void onBindViewHolder(TracksViewHolder tracksViewHolder, int i) {
            tracksViewHolder.populateFrom(trackList.get(i));

        }

        @Override
        public TracksViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View trackView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.spotify_list_song, viewGroup, false);

            return  new TracksViewHolder(trackView);
        }


    }

    public class TracksViewHolder extends RecyclerView.ViewHolder{
        @InjectView(R.id.albumName) TextView album;
        @InjectView(R.id.songName) TextView song;
        @InjectView(R.id.songThumbnail) ImageView thumbnail;
        @InjectView(R.id.back) ImageView back;


        public TracksViewHolder(View row){
            super(row);
            ButterKnife.inject(this, row);

        }

        void populateFrom(MyTrack track){
            album.setText(track.trackAlbum);
            song.setText(track.trackName);
            if (track.trackImage != null){
                Picasso.with(getActivity()).load(track.trackImage).into(thumbnail);
            }
            if (track.trackBackImage !=null){
                Picasso.with(getActivity()).load(track.trackBackImage).into(back);
            }


        }
    }


    public class retrieveSpotifyData extends AsyncTask<String, Void, Tracks> {

        //private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected Tracks doInBackground(String... params) {

            // We pass the filter text and call spotify wrapper API to get the artist�s
            Tracks tracks = null;

            try{
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                //We use this map as a country parameter to get the top tracks of the desired artist
                Map<String, Object> map = new HashMap<>();
                map.put("country", Locale.getDefault().getCountry());
                tracks = spotify.getArtistTopTrack(params[0], map);
            }catch (RetrofitError e){
                ToastText(getActivity().getResources().getString(R.string.spotifyError) + " " + e.toString());
            }





            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks result) {
            super.onPostExecute(result);

            //We dd the results to a list and call the adapter so the view get�s updated

            if (result != null) {
                if (myTrackList == null) {
                    myTrackList = new ArrayList<>();
                } else {
                    myTrackList.clear();
                }
                for(Track track : result.tracks){
                    myTrackList.add(new MyTrack(track));
                }

                fancyAdapter = new FancyAdapter(myTrackList);
                songsList.setAdapter(fancyAdapter);
                fancyAdapter.notifyDataSetChanged();


            }


        }

        void ToastText(String s){
            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        }


    }
}
