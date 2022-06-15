package pdt.autoreg.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.app.App;
import pdt.autoreg.app.R;
import pdt.autoreg.app.model.AppModel;
import pdt.autoreg.app.services.TiktokAppService;
import pdt.autoreg.accessibility.services.ApiAccessibilityService;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.devicefaker.helper.RootHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static MainActivity mainActivity = null;
    public static Context context = null;

    /* UI Component variables  */
    TextView txtAndroidID;
    TextView txtImei1;
    TextView txtImei2;
    Button btnStartStop;
    Button buttonTest;
    TextView txtVersion;
    EditText tokenField;
    Spinner spnAppList;
    AlertDialog acspDialog = null;
    AlertDialog.Builder acspDialogBuilder = null;
    /* END -------- UI Component variable variables */

    /* Permission variable defination */
    final int REQUEST_ALL_PERMISSION_CODE = 1;
    final String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_PHONE_NUMBERS
    };
    /* END --------- Permission variable defination */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.D(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        context = MainActivity.this;

        txtVersion =  (TextView) findViewById(R.id.version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            txtVersion.setText("Version: " + pInfo.versionName);
        }catch (PackageManager.NameNotFoundException e) { }

        btnStartStop = findViewById(R.id.btn_start);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!AppModel.instance().isServiceStarted()) {
                    startMission();
                } else {
                    stopMission();
                }
            }
        });

        btnStartStop.setText(AppModel.instance().isServiceStarted()? "Stop" : "Start");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        LOG.D(TAG, "onResume");
        super.onResume();
            /* Init app when all permission are accepted */
            if(!AppModel.instance().isServiceStarted()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMission();
                    }
                }, 30 * 1000);
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        context = null;
        mainActivity = null;
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_ALL_PERMISSION_CODE) {
            if(!hasPermissions(this,PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ALL_PERMISSION_CODE);
            } else{
                App.initApplication(this);
            }
        }
    }

    private void startMission() {
        LOG.D(TAG,"startMission");
        /*
         * -------------- IMPORTANT --------------
         * CGBInterface has to be initialized when start new mission
         */
        /* Always check accessibility permission when app is resumed*/
        if (hasPermissions(this, PERMISSIONS) == false) {
            for (String permission : PERMISSIONS) {
                RootHelper.acceptPermission(permission, this.getPackageName());
            }
        }

        if (!ASInterface.isAccessibilitySettingsOn(this)) {
            String cmd = "settings put secure enabled_accessibility_services %accessibility:" + getPackageName() + "/" + ApiAccessibilityService.class.getCanonicalName();
            RootHelper.execute(cmd);
        }

        if (!Settings.canDrawOverlays(this)) {
            RootHelper.acceptPermission("android.permission.SYSTEM_ALERT_WINDOW", this.getPackageName());
        }

        if (!AppModel.instance().isServiceStarted()) {
            startService(new Intent(MainActivity.this, TiktokAppService.class));
        }
        btnStartStop.setText("Stop");

    }

    private void stopMission() {
        LOG.D(TAG,"stopMission");
        if(AppModel.instance().isServiceStarted()) {
            stopService(new Intent(MainActivity.this, TiktokAppService.class));
        }
        btnStartStop.setText("Start");
    }
}
