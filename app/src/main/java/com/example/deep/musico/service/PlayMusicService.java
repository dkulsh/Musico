package com.example.deep.musico.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.example.deep.musico.MusicPlayer;
import com.example.deep.musico.R;
import com.example.deep.musico.SongsActivityFragment;
import com.example.deep.musico.TrackInfo;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PlayMusicService extends /*Intent*/Service {

    private int playTrack;
    private ArrayList<TrackInfo> trackList ;

    private static final String LOGTAG = "PlayMusicService";
    private final IBinder iBinder = new PlayMusicBinder();
    private Intent intent;

    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;

    TrackInfo trackInfo;

    /*public PlayMusicService() {
        super("PlayMusicService");
    }*/

    @Override
    public void onCreate() {

        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

        this.intent = intent;

        playTrack = intent.getIntExtra(SongsActivityFragment.PLAY_TRACK, 0);
        trackList = intent.getParcelableArrayListExtra(SongsActivityFragment.ALL_TRACKS);

        init(playTrack);

        return START_STICKY;
    }

    private void init(){

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.v(LOGTAG, "Inside onPrepared method");
                mp.start();
            }
        });

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).
                createWifiLock(WifiManager.WIFI_MODE_FULL, "MusicLock");
        wifiLock.acquire();
    }

    private void init(int trackNumber){

        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        trackInfo = trackList.get(trackNumber);

        if (intent != null) {
//            String url = intent.getStringExtra(MusicPlayer.PLAY_URL);
            try { mediaPlayer.setDataSource(trackInfo.getPreviewURL());
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.prepareAsync();
        }
    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    public void playPreviousTrack(View view){

        mediaPlayer.reset();

        if(playTrack == 0) {
            playTrack = trackList.size() - 1;
        } else {
            playTrack = playTrack - 1;
        }

        init(playTrack);
    }

    public void playNextTrack(View view){

        mediaPlayer.reset();

        if(playTrack == trackList.size() - 1) {
            playTrack = 0;
        } else {
            playTrack = playTrack + 1;
        }

        init(playTrack);
    }

    public void playOrPause(View view){

        if(mediaPlayer.isPlaying()) { mediaPlayer.pause(); }
        else { mediaPlayer.start(); }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class PlayMusicBinder extends Binder {

        public PlayMusicService getService(){
            return PlayMusicService.this;
        }
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if(wifiLock != null) {
            wifiLock.release();
        }
    }
}
