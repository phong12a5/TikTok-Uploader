package pdt.autoreg.app;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chilkatsoft.CkHttp;
import com.chilkatsoft.CkSsh;
import com.chilkatsoft.CkSshTunnel;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Protocol;
import pdt.autoreg.cgblibrary.BuildConfig;
import pdt.autoreg.cgblibrary.CGBInterface;
import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.cgblibrary.ModuleData;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenInfo;
import pdt.autoreg.devicefaker.Constants;

import static pdt.autoreg.app.AppDefines.COUNTRY_ARGENTINA;
import static pdt.autoreg.app.AppDefines.COUNTRY_AUSTRALIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_BANGLADESH;
import static pdt.autoreg.app.AppDefines.COUNTRY_BRAZIL;
import static pdt.autoreg.app.AppDefines.COUNTRY_BULGARIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_CANADA;
import static pdt.autoreg.app.AppDefines.COUNTRY_DENMARK;
import static pdt.autoreg.app.AppDefines.COUNTRY_FRANCE;
import static pdt.autoreg.app.AppDefines.COUNTRY_GEORGIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_GERMANY;
import static pdt.autoreg.app.AppDefines.COUNTRY_HONGKONG;
import static pdt.autoreg.app.AppDefines.COUNTRY_HUNGARY;
import static pdt.autoreg.app.AppDefines.COUNTRY_INDIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_INDONESIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_IRAN;
import static pdt.autoreg.app.AppDefines.COUNTRY_ISRAEL;
import static pdt.autoreg.app.AppDefines.COUNTRY_ITALY;
import static pdt.autoreg.app.AppDefines.COUNTRY_JAPAN;
import static pdt.autoreg.app.AppDefines.COUNTRY_KAZAKHSTAN;
import static pdt.autoreg.app.AppDefines.COUNTRY_KOREA;
import static pdt.autoreg.app.AppDefines.COUNTRY_MALAYSIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_MEXICO;
import static pdt.autoreg.app.AppDefines.COUNTRY_MOLDOVA;
import static pdt.autoreg.app.AppDefines.COUNTRY_NEPAL;
import static pdt.autoreg.app.AppDefines.COUNTRY_NETHERLANDS;
import static pdt.autoreg.app.AppDefines.COUNTRY_NEW_ZEALAND;
import static pdt.autoreg.app.AppDefines.COUNTRY_NIGERIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_NORWAY;
import static pdt.autoreg.app.AppDefines.COUNTRY_PERU;
import static pdt.autoreg.app.AppDefines.COUNTRY_PHILIPINS;
import static pdt.autoreg.app.AppDefines.COUNTRY_POLAND;
import static pdt.autoreg.app.AppDefines.COUNTRY_PORTUGAL;
import static pdt.autoreg.app.AppDefines.COUNTRY_ROMANIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_RUSSIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_SAUDI_ARABIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_SINGAPORE;
import static pdt.autoreg.app.AppDefines.COUNTRY_SLOVAKIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_SLOVENIA;
import static pdt.autoreg.app.AppDefines.COUNTRY_SOUTH_AFRICA;
import static pdt.autoreg.app.AppDefines.COUNTRY_SPAIN;
import static pdt.autoreg.app.AppDefines.COUNTRY_SWEDEN;
import static pdt.autoreg.app.AppDefines.COUNTRY_SWITZERLAND;
import static pdt.autoreg.app.AppDefines.COUNTRY_TAIWAN;
import static pdt.autoreg.app.AppDefines.COUNTRY_THAILAND;
import static pdt.autoreg.app.AppDefines.COUNTRY_TURKEY;
import static pdt.autoreg.app.AppDefines.COUNTRY_UKRAINE;
import static pdt.autoreg.app.AppDefines.COUNTRY_UNITED_KINGDOM;
import static pdt.autoreg.app.AppDefines.COUNTRY_UNITED_STATES;
import static pdt.autoreg.app.AppDefines.COUNTRY_VENEZUELA;
import static pdt.autoreg.app.AppDefines.COUNTRY_VIETNAM;
import static pdt.autoreg.app.AppDefines.PICTURES_FOLDER;

public abstract class BaseService extends Service {
    private static final String TAG = "BaseService";
    private static final int RATIO = 100;
    private int m_time_to_update = 10;
    private int m_time_to_gc = 0;
    protected int heightOfScreen = 0;
    protected List<String> m_screenStack = new ArrayList<String>();

    /* --------------------------------------- Abstract methods --------------------------------------- */
    protected abstract void mainOperations();

    @Override
    public void onCreate() {
        super.onCreate();
        m_workerThread.startWorker();
        LOG.D(TAG, "Created " + this.getClass().getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppModel.instance().setServiceStarted(false);
        m_workerThread.stopWorker();
        m_workerThread = null;
        LOG.D(TAG,"onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.D(TAG,"onStartCommand");
        AppModel.instance().setServiceStarted(true);
        return START_STICKY;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LOG.E(TAG,"onTrimMemory: " + level);
    }

    private WorkerThread m_workerThread = new WorkerThread() {
        public void run() {

            stopProxySwitch();
            initEnv(BaseService.this);
            disableAirplane();

            while (isRunning()) {
                try {
                    LOG.D(TAG, "*********************************** New cycle *********************************** ");

                    if(isScreenOff()) {
                        execute("input keyevent KEYCODE_POWER");
                    } else if(isScreenLocked()) {
                        execute("input keyevent 82");
                    }

                    CGBInterface.getInstance().updateKeywordDefinitions();

                    if (m_time_to_update >= 20) {
//                        checkAndUpdateNewVerion();
                        m_time_to_update = 0;
                    } else {
                        m_time_to_update++;
                        mainOperations();
                    }
                } catch (Exception e) {
                    LOG.E(TAG, "Thread::run Error: " + e);
                } finally {
                    if(m_time_to_gc > 30) {
                        System.gc();
                        delay(1000);
                        m_time_to_gc = 0;
                    } else {
                        m_time_to_gc ++;
                    }
                }
                delay(RATIO);
            }
            LOG.D(TAG, "*********************************** End cycle *********************************** ");
        }
    };

    protected void initEnv(Context context) {
        LOG.D(TAG, "Init environment");
        try {
            InputStream stream = getAssets().open("devices.json");
            File devicesInfo = new File(context.getCacheDir(), "devices.json");
            LOG.D(TAG, "devicesInfo: " + devicesInfo);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            String content = new String(buffer);
            FileUtils.writeStringToFile(devicesInfo, content);

            Runtime.getRuntime().exec("su -c cp " + devicesInfo.getAbsolutePath() + " /data/local/tmp/devices.json");
            Runtime.getRuntime().exec("su -c chmod 777 /data/local/tmp/devices.json");
        } catch (IOException e) {
            // Handle exceptions here
            e.printStackTrace();
        }

        // change screen resolution
        if(BuildConfig.DEBUG)
            execute("settings put system screen_brightness 40");
        else
            execute("settings put system screen_brightness 0");

        // change screen brightness
//        execute("settings put system screen_off_timeout 2147483647");

        // change screen timeout
        execute("wm density 320");
        execute("wm size 720x1280");

        // disable screen lock
        execute("svc power stayon true");
    }

    protected static void enableAirplane() {
        LOG.D("enableAirplane", "enableAirplane");
        String command1 = "settings put global airplane_mode_on 1";
        String command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true";
        String[] commands = {command1, command2};

        for (int i = 0; i < 3; i++) {
            if (isAirplaneEnable()) {
                return;
            }
            execute(commands);
        }
    }

    protected boolean isScreenOff() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return !pm.isScreenOn();
    }

    protected boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    protected static boolean isAirplaneEnable() {
        return Settings.System.getInt(
                App.getApplication().getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    protected static void disableAirplane() {
        LOG.D("disableAirplane", "disableAirplane");
        String command1 = "settings put global airplane_mode_on 0";
        String command2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false";
        String[] commands = {command1, command2};

        for (int i = 0; i < 3; i++) {
            if (!isAirplaneEnable()) {
                return;
            }
            execute(commands);
        }
    }

    public boolean detectScreen(boolean save2Stack) {
        try {
            ScreenInfo screenInfo = CGBInterface.getInstance().detectScreen(AppDefines.TIKTOK_APPNAME);
            if (screenInfo != null) {
                AppModel.instance().setCurrentScreenID(screenInfo.detected_screen_id);
                AppModel.instance().setCurrentScreenInfo(screenInfo.nodes_in_screen);
                LOG.D(TAG, "screenInfo.detected_screen_id: " + screenInfo.detected_screen_id);
                if (save2Stack) {
                    m_screenStack.add(AppModel.instance().getCurrentScreenID());
                }
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
        /* ------------- END Detect screen ------------- */
    }

    private void checkAndUpdateNewVerion() {
        try {
            PackageInfo pInfo = App.getContext().getPackageManager().getPackageInfo(BaseService.this.getPackageName(), 0);
            int versionCode = pInfo.versionCode;
            int new_version_code = versionCode + 1;
            if (new_version_code > versionCode) {
                String apkFileName = BaseService.this.getPackageName() + "." + new_version_code + ".apk";
                if(DropboxAPI.downloadFileFromDropbox("/apk/" + apkFileName, AppDefines.AUTOREG_FOLDER + apkFileName)) {
                    execute("cp " + AppDefines.AUTOREG_FOLDER + apkFileName + " /data/local");
                    execute("pm install -r /data/local/" + apkFileName);
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "submitClone error: " + e);
        }
    }

    protected void deletePicInPicFolderRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deletePicInPicFolderRecursive(child);
            }
        if (fileOrDirectory.isFile() && (fileOrDirectory.getName().contains(".png") || fileOrDirectory.getName().contains(".jpg"))) {
            deleteFileFromMediaStore(App.getApplication().getContentResolver(), fileOrDirectory);
        }
    }

    protected void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    protected void saveImageToExternal(String imgNamePath, Bitmap bm) {
        LOG.D(TAG, "saveImageToExternal: " + imgNamePath);
        try{
            File imageFile = new File(imgNamePath); // Imagename.png
            FileOutputStream out = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(App.getContext(),new String[] { imageFile.getAbsolutePath() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    LOG.I(TAG, "ExternalStorage Scanned " + path + ":");
                    LOG.I(TAG, "ExternalStorage -> uri=" + uri);
                }
            });
        } catch(Exception e) {
            LOG.E(TAG,"saveImageToExternal: " + e);
        }
    }

    /* Show Toast message */
    void showToastMessage(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected JSONObject scanProxy() {
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url("https://api.getproxylist.com/proxy?protocol[]=http&minUptime=75")
                    .get()
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);
            LOG.D(TAG, "code: " + response.code());
            if (response.code() == 200) {
                return new JSONObject(resStr);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
            return null;
        }
    }

    public static JSONObject scanProxyFromWeb() {
        LOG.D(TAG, "scanProxyFromWeb");
        JSONObject proxy = null;
        try {
            OkHttpClient client = new OkHttpClient();
            client.setCache(null);
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            String url = "http://free-proxy.cz/en/proxylist/main/3";
            url = url.replace(" ","+") + (url.contains("?") ? "&" : "?") + "_=" + System.currentTimeMillis();

            LOG.D(TAG, "url: " + url);
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String resStr = response.body().string();
            LOG.D(TAG, "resStr: " + resStr);

            List<String> listIps = regex(resStr,"(?<=Base64.decode\\(\\\")(.*?)(?=\\\"\\))");
            LOG.D(TAG, "listIps: " + listIps);

            List<String> listPort = regex(resStr,"(?<=<td style=\\\"\\\"><span class=\\\"fport\\\" style=\\'\\'>)(.*?)(?=</span></td>)");;
            LOG.D(TAG, "listPort: " + listPort);

            List<String> listType = regex(resStr,"(?<=</span></td><td><small>)(.*?)(?=</small></td>)");;
            LOG.D(TAG, "listType: " + listType);
            if(listIps.size() == listPort.size() &&
                    listPort.size() == listType.size()) {
                LOG.D(TAG, "listType: " + listType);
                for (int i = 0; i < listIps.size(); i++ ) {
                    String ip =  new String(java.util.Base64.getDecoder().decode(listIps.get(i)), "UTF-8");
                    if(ping(ip)) {
                        proxy = new JSONObject();
                        proxy.put("ip",ip);
                        proxy.put("port",Integer.valueOf(listPort.get(i)));
                        proxy.put("type",listType.get(i));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
        }
        return proxy;
    }

    JSONObject scanProxyFromGeonode(int limit, int page, String protocol, String countryPrefer) {
        //https://proxylist.geonode.com/api/proxy-list?limit=50&page=1&sort_by=lastChecked&sort_type=desc&fbclid=IwAR1svtJ4Y3C_INVfgD8ai8QCzMFagQioNHikd2VaUo2iEIKLLtiRLWH39Nw
        //http://pubproxy.com/api/proxy?limit=20
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(com.squareup.okhttp.Protocol.HTTP_1_1));

            String url = new Random().nextBoolean()? String.format("https://proxylist.geonode.com/api/proxy-list?limit=%d&page=%d&sort_by=lastChecked&sort_type=desc",limit,page) :
                    "http://pubproxy.com/api/proxy?limit=20";
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
                JSONObject responseJson = new JSONObject(resStr);
                JSONArray proxyList = responseJson.getJSONArray("data");
                LOG.D(TAG, "proxyList: " + proxyList);
                JSONObject retVal = null;

                while (proxyList.length() > 0) {
                    int index = new Random().nextInt(proxyList.length());
                    JSONArray protocols = proxyList.getJSONObject(index).getJSONArray("protocols");
                    String country = proxyList.getJSONObject(index).getString("country");
                    if(protocols.toString().contains(protocol) && (countryPrefer == null || countryPrefer.equals(country))) {
                        retVal = new JSONObject();
                        retVal.put("ip", proxyList.getJSONObject(index).getString("ip"));
                        retVal.put("port", proxyList.getJSONObject(index).getInt("port"));
                        retVal.put("country", proxyList.getJSONObject(index).getString("country"));
                        break;
                    } else {
                        proxyList.remove(index);
                    }
                }
                return retVal;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.E(TAG, "scanProxy error: " + e);
            return null;
        }
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

    public class ScanTask implements Callable<ScanResult> {

        private String ipAddress;
        private int port;
        private String username;
        private String password;
        private String countryCode;

        public ScanTask(String ipAddress, int port, String username, String password, String countryCode) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.username = username;
            this.password = password;
            this.countryCode = countryCode;
        }

        @Override
        public ScanResult call() {
            long startTime = System.nanoTime();
            if(checkSSHConnection(ipAddress, port,countryCode)) {
                long stopTime = System.nanoTime();
                return new ScanResult(ipAddress, port, username, password, stopTime - startTime, countryCode,0);
            } else {
                return new ScanResult(ipAddress, port, username, password, -1, countryCode,-1);
            }
        }

        private boolean checkSSHConnection(String ipAddress, int port, String countryCode) {
            LOG.D(TAG, "checkSSHConnection ipAddress: " + ipAddress + " -- countryCode: " + countryCode);
            CkSsh ssh = new CkSsh();
            ssh.put_ConnectTimeoutMs(3000);
            ssh.put_IdleTimeoutMs(3000);
            boolean connected = false;
            if (ssh.Connect(ipAddress,port)) {

                //  Am I connected?
                connected = ssh.get_IsConnected();

                //  Disconnect.
                ssh.Disconnect();
            }
            LOG.D(TAG, "checkSSHConnection -- ipAddress: " + ipAddress + " -- connect: " + (connected?  "success" : "failed"));
            return connected;
        }


        private double speedTest(String host) {
            CkHttp http = new CkHttp();
            http.put_ConnectTimeout(10);
            http.put_ReadTimeout(10);

            double start = System.nanoTime();

            String testFilePath = Environment.getExternalStorageDirectory() + "/Download/" + host;
            if (!http.Download("http://dl.google.com/googletalk/googletalk-setup.exe",
                    testFilePath)) {
                LOG.E(TAG, "Download rrror: " + http.lastErrorText());
                new File(testFilePath).delete();
                return -1;
            } else {
                // some time passes
                double end = System.nanoTime();
                double elapsedTime = end - start;
                double speed = 1.6 * 1000000000 / elapsedTime;
                LOG.D(TAG, "speedTest host: " + host + " -- speed: " + speed);
                new File(testFilePath).delete();
                return speed;
            }
        }
    }

    public class ScanResult {
        private String ipAddress;
        private int port;
        private String username;
        private String password;
        private long excuteTime;
        private String countryCode;
        private int resultCode;

        public ScanResult(String ipAddress, int port, String username, String password, long excuteTime, String countryCode, int resultCode) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.username = username;
            this.password = password;
            this.excuteTime = excuteTime;
            this.countryCode = countryCode;
            this.resultCode = resultCode;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public double getExcuteTime() { return excuteTime; }

        public String getcountryCode() { return countryCode; }

        public int getResultCode() {
            return resultCode;
        }

        public String toString() {
            return "IpAddress :: "+ ipAddress + " Result Code : "+ resultCode;
        }
    }

    private JSONObject scanProxyBySshtunnel(String ipAddress, int port, String username, String password) {
        LOG.D(TAG, "scanProxyBySshtunnel ipAddress: " + ipAddress + " -- username: " + username + " -- password: " + password);
        boolean success = false;
        JSONObject result = null;
        //  This example requires Chilkat version 9.5.0.50 or greater.
        CkSshTunnel tunnel = new CkSshTunnel();

        //  Connect to an SSH server and establish the SSH tunnel:
//            tunnel.put_ConnectTimeoutMs(5000);
//            tunnel.put_IdleTimeoutMs(5000);
        success = tunnel.Connect(ipAddress,port);
        if (success != true) {
                System.out.println(tunnel.lastErrorText());
            return null;
        }

        //  Authenticate with the SSH server via a login/password
        //  or with a public key.
        //  This example demonstrates SSH password authentication.
        success = tunnel.AuthenticatePw(username,password);
        if (success != true) {
                System.out.println(tunnel.lastErrorText());
            return null;
        }

        //  Indicate that the background SSH tunnel thread will behave as a SOCKS proxy server
        //  with dynamic port forwarding:
        tunnel.put_DynamicPortForwarding(true);

        //  We may optionally require that connecting clients authenticate with our SOCKS proxy server.
        //  To do this, set an inbound username/password.  Any connecting clients would be required to
        //  use SOCKS5 with the correct username/password.
        //  If no inbound username/password is set, then our SOCKS proxy server will accept both
        //  SOCKS4 and SOCKS5 unauthenticated connections.

//        tunnel.put_InboundSocksUsername("chilkat123");
//        tunnel.put_InboundSocksPassword("password123");

        //  Start the listen/accept thread to begin accepting SOCKS proxy client connections.
        //  Listen on port 1080.
        success = tunnel.BeginAccepting(1080);
        if (success != true) {
                System.out.println(tunnel.lastErrorText());
            return null;
        }

        //  Now that a background thread is running a SOCKS proxy server that forwards connections
        //  through an SSH tunnel, it is possible to use any Chilkat implemented protocol that is SOCKS capable,
        //  such as HTTP, POP3, SMTP, IMAP, FTP, etc.  The protocol may use SSL/TLS because the SSL/TLS
        //  will be passed through the SSH tunnel to the end-destination.  Also, any number of simultaneous
        //  connections may be routed through the SSH tunnel.

        //  For this example, let's do a simple HTTPS request:
        String url = "https://api.getproxylist.com/proxy?protocol[]=http&minUptime=240";

        CkHttp http = new CkHttp();

        //  Indicate that the HTTP object is to use our portable SOCKS proxy/SSH tunnel running in our background thread.
        http.put_SocksHostname("localhost");
        http.put_SocksPort(1080);
        http.put_SocksVersion(5);
        http.put_SocksUsername("chilkat123");
        http.put_SocksPassword("password123");

//        http.put_SendCookies(true);
//        http.put_SaveCookies(true);
//        http.put_CookieDir("memory");

        //  Do the HTTPS page fetch (through the SSH tunnel)
        String html = http.quickGetStr(url);
        LOG.D(TAG, "html: " + html);
        if (http.get_LastMethodSuccess() != true) {
                System.out.println(http.lastErrorText());
            tunnel.StopAccepting(true);
            tunnel.CloseTunnel(true);
            tunnel.delete();
            tunnel = null;
            return null;
        } else {
            try {
                result = new JSONObject(html);
            } catch (Exception e){}
        }

        //  Stop the background listen/accept thread:
        boolean waitForThreadExit = true;
        tunnel.StopAccepting(waitForThreadExit);
        tunnel.CloseTunnel(waitForThreadExit);
        tunnel.DisconnectAllClients(waitForThreadExit);
        while(!tunnel.CloseTunnel(waitForThreadExit)) {
            LOG.E(TAG, "CloseTunnel: " + tunnel.lastErrorText());
            delay(1000);
        }
        tunnel.delete();
        tunnel = null;
        return result;
    }


    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
        }
    }

    public static void delayRandom(long timeStart, long timeEnd ) {
        delay(timeStart + new Random().nextInt((int) (timeEnd - timeStart)));
    }

    protected void starProxySwitch() {
        LOG.D(TAG,"starProxySwitch");
        String cmd1 = "chmod 700 /data/data/pdt.autoreg.app/iptables";
        String cmd2 = "chmod 700 /data/data/pdt.autoreg.app/redsocks";
        String cmd3 = "chmod 700 /data/data/pdt.autoreg.app/proxy.sh";
        String cmd4 = "chmod 700 /data/data/pdt.autoreg.app/cntlm";
        String cmd5 = "chmod 700 /data/data/pdt.autoreg.app/stunnel";
        String cmd6 = "chmod 700 /data/data/pdt.autoreg.app/shrpx";
        String cmd7 = "/data/data/pdt.autoreg.app/proxy.sh start socks5 127.0.0.1 1080 false \"\" \"\"";
        String cmd8 = "/system/bin/iptables -t nat -A OUTPUT -p tcp -d 127.0.0.1 -j RETURN\n /system/bin/iptables -t nat -A OUTPUT -p tcp -j REDIRECT --to 8123";
        String[] commands = {cmd1,cmd2,cmd3,cmd4,cmd5,cmd6,cmd7,cmd8};
        execute(commands);
    }

    public static void starProxySwitch(Context context, String host, int port, String protocol) {
        LOG.D(TAG, "starProxySwitch -- host: "+ host + " -- port: " + port + " -- protocol: " + protocol);
        // /system/bin/iptables -t nat -A OUTPUT -p tcp -d 189.231.202.141 -j RETURN
        LOG.D(TAG,"starProxySwitch");
        String cmd1 = "chmod 700 /data/data/pdt.autoreg.app/files/redsocks";
        String cmd2 = "chmod 700 /data/data/pdt.autoreg.app/files/proxy.sh";
        String cmd3 = "chmod 700 /data/data/pdt.autoreg.app/files/gost.sh";
        String cmd4 = "chmod 700 /data/data/pdt.autoreg.app/files/cntlm";
        String cmd5 = "chmod 700 /data/data/pdt.autoreg.app/files/gost";


        switch (protocol) {
            case AppDefines.PROXY_PROTOCOL_HTTP: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start %s %s %d false \"\" \"\"", protocol, host, port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd8 = AppDefines.CMD_IPTABLES_REDIRECT_ADD_HTTP;
                execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            case AppDefines.PROXY_PROTOCOL_HTTPS: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " -L=http://127.0.0.1:8126 -F=https://%s:%d?ip=%s", host, port, host);
                String cmd7 = context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start http 127.0.0.1 8126 false \"\" \"\"";
                String cmd8 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd9 = AppDefines.CMD_IPTABLES_REDIRECT_ADD_HTTP;
                execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8, cmd9});
            }
            break;
            case AppDefines.PROXY_PROTOCOL_SOCKS4:
            case AppDefines.PROXY_PROTOCOL_SOCKS5: {
                String cmd6 = String.format(context.getFilesDir() + File.separator + "proxy.sh " + context.getFilesDir() + File.separator + " start %s %s %d false \"\" \"\"", protocol, host, port);
                String cmd7 = String.format("/system/bin/iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN", host);
                String cmd8 = AppDefines.CMD_IPTABLES_REDIRECT_ADD_SOCKS;
                execute(new String[]{cmd1, cmd2, cmd3, cmd4, cmd5, cmd6, cmd7, cmd8});
            }
            break;
            default:
                LOG.E(TAG, "starProxySwitch: " + protocol + " is not supported!");
                break;
        }
    }

    public static void stopProxySwitch() {
        LOG.D(TAG,"stopProxySwitch");
        String cmd1 = "/system/bin/iptables -t nat -F OUTPUT";
        String cmd2 = "/data/data/pdt.autoreg.app/files/proxy.sh /data/data/pdt.autoreg.app/files/ stop";
        String[] commands = {cmd1,cmd2};
        execute(commands);
    }

    public static String execute(String[] commands, Charset type) {
        String result = null;
        try {
            Process exec = Runtime.getRuntime().exec("su");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());

            for (String command : commands) {
                LOG.D(TAG, "" + command);
                if (type != null){
                    dataOutputStream.writeBytes(Arrays.toString(command.getBytes(type)) + "\n");
                }else {
                    dataOutputStream.writeBytes(command + "\n");
                }
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            dataOutputStream.close();
            try {
                exec.waitFor();
            } catch (InterruptedException ignored) {
                Log.d("VDRootError",ignored.toString());
            }
            if (bufferedReader.ready()) {
                StringBuilder builder = new StringBuilder();
                String aux;
                while ((aux = bufferedReader.readLine()) != null) {
                    builder.append(aux);
                }
                result = builder.toString();
            }
        } catch (Exception ignored) {
            Log.d("VDRootError",ignored.toString());
        }

        return result;
    }

    public static String execute(String command) {
        LOG.D(TAG, "command: " + command);
        String result = null;
        try {
            String retour = "";
            Runtime runtime = Runtime.getRuntime();

            Process p = runtime.exec("su -c " + command);

            java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            java.io.BufferedReader errorIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                retour += line + "\n";
            }
            while ((line = errorIn.readLine()) != null) {
                retour += line + "\n";
            }
            result = retour;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String execute(String[] command) {
        return execute(command,null);
    }

    public static String execute(String command, Charset type) {
        String[] commands = {command};
        return execute(commands,type);
    }

}
