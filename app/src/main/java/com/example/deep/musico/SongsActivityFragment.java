package com.example.deep.musico;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class SongsActivityFragment extends ListFragment {

//    String[] albums = new String[]{"Album 1", "Album 2", "Album 3", "Album 4", "Album 5"};
//    String[] songs = new String[]{"Songs 1", "Songs 2", "Songs 3", "Songs 4", "Songs 5"};

    private static final String LOGTAG = "SongsFragment";
    private static String COUNTRY ;
    private static final int TRACK_NUMBER = 10;

    public static final String PLAY_TRACK = "PlayTrack";
    public static final String ALL_TRACKS = "AllTracks";

    SongsAdapter songsAdapter;
    ArrayList trackInfoList = new ArrayList<TrackInfo>();

    public SongsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_songs, container, false);

//        ArrayAdapter<String> artistsAdapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.artists_view, R.id.artistNameTextView, albums);

        String artistName = getActivity().getIntent().getStringExtra(MainActivityFragment.ARTIST_NAME);
        String artistId = getActivity().getIntent().getStringExtra(MainActivityFragment.ARTIST_ID);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(artistName);

        QuerySongsFromSpotify querySongsFromSpotify = new QuerySongsFromSpotify();
        querySongsFromSpotify.execute(artistId);

        songsAdapter = new SongsAdapter(getActivity(), R.layout.songs_view, R.id.albumNameTextView, trackInfoList);
        setListAdapter(songsAdapter);

        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), MusicPlayer.class);
        intent.putParcelableArrayListExtra(ALL_TRACKS, trackInfoList);
        intent.putExtra(PLAY_TRACK, position);
        startActivity(intent);
    }

    private class SongsAdapter extends ArrayAdapter {

        Context context;
        List items;

        public SongsAdapter(Context context, int resource, int textViewResourceId, List items) {
            super(context, resource, textViewResourceId, items);

            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            return super.getView(position, convertView, parent);

            View view = LayoutInflater.from(context).inflate(R.layout.songs_view, parent, false);
            ImageView songImage = (ImageView) view.findViewById(R.id.songImageView);
            TextView albumName = (TextView) view.findViewById(R.id.albumNameTextView);
            TextView songName = (TextView) view.findViewById(R.id.songNameTextView);

            if(! ((TrackInfo) trackInfoList.get(position)).getAlbumImageURL().equals("")) {

                Picasso.with(getActivity()).load(((TrackInfo) trackInfoList.get(position)).getAlbumImageURL()).into(songImage);
            }
//            songImage.setImageResource(R.drawable.button_blank_blue_01);
            albumName.setText(((TrackInfo) trackInfoList.get(position)).getAlbum());
            songName.setText(((TrackInfo)trackInfoList.get(position)).getTrack());

            Log.v(LOGTAG, ((TrackInfo) trackInfoList.get(position)).getAlbum());
            Log.v(LOGTAG, ((TrackInfo)trackInfoList.get(position)).getTrack());
            Log.v(LOGTAG, ((TrackInfo)trackInfoList.get(position)).getAlbumImageURL());

            return view;
        }
    }

    private class QuerySongsFromSpotify extends AsyncTask<String, Void, Tracks>{

        @Override
        protected Tracks doInBackground(String... params) {

            Map<String, Object> map = new HashMap<String, Object>();

            COUNTRY = PreferenceManager.getDefaultSharedPreferences(getActivity()).
                    getString("example_list", getString(R.string.default_country));

            map.put(SpotifyService.COUNTRY, COUNTRY);
            map.put(SpotifyService.LIMIT, TRACK_NUMBER);

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            Tracks tracks = spotifyService.getArtistTopTrack(params[0], map);

            return tracks;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
//            super.onPostExecute(tracks);

            Log.v(LOGTAG, "In post Execute");
            Iterator<Track> iterator =  tracks.tracks.iterator();

            while(iterator.hasNext()) {

                Track track = iterator.next();
//                String albumName = track.album.name;
//                String trackName = track.name;
                String trackImageURL;

                if(track.album.images.size() == 0){

                    trackImageURL = "";
                } else {

                    trackImageURL = track.album.images.get(track.album.images.size() - 1).url;
                }

                TrackInfo trackInfo = new TrackInfo(track.name, track.album.name, trackImageURL,
                        track.preview_url, track.artists.get(0).name, track.duration_ms);

                trackInfoList.add(trackInfo);
                songsAdapter.add(trackInfo);
            }

            if(trackInfoList.size() == 0 ) {

                Log.v(LOGTAG, "track list size is zero");
                Toast.makeText(getActivity(), "No tracks found", Toast.LENGTH_SHORT).show();
            } else {

                Log.v(LOGTAG, "track list is NOT size zero");

                /*songsAdapter.clear();
                songsAdapter.addAll(trackInfoList);*/
                songsAdapter.notifyDataSetChanged();
            }
        }
    }
}
