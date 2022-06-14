 package pdt.autoreg.devicefaker.xposed;

 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.annotation.SuppressLint;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Color;
 import android.net.wifi.WifiInfo;
 import android.os.Build;
 import android.os.StrictMode;
 import android.provider.Settings;
 import android.telephony.CellInfo;
 import android.telephony.SubscriptionInfo;
 import android.telephony.TelephonyManager;
 import android.view.autofill.AutofillManager;
 import android.widget.TextView;

 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;

 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;

 import de.robv.android.xposed.IXposedHookLoadPackage;
 import de.robv.android.xposed.XC_MethodHook;
 import de.robv.android.xposed.XposedBridge;
 import de.robv.android.xposed.callbacks.XC_LoadPackage;
 import pdt.autoreg.devicefaker.Constants;

 import static de.robv.android.xposed.XposedBridge.hookAllMethods;
 import static de.robv.android.xposed.XposedBridge.hookMethod;
 import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
 import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
 import static de.robv.android.xposed.XposedHelpers.findField;
 import static pdt.autoreg.devicefaker.helper.FileHelper.readFile;

 public class DeviceModule implements IXposedHookLoadPackage {

     private static ClassLoader classLoader;
     private static JSONObject deviceObject;
     private static JSONObject buildPropObject;
     private static JSONObject otherObject;
     private static JSONObject telephonyObject;

     private ClassLoader getClassLoader(){
         return classLoader;
     }

     @Override
     public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
         classLoader = lpparam.classLoader;
         if (lpparam.packageName.equals(Constants.REG_PACKAGE)) {
             XposedBridge.log("Start changer: " + lpparam.packageName);

             StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
             StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                     .permitDiskReads()
                     .permitDiskWrites()
                     .build());

             /*
             TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
             if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
                 ArrayList<String> _lst = new ArrayList<>();
                 Log.d(TAG, "phoneMgr: " + phoneMgr);
                 _lst.add(String.valueOf(phoneMgr.getCallState()));
                 _lst.add("IMEI NUMBER :-" + phoneMgr.getImei());
                 _lst.add("MOBILE NUMBER :-" + phoneMgr.getLine1Number());
                 _lst.add("SERIAL NUMBER :-" + phoneMgr.getSimSerialNumber());
                 _lst.add("SIM OPERATOR NAME :-" + phoneMgr.getSimOperatorName());
                 _lst.add("MEI NUMBER :-" + phoneMgr.getMeid());
                 _lst.add("SIM STATE :-" + String.valueOf(phoneMgr.getSimState()));
                 _lst.add("COUNTRY ISO :-" + phoneMgr.getSimCountryIso());
                 Log.d(TAG, "_lst: " + _lst);
             } else {
                 Log.e(TAG, "Permission required");
             }
              */

             try {
                 importDevice();

                 generateRandomInfo();

                 generateSimInfo();

                 new HideChecking(getClassLoader());

                 updateBuildProp();

 //                updateProperty();

                 updateUserAgent();

                 updateWifi();

                 updateBluetooth();

                 updateIMEI();

                 updateAndroidId();

                 updateSerial();

                 updateTelephony();

                 updateBaseBand();

                 disableAutofill();

                 hookGms();

                 XposedBridge.log("Changer Success: " + lpparam.packageName);

             } catch (Exception e) {
                 XposedBridge.log("Module Error: " + e.toString());
             } finally {
                 StrictMode.setThreadPolicy(old);
             }
         }

         if (lpparam.packageName.startsWith("com.android.systemui")) {
             updateClock();
         }
         try {
             hookAllMethods(SubscriptionInfo.class, "getIccId", new XC_MethodHook() {
                 @Override
                 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                     super.afterHookedMethod(param);
                     param.setResult("2149759783974724");
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }

         //com.android.internal.telephony.uicc.UiccCard
//         try {
//             hookAllMethods(Class.forName("com.android.internal.telephony.uicc.UiccCard"), "getIccId", new XC_MethodHook() {
//                 @Override
//                 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                     super.afterHookedMethod(param);
//                     param.setResult("2149759783974724");
//                 }
//             });
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

     }

     private void updateClock() {
         XposedBridge.log("updateClock");
         findAndHookMethod("com.android.systemui.statusbar.policy.Clock",
                 getClassLoader(),
                 "updateClock",
                 new XC_MethodHook() {
                     @SuppressLint("SetTextI18n")
                     @Override
                     protected void afterHookedMethod(MethodHookParam param) {
                         TextView tv = (TextView) param.thisObject;
                         tv.setText("DeviceFaker Enable");
                         tv.setTextColor(Color.GREEN);
                     }
                 });
     }

     private void disableAutofill() {
         XposedBridge.log("disableAutofill");
         XposedBridge.hookAllMethods(AutofillManager.class, "notifyViewEntered", new XC_MethodHook() {
             @Override
             protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                 super.beforeHookedMethod(param);
                 Field AutoFillManagerServiceField = param.thisObject.getClass().getDeclaredField("mService");
                 AutoFillManagerServiceField.setAccessible(true);
                 AutoFillManagerServiceField.set(param.thisObject, null);
             }
         });
     }

     private void hookGms() {
         //com.google.android.gms.ads.identifier.AdvertisingIdClient
         XposedBridge.log("hookGms");
         try {
             XposedBridge.hookAllMethods(Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient"),"getAdvertisingIdInfo" ,new XC_MethodHook() {
                 @Override
                 protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                     super.beforeHookedMethod(param);
                     XposedBridge.log("AdvertisingIdClient");
                 }
             });
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }

         try {
             XposedBridge.hookAllConstructors(Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient"), new XC_MethodHook() {
                 @Override
                 protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                     super.beforeHookedMethod(param);
                     XposedBridge.log("AdvertisingIdClient");
                 }
             });
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }

         XposedBridge.hookAllMethods(AccountManager.class,"getAccounts" ,new XC_MethodHook() {
             @Override
             protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                 super.afterHookedMethod(param);
                 XposedBridge.log("getAccounts");
                 if(param.getResult() != null) {
                     if(((Account[]) param.getResult()).length > 0) {
                         param.setResult(new Account[]{});
                     }
                 }
             }
         });

         XposedBridge.hookAllMethods(AccountManager.class,"getAccountsAsUser" ,new XC_MethodHook() {
             @Override
             protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                 super.afterHookedMethod(param);
                 XposedBridge.log("getAccountsAsUser");
                 if(param.getResult() != null) {
                     if(((Account[]) param.getResult()).length > 0) {
                         param.setResult(new Account[]{});
                     }
                 }
             }
         });

         try {
             XposedBridge.hookAllMethods(Class.forName("com.google.android.gms.safetynet.SafetyNet"),"getClient" ,new XC_MethodHook() {
                 @Override
                 protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                     super.beforeHookedMethod(param);
                     XposedBridge.log("SafetyNet.getClient");
                 }
             });
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }

     private void importDevice() {
         String jsonContent = readFile("/data/local/tmp/devices.json");
         XposedBridge.log("jsonContent: " +  jsonContent);
         try {
             JSONArray devices = new JSONArray(jsonContent);
             if (devices.length() > 0) {
                 deviceObject = devices.getJSONObject(new Random().nextInt(devices.length()));
                 otherObject = deviceObject.getJSONObject("other");
                 buildPropObject = deviceObject.getJSONObject("buildProp");
                 XposedBridge.log("importDevice deviceObject: " +  deviceObject);

                 for (Iterator<String> iterator = buildPropObject.keys(); iterator.hasNext(); ) {
                     try {
                         String key = iterator.next();
                         JSONObject tmpDeviceObj = devices.getJSONObject(new Random().nextInt(devices.length()));
                         JSONObject tmpBuildPropObj = tmpDeviceObj.getJSONObject("buildProp");
                         buildPropObject.put(key, tmpBuildPropObj.getString(key));
                     } catch (Exception e) {
                         XposedBridge.log("Error: " + e);
                     }
                 }

                 for (Iterator<String> iterator = otherObject.keys(); iterator.hasNext(); ) {
                     try {
                         String key = iterator.next();
                         JSONObject tmpDeviceObj = devices.getJSONObject(new Random().nextInt(devices.length()));
                         JSONObject tmpOtherObj = tmpDeviceObj.getJSONObject("other");
                         otherObject.put(key, tmpOtherObj.getString(key));
                     } catch (Exception e) {
                         XposedBridge.log("Error: " + e);
                     }
                 }

                 deviceObject.put("other",otherObject);
                 deviceObject.put("buildProp",buildPropObject);
                 XposedBridge.log("deviceObject: " + deviceObject);
             }
         } catch (Exception e) {}
     }

     private void generateRandomInfo() {
         try {
             Random random = new Random();

             // Generate Imei
             String imei = String.valueOf(random.nextInt(8) + 1);
             int length = random.nextInt(3) + 14;
             for (int i = 0; i < length; i++) {
                 imei += String.valueOf(random.nextInt(9));
             }
             XposedBridge.log("random imei: " + imei);
             otherObject.put("imei", imei);

             // Generate serial
             String serial = new String();
             for (int i = 0; i < 18; i++){
                 serial += Integer.toHexString(random.nextInt(16));
             }
             otherObject.put("serial", serial);

             // Generate android id
             String androidId = new String();
             for (int i = 0; i < 15; i++){
                 androidId += Integer.toHexString(new Random().nextInt(16));
             }
             otherObject.put("androidId",androidId);

             // Generate macBluetooth
             String btMacAdd = Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16));
             otherObject.put("macBluetooth",btMacAdd);

             // Generate macWifi
             String macAdd = Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16));
             otherObject.put("macWifi",macAdd);

             String ssid = String.valueOf((char) (random.nextInt(26) + 'A')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a')) +
                     String.valueOf((char) (random.nextInt(26) + 'a'));
             otherObject.put("ssidWifi",ssid);

             String bssid = Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16)) + ":" +
                     Integer.toHexString(random.nextInt(16)) + Integer.toHexString(random.nextInt(16));
             otherObject.put("bssidWifi",bssid);
             XposedBridge.log("generateRandomInfo otherObject: " +  otherObject);

         } catch (Exception e) {}
     }

     private void generateSimInfo() {
         try {
             Random random = new Random();
             telephonyObject = getRandomSimInfoByCarrier(Constants.CARRIER_LIST[random.nextInt(Constants.CARRIER_LIST.length)]);

//             telephonyObject.put("getAllCellInfo", new ArrayList<CellInfo>());
//             telephonyObject.put("getCallState", 0);
//             telephonyObject.put("getCardIdForDefaultEuicc", -1);
             telephonyObject.put("getDataActivity", 0);
//             telephonyObject.put("getDataNetworkType", 0);
             telephonyObject.put("getDeviceId", otherObject.getString("imei"));
             telephonyObject.put("getImei", otherObject.getString("imei"));
//             telephonyObject.put("getMmsUserAgent", "");
//             telephonyObject.put("getNetworkCountryIso", "");

             XposedBridge.log("generateSimInfo telephonyObject: " +telephonyObject);
         } catch (Exception e) {
             XposedBridge.log("generateSimInfo error: " + e);
         }
     }

     private JSONObject getRandomSimInfoByCarrier(String carrier) {
         JSONObject result = null;
         try {
             result = new JSONObject();
             String[] phonePrefix = null;
             String phoneNumber = null;
             String id = null;
             Random random = new Random();

//             result.put("getCarrierIdFromSimMccMnc", 0);
//             phoneNumber = readFile(Constants.AUTOREG_DATA_FOLDER + "phone_number.txt");
//             result.put("getLine1Number", phoneNumber);
//             result.put("getNetworkOperator", "");
//             result.put("getNetworkOperatorName", "");
//             result.put("getSimCarrierId", 0);
//             result.put("getSimCarrierIdName", "");
//             result.put("getSimOperator", "");
//             result.put("getSimOperatorName", "");
//             result.put("getSimSpecificCarrierId", 0);
//             result.put("getSimSpecificCarrierIdName", ""); //getSimSerialNumber: 89840480003243679236
//             result.put("getSimSerialNumber", getNumberRandom(15));
//             result.put("getSubscriberId", getNumberRandom(10));

         } catch (Exception e) {
             XposedBridge.log("getRandomSimInfoByCarrier error: " + e);
         }
         return result;
     }

     private String getNumberRandom(int length) {
         String result = "";
         Random random = new Random();
         for(int i = 0; i < length; i++) {
             result += random.nextInt(10);
         }
         return result;
     }

     private void updateBaseBand() throws JSONException {
         XposedBridge.log("updateBaseBand");
         findAndHookMethod(Build.class, "getRadioVersion",new XC_ResultHook(otherObject.getString("BaseBand")));
     }

     private void updateUserAgent() throws Exception {
         XposedBridge.log("updateUserAgent");
 //        changeProperty("http.agent",otherObject.getString("UserAgent"));

         findAndHookMethod(
                 "android.webkit.WebSettings",
                 getClassLoader(),
                 "getDefaultUserAgent",
                 Context.class,
                 new XC_ResultHook(otherObject.getString("UserAgent"))
         );
     }

     private void updateProperty() throws Exception {
         XposedBridge.log("updateProperty");
         String[] keys = {"os.version","os.arch","os.name"};

         for (String key : keys){
             changeProperty(key,otherObject.getString(key));
         }
     }

     @SuppressLint("PrivateApi")
     private void changeProperty(final String key, final String value) throws ClassNotFoundException {
         Class<?> cls = Class.forName("java.lang.System");
         for (Member mem : cls.getDeclaredMethods()) {
             XposedBridge.hookMethod(mem, new XC_MethodHook() {
                 @Override
                 protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                     super.beforeHookedMethod(param);

                     if (param.args.length > 0 && param.args[0] != null && param.args[0].equals(key)) {
                         param.setResult(value);
                     }
                 }
             });
         }
     }

     private void updateTelephony() throws JSONException {
         //hookAllMethods(TelephonyManager.class, "getLine1Number", new XC_ResultHook(otherObject.getString("phoneNumber")));

         for (Iterator<String> keyItr = telephonyObject.keys() ; keyItr.hasNext() ;) {
             String key = keyItr.next();
             XposedBridge.log("updateTelephony key: " + key  + " -- value: " + telephonyObject.get(key));
             hookAllMethods(TelephonyManager.class, key, new XC_ResultHook(telephonyObject.get(key)));
         }
//         hookAllMethods(SubscriptionInfo.class, "getIccId", new XC_ResultHook(telephonyObject.get("getSimSerialNumber")));
     }

     private void updateSerial() throws Exception {
         XposedBridge.log("updateSerial");
         String[] serialProperties = {
                 "no.such.thing",
                 "ro.serialno",
                 "ro.boot.serialno",
                 "ril.serialnumber",
                 "gsm.sn1",
                 "sys.serialnumber"
         };

         for (String properties : serialProperties){
             changeProperties(properties,otherObject.getString("serial"));
         }
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             findAndHookMethod(Build.class, "getSerial", new XC_ResultHook(otherObject.getString("serial")));
         }
         findField(Build.class, "SERIAL").set(null, otherObject.getString("serial"));
     }

     private void updateAndroidId() throws JSONException {
         XposedBridge.log("updateAndroidId");
         findAndHookMethod("android.provider.Settings.Secure", getClassLoader(), "getString", ContentResolver.class, String.class, new XC_MethodHook() {
             @Override
             protected void afterHookedMethod(MethodHookParam param) throws JSONException {
                 if (param.args[1].equals(Settings.Secure.ANDROID_ID)) {
                     if (param.args[1].equals(Settings.Secure.ANDROID_ID)) {
                         param.setResult(otherObject.getString("androidId"));
                     }
                 }
             }
         });
     }

     private void updateIMEI() throws JSONException {
         XposedBridge.log("updateIMEI");
         findAndHookMethod("com.android.internal.telephony.imsphone.ImsPhone", getClassLoader(), "getDeviceId", new XC_ResultHook(otherObject.getString("imei")));
         findAndHookMethod("com.android.internal.telephony.sip.SipPhone",getClassLoader(), "getDeviceId", new XC_ResultHook(otherObject.getString("imei")));
         findAndHookMethod("android.telephony.TelephonyManager",getClassLoader(), "getDeviceId", new XC_ResultHook(otherObject.getString("imei")));
         findAndHookMethod(TelephonyManager.class, "getImei", new XC_ResultHook(otherObject.getString("imei")));
     }

     private void updateBluetooth() throws JSONException {
         XposedBridge.log("updateBluetooth");
         findAndHookMethod(BluetoothAdapter.class, "getAddress", new XC_ResultHook(otherObject.getString("macBluetooth")));
         findAndHookMethod(BluetoothDevice.class, "getAddress", new XC_ResultHook(otherObject.getString("macBluetooth")));
     }

     private void updateWifi() throws JSONException {
         XposedBridge.log("updateWifi");
         findAndHookMethod(WifiInfo.class,"getMacAddress",new XC_ResultHook(otherObject.getString("macWifi")));
         findAndHookMethod(WifiInfo.class,"getSSID",new XC_ResultHook(otherObject.getString("ssidWifi")));
         findAndHookMethod(WifiInfo.class,"getBSSID",new XC_ResultHook(otherObject.getString("bssidWifi")));
     }

     private void updateBuildProp() throws Exception{
         XposedBridge.log("updateBuildProp");
         for (Iterator<String> it = buildPropObject.keys(); it.hasNext(); ) {
             String key = it.next();

             changeProperties(key,buildPropObject.getString(key));
         }

         findField(Build.class, "BRAND").set(null, buildPropObject.getString("ro.product.brand"));
         findField(Build.class, "DEVICE").set(null, buildPropObject.getString("ro.product.device"));
         findField(Build.class, "DISPLAY").set(null, buildPropObject.getString("ro.build.display.id"));
         findField(Build.class, "FINGERPRINT").set(null, buildPropObject.getString("ro.build.fingerprint"));
         findField(Build.class, "HARDWARE").set(null, buildPropObject.getString("ro.hardware"));
         findField(Build.class, "ID").set(null, buildPropObject.getString("ro.build.id"));
         findField(Build.class, "MANUFACTURER").set(null, buildPropObject.getString("ro.product.manufacturer"));
         findField(Build.class, "MODEL").set(null, buildPropObject.getString("ro.product.model"));
         findField(Build.class, "PRODUCT").set(null, buildPropObject.getString("ro.build.product"));
         findField(Build.class, "BOOTLOADER").set(null, buildPropObject.getString("ro.bootloader"));
         findField(Build.class, "HOST").set(null, buildPropObject.getString("ro.build.host"));
         findField(Build.class, "USER").set(null, buildPropObject.getString("ro.build.user"));
         findField(Build.class, "TIME").set(null, (Long.parseLong(buildPropObject.getString("ro.build.date.utc")) / 1000));
         findField(Build.class, "TAGS").set(null, buildPropObject.getString("ro.build.tags"));
         findField(Build.class, "RADIO").set(null, otherObject.getString("BaseBand"));
         findField(Build.VERSION.class, "INCREMENTAL").set(null, buildPropObject.getString("ro.build.version.incremental"));
         findField(Build.VERSION.class, "CODENAME").set(null, buildPropObject.getString("ro.build.version.codename"));
         findField(Build.VERSION.class, "BASE_OS").set(null, buildPropObject.getString("ro.build.version.base_os"));
         findField(Build.VERSION.class, "SECURITY_PATCH").set(null, buildPropObject.getString("ro.build.version.security_patch"));
     }

     @SuppressLint("PrivateApi")
     private void changeProperties(final String properties, final String value) throws ClassNotFoundException {
         Class<?> cls = Class.forName("android.os.SystemProperties");
         for (Member mem : cls.getDeclaredMethods()) {
             XposedBridge.hookMethod(mem, new XC_MethodHook() {
                 @Override
                 protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                     super.beforeHookedMethod(param);

                     if (param.args.length > 0 && param.args[0] != null && param.args[0].equals(properties)) {
                         param.setResult(value);
                     }
                 }
             });
         }
     }

     private static class XC_ResultHook extends XC_MethodHook {

         private Object resultObject;

        public XC_ResultHook(Object resultObject) {
             this.resultObject = resultObject;
         }

         @Override
         protected void afterHookedMethod(MethodHookParam param) {
             if (resultObject != null)
                 param.setResult(resultObject);
         }
     }
 }
