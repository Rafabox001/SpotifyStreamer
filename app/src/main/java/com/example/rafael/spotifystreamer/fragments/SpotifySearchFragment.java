package com.example.rafael.spotifystreamer.fragments;

import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafael.spotifystreamer.R;
import com.example.rafael.spotifystreamer.utils.MyArtist;
import com.example.rafael.spotifystreamer.utils.RecyclerItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifySearchFragment extends Fragment {
    @InjectView(R.id.search_artist) SearchView spotifySearch;
    @InjectView(R.id.spotify_search_list) RecyclerView spotifyList;
    private List<MyArtist> storedList;
    private FancyAdapter fancyAdapter;
    private String recoveredFilter = "";
    private Boolean mTwoPane;

    private AnimationSet set;
    private LayoutAnimationController controller;

    public SpotifySearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        set = new AnimationSet(true);
        Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_top_to_bottom);
        animation.setDuration(1200);
        set.addAnimation(animation);
        spotifyList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        spotifyList.setLayoutManager(llm);


        controller = new LayoutAnimationController(set, 0.5f);

        spotifyList.setLayoutAnimation(controller);

        spotifyList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Log.d("clickListener", String.valueOf(position));
                        String artistID = storedList.get(position).artistId;
                        String artistName = storedList.get(position).artistName;
                        String artistArt = storedList.get(position).backImage;
                        if (artistID != null){
                            ((Callback)getActivity())
                                    .onItemSelected(artistID, artistName, artistArt);
                        }
                    }
                })
        );


        spotifySearch.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // Start searching on submit
                        // ...
                        /**
                         *  We filter artist on each change of the EditText
                         *  Im not sure if it is the right approach but it was the more friendly
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
                         *  Im not sure if it is the right approach but it was the more friendly
                         *  I could think of
                         */
                        if (!newText.contentEquals("") && !newText.contentEquals(recoveredFilter)) {
                            searchArtist();
                        }
                        return true;
                    }
                });

        // get saved datasource if present
        if (savedInstanceState != null) {
            storedList = savedInstanceState.getParcelableArrayList("artist");
            recoveredFilter = savedInstanceState.getString("filter");

            fancyAdapter = new FancyAdapter(storedList);
            spotifyList.setAdapter(fancyAdapter);
            fancyAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    public void isTwoPane(Boolean twoPane){
        mTwoPane = twoPane;
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

            fancyAdapter = new FancyAdapter(storedList);
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

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        // get saved datasource if present

        if (savedInstanceState != null) {
            storedList = savedInstanceState.getParcelableArrayList("artist");
            recoveredFilter = savedInstanceState.getString("filter");

            fancyAdapter = new FancyAdapter(storedList);
            spotifyList.setAdapter(fancyAdapter);
            fancyAdapter.notifyDataSetChanged();
        }
        super.onViewStateRestored(savedInstanceState);
    }



    public class FancyAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

        private List<MyArtist> artistList;
        public FancyAdapter(List<MyArtist> artistList){
            this.artistList = artistList;
        }


        @Override
        public int getItemCount() {
            return (artistList == null)? 0 : artistList.size();
        }

        @Override
        public void onBindViewHolder(ArtistViewHolder tracksViewHolder, int i) {
            tracksViewHolder.populateFrom(artistList.get(i));

        }

        @Override
        public ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View trackView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.spotify_list_artist, viewGroup, false);

            return  new ArtistViewHolder(trackView);
        }


    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @InjectView(R.id.artistName) TextView name;
        @InjectView(R.id.artistThumbnail) ImageView thumbnail;
        @InjectView(R.id.back) ImageView back;


        public ArtistViewHolder(View row){
            super(row);
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

        @Override
        public void onClick(View v) {
            Log.d("click", String.valueOf(spotifyList.getChildItemId(v)));
            int itemPosition = spotifyList.getChildAdapterPosition(v);
            String artistID = storedList.get(itemPosition).artistId;
            String artistName = storedList.get(itemPosition).artistName;
            String artistArt = storedList.get(itemPosition).backImage;
            if (artistID != null){
                ((Callback)getActivity())
                        .onItemSelected(artistID, artistName, artistArt);
            }
        }
    }




    public class retrieveSpotifyData extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {

            ArtistsPager results = null;

            // We pass the filter text and call spotify wrapper API to get the artists
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


            //We dd the results to a list and call the adapter so the view gets updated

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

                    spotifyList.startLayoutAnimation();

                    fancyAdapter = new FancyAdapter(storedList);
                    spotifyList.setAdapter(fancyAdapter);
                    fancyAdapter.notifyDataSetChanged();
                }

            }


        }

        void ToastText(String s){
            Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String artistId, String artistName, String artistArt);
    }
}
