package com.example.rafael.spotifystreamer.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.preference.Preference;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import android.app.PendingIntent;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.example.rafael.spotifystreamer.R;
import com.example.rafael.spotifystreamer.TopTracksActivity;
import com.example.rafael.spotifystreamer.fragments.MediaPlayerFragment;
import com.example.rafael.spotifystreamer.utils.MusicIntentReceiver;
import com.example.rafael.spotifystreamer.utils.MyTrack;
import com.example.rafael.spotifystreamer.utils.MyTrackList;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    private static final String LOG_TAG = "MusicPlaybackService";

    private MediaPlayer mediaPlayer;
    private int songPosition;
    private List<MyTrack> myTrackList;
    private LocalBroadcastManager broadcastManager;

    private final IBinder musicBind = new MusicBinder();

    private static final int NOTIFY_ID = 1;
    private int REQUEST_ID;

    private boolean shuffle = false;
    private Random rand;

    private PendingIntent pendingIntent;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_TOGGLE_PLAYBACK = "action_toggle";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PREVIOUS = "action_prev";
    public static final String ACTION_NEXT = "action_next";
    public static final String STOP_ACTION = "action_stop";


    public static final String ACTION_REFRESH = "action_refresh";


    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaControllerCompat.TransportControls mController;
    private AudioManager mAudioManager;
    private MediaMetadataCompat mMetadata;

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            switch (action){
                case ACTION_PLAY:
                    if (!mediaPlayer.isPlaying()){
                        continuePlaying();
                    }else{
                        pauseSong();
                    }
                    break;
                case ACTION_TOGGLE_PLAYBACK:
                    if (!mediaPlayer.isPlaying()){
                        continuePlaying();
                    }else{
                        pauseSong();
                    }
                    break;
                case ACTION_PAUSE:
                    pauseSong();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrev();
                    break;
                case STOP_ACTION:
                    mediaPlayer.stop();
                    stopForeground(true);
                    unregisterReceiver(broadcastReceiver);
                    break;
            }
        }
    };



    public MusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            final String action = intent.getAction();
            if (action != null){
                Log.d("ACTION_SERVICE", action);
                switch (action){
                    case ACTION_PLAY:
                        if (!mediaPlayer.isPlaying()){
                            continuePlaying();
                        }else{
                            pauseSong();
                        }
                        break;
                    case ACTION_TOGGLE_PLAYBACK:
                        if (!mediaPlayer.isPlaying()){
                            continuePlaying();
                        }else{
                            pauseSong();
                        }
                        break;
                    case ACTION_PAUSE:
                        pauseSong();
                        break;
                    case ACTION_NEXT:
                        playNext();
                        break;
                    case ACTION_PREVIOUS:
                        playPrev();
                        break;
                    case STOP_ACTION:
                        mediaPlayer.stop();
                        stopForeground(true);
                        unregisterReceiver(broadcastReceiver);
                        break;
                }
            }

        }
        return START_STICKY;
    }

    public void onCreate(){
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(STOP_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        broadcastManager = LocalBroadcastManager.getInstance(this);

    }

    private void setupMediaSession(Bitmap image) throws IOException {
        /* Activate Audio Manager */
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
                MusicIntentReceiver.class.getName());
        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mRemoteControlResponder);
        boolean useLockscreen = true;
        mSession = new MediaSessionCompat(getApplication(), "SpotifyStreamerSession", mRemoteControlResponder, null);
        mSession.setFlags(useLockscreen ? MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS : MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mSession.setCallback(mMediaSessionCallback);
        //mMediaSessionCompat.setRatingType(RatingCompat.RATING_HEART);
        mSession.setSessionActivity(retrievePlaybackActions(5));
        updateMediaSessionMetaData(image);
        mSession.setActive(true);
        mController = mSession.getController().getTransportControls();

        Log.d(LOG_TAG, "isActive = " + mSession.isActive());
        Log.d(LOG_TAG, "Remote = " + mSession.getRemoteControlClient());
    }

    private PendingIntent retrievePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(getBaseContext(), MusicService.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(MusicService.ACTION_PLAY);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(getBaseContext(), 1, action, 0);
                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(MusicService.ACTION_NEXT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(getBaseContext(), 2, action, 0);
                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(MusicService.ACTION_PREVIOUS);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(getBaseContext(), 3, action, 0);
                return pendingIntent;
            case 4:
                // Stop and collapse the notification
                action = new Intent(MusicService.STOP_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(getBaseContext(), 4, action, 0);
                return pendingIntent;
            case 5:
                Intent player = new Intent(getBaseContext(), TopTracksActivity.class);
                pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, player, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            default:
                break;
        }

        return null;
    }

    /**
     * Audio focus listener for Jair lockscreen key press listening
     */
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            // TODO: Add fading to loss & gain
            if (mediaPlayer.isPlaying()) {
                Log.d("LOG_TAG", "F = " + focusChange);
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        if (isPlaying())
                        pauseSong();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (isPlaying()) {
                            pauseSong();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (!isPlaying()) {
                            //mCurrentVolume = 0f;
                            //mMediaPlayer.setVolume(0f);
                            continuePlaying();
                        }
                        break;
                    default:
                }
            }
        }
    };

    /**
     * Updates the lockscreen controls, if enabled.
     */
    private void updateMediaSessionMetaData(Bitmap image) throws IOException {
        mMetadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Now Playing...")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, myTrackList.get(songPosition).getTrackAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, myTrackList.get(songPosition).getTrackName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDur())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, songPosition)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, myTrackList.size())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image)
                .build();

        boolean useLockscreen = true;
        if (useLockscreen/* && mSong != null*/) {
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(
                            PlaybackStateCompat.ACTION_SEEK_TO |
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                    PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PAUSE |
                                    PlaybackStateCompat.ACTION_STOP)
                    .setState(
                            isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                            getPos(),
                            1.0f)
                    .build());
            mSession.setMetadata(mMetadata);
            //mMediaSessionCompat.setActive(true);
        }
    }




    private final MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            final String intentAction = mediaButtonEvent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
                    pauseSong();

            } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) return super.onMediaButtonEvent(mediaButtonEvent);
                final int keycode = event.getKeyCode();
                final int action = event.getAction();
                final long eventTime = event.getEventTime();
                if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            mController.stop();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            if (isPlaying()) {
                                if (isPlaying()) mController.pause();
                                else mController.play();
                            }
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            mController.skipToNext();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            mController.skipToPrevious();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            mController.pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            mController.play();
                            break;
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            continuePlaying();
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseSong();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            playPrev();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            seek((int) pos);
        }

        @Override
        public void onStop() {
            super.onStop();
            pauseSong();
            stopSelf();
        }
    };

    public void sendResult(int position) {
        Intent intent = new Intent(ACTION_REFRESH);
        intent.putExtra("position", position);
        broadcastManager.sendBroadcast(intent);
    }

    public void initMusicPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void setList (List<MyTrack> songs, int position){

        myTrackList = songs;
        songPosition = position;


    }

    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    public void setShuffle(){
        if (shuffle) shuffle = false;
        else shuffle = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Return the communication channel to the service.
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mp.reset();
        playNext();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendResult(songPosition);
        mp.start();

        new sendNotification(getApplicationContext()).execute(myTrackList.get(songPosition).getTrackBackImage());

    }

    public void makeNotification(Bitmap trackImage) {
        REQUEST_ID = (int) System.currentTimeMillis();
        Intent intent = new Intent(this, TopTracksActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            setupMediaSession(trackImage);
        } catch (IOException e) {
            e.printStackTrace();
        }



        android.support.v4.app.NotificationCompat.Builder noti = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Now playing...")
                .setContentText(myTrackList.get(songPosition).getTrackName())
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setLargeIcon(trackImage)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mSession.getSessionToken())))
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS))
                .addAction(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY))
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT))
                .addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", STOP_ACTION))
                .setWhen(0);


        startForeground(NOTIFY_ID, noti.build());


    }

    private PendingIntent makePendingIntent(String broadcast)
    {
        Intent intent = new Intent(broadcast);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction){


        Intent intent = new Intent(intentAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private class sendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String message;

        public sendNotification(Context context) {
            super();
            this.ctx = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            makeNotification(result);



        }
    }

    public void pauseSong(){
        mediaPlayer.pause();

    }

    public void continuePlaying(){
        mediaPlayer.start();
    }

    public void playSong(){
        //play a song
        mediaPlayer.reset();

        MyTrack song = myTrackList.get(songPosition);
        String url = song.previewUrl;
        try{
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }


    }

    public void playPrev(){
        songPosition--;
        if (songPosition == 0) songPosition = myTrackList.size()-1;
        playSong();
    }

    public void playNext(){
        if (shuffle){
            int newSong = songPosition;
            while (newSong == songPosition){
                newSong = rand.nextInt(myTrackList.size());
            }
            songPosition = newSong;
        }else{
            songPosition++;
            if (songPosition == myTrackList.size()-1) songPosition = 0;
        }

        playSong();
    }

    public int getPos(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public Boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void seek(int pos){
        mediaPlayer.seekTo(pos);
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        stopForeground(true);
        unregisterReceiver(broadcastReceiver);
    }
}
