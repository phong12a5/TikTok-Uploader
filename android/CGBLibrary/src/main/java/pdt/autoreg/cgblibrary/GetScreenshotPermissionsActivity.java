package pdt.autoreg.cgblibrary;


import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public class GetScreenshotPermissionsActivity extends Activity {
    private static final String TAG = "SSACTIVITY";
    private static final int REQUEST_SCREENSHOT = 59706;
    private static final int READ_PERMISSION_REQUEST_CODE = 420;
    private static final int WRITE_PERMISSION_REQUEST_CODE = 421;
    private MediaProjectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        checkAndAskForNextPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent();
                i.setAction(ModuleDefines.CAPTURE_SCREEN_RESPONSE);
                i.putExtra("resultCode", resultCode);
                i.putExtra("resultIntent", data);
                sendBroadcast(i);
            }
        }
        finish();
    }

    private void checkAndAskForNextPermission() {
        startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
    }
}



