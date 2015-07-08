package com.example.deep.musico;

import android.app.Fragment;

import android.app.SearchManager;
import android.content.Context;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment {

    SearchView searchView;
    ArtistsAdapter artistsAdapter;
    ArrayList mItems;

    private static final String LOGTAG = "MainFragment";
    private static final String ARTIST_LIST = "ArtistList";
    public static final String ARTIST_NAME = "ArtistName";
    public static final String ARTIST_ID = "ArtistId";

/*    String[] items = new String[]{"Today - Sunny - 88/63", "Tomorrow - Foggy - 88/63",
            "Wednesday - Cloudy - 88/63", "Thursday - Rainy - 88/63", "Friday - Sunny - 88/63"};*/

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);

/*        if(savedInstanceState != null){
            mItems = savedInstanceState.getParcelableArrayList(ARTIST_LIST);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) rootView.findViewById(R.id.artistsSearchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        /*ArrayAdapter<String> artistsAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.artists_view, R.id.artistNameTextView, items);*/

        if(mItems == null) { mItems = new ArrayList(); }

        artistsAdapter = new ArtistsAdapter(getActivity(), R.layout.artists_view,
                R.id.artistNameTextView, /*new ArrayList()*/ mItems);

        setListAdapter(artistsAdapter);

        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

//        Bundle bundle = new Bundle();
//        bundle.putString(ARTIST_NAME, ((ArtistInfo)mItems.get(position)).getName());

        Intent intent = new Intent(getActivity(), SongsActivity.class);
        intent.putExtra(ARTIST_NAME, ((ArtistInfo)mItems.get(position)).getName());
        intent.putExtra(ARTIST_ID, ((ArtistInfo)mItems.get(position)).getId());
        startActivity(intent);
//        super.onListItemClick(l, v, position, id);
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mItems != null) {
            outState.putParcelableArrayList(ARTIST_LIST, mItems);
        }
    }*/

    class ArtistsAdapter extends ArrayAdapter {

        Context context;

        public ArtistsAdapter(Context context, int resource, int textViewResource,
                              ArrayList items) {

            super(context, resource, textViewResource, items);
            this.context = context;
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

//            return super.getView(position, convertView, parent);
//            View view = LayoutInflater.from(context).inflate(R.layout.artists_view, parent, false);
            Log.v(LOGTAG, "");

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.artists_view, parent, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.artistsNameImageView);
            TextView textView = (TextView) view.findViewById(R.id.artistNameTextView);

            if(mItems.size() > 0) {

//                Log.v(LOGTAG, ((ArtistInfo) mItems.get(position)).getImageURL());

                if(! ((ArtistInfo)mItems.get(position)).getImageURL().equals("")) {

                    Picasso.with(getActivity()).load(((ArtistInfo) mItems.get(position)).getImageURL()).into(imageView);
                }

                textView.setText(((ArtistInfo)mItems.get(position)).getName());
            }

            return view;
        }
    }
}
