package com.example.rafael.spotifystreamer.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rafael.spotifystreamer.MainActivity;
import com.example.rafael.spotifystreamer.SettingsActivity;
import com.example.rafael.spotifystreamer.TopTracksActivity;
import com.example.rafael.spotifystreamer.utils.MusicController;
import com.example.rafael.spotifystreamer.utils.MyTrack;
import com.example.rafael.spotifystreamer.R;
import com.example.rafael.spotifystreamer.services.MusicService;
import com.example.rafael.spotifystreamer.services.MusicService.MusicBinder;
import com.squareup.picasso.Picasso;
import android.widget.MediaController.MediaPlayerControl;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MediaPlayerFragment extends Fragment implements MediaPlayerControl{
    @InjectView(R.id.playButton)Button playButton;
    @InjectView(R.id.nextButton)Button nextButton;
    @InjectView(R.id.prevButton)Button prevButton;
    @InjectView(R.id.pauseButton)Button pauseButton;
    @InjectView(R.id.artistName)TextView artistName;
    @InjectView(R.id.albumName)TextView albumName;
    @InjectView(R.id.trackName)TextView trackName;
    @InjectView(R.id.backgroundImage)ImageView backImage;
    @InjectView(R.id.trackImage)ImageView trackImage;
    @InjectView(R.id.currentDuration)TextView currentDuration;
    @InjectView(R.id.finalDuration)TextView finalDuration;
    @InjectView(R.id.seekBar)SeekBar seekBar;

    private MusicService musicService;
    private Intent playIntent;
    private Boolean musicBound = false;
    private MusicController controller;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private ShareActionProvider mShareActionProvider;
    private static final String SPOTIFYSTREAMER_SHARE_HASHTAG = " #SpotifyStreamerApp";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PREV = "action_prev";
    public static final String ACTION_NEXT = "action_next";



    private ServiceConnection musicConnection;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SONGS_LIST_PARAM = "tracks";
    private static final String SONGS_POSITION_PARAM = "position";
    private static final String ARTIST_NAME = "artist";

    private ArrayList<MyTrack> mPlayList = new ArrayList<MyTrack>();
    private int mPosition;
    private String mArtist;

    private BroadcastReceiver receiver;

    private OnFragmentInteractionListener mListener;

    private Handler seekHandler = new Handler();

    private static final int NOTIFY_ID = 1;




    public MediaPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mPlayList = bundle.getParcelableArrayList(SONGS_LIST_PARAM);
            mPosition = bundle.getInt(SONGS_POSITION_PARAM);
            mArtist = bundle.getString(ARTIST_NAME);
        }
        setController();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra("position", 0);
                mPosition = pos;
                mShareActionProvider.setShareIntent(createShareSpotifyStreamerIntent());
                updateUI(pos);
                seekHandler.removeCallbacks(run);
                setSeekBar();

            }
        };

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_media_player, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mPlayList != null) {
            mShareActionProvider.setShareIntent(createShareSpotifyStreamerIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share){
            mShareActionProvider.setShareIntent(createShareSpotifyStreamerIntent());
        }
        return super.onOptionsItemSelected(item);
    }



    private Intent createShareSpotifyStreamerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mPlayList.get(mPosition).getTrackUrl() + SPOTIFYSTREAMER_SHARE_HASHTAG);
        return shareIntent;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_media_player, container, false);
        ButterKnife.inject(this, rootView);



        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicBinder binder = (MusicBinder)service;
                //get service
                musicService = binder.getService();
                //pass list
                musicService.setList(mPlayList, mPosition);
                musicService.setSong(mPosition);
                musicBound = true;
                musicService.playSong();
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    musicService.seek(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateUI(mPosition);
        return rootView;
    }

    public void updateUI(int position){
        MyTrack currentSong = mPlayList.get(position);
        artistName.setText(mArtist);
        albumName.setText(currentSong.trackAlbum);
        trackName.setText(currentSong.trackName);


        if (currentSong.trackBackImage !=null){
            Picasso.with(getActivity()).load(currentSong.trackBackImage).into(backImage);
            Picasso.with(getActivity()).load(currentSong.trackBackImage).into(trackImage);
        }else if (currentSong.trackImage != null){
            Picasso.with(getActivity()).load(currentSong.trackImage).into(backImage);
            Picasso.with(getActivity()).load(currentSong.trackImage).into(trackImage);
        }




    }

    // set up seek bar properties and also update current and max duration
    private void setSeekBar() {
        int secondsDuration = (musicService.getDur()/1000)%60;
        int secondsPlayed = (musicService.getPos()/1000)%60;
        finalDuration.setText("00:" + String.valueOf(secondsDuration));
        if (secondsPlayed < 10){
            currentDuration.setText("00:0" + String.valueOf(secondsPlayed));
        }else{
            currentDuration.setText("00:" + String.valueOf(secondsPlayed));
        }


        seekBar.setMax(musicService.getDur());

        seekBar.setProgress(musicService.getPos());



        // ping for updated position every second
        seekHandler.postDelayed(run, 1000);
    }

    // seperate thread for pinging seekbar position
    Runnable run = new Runnable() {
        @Override
        public void run() {
            setSeekBar();
        }
    };

    @OnClick(R.id.playButton)
    public void playMusic(){
        if (musicBound){
            musicService.continuePlaying();
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.nextButton)
    public void nextSong(){
        if (musicBound){
            musicService.setSong(mPosition +1);
            musicService.playSong();
            mPosition++;
            updateUI(mPosition);
        }
    }

    @OnClick(R.id.prevButton)
    public void prevSong(){
        if (musicBound){
            musicService.setSong(mPosition -1);
            musicService.playSong();
            mPosition--;
            updateUI(mPosition);
        }
    }

    @OnClick(R.id.pauseButton)
    public void pauseMusic(){
        if (musicBound){
            musicService.pauseSong();
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(getActivity().getApplicationContext(), MusicService.class);
            getActivity().getApplicationContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().getApplicationContext().startService(playIntent);
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter(MusicService.ACTION_REFRESH)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void setController(){
        //set the controller up
        controller = new MusicController(getActivity());
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);

        controller.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext();
        controller.show(0);
        updateUI(mPosition);
    }

    private void playPrev(){
        musicService.playPrev();
        controller.show(0);
        updateUI(mPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void start() {
        musicService.continuePlaying();
    }

    @Override
    public void pause() {
        musicService.pauseSong();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()){
            return musicService.getDur();
        }else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()){
            return musicService.getPos();
        }else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }



}
