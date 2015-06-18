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

    public MyArtist(Artist artist) {
        artistName = artist.name;
        artistId = artist.id;
        for (Image image : artist.images) {
            if (image.width >= 150 && image.width <= 300) {
                artistImage = image.url;
                break;
            }
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
    }
}
