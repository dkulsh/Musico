package com.example.deep.musico;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;


public class MainActivity extends ActionBarActivity {

    Intent intent;
    private static final String MAINFRAGMENT_TAG = "MFTAG";
    private static final String LOGTAG = "MainActivity";
    MainActivityFragment mainActivityFragment;

//    List<String> artistsName = new ArrayList<String>();
//    List<String> spotifyId = new ArrayList<String>();
    List artistInfoList = new ArrayList<ArtistInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(MAINFRAGMENT_TAG);
        if(mainActivityFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.artists_detail_container, new MainActivityFragment()
                    /*mainActivityFragment*/, MAINFRAGMENT_TAG).commit();

            queryArtists(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        queryArtists(intent);
    }

    private void queryArtists(Intent intent){

        this.intent = intent;

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String queryString = intent.getStringExtra(SearchManager.QUERY).replace(" ", "+");

            QueryArtistsFromSpotify queryArtistsFromSpotify = new QueryArtistsFromSpotify(this);
            queryArtistsFromSpotify.execute(queryString);
        }
    }

    private class QueryArtistsFromSpotify extends AsyncTask<String, Void, ArtistsPager> {

        Context context;

        public QueryArtistsFromSpotify(Context context) {
            this.context = context;
        }

        @Override
        protected ArtistsPager doInBackground(String... params) {

            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            ArtistsPager artistsPager = spotifyService.searchArtists(params[0]);

            return artistsPager;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            //super.onPostExecute(artistsPager);

            List<Artist> artistsList = artistsPager.artists.items;
            Iterator<Artist> iterator = artistsList.iterator();
            String imageURL = "";

            /*artistsName.clear();
            spotifyId.clear();
            artistImageURL.clear();*/
            artistInfoList.clear();

            while(iterator.hasNext()){

                Artist artist = iterator.next();

//                artistsName.add(artist.name);
//                spotifyId.add(artist.id);

                if(artist.images.size() == 0 ) {

                    imageURL = "";
                } else if(artist.images.size() > 0 ) {

                    imageURL = artist.images.get(artist.images.size() - 1).url;
                    Log.v(LOGTAG, artist.id);
                    Log.v(LOGTAG, artist.name);
                    Log.v(LOGTAG, artist.images.get(artist.images.size() - 1).url);
                }

                artistInfoList.add(new ArtistInfo(imageURL, artist.name, artist.id));
            }

            if(artistInfoList.size() == 0 ) {

                Log.v(LOGTAG, "No results fetched");
                Toast.makeText(context, "No artists found", Toast.LENGTH_SHORT).show();
            } else {

                Log.v(LOGTAG, "Sending data to MainFragment");
                mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(MAINFRAGMENT_TAG);
                ((ArrayAdapter<String>) mainActivityFragment.getListAdapter()).clear();
                ((ArrayAdapter<String>) mainActivityFragment.getListAdapter()).addAll(artistInfoList);
            }
        }
    }
}
