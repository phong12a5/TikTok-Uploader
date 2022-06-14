package pdt.autoreg.cgblibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.content.DialogInterface;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;

import com.an.deviceinfo.permission.PermissionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public class DeviceInfo implements PermissionManager.PermissionCallback {
    private Context context;
    private boolean isSimulator;
    private boolean hasRoot;
    private String DeviceName;
    private String IMEI;
    private String GoogleSF;
    private String DeviceUUID;
    private String BaseBand;
    private String Board;
    private String Fingerprint;
    private String Manufacturer;
    private String Model;
    private String Product;
    private String Bootloader;
    private String Release;
    private String Host;
    private String Ssid;
    private String SerialNumber;
    private String DISPLAY;
    private String ABI;
    private String API;
    private String AndroidVersion;
    private String AndroidId;
    private String OsVersion;
    private String GLRenderer;
    private String GLVendor;
    private String TimeZone;
    private String SimSerial;
    private String UserAgent;
    private String Dpi;
    private String Width;
    private String Height;
    private String PhoneNumber;
    private String NetworkCountryIso;
    private String NetworkOperator;
    private String NetworkCode;
    private String Ip;
    private String macAddress;

    public DeviceInfo(Context context) {
        this.context = context;
        GetDeviceInfo();
    }

    private void GetDeviceInfo() {
        isSimulator = isEmulator();
        hasRoot = isDeviceRooted();
        DeviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        Fingerprint = Build.FINGERPRINT;
        Manufacturer = Build.MANUFACTURER;
        Model = Build.MODEL;
        Product = Build.PRODUCT;
        Bootloader = Build.BOOTLOADER;
        Host = Build.HOST;
        DISPLAY = Build.DISPLAY;
        AndroidVersion = Integer.toString(Build.VERSION.SDK_INT);

        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || mTelephonyMgr.getImei() == null || mTelephonyMgr.getImei().isEmpty()) {
                IMEI = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } else {
                IMEI = mTelephonyMgr.getImei();
            }

            GoogleSF = getGSFID(context);
            DeviceUUID = getUUID();
            AndroidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            java.util.TimeZone tz = java.util.TimeZone.getDefault();
            TimeZone = tz.getDisplayName(false, java.util.TimeZone.SHORT) + " Timezon id :: " + tz.getID();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            Dpi = Integer.toString(metrics.densityDpi);
            Width = Integer.toString(metrics.widthPixels);
            Height = Integer.toString(metrics.heightPixels);
        } catch (Exception _ex) {
        }
    }

    private static final Uri sUri = Uri.parse("content://com.google.android.gsf.gservices");

    private boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private boolean isEmulator() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String networkOperator = tm.getNetworkOperatorName();
        boolean isSimulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("x86")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
        boolean isSimulator2 = false;
        if ("Android".equals(networkOperator)) {
            // Emulator
            isSimulator2 = true;
        } else {
            isSimulator2 = false;
            // Device
        }
        return isSimulator || isSimulator2;
    }

    private String getUUID() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String tmSerial = tm.getSimSerialNumber();
            String tmDeviceId = tm.getDeviceId();
            String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            if (tmSerial  == null) tmSerial   = "1";
            if (tmDeviceId== null) tmDeviceId = "1";
            if (androidId == null) androidId  = "1";
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDeviceId.hashCode() << 32) | tmSerial.hashCode());
            String uniqueId = deviceUuid.toString();
            return uniqueId;
        }
        return null;
    }

    private  String getGSFID(final Context context) {
        try {
            Cursor query = context.getContentResolver().query(sUri, null, null, new String[]{"android_id"}, null);
            if (query == null) {
                return "Not found";
            }
            if (!query.moveToFirst() || query.getColumnCount() < 2) {
                query.close();
                return "Not found";
            }
            final String toHexString = Long.toHexString(Long.parseLong(query.getString(1)));
            query.close();
            return toHexString.toUpperCase().trim();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private String getUserAgent(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return WebSettings.getDefaultUserAgent(context);
        } else {
            try {
                final Class<?> webSettingsClassicClass = Class.forName("android.webkit.WebSettingsClassic");
                final Constructor<?> constructor = webSettingsClassicClass.getDeclaredConstructor(Context.class, Class.forName("android.webkit.WebViewClassic"));
                constructor.setAccessible(true);
                final Method method = webSettingsClassicClass.getMethod("getUserAgentString");
                return (String) method.invoke(constructor.newInstance(context, null));
            } catch (final Exception e) {
                return new WebView(context).getSettings()
                        .getUserAgentString();
            }
        }
    }

    public String getJson() {
        String retValue= "";
        JSONObject person = new JSONObject();
        try {
            person.put("isSimulator", isSimulator);
            person.put("hasRoot", hasRoot);
            person.put("DeviceName", DeviceName);
            person.put("IMEI", IMEI);
            person.put("GoogleSF", GoogleSF);
            person.put("UUID", DeviceUUID);
            person.put("BaseBand", "BaseBand");
            person.put("Board", "Board");
            person.put(" Fingerprint", Fingerprint);
            person.put("Manufacturer", Manufacturer);
            person.put("Model", Model);
            person.put("Product", Product);
            person.put("Bootloader", Bootloader);
            person.put("Release", Release);
            person.put("Host", Host);
            person.put("Ssid", Ssid);
            person.put("SerialNumber", SerialNumber);
            person.put("DISPLAY", DISPLAY);
            person.put("ABI", "ABI");
            person.put("API", "API");
            person.put("AndroidVersion", AndroidVersion);
            person.put("AndroidId", AndroidId);
            person.put("OsVersion", OsVersion);
            person.put("GLRenderer", GLRenderer);
            person.put("GLVendor", GLVendor);
            person.put("TimeZone", TimeZone);
            person.put("SimSerial", SimSerial);
            person.put("UserAgent", UserAgent);
            person.put("Dpi", Dpi);
            person.put("Width", Width);
            person.put("Height", Height);
            person.put("PhoneNumber",PhoneNumber);
            person.put("NetworkCountryIso",NetworkCountryIso);
            person.put("NetworkOperator", NetworkOperator);
            person.put("NetworkCode", NetworkCode);
            person.put("Ip", Ip);
            person.put("macAddress", macAddress);

            retValue=person.toString(2);
        } catch (JSONException e) {
            LOG.E("log_tag", "Error parsing data " + e.toString());
        }
        return retValue;
    }

    @Override
    public void onPermissionGranted(String[] permissions, int[] grantResults) { }

    @Override
    public void onPermissionDismissed(String permission) { }

    @Override
    public void onPositiveButtonClicked(DialogInterface dialog, int which) { }

    @Override
    public void onNegativeButtonClicked(DialogInterface dialog, int which) { }

    public boolean isSimulator() {
        return isSimulator;
    }

    public boolean isHasRoot() {
        return hasRoot;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public String getIMEI() {
        return IMEI;
    }

    public String getGoogleSF() {
        return GoogleSF;
    }

    public String getDeviceUUID() {
        return DeviceUUID;
    }

    public String getBaseBand() {
        return BaseBand;
    }

    public String getBoard() {
        return Board;
    }

    public String getFingerprint() {
        return Fingerprint;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public String getModel() {
        return Model;
    }

    public String getProduct() {
        return Product;
    }

    public String getBootloader() {
        return Bootloader;
    }

    public String getRelease() {
        return Release;
    }

    public String getHost() {
        return Host;
    }

    public String getSsid() {
        return Ssid;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public String getDISPLAY() {
        return DISPLAY;
    }

    public String getABI() {
        return ABI;
    }

    public String getAPI() {
        return API;
    }

    public String getAndroidVersion() {
        return AndroidVersion;
    }

    public String getAndroidId() {
        return AndroidId;
    }

    public String getOsVersion() {
        return OsVersion;
    }

    public String getGLRenderer() {
        return GLRenderer;
    }

    public String getGLVendor() {
        return GLVendor;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public String getSimSerial() {
        return SimSerial;
    }

    public String getUserAgent() {
        return UserAgent;
    }

    public String getDpi() {
        return Dpi;
    }

    public String getWidth() {
        return Width;
    }

    public String getHeight() {
        return Height;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public String getNetworkCountryIso() {
        return NetworkCountryIso;
    }

    public String getNetworkOperator() {
        return NetworkOperator;
    }

    public String getNetworkCode() {
        return NetworkCode;
    }

    public String getIp() {
        return Ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public static Uri getsUri() {
        return sUri;
    }
}
