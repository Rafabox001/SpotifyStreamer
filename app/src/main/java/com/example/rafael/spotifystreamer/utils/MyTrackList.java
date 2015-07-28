package com.example.rafael.spotifystreamer.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Rafael on 24/07/2015.
 */
public class MyTrackList extends ArrayList<MyTrack> implements Parcelable {

    public MyTrackList(){

    }

    public MyTrackList(Parcel in){
        this.clear();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.clear();

        // First we have to read the list size
        int size = in.readInt();

        for (int i = 0; i < size; i++) {
            MyTrack track = new MyTrack(in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
            this.add(track);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final Parcelable.Creator<MyTrackList> CREATOR = new Parcelable.Creator<MyTrackList>() {
        public MyTrackList createFromParcel(Parcel in) {
            return new MyTrackList(in);
        }

        public MyTrackList[] newArray(int size) {
            return new MyTrackList[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int size = this.size();

        // We have to write the list size, we need him recreating the list
        dest.writeInt(size);

        for (int i = 0; i < size; i++) {
            MyTrack track = this.get(i);

            dest.writeString(track.getTrackName());
            dest.writeString(track.getTrackAlbum());
            dest.writeString(track.getTrackImage());
            dest.writeString(track.getTrackBackImage());
            dest.writeString(track.getPreviewUrl());
        }
    }
}
