package com.wang.qrcode.widget;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.wang.qrcode.R;

import java.io.Closeable;
import java.io.IOException;


/**
 * Created by wang
 * on 2017/2/22
 */
final class BeepManager implements MediaPlayer.OnErrorListener, Closeable {

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private int mBeep;

    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Context mContext;

    BeepManager(Context context, int beep) {
        mContext = context;
        mBeep = beep;
    }

    void vibrate(){
        if (mVibrator == null){
            mVibrator = buildVibrator(mContext);
        }
        if (mVibrator.hasVibrator()){
            mVibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private Vibrator buildVibrator(Context context){
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    void stopVibrate() {
        mVibrator.cancel();
    }


    void PlayBeep() {
        if (mMediaPlayer == null){
            mMediaPlayer = buildMediaPlayer(mContext);
        }else if (mMediaPlayer.isPlaying()){
            stopAudio();
        }
        mMediaPlayer.start();
    }

    private MediaPlayer buildMediaPlayer(Context context) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor file = context.getResources().openRawResourceFd(mBeep);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            return mediaPlayer;
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.release();
            return null;
        }
    }

    void stopAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // we are finished, so put up an appropriate error toast if required and finish
            close();
        } else {
            // possibly media player error, so release and recreate
            close();
        }
        return true;
    }

    @Override
    public void close() {
        if (mVibrator != null){
            mVibrator.cancel();
            mVibrator = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
