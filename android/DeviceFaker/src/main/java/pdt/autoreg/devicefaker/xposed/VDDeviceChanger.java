package pdt.autoreg.devicefaker.xposed;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;
import pdt.autoreg.devicefaker.Constants;
import pdt.autoreg.devicefaker.xposed.VDRandom;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.findField;

public class VDDeviceChanger {

    private static final String MY_PACKAGE_NAME = "com.vietdung.vdtools";
    private static ClassLoader classLoader;
    private static final String[] APP_ROOT = {
            "supersu", "superuser", "Superuser",
            "noshufou", "xposed", "rootcloak",
            "chainfire", "titanium", "Titanium",
            "substrate", "greenify", "daemonsu",
            "root", "busybox", "titanium","magisk",
            ".tmpsu", "su", "rootcloak2",
            "XposedBridge.jar","framework",
    };

    private ClassLoader getClassLoader(){
        return classLoader;
    }

    public void init(XC_LoadPackage.LoadPackageParam lpparam) {
        classLoader = lpparam.classLoader;

        if (lpparam.packageName.startsWith("com.android.")) {

            if (lpparam.packageName.equals("com.android.systemui")){

                updateClock();
            }
            return;
        }

//        String bootId = VDFileHelper.readTextFile("/proc/sys/kernel/random/boot_id");

        if (lpparam.packageName.equals(Constants.REG_PACKAGE)) {

            @SuppressLint("SdCardPath")
            String deviceInfor = VDDeviceInfor.fakeRandom();

            XposedBridge.log("Device raw: " + deviceInfor);

            if (deviceInfor.isEmpty()) {
                return;
            }

            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                    .permitDiskReads()
                    .permitDiskWrites()
                    .build());

            try {
                JSONObject deviceJson = new JSONObject(deviceInfor);

                updateBuildProp(deviceJson);

                updateUserAgent(deviceJson);

                updateWifi(deviceJson);

                updateBluetooth(deviceJson);

                updateIMEI(deviceJson);

                updateAndroidId(deviceJson);

                updateSerial(deviceJson);

                updateTelephony(deviceJson);
                
                updateGPU();

                hideDebug();

                hideAdbDebug();

                enableSELinux();

                preventProcessBuilder();

                preventRuntime();

                hideXposedClass();

                hideSearchRootXposedFile();

                baseBand(deviceJson);

                if (!lpparam.packageName.equals("com.facebook.katana")){
                    hidePackageManager();
                }

                hideActivityManager();

                XposedBridge.log("Changer Success: " + lpparam.packageName);

            } catch (Exception e) {
                XposedBridge.log("Module Error: " + e.toString());
            } finally {
                StrictMode.setThreadPolicy(old);
            }
        }
    }

    private void updateGPU() {
        XposedHelpers.findAndHookMethod("com.google.android.gles_jni.GLImpl", getClassLoader(), "glGetString", Integer.TYPE, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {

                XposedBridge.log("VDLogger GPU: " + param.args[0]);

                if (param.args[0] != null) {
                    if (param.args[0].equals(7936)) {
                        param.setResult(VDRandom.randomNumber(7000,8000));
                    }
                    if (param.args[0].equals(7937)) {
                        param.setResult(VDRandom.randomNumber(7000,8000));
                    }
                }
            }

        });
    }

    private void baseBand(final JSONObject deviceJson) throws Exception {
        String baseBand = deviceJson.getString("BaseBand");
        findField(Build.class, "RADIO").set(null,baseBand);
        findAndHookMethod(Build.class, "getRadioVersion",new XC_ResultHook(baseBand));


        @SuppressLint("PrivateApi") Class<?> classSysProp = Class.forName("android.os.SystemProperties");
        XposedHelpers.findAndHookMethod(classSysProp, "get", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        String serialno = (String) param.args[0];

                        if (serialno.equals("gsm.version.baseband") || serialno.equals("no message")) {
                            param.setResult(deviceJson.getString("BaseBand"));
                        }
                    }

                });

        XposedHelpers.findAndHookMethod(classSysProp, "get", String.class,String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        String serialno = (String) param.args[0];

                        if (serialno.equals("gsm.version.baseband") || serialno.equals("no message")) {
                            param.setResult(deviceJson.getString("BaseBand"));
                        }
                    }

                });
    }

    private void hideActivityManager() {
        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningServices", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedBridge.log("ActivityManagerLogger 1: " + param.getResult());
                param.setResult(new ArrayList<>());
            }
        });

        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningTasks", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedBridge.log("ActivityManagerLogger 2: " + param.getResult());
                param.setResult(new ArrayList<>());
            }
        });

        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningAppProcesses", new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedBridge.log("ActivityManagerLogger 3: " + param.getResult());
                param.setResult(new ArrayList<>());
            }
        });
    }

    private void hidePackageManager() {
        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getInstalledApplications", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(new ArrayList<>());
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(new ArrayList<>());
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
    }

    private void hideSearchRootXposedFile() {
//        hookMethod(findConstructorExact(File.class, String.class), new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) {
//                for (int i=0; i < param.args.length; i++){
//                    XposedBridge.log("VDLogger 1: " + param.args[i]);
//                    XposedBridge.log("VDLogger 2: " + param.getResult());
//                }
//            }
//        });
//
//        hookMethod(findConstructorExact(File.class, String.class, String.class), new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) {
//                for (int i=0; i < param.args.length; i++){
//                    if (!param.args[i].toString().contains("com.facebook.katana")){
//                        XposedBridge.log("VDLogger 1: " + param.args[i]);
//                        XposedBridge.log("VDLogger 2: " + param.getResult());
//                        param.args[i] = "/";
//                    }
//                }
//            }
//        });
    }

    private void hideXposedClass() {
        findAndHookMethod("java.lang.Class", getClassLoader(), "forName", String.class, boolean.class, ClassLoader.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String classname = (String) param.args[0];

                if (classname != null &&
                        (classname.equals("de.robv.android.xposed.XposedBridge") ||
                                classname.equals("de.robv.android.xposed.IXposedHookLoadPackage") ||
                                classname.equals("de.robv.android.xposed.IXposedHookZygoteInit") ||
                                classname.equals("de.robv.android.xposed.XC_MethodHook") ||
                                classname.equals("de.robv.android.xposed.XC_MethodReplacement") ||
                                classname.equals("de.robv.android.xposed.XSharedPreferences") ||
                                classname.equals("de.robv.android.xposed.XposedHelpers") ||
                                classname.equals("de.robv.android.xposed.IXposedHookInitPackageResources") ||
                                classname.equals("de.robv.android.xposed.SELinuxHelper") ||

                                classname.equals("de.robv.android.xposed.callbacks.XC_LoadPackage") ||
                                classname.equals("de.robv.android.xposed.callbacks.IXUnhook") ||
                                classname.equals("de.robv.android.xposed.callbacks.XC_InitPackageResources") ||
                                classname.equals("de.robv.android.xposed.callbacks.XC_LayoutInflated") ||
                                classname.equals("de.robv.android.xposed.callbacks.XCallback") ||

                                classname.equals("de.robv.android.xposed.services.BaseService") ||
                                classname.equals("de.robv.android.xposed.services.FileResult"))) {

                    param.setThrowable(new ClassNotFoundException());
                }
            }
        });
    }

    private void preventProcessBuilder() {
        Constructor<?> processBuilderConstructor2 = findConstructorExact(ProcessBuilder.class, String[].class);
        hookMethod(processBuilderConstructor2, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
    }

    private void preventRuntime() {
        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String.class,String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String.class, String[].class, File.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String[].class,String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String[].class, String[].class, File.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "loadLibrary", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
    }

    private void enableSELinux() {
        findAndHookMethod("android.os.SystemProperties", getClassLoader(), "get", String.class , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals("ro.build.selinux")) {
                    param.setResult("1");
                }
            }
        });
    }

    private void hideAdbDebug() {
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(0);
            }
        });

        findAndHookMethod(Settings.Secure.class, "getInt", ContentResolver.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(0);
            }
        });
    }

    private void hideDebug() {
        findAndHookMethod(
                "android.os.Debug",
                getClassLoader(),
                "isDebuggerConnected",
                XC_MethodReplacement.returnConstant(false)
        );
    }

    private void updateClock() {
        findAndHookMethod("com.android.systemui.statusbar.policy.Clock",
                getClassLoader(),
                "updateClock",
                new XC_MethodHook() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        TextView tv = (TextView) param.thisObject;
                        tv.setText("VDTools Enable");
                        tv.setTextColor(Color.GREEN);
                    }
                });
    }

    private void updateUserAgent(final JSONObject deviceJson) throws Exception {
        findAndHookMethod(
                "android.webkit.WebSettings",
                getClassLoader(),
                "getDefaultUserAgent",
                Context.class,
                new XC_ResultHook(deviceJson.getString("UserAgent"))
        );

        findAndHookMethod(System.class, "getProperty", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                if(param.args[0].equals("http.agent")){
                    param.setResult(deviceJson.getString("DalvikUserAgent"));
                }
            }
        });
    }

    private void updateTelephony(JSONObject deviceJson) throws JSONException {
        hookAllMethods(TelephonyManager.class,"getLine1Number", new XC_ResultHook(deviceJson.getString("PhoneNumber")));
        hookAllMethods(TelephonyManager.class,"getSubscriberId",new XC_ResultHook(deviceJson.getString("SubscriberId")));
        hookAllMethods(TelephonyManager.class,"getSimSerialNumber",new XC_ResultHook(deviceJson.getString("SimSerialNumber")));
        hookAllMethods(TelephonyManager.class,"getSimCountryIso",new XC_ResultHook(deviceJson.getString("SimCountryIso")));
        hookAllMethods(TelephonyManager.class,"getSimOperator",new XC_ResultHook(deviceJson.getString("SimOperator")));
        hookAllMethods(TelephonyManager.class,"getSimOperatorName",new XC_ResultHook(deviceJson.getString("SimOperatorName")));
        hookAllMethods(TelephonyManager.class,"getNetworkCountryIso",new XC_ResultHook(deviceJson.getString("NetworkCountryIso")));
        hookAllMethods(TelephonyManager.class,"getNetworkOperator",new XC_ResultHook(deviceJson.getString("NetworkOperator")));
        hookAllMethods(TelephonyManager.class,"getNetworkOperatorName",new XC_ResultHook(deviceJson.getString("NetworkOperatorName")));
        hookAllMethods(TelephonyManager.class,"getDeviceSoftwareVersion",new XC_ResultHook(deviceJson.getString("DeviceSoftwareVersion")));
        hookAllMethods(TelephonyManager.class,"getMmsUAProfUrl",new XC_ResultHook(deviceJson.getString("MmsUAProfUrl")));
        hookAllMethods(TelephonyManager.class,"getMmsUserAgent",new XC_ResultHook(deviceJson.getString("MmsUserAgent")));
        hookAllMethods(TelephonyManager.class,"getDataNetworkType",new XC_ResultHook(deviceJson.getString("DataNetworkType")));
        hookAllMethods(TelephonyManager.class,"getDataState",new XC_ResultHook(deviceJson.getString("DataState")));
        hookAllMethods(TelephonyManager.class,"getPhoneCount",new XC_ResultHook(1));

        hookAllMethods(TelephonyManager.class,"getPhoneType",new XC_ResultHook(TelephonyManager.PHONE_TYPE_GSM));
        hookAllMethods(TelephonyManager.class,"getNetworkType",new XC_ResultHook(TelephonyManager.NETWORK_TYPE_HSPAP));
        hookAllMethods(TelephonyManager.class,"getSimState",new XC_ResultHook(TelephonyManager.SIM_STATE_READY));
        hookAllMethods(TelephonyManager.class,"hasIccCard",new XC_ResultHook(true));
    }

    private void updateSerial(JSONObject deviceJson) throws Exception {

        String serial = deviceJson.getString("Serial");

        String[] serialProperties = {
                "no.such.thing",
                "ro.serialno",
                "ro.boot.serialno",
                "ril.serialnumber",
                "gsm.sn1",
                "sys.serialnumber"
        };

        for (String properties : serialProperties){
            changeProperties(properties,serial);
        }

        findAndHookMethod(Build.class,"getSerial",new XC_ResultHook(serial));
        findField(Build.class, "SERIAL").set(null, serial);
    }

    private void updateAndroidId(final JSONObject deviceJson){
        findAndHookMethod("android.provider.Settings.Secure", getClassLoader(), "getString", ContentResolver.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws JSONException {
                if (param.args[1].equals(Settings.Secure.ANDROID_ID)) {
                    param.setResult(deviceJson.getString("AndroidId"));
                }
            }
        });

        XposedBridge.hookAllMethods(android.provider.Settings.Secure.class, "getString", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length > 1 && param.args[1] != null) {
                    param.setResult(deviceJson.getString("AndroidId"));
                }
            }
        });
    }

    private void updateIMEI(JSONObject deviceJson) throws JSONException {
        String imei = deviceJson.getString("Imei");

        findAndHookMethod("com.android.internal.telephony.imsphone.ImsPhone", getClassLoader(), "getDeviceId", new XC_ResultHook(imei));
        findAndHookMethod("com.android.internal.telephony.sip.SipPhone",getClassLoader(), "getDeviceId", new XC_ResultHook(imei));
        findAndHookMethod(TelephonyManager.class, "getDeviceId", new XC_ResultHook(imei));
        findAndHookMethod(TelephonyManager.class, "getImei", new XC_ResultHook(imei));
    }

    private void updateBluetooth(JSONObject deviceJson) throws JSONException {
        String macBluetooth = deviceJson.getString("MacBluetooth");

        findAndHookMethod(BluetoothAdapter.class, "getAddress", new XC_ResultHook(macBluetooth));
        findAndHookMethod(BluetoothDevice.class, "getAddress", new XC_ResultHook(macBluetooth));
    }

    private void updateWifi(JSONObject deviceJson) throws JSONException {
        findAndHookMethod(WifiInfo.class,"getMacAddress",new XC_ResultHook(deviceJson.getString("MacWifi")));
        findAndHookMethod(WifiInfo.class,"getSSID",new XC_ResultHook(deviceJson.getString("SSIDWifi")));
        findAndHookMethod(WifiInfo.class,"getBSSID",new XC_ResultHook(deviceJson.getString("BSSIDWifi")));
        findAndHookMethod(java.net.NetworkInterface.class.getName(),getClassLoader(),"getHardwareAddress" , new XC_ResultHook(deviceJson.getString("MacWifi")));

    }

    private void updateBuildProp(JSONObject deviceJson) throws Exception{

        String buildId = deviceJson.getString("ro.build.id");
        findField(Build.class, "ID").set(null,buildId);
        changeProperties("ro.build.id",buildId);

        String displayId = deviceJson.getString("ro.build.display.id");
        findField(Build.class, "DISPLAY").set(null,displayId);
        changeProperties("ro.build.display.id",displayId);

        String incremental = deviceJson.getString("ro.build.version.incremental");
        findField(Build.VERSION.class, "INCREMENTAL").set(null, incremental);
        changeProperties("ro.build.version.incremental",incremental);

//        int versionSDK = deviceJson.getInt("ro.build.version.sdk");
//        findField(Build.VERSION.class, "SDK_INT").set(null,versionSDK);
//        changeProperties("ro.build.version.sdk",versionSDK);

        String codeName = deviceJson.getString("ro.build.version.codename");
        findField(Build.VERSION.class, "CODENAME").set(null,codeName);
        changeProperties("ro.build.version.codename",codeName);

        String versionRelease = deviceJson.getString("ro.build.version.release");
        findField(Build.VERSION.class, "RELEASE").set(null,versionRelease);
        changeProperties("ro.build.version.release",versionRelease);

        String securityPatch = deviceJson.getString("ro.build.version.security_patch");
        findField(Build.VERSION.class, "SECURITY_PATCH").set(null,securityPatch);
        changeProperties("ro.build.version.security_patch",securityPatch);

        String baseOS = deviceJson.getString("ro.build.version.base_os");
        findField(Build.VERSION.class, "BASE_OS").set(null, baseOS);
        changeProperties("ro.build.version.base_os",baseOS);

        String date = deviceJson.getString("ro.build.date");
        changeProperties("ro.build.date",date);

        long dateUTC = deviceJson.getLong("ro.build.date.utc");
        findField(Build.class, "TIME").set(null, dateUTC / 1000);
        changeProperties("ro.build.date.utc",dateUTC);

        String type = deviceJson.getString("ro.build.type");
        findField(Build.class, "TYPE").set(null,type);
        changeProperties("ro.build.type",type);

        String user = deviceJson.getString("ro.build.user");
        findField(Build.class, "USER").set(null,user);
        changeProperties("ro.build.user",user);

        String host = deviceJson.getString("ro.build.host");
        findField(Build.class, "HOST").set(null, host);
        changeProperties("ro.build.host",host);

        String tags = deviceJson.getString("ro.build.tags");
        findField(Build.class, "TAGS").set(null, tags);
        changeProperties("ro.build.tags",tags);

        String flavor = deviceJson.getString("ro.build.flavor");
        changeProperties("ro.build.flavor",flavor);

        String model = deviceJson.getString("ro.product.model");
        findField(Build.class, "MODEL").set(null, model);
        changeProperties("ro.product.model",model);

        String brand = deviceJson.getString("ro.product.brand");
        findField(Build.class, "BRAND").set(null,brand);
        changeProperties("ro.product.brand",brand);

        String name = deviceJson.getString("ro.product.name");
        changeProperties("ro.product.name",name);

        String device = deviceJson.getString("ro.product.device");
        findField(Build.class, "DEVICE").set(null,device);
        changeProperties("ro.product.device",device);

        String board = deviceJson.getString("ro.product.board");
        findField(Build.class, "BOARD").set(null,board);
        changeProperties("ro.product.board",board);

        String abi = deviceJson.getString("ro.product.cpu.abi");
        findField(Build.class, "CPU_ABI").set(null,abi);
        changeProperties("ro.product.cpu.abi",abi);

        String abi2 = deviceJson.getString("ro.product.cpu.abi2");
        findField(Build.class, "CPU_ABI2").set(null,abi2);
        changeProperties("ro.product.cpu.abi2",abi2);

        String manufacturer = deviceJson.getString("ro.product.manufacturer");
        findField(Build.class, "MANUFACTURER").set(null,manufacturer);
        changeProperties("ro.product.manufacturer",manufacturer);

        String platform = deviceJson.getString("ro.board.platform");
        changeProperties("ro.board.platform",platform);

        String product = deviceJson.getString("ro.build.product");
        findField(Build.class, "PRODUCT").set(null, product);
        changeProperties("ro.build.product",product);

        String description = deviceJson.getString("ro.build.description");
        changeProperties("ro.build.description",description);

        String fingerprint = deviceJson.getString("ro.build.fingerprint");
        findField(Build.class, "FINGERPRINT").set(null,fingerprint);
        changeProperties("ro.build.fingerprint",fingerprint);

        String hardware = deviceJson.getString("ro.hardware");
        findField(Build.class, "HARDWARE").set(null,hardware);
        changeProperties("ro.hardware",hardware);

        String bootloader = deviceJson.getString("ro.bootloader");
        findField(Build.class, "BOOTLOADER").set(null, bootloader);
        changeProperties("ro.bootloader",bootloader);
    }

    @SuppressLint("PrivateApi")
    private void changeProperties(final String key, final Object value) throws ClassNotFoundException {
        for (Member mem : Class.forName("android.os.SystemProperties").getDeclaredMethods()) {
            XposedBridge.hookMethod(mem, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    if (param.args.length > 0 && param.args[0] != null && param.args[0].equals(key)) {
                        param.setResult(value);
                    }
                }
            });
        }

        findAndHookMethod( "android.os.SystemProperties" ,getClassLoader(),"native_get" , String.class,String.class,new XC_ResultHook(null));
    }

    private static class XC_ResultHook extends XC_MethodHook {

        private final Object resultObject;

       public XC_ResultHook(Object resultObject) {
            this.resultObject = resultObject;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) {

           if (resultObject == null){
               return;
           }

            for (Object object : param.args){
                XposedBridge.log("VDLogger Hook Param 1: " + object.toString());
            }

            XposedBridge.log("VDLogger Hook Result 2: " + param.getResult());

            param.setResult(resultObject);

            XposedBridge.log("VDLogger Hook Result 3: " + param.getResult());
        }
    }
}
