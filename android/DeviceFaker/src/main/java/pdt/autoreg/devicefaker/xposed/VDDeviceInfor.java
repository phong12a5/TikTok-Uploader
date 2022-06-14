package pdt.autoreg.devicefaker.xposed;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class VDDeviceInfor {

    private static final String deviceInforPath = "/system/vendor/etc/DeviceInfor.txt";

    public static String fakeRandom() {

        String deviceEncryt = null;

        try{
            JSONObject deviceJson = new JSONObject();
            deviceJson.put("ro.build.id", VDRandom.randomString(30));
            deviceJson.put("ro.build.display.id",VDRandom.randomString(30));
            deviceJson.put("ro.build.version.incremental",VDRandom.randomString(30));
            int version = VDRandom.randomNumber(0,10);
            deviceJson.put("ro.build.version.sdk",getVersionSDK(version));
            deviceJson.put("ro.build.version.codename",VDRandom.randomString(30));
            deviceJson.put("ro.build.version.release",getVersionRelease(version));
            deviceJson.put("ro.build.version.security_patch",VDRandom.randomString(30));
            deviceJson.put("ro.build.version.base_os",VDRandom.randomString(30));
            String date = getDate();
            deviceJson.put("ro.build.date",date);
            deviceJson.put("ro.build.date.utc",parseDateUTC(date));
            deviceJson.put("ro.build.type",VDRandom.randomString(30));
            deviceJson.put("ro.build.user",VDRandom.randomString(30));
            deviceJson.put("ro.build.host",VDRandom.randomString(30));
            deviceJson.put("ro.build.tags","release-keys");
            deviceJson.put("ro.build.flavor",VDRandom.randomString(30));
            deviceJson.put("ro.product.model",VDRandom.randomString(30));
            deviceJson.put("ro.product.brand",VDRandom.randomString(30));
            deviceJson.put("ro.product.name",VDRandom.randomString(30));
            deviceJson.put("ro.product.device",VDRandom.randomString(30));
            deviceJson.put("ro.product.board",VDRandom.randomString(30));
            deviceJson.put("ro.product.cpu.abi","arm64-v8a");
            deviceJson.put("ro.product.cpu.abi2","arm64-v8a");
            deviceJson.put("ro.product.manufacturer",VDRandom.randomString(30));
            deviceJson.put("ro.board.platform",VDRandom.randomString(30));
            deviceJson.put("ro.build.product",VDRandom.randomString(30));
            deviceJson.put("ro.build.description",VDRandom.randomString(30));
            String fingerprint = deviceJson.getString("ro.product.brand") + "/" +
                    deviceJson.getString("ro.product.name") + "/" +
                    deviceJson.getString("ro.product.device") + ":" +
                    deviceJson.getString("ro.build.version.release") + "/" +
                    deviceJson.getString("ro.build.id") + "/" +
                    deviceJson.getString("ro.build.version.incremental") + ":" +
                    deviceJson.getString("ro.build.type") + "/" +
                    deviceJson.getString("ro.build.tags");
            deviceJson.put("ro.build.fingerprint",fingerprint);
            deviceJson.put("ro.hardware",VDRandom.randomString(30));
            deviceJson.put("ro.bootloader",VDRandom.randomString(30));

            deviceJson.put("BaseBand",VDRandom.randomString(30));
            deviceJson.put("MacWifi",getMacAddress());
            deviceJson.put("SSIDWifi",VDRandom.randomString(30));
            deviceJson.put("BSSIDWifi",VDRandom.randomString(30));
            deviceJson.put("MacBluetooth",getMacAddress());
            deviceJson.put("Imei",VDRandom.randomNumber(15));
            deviceJson.put("AndroidId",VDRandom.randomString(16));
            deviceJson.put("Serial",VDRandom.randomString(30));
            deviceJson.put("PhoneNumber",getPhoneNumber());

            deviceJson.put("SubscriberId",VDRandom.randomString(30));
            deviceJson.put("SimSerialNumber",VDRandom.randomString(30));
            deviceJson.put("SimCountryIso",VDRandom.randomString(30));
            deviceJson.put("SimOperator",VDRandom.randomString(30));
            deviceJson.put("SimOperatorName",VDRandom.randomString(30));
            deviceJson.put("NetworkCountryIso",VDRandom.randomString(30));
            deviceJson.put("NetworkOperator",VDRandom.randomString(30));
            deviceJson.put("NetworkOperatorName",VDRandom.randomString(30));
            deviceJson.put("DeviceSoftwareVersion",VDRandom.randomString(30));
            deviceJson.put("MmsUAProfUrl",VDRandom.randomString(30));
            deviceJson.put("MmsUserAgent",VDRandom.randomString(30));
            deviceJson.put("DataNetworkType",VDRandom.randomString(30));
            deviceJson.put("DataState",VDRandom.randomString(30));

            String userAgentVersion = VDRandom.randomNumber(500,550) + "." + VDRandom.randomNumber(1,50);
            String userAgent = "Mozilla/5.0 (Linux; Android " + deviceJson.getString("ro.build.version.release") + "; " +
                    deviceJson.getString("ro.product.model") + " Build/" + deviceJson.getString("ro.build.id") +
                    "; wv) AppleWebKit/" + userAgentVersion +
                    " (KHTML, like Gecko) Version/4.0 Chrome/85.0." + VDRandom.randomNumber(4000,4100) + "." + VDRandom.randomNumber(1,150) +
                    " Mobile Safari/" + userAgentVersion;
            deviceJson.put("UserAgent",userAgent);


            String dalvikUserAgent = "Dalvik/" + VDRandom.randomNumber(1) + "." + VDRandom.randomNumber(1) + "." + VDRandom.randomNumber(1) +
                    " (Linux; U; Android " + VDRandom.randomNumber(6,10) + "; " + deviceJson.getString("ro.product.model") +
                    " Build/" + deviceJson.getString("ro.build.id") + ")";

            deviceJson.put("DalvikUserAgent",dalvikUserAgent);

            deviceEncryt = deviceJson.toString();

        }catch (Exception ignored){}

        return deviceEncryt;
    }

    private static String getMacAddress() {
        return (VDRandom.randomString(2) + ":" +
                VDRandom.randomString(2) + ":" +
                VDRandom.randomString(2) + ":" +
                VDRandom.randomString(2) + ":" +
                VDRandom.randomString(2) + ":" +
                VDRandom.randomString(2)).toUpperCase();
    }

    private static String getDate(){
        int day     = VDRandom.randomNumber(1,28);
        int month   = VDRandom.randomNumber(1,12);
        int year    = VDRandom.randomNumber(2015,2020);

        return day + "/" + month + "/" + year;
    }

    private static long parseDateUTC(String date){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            return dateFormat.parse(date).getTime();
        } catch (ParseException ignored) {
            return System.currentTimeMillis() - 1000000;
        }
    }

    private static String getPhoneNumber(){
        String[] first = {"032","033","034","035","036","037","038","039","070","079","077","076","078","083","084","085","081","082"};
        return first[VDRandom.random.nextInt(first.length)] + VDRandom.randomNumber(7);
    }

    private static int getVersionSDK(int position){
        int[] version = {19,20,21,22,23,24,25,26,27,28,29};

        return version[position];
    }

    private static String getVersionRelease(int position){
        String[] version = {"4.4.2","4.4.4","5.0","5.1","6.0.1","7.0","7.1","8.0","8.1","9.0","10.0"};

        return version[position];
    }
}
