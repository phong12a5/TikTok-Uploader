package pdt.autoreg.app;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.devicefaker.VDRoot;

import static android.content.ContentValues.TAG;

public class VolumeObserver extends ContentObserver {

    private static final String TAG = "VolumeObserver" ;
    private final AudioManager audioManager;
    private static int volumeSave;

    public VolumeObserver(Handler handler) {
        super(handler);

        audioManager = (AudioManager) App.getContext().getSystemService(Context.AUDIO_SERVICE);

        volumeSave = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onChange(boolean selfChange) {

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(volumeSave == currentVolume -1 )
        LOG.D(TAG, "volume changed: " + currentVolume);

        if(currentVolume == 0 && volumeSave == currentVolume + 1) {
            //
        } else if (currentVolume == audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) &&
                volumeSave == currentVolume -1){
            VDRoot.execute("am force-stop " + App.getContext().getPackageName());
        }

        volumeSave = currentVolume;
    }

    void showToastMessage(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
