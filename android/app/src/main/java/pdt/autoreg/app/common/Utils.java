package pdt.autoreg.app.common;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.app.App;

public class Utils {
    private static final String TAG = "Utils";

    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
        }
    }


    /* Show Toast message */
    public static void showToastMessage(Context context, String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void delayRandom(long timeStart, long timeEnd ) {
        delay(timeStart + new Random().nextInt((int) (timeEnd - timeStart)));
    }

    public static boolean pingGoogle() {
        return ping("8.8.8.8");
    }

    public static boolean ping(String ip) {
        boolean retVal = false;
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + ip);
            int mExitValue = mIpAddrProcess.waitFor();
            if (mExitValue == 0) {
                retVal = true;
            } else {
                retVal = false;
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            LOG.E(TAG, " Exception:" + ignore);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.E(TAG, " Exception:" + e);
        }
        LOG.D(TAG, "ping " + ip + ": "  + retVal);
        return retVal;
    }

    public static List<String> regex(String subject, String partern) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(partern);
        Matcher matcher = pattern.matcher(subject);

        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public static String regexStr(String subject, String partern) {
        String result = null;
        Pattern pattern = Pattern.compile(partern);
        Matcher matcher = pattern.matcher(subject);

        while (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    public static String getPuclicIP() {
        //https://api64.ipify.org/?format=text
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            String url = "https://api64.ipify.org/?format=text";
            LOG.D(TAG, "url: " + url);
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(url)
                    .get()
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);
            LOG.D(TAG, "code: " + response.code());
            if (response.code() == 200) {
                return resStr;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
            return null;
        }
    }

    public static boolean isAirplaneEnable(Context context) {
        return Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    public static boolean isScreenOff(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return !pm.isScreenOn();
    }

    public static boolean isScreenLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    private boolean checkGrantedPermission(Context context, String packageName, String per) {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(per, packageName);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            // do stuff
            LOG.D(TAG, "packageName: " + packageName + " -- requestedPerm: " + per + "-- GRANTED");
            return true;
        } else {
            LOG.E(TAG, "packageName: " + packageName + " -- requestedPerm: " + per + "-- DENIED");
            return false;
        }
    }
}
