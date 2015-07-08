package com.example.deep.musico;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.deep.musico.service.PlayMusicService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MusicPlayer extends ActionBarActivity /*implements MediaPlayer.OnPreparedListener*/ {

    private int playTrack;
    private ArrayList<TrackInfo> trackList ;

    public static final String PLAY_URL = "url";
    private static final String LOGTAG = "MusicPlayer";
    private static int elapsedTime ;
    private static int remainingTime;

    TextView artistName;
    TextView albumName;
    ImageView albumImage;
    TextView songName;
    SeekBar songProgress;
    TextView startTime;
    TextView endTime;
    ImageButton previousSong;
    ImageButton playPause;
    ImageButton nextSong;
    TrackInfo trackInfo;

//    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    Handler seekBarHandler = new Handler();
    private int NOTIFICATION_ID = 1;
    private static final String ACTION_PLAY_PAUSE = "com.musico.deep.ACTION_PLAY_PAUSE";
    private static final String ACTION_PREVIOUS = "com.musico.deep.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.musico.deep.ACTION_NEXT";

    private static MusicPlayer musicPlayer;
    NotificationManager notificationManager;
    RemoteViews notificationViews;
    NotificationCompat.Builder builder;
    ShareActionProvider shareActionProvider;
    PlayMusicService playMusicService;
    boolean serviceBound = false;

    private Runnable seekBarUpdate = new Runnable() {
        @Override
        public void run() {
            if(playMusicService.getMediaPlayer() != null) {

                elapsedTime = (int) playMusicService.getMediaPlayer().getCurrentPosition();
                remainingTime = (int) playMusicService.getMediaPlayer().getDuration() - elapsedTime;

//                Log.v(LOGTAG, "Elapsed time from runnable : " + mediaPlayer.getCurrentPosition());
//                Log.v(LOGTAG, "Remaining time from runnable : " + remainingTime);

                String formatTimeStart = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                        TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));

                String formatTimeEnd = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(remainingTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime)));

//                Log.v(LOGTAG, "Formatted start time : " + formatTimeStart);
//                Log.v(LOGTAG, "Formatted end time : " + formatTimeEnd);

                startTime.setText(formatTimeStart);
                endTime.setText(formatTimeEnd);

                songProgress.setProgress(elapsedTime);
                seekBarHandler.postDelayed(seekBarUpdate, 50);
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayMusicService.PlayMusicBinder playMusicBinder = (PlayMusicService.PlayMusicBinder) service;
            playMusicService = playMusicBinder.getService();
            serviceBound = true;

            init(playTrack);
            seekBarHandler.postDelayed(seekBarUpdate, 50);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { serviceBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        musicPlayer = this;

//        Local variables introduced while migrating to service
        playTrack = getIntent().getIntExtra(SongsActivityFragment.PLAY_TRACK, 0);
        trackList = getIntent().getParcelableArrayListExtra(SongsActivityFragment.ALL_TRACKS);
        trackInfo = trackList.get(playTrack);

        Intent intent = new Intent(this, PlayMusicService.class);
//        intent.putExtra(PLAY_URL, trackList.get(playTrack).getPreviewURL());
        intent.putParcelableArrayListExtra(SongsActivityFragment.ALL_TRACKS, trackList);
        intent.putExtra(SongsActivityFragment.PLAY_TRACK, playTrack);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        init();
//        init(playTrack);
//        Log.v(LOGTAG, "Before SendNotification method");

        sendNotification();
    }

    public static class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver(){ }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action != null && action.equalsIgnoreCase(ACTION_PLAY_PAUSE)) {

                musicPlayer.playOrPause(null);
                // Change button image
            } else if(action.equalsIgnoreCase(ACTION_NEXT)) {

                musicPlayer.playNextTrack(null);
            } else if(action.equalsIgnoreCase(ACTION_PREVIOUS)) {

                musicPlayer.playPreviousTrack(null);
            } else{

                Log.v(LOGTAG, "This should never happen. Action cannot have values except the valid ones");
            }
        }
    }

    private void sendNotification() {

        notificationViews = new RemoteViews(getPackageName(), R.layout.notification);
        notificationViews.setTextViewText(R.id.songNameTextViewNotify, trackInfo.getTrack());

        if(albumImage != null) {
            Bitmap bitmap = ((BitmapDrawable)albumImage.getDrawable()).getBitmap();
            notificationViews.setImageViewBitmap(R.id.songImageViewNotify, bitmap);
        }

        builder = new NotificationCompat.Builder(this)
                .setContent(notificationViews)
                .setSmallIcon(R.drawable.no_image);

//        builder.build().contentView = notificationViews;

        Intent intentPlayPause = new Intent(ACTION_PLAY_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(ACTION_NEXT);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPrevious = new Intent(ACTION_PREVIOUS);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationViews.setOnClickPendingIntent(R.id.playPauseButtomNotify, pendingIntent);
        notificationViews.setOnClickPendingIntent(R.id.previousButtomNotify, pendingIntent2);
        notificationViews.setOnClickPendingIntent(R.id.nextButtomNotify, pendingIntent1);

//        builder.setContentIntent(pendingIntent);

        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void init() {

        artistName = (TextView) findViewById(R.id.artistNameTextView);
        albumName = (TextView) findViewById(R.id.albumNameTextView);
        albumImage = (ImageView) findViewById(R.id.albumImageView);
        songName = (TextView) findViewById(R.id.songNameTextView);
        songProgress = (SeekBar) findViewById(R.id.songProgressBar);
        startTime = (TextView) findViewById(R.id.startTimeTextView);
        endTime = (TextView) findViewById(R.id.endTimeTextView);
        previousSong = (ImageButton) findViewById(R.id.previousButtom);
        playPause = (ImageButton) findViewById(R.id.playPauseButtom);
        nextSong = (ImageButton) findViewById(R.id.nextButtom);

//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
//        mediaPlayer.setOnPreparedListener(this);

        wifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).
                createWifiLock(WifiManager.WIFI_MODE_FULL, "MusicLock");
        wifiLock.acquire();
    }

    private void init(int trackNumber){

//        if(mediaPlayer != null && mediaPlayer.isPlaying()){
//            mediaPlayer.stop();
//        }
        trackInfo = trackList.get(playTrack);

        artistName.setText(trackInfo.getArtist());
        albumName.setText(trackInfo.getAlbum());

        if(! (trackInfo.getAlbumImageURL().equals(""))) {
            Picasso.with(this).load(trackInfo.getAlbumImageURL()).into(albumImage);
        }

        elapsedTime = 0;
        startTime.setText(String.valueOf(elapsedTime));

        if(playMusicService == null) { Log.v(LOGTAG, "playMusicService is NULL"); }
        else { Log.v(LOGTAG, "playMusicService is NOT null"); }
        remainingTime = (int) playMusicService.getMediaPlayer().getDuration();
        endTime.setText(String.valueOf(remainingTime));

        songName.setText(trackInfo.getTrack());
        songProgress.setProgress(elapsedTime);

//        Moving below line to Runnable because mediaPlayer.getDuration() does not return right value
        songProgress.setMax(30 * 1000);

//        Log.v(LOGTAG, "Duration from track info : " + String.valueOf(trackInfo.getDuration()));
//        Log.v(LOGTAG, "Duration from mediaPlayer : " +String.valueOf(mediaPlayer.getDuration()));

//        Log.v(LOGTAG, trackInfo.getPreviewURL());
//        try {
//            mediaPlayer.setDataSource(trackInfo.getPreviewURL());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        mediaPlayer.prepareAsync();

        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    playMusicService.getMediaPlayer().seekTo(progress);
                }

                elapsedTime = (int) playMusicService.getMediaPlayer().getCurrentPosition();
                remainingTime = (int) playMusicService.getMediaPlayer().getDuration() - elapsedTime;

//                Log.v(LOGTAG, "Elapsed time from runnable : " + mediaPlayer.getCurrentPosition());
//                Log.v(LOGTAG, "Remaining time from runnable : " + remainingTime);

                String formatTimeStart = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                        TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));

                String formatTimeEnd = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(remainingTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime)));

//                Log.v(LOGTAG, "Formatted start time : " + formatTimeStart);
//                Log.v(LOGTAG, "Formatted end time : " + formatTimeEnd);

                startTime.setText(formatTimeStart);
                endTime.setText(formatTimeEnd);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void playPreviousTrack(View view){

//        mediaPlayer.reset();
        if(playTrack == 0) {
            playTrack = trackList.size() - 1;
        } else {
            playTrack = playTrack - 1;
        }
        init(playTrack);

        playMusicService.playPreviousTrack(null);
        playPause.setImageResource(android.R.drawable.ic_media_pause);
        notificationViews.setTextViewText(R.id.songNameTextViewNotify, trackInfo.getTrack());
        if(albumImage != null) {
            Bitmap bitmap = ((BitmapDrawable)albumImage.getDrawable()).getBitmap();
            notificationViews.setImageViewBitmap(R.id.songImageViewNotify, bitmap);
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void playNextTrack(View view){

//        mediaPlayer.reset();
        if(playTrack == trackList.size() - 1) {
            playTrack = 0;
        } else {
            playTrack = playTrack + 1;
        }
        init(playTrack);

        playMusicService.playNextTrack(null);
        playPause.setImageResource(android.R.drawable.ic_media_pause);
        notificationViews.setTextViewText(R.id.songNameTextViewNotify, trackInfo.getTrack());
        if(albumImage != null) {
            Bitmap bitmap = ((BitmapDrawable)albumImage.getDrawable()).getBitmap();
            notificationViews.setImageViewBitmap(R.id.songImageViewNotify, bitmap);
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void playOrPause(View view){

        if(playMusicService.getMediaPlayer().isPlaying()) {
            playPause.setImageResource(android.R.drawable.ic_media_play);
            playMusicService.getMediaPlayer().pause();

            notificationViews.setImageViewResource(R.id.playPauseButtomNotify, android.R.drawable.ic_media_play);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            playPause.setImageResource(android.R.drawable.ic_media_pause);
            playMusicService.getMediaPlayer().start();

            notificationViews.setImageViewResource(R.id.playPauseButtomNotify, android.R.drawable.ic_media_pause);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_player, menu);

        MenuItem item = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(trackInfo.getPreviewURL() != null){
            shareActionProvider.setShareIntent(createShareIntent());
        }
        return true;
    }

    private Intent createShareIntent(){

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra(Intent.EXTRA_TEXT, trackInfo.getPreviewURL() + "#Musico App");
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (item.getItemId() == R.id.share) {
            shareActionProvider.setShareIntent(createShareIntent());
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onPrepared(MediaPlayer mp) {

        mediaPlayer.start();
        seekBarHandler.postDelayed(seekBarUpdate, 50);
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        stopService(new Intent(this, PlayMusicService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        stopService(new Intent(this, PlayMusicService.class));

//        mediaPlayer.release();
//        mediaPlayer = null;
//        wifiLock.release();
//        notificationManager.cancel(NOTIFICATION_ID);
    }
}
