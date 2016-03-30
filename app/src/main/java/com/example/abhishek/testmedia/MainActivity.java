package com.example.abhishek.testmedia;

import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;


import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, ExoPlayer.Listener {

    private ExoPlayer player;
    private Surface surface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //VideoView mVideoView = (VideoView)findViewById(R.id.videoView);

        SurfaceHolder holder;
        SurfaceView view = (SurfaceView) findViewById(R.id.videoView);
        holder = view.getHolder();
        holder.addCallback(this);
        //holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        String path =  "/sdcard/Android/data" ;//Environment.getExternalStorageDirectory() .getAbsolutePath().toString();
        path += File.separator + "com.my.app";
        String fileName = "dizzy.mp4";//"test.txt";
        Encrypter enc = Encrypter.GetInstance();
        enc.Init(this, "testEntity");
        try {
            enc.BreakAndEncrypt(path,fileName, 4096);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        surface = holder.getSurface();
        MediaPlayer mMediaPlayer = new MediaPlayer();
        //String path = "/sdcard/WhatsApp/Media/WhatsApp Video/VID-20130421-WA0000.mp4";
        String path =  "/sdcard/Android/data" ;//Environment.getExternalStorageDirectory() .getAbsolutePath().toString();
        path += File.separator + "com.my.app";//+File.separator + "dizzy.mp4";
        try {
//            mMediaPlayer.setDataSource(path);
//            mMediaPlayer.setDisplay(holder);
//            mMediaPlayer.prepare();
//            mMediaPlayer.start();
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            Allocator allocator = new DefaultAllocator(4096);
           // DataSource dataSource = new DefaultUriDataSource(this, null, "test");
            DataSource dataSource = new StreamDataSource(path,"dizzy.mp4",4096);
            ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                    uri, dataSource, allocator, 4096*64);
            MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
                    this, sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
                    sampleSource, MediaCodecSelector.DEFAULT);
            player = ExoPlayer.Factory.newInstance(4,1000,5000);
            player.prepare(videoRenderer,audioRenderer);
            player.addListener(this);
            player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
// 5. Start playback.
            player.setPlayWhenReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("MainActicity","changed surface");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.stop();
        player.release();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.i("exoplayerstatechanged","state changed "+playWhenReady);
    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.i("##ERROR",error.getMessage());
        error.printStackTrace();
    }
}
