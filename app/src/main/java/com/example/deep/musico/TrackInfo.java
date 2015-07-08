package com.example.deep.musico;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Deep on 6/11/2015.
 */
public class TrackInfo implements Parcelable {

    private static final String KEY_TRACK = "track";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_ALBUM_IMAGE = "albumImageURL";
    private static final String KEY_PREVIEW = "previewURL";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_DURATION = "duration";

    private String track;
    private String album;
    private String albumImageURL;
    private String previewURL;
    private String artist;
    private long duration;

    public TrackInfo(String track, String album, String albumImageURL, String previewURL,
                     String artist, long duration){

        this.track = track;
        this.album = album;
        this.albumImageURL = albumImageURL;
        this.previewURL = previewURL;
        this.artist = artist;
        this.duration = duration;
    }

    public String getTrack(){ return track; }

    public String getAlbum(){ return album; }

    public String getAlbumImageURL(){
        return albumImageURL;
    }

    public String getPreviewURL() { return previewURL; }

    public String getArtist() { return artist; }

    public long getDuration() { return duration; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        Bundle bundle = new Bundle();

        bundle.putString(KEY_TRACK, track);
        bundle.putString(KEY_ALBUM, album);
        bundle.putString(KEY_ALBUM_IMAGE, albumImageURL);
        bundle.putString(KEY_PREVIEW, previewURL);
        bundle.putString(KEY_ARTIST, artist);
        bundle.putLong(KEY_DURATION, duration);

        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {

        @Override
        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }

        @Override
        public TrackInfo createFromParcel(Parcel source) {

            Bundle bundle = source.readBundle();

            return new TrackInfo(bundle.getString(KEY_TRACK),
                    bundle.getString(KEY_ALBUM),
                    bundle.getString(KEY_ALBUM_IMAGE),
                    bundle.getString(KEY_PREVIEW),
                    bundle.getString(KEY_ARTIST),
                    bundle.getLong(KEY_DURATION));
        }
    };
}
