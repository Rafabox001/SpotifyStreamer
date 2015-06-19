package com.example.rafael.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Developer-I on 19/06/2015.
 */
public class MyTrack extends Track implements Parcelable {
    String trackName;
    String trackAlbum;
    String trackImage;
    String trackBackImage;

    public MyTrack(Track track) {
        trackName = track.name;
        trackAlbum = track.album.name;
        int sizeOfList = track.album.images.size();
        if (sizeOfList > 0){
            trackImage = track.album.images.get(sizeOfList - 1).url;
            trackBackImage = track.album.images.get(1).url;
        }
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
    }
}
