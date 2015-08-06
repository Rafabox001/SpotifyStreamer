package com.example.rafael.spotifystreamer.utils;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Developer-I on 19/06/2015.
 */
public class MyTrack extends Track implements Parcelable {
    public String trackName;
    public String trackAlbum;
    public String trackImage;
    public String trackBackImage;
    public String previewUrl;
    public String trackDuration;
    public String trackUrl;


    public MyTrack(String trackName, String trackAlbum, String trackImage, String trackBackImage, String previewUrl, String trackDuration, String trackUrl){
        super();
        this.trackName = trackName;
        this.trackAlbum = trackAlbum;
        this.trackImage = trackImage;
        this.trackBackImage = trackBackImage;
        this.previewUrl = previewUrl;
        this.trackDuration = trackDuration;
        this.trackUrl = trackUrl;



    }

    public MyTrack(){}

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackAlbum() {
        return trackAlbum;
    }

    public void setTrackAlbum(String trackAlbum) {
        this.trackAlbum = trackAlbum;
    }

    public String getTrackImage() {
        return trackImage;
    }

    public void setTrackImage(String trackImage) {
        this.trackImage = trackImage;
    }

    public String getTrackBackImage() {
        return trackBackImage;
    }

    public void setTrackBackImage(String trackBackImage) {
        this.trackBackImage = trackBackImage;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getTrackDuration() {
        return trackDuration;
    }

    public void setTrackDuration(String trackDuration) {
        this.trackDuration = trackDuration;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public MyTrack(Track track) {
        trackName = track.name;
        trackAlbum = track.album.name;
        previewUrl = track.preview_url;
        int sizeOfList = track.album.images.size();
        if (sizeOfList > 0){
            trackImage = track.album.images.get(sizeOfList - 1).url;
            trackBackImage = track.album.images.get(1).url;
        }
        this.trackDuration = String.valueOf(track.duration_ms);
        this.trackUrl = track.uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(trackAlbum);
        dest.writeString(trackImage);
        dest.writeString(trackBackImage);
        dest.writeString(previewUrl);
        dest.writeString(trackDuration);
        dest.writeString(trackUrl);
    }

    public static final Parcelable.Creator<MyTrack> CREATOR
            = new Parcelable.Creator<MyTrack>() {
        public MyTrack createFromParcel(Parcel in) {
            return new MyTrack(in);
        }

        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };

    public MyTrack(Parcel in) {

        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        trackName = in.readString();
        trackAlbum = in.readString();
        trackImage = in.readString();
        trackBackImage = in.readString();
        previewUrl = in.readString();
        trackDuration = in.readString();
        trackUrl = in.readString();
    }



}