package pdt.autoreg.app;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import pdt.autoreg.accessibility.LOG;

public class FloatingWindow extends Service {
    final static String TAG = "FloatingWindow";

    final static String ACTION_REGISTER_SUCCESS = "ACTION_REGISTERCCESS";
    final static String ACTION_CHANGE_REGISTER_MODE = "ACTION_CHANGE_REGISTER_MODE";

    WindowManager wm;
    LinearLayout ll;
    TextView txtAndroidID;
    TextView txtCounter;

    int widthOfScreen;
    int heightOfScreen;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreate() {
        LOG.D(TAG, "onCreate");
        super.onCreate();
        registerReceiver();

        /* -------------- CREATE VIEW -------------- */
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        widthOfScreen = metrics.widthPixels;
        heightOfScreen = metrics.heightPixels;

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.float_window, null);

        txtAndroidID = layout.findViewById(R.id.AndroidID);
        int versionCode = -1;
        try {
            PackageInfo pInfo = App.getContext().getPackageManager().getPackageInfo(this.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (Exception e) {}
        String androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        txtAndroidID.setText("ID: " + androidId + " -- version: " + versionCode);

        txtCounter = layout.findViewById(R.id.ServiceCounter);

        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ERROR ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        parameters.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        wm.addView(layout, parameters);
        /* -------------- END -------------- */

        updateView();
        /* ------------ END ------------- */
    }

    @Override
    public void onDestroy() {
        LOG.D(TAG, "onDestroy");
        super.onDestroy();
        stopSelf();
        unregisterReceiver(updateResultReceiver);
    }

    private void registerReceiver() {
        LOG.D(TAG, "registerReceiver");
        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(ACTION_REGISTER_SUCCESS);
        theFilter.addAction(ACTION_CHANGE_REGISTER_MODE);
        registerReceiver(this.updateResultReceiver, theFilter);
    }

    private BroadcastReceiver updateResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction() == null) {
                    LOG.E("BroadcastReceiver", "Action: NULL");
                    return;
                } else {
                    LOG.D("BroadcastReceiver", "Action: " + intent.getAction());
                }

                switch (intent.getAction()) {
                    case ACTION_REGISTER_SUCCESS:
                    case ACTION_CHANGE_REGISTER_MODE:
                        updateView();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOG.E(TAG, "onReceive: " + e);
            }
        }
    };

    private void updateView() {
        try {
            File clonesFile = new File(AppDefines.AUTOREG_DATA_FOLDER + "clones.txt");
            BufferedReader br = new BufferedReader(new FileReader(clonesFile));
            int lineCount = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                lineCount++;
            }
            txtCounter.setText("Mode: " + (AppModel.instance().networkType()) + " - count: " + lineCount);
            LOG.D(TAG, " txtCounter: " + txtCounter.getText());
        } catch (Exception e) {
            txtCounter.setText("Mode: " + (AppModel.instance().networkType()) + " - count: " + 0);
        }
    }

}
