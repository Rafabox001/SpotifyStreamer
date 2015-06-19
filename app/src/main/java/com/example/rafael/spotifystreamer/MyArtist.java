package com.example.rafael.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Followers;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Developer-I on 18/06/2015.
 */
public class MyArtist extends Artist implements Parcelable{
    String artistName;
    String artistId;
    String artistImage;
    String backImage;

    public MyArtist(Artist artist) {
        artistName = artist.name;
        artistId = artist.id;
        int sizeOfList = artist.images.size();
        if (sizeOfList > 0){
            artistImage = artist.images.get(sizeOfList - 1).url;
            backImage = artist.images.get(1).url;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(artistId);
        dest.writeString(artistImage);
        dest.writeString(backImage);
    }

    public static final Parcelable.Creator<MyArtist> CREATOR
            = new Parcelable.Creator<MyArtist>() {
        public MyArtist createFromParcel(Parcel in) {
            return new MyArtist(in);
        }

        public MyArtist[] newArray(int size) {
            return new MyArtist[size];
        }
    };

    public MyArtist(Parcel in) {

        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        artistName = in.readString();
        artistId = in.readString();
        artistImage = in.readString();
        backImage = in.readString();
    }
}
