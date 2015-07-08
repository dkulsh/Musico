package com.example.deep.musico;

import android.widget.ImageView;

/**
 * Created by Deep on 6/9/2015.
 */
public class ArtistInfo {

    private String imageURL;
    private String name;
    private String id;

    public ArtistInfo(String imageURL, String name, String id){

        this.imageURL = imageURL;
        this.name = name;
        this.id = id;
    }

    public String getImageURL(){
        return imageURL;
    }

    public String getName(){
        return name;
    }

    public String getId() { return id; }

}
