package com.beicompany.psihey.testmetronom.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import com.beicompany.psihey.testmetronom.R;
import com.beicompany.psihey.testmetronom.activitys.MainActivity;


public class ActionService extends Service {
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Camera mCamera = null;
    private Parameters mParameters;
    private Intent mActionIntent = new Intent(MainActivity.ACTION_ACTION);
    private boolean mIsRunning;
    private int i;
    Binder binder = new MyBinder();
    private boolean vib = true, sou = true;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public void setSou(boolean sou) {
        this.sou = sou;
    }

    public void setVib(boolean vib) {
        this.vib = vib;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupVibrator(intent);
        setupFlashlight(intent);
        setupSignal(intent);
        i = intent.getIntExtra(MainActivity.EXTRA_BPM_VALUE, 80);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                tic();
            }
        });
        thread.start();


        return super.onStartCommand(intent, flags, startId);
    }

    public void setI(int i) {
        this.i = i;
    }

    public void tic() {
        Log.e("service - tic", "i" + i);
        if (i == 0) {
            Log.e("service - tic", "i" + i);
            i = 80;
            Log.e("service - tic", "i" + i);
        }
        mIsRunning = true;
        while (mIsRunning) {
            if (sou == false) {
                mMediaPlayer = null;
            } else {
                mMediaPlayer = MediaPlayer.create(this, R.raw.classic_d);
                mMediaPlayer.setVolume(1f, 1f);
            }
            if (vib == false) {
                mVibrator = null;
            } else {
                mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }
            Log.e("service - while", "i" + i);
            int delay = (60 * 1000) / i;
            Log.e("service- while", "i" + i);
            Log.e("service - while", "delay" + delay);
            sendBroadcast(mActionIntent);

            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
            if (mVibrator != null) {
                mVibrator.vibrate(100);
            }
            cameraBlink();

            sendBroadcast(mActionIntent);
            try {
                Thread.sleep(delay);
                delay = 0;
                Log.e("service - while", "delay" + delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupSignal(Intent intent) {
        if (!intent.getBooleanExtra(MainActivity.EXTRA_SOUND_STATUS, true)) return;

        mMediaPlayer = MediaPlayer.create(this, R.raw.classic_d);
        mMediaPlayer.setVolume(1f, 1f);
    }

    private void setupVibrator(Intent intent) {
        if (!intent.getBooleanExtra(MainActivity.EXTRA_VIBRATION_STATUS, true)) return;
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void setupFlashlight(Intent intent) {
        if (!intent.getBooleanExtra(MainActivity.EXTRA_FLASHLIGHT_STATUS, true))
            return;
        if ((mCamera = Camera.open()) != null)
            mParameters = mCamera.getParameters();
    }

    private void cameraBlink() {
        if (mCamera == null) return;

        mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
    }

    @Override
    public void onDestroy() {
        mIsRunning = false;
        super.onDestroy();
        if (mCamera != null) {
            mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            mCamera.release();
        }
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        public ActionService getService() {
            return ActionService.this;
        }

    }
}
