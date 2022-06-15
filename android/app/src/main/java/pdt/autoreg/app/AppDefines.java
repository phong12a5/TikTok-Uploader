package pdt.autoreg.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentUris.parseId;

public class AppDefines {
    public static final String PDT_PREFS_NAME = "PDT_PREFS_NAME";
    public static final boolean USE_TEMPMAIL = false;
    public static final boolean USE_SSHTUNEL = true;
    public static final boolean USE_PROXY = true;
    public static final boolean US_CLONE = true;
    public static final boolean SET_2FA = true;
    public static final boolean REG_NOVERY = false;
    public static final boolean REG_BY_PHONE_NUMBER = true;
    public static final boolean USE_GMAIL = false;
    public static final int MAX_THREAD_CHECK_SSH = 20;
    public static final int SSH_LIST_BUFFER_SIZE = 10;

    public static final int MOBILE_NETWORK = 0;
    public static final int PROXY_NETWORK = 1;
    public static final int SSHTUNNEL_NETWORK = 2;


    public static final String FACEBOOK_APPNAME = "Facebook";
    public static final String INSTAGRAM_APPNAME = "Instagram";
    public static final String TIKTOK_APPNAME = "Tiktok";

    /* Constant value */
    public static final String AUTOREG_FOLDER = Environment.getExternalStorageDirectory() + "/pdt.autoreg.app/";
    public static final String AUTOREG_DATA_FOLDER = AUTOREG_FOLDER + "data/";
    public static final String PICTURES_FOLDER = Environment.getExternalStorageDirectory() + "/Pictures/";

    /************************************* PAGE_ID ************************************/

    /******************** TIKTOK APP ********************/

    public static final String SCREEN_TIKTOK_HOME_FOR_YOU = "SCREEN_TIKTOK_HOME_FOR_YOU";
    public static final String SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK = "SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK";
    public static final String SCREEN_TIKTOK_SIGN_UP_BIRTHDAY = "SCREEN_TIKTOK_SIGN_UP_BIRTHDAY";
    public static final String SCREEN_TIKTOK_SIGN_UP_PHONE = "SCREEN_TIKTOK_SIGN_UP_PHONE";
    public static final String SCREEN_TIKTOK_SIGN_UP_EMAIL = "SCREEN_TIKTOK_SIGN_UP_EMAIL";
    public static final String SCREEN_TIKTOK_VERIFY_CAPTCHA = "SCREEN_TIKTOK_VERIFY_CAPTCHA";
    public static final String SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD = "SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD";
    /******************** END TIKTOK APP ********************/

    static public  String[] phonePrefixList = {"+4471", "+4473", "+4474", "+4475", "+4477", "+4478", "+4479"};

    static public final String COUNTRY_KOREA = "KR";
    static public final String COUNTRY_VIETNAM = "VN";
    static public final String COUNTRY_SINGAPORE = "SG";
    static public final String COUNTRY_MALAYSIA = "MY";
    static public final String COUNTRY_TAIWAN = "TW";
    static public final String COUNTRY_HONGKONG = "HK";
    static public final String COUNTRY_LAOS = "LA";
    static public final String COUNTRY_MYANMA = "MM";
    static public final String COUNTRY_THAILAND = "TH";
    static public final String COUNTRY_PHILIPINS = "PH";
    static public final String COUNTRY_ISREAL = "IL";
    static public final String COUNTRY_AUSTRALIA = "AU";
    static public final String COUNTRY_UNITED_KINGDOM = "GB";
    static public final String COUNTRY_RUSSIA = "RU";
    static public final String COUNTRY_POLAND = "PL";
    static public final String COUNTRY_UNITED_STATES = "US";
    static public final String COUNTRY_SAUDI_ARABIA = "SA";
    static public final String COUNTRY_BRAZIL = "BR";
    static public final String COUNTRY_ARGENTINA = "AR";
    static public final String COUNTRY_COLOMBIA = "CO";
    static public final String COUNTRY_GERMANY = "DE";
    static public final String COUNTRY_EGYPT = "EG";
    static public final String COUNTRY_SPAIN = "ES";
    static public final String COUNTRY_GHANA = "GH";
    static public final String COUNTRY_INDONESIA = "ID";
    static public final String COUNTRY_INDIA = "IN";
    static public final String COUNTRY_IRAN = "IR";
    static public final String COUNTRY_MALAWI = "MW";
    static public final String COUNTRY_MEXICO = "MX";
    static public final String COUNTRY_NETHERLANDS = "NL";
    static public final String COUNTRY_NORWAY = "NO";
    static public final String COUNTRY_NEPAL = "NP";
    static public final String COUNTRY_OMAN = "OM";
    static public final String COUNTRY_PAKISTAN = "PK";
    static public final String COUNTRY_TURKEY = "TR";
    static public final String COUNTRY_UKRAINE = "UA";

    static public final String COUNTRY_ANGOLA = "AO";
    static public final String COUNTRY_AFGHANISTAN = "AF";
    static public final String COUNTRY_BANGLADESH = "BD";
    static public final String COUNTRY_BOLIVIA = "BO";
    static public final String COUNTRY_CANADA = "CA";
    static public final String COUNTRY_SWITZERLAND = "CH";
    static public final String COUNTRY_CHILE = "CL";
    static public final String COUNTRY_CAMEROON = "CM";
    static public final String COUNTRY_DENMARK = "DK";
    static public final String COUNTRY_DOMINICAN = "DO";
    static public final String COUNTRY_ALGERIA = "DZ";
    static public final String COUNTRY_FINLAND = "FI";
    static public final String COUNTRY_FRANCE = "FR";
    static public final String COUNTRY_IRAQ = "IQ";
    static public final String COUNTRY_JAPAN = "JP";
    static public final String COUNTRY_KENYA = "KE";
    static public final String COUNTRY_KAZAKHSTAN = "KZ";
    static public final String COUNTRY_LEBANON = "LB";
    static public final String COUNTRY_MOROCCO = "MA";
    static public final String COUNTRY_MOLDOVA = "MD";
    static public final String COUNTRY_MAURITIUS = "MU";
    static public final String COUNTRY_MALDIVES = "MV";
    static public final String COUNTRY_PALESTINIAN = "PS";
    static public final String COUNTRY_ROMANIA = "RO";
    static public final String COUNTRY_SUDAN = "SD";
    static public final String COUNTRY_SWEDEN = "SE";
    static public final String COUNTRY_SWAZILAND = "SZ";
    static public final String COUNTRY_UZBEKISTAN = "UZ";
    static public final String COUNTRY_SAINT_VINCENT = "VC";
    static public final String COUNTRY_SOUTH_AFRICA = "ZA";
    static public final String COUNTRY_ZIMBABWE = "ZW";
    static public final String COUNTRY_SLOVENIA = "SI";
    static public final String COUNTRY_SLOVAKIA = "SK";
    static public final String COUNTRY_PORTUGAL = "PT";
    static public final String COUNTRY_NIGERIA = "NG";
    static public final String COUNTRY_NEW_ZEALAND = "NZ";
    static public final String COUNTRY_ITALY = "IT";
    static public final String COUNTRY_ISRAEL = "IL";
    static public final String COUNTRY_IRELAND = "IE";
    static public final String COUNTRY_HUNGARY = "HU";
    static public final String COUNTRY_GEORGIA = "GE";
    static public final String COUNTRY_BULGARIA = "BG";
    static public final String COUNTRY_PERU = "PE";
    static public final String COUNTRY_VENEZUELA = "VE";

    static public final String CODE_KOREA = "+82";
    static public final String CODE_VIETNAM = "+84";
    static public final String CODE_SINGAPORE = "+65";
    static public final String CODE_MALAYSIA = "+60";
    static public final String CODE_TAIWAN = "+886";
    static public final String CODE_HONGKONG = "+852";
    static public final String CODE_LAOS = "+856";
    static public final String CODE_MYANMA = "+95";
    static public final String CODE_THAILAND = "+66";
    static public final String CODE_PHILIPINS = "+63";
    static public final String CODE_ISREAL = "+972";
    static public final String CODE_AUSTRALIA = "+61";
    static public final String CODE_UNITED_KINGDOM = "+44";
    static public final String CODE_UNITED_STATES = "+1";
    static public final String CODE_RUSSIA = "+7";
    static public final String CODE_POLAND = "+48";
    static public final String CODE_SAUDI_ARABIA = "+966";
    static public final String CODE_BRAZIL = "+55";
    static public final String CODE_ARGENTINA = "+54";
    static public final String CODE_COLOMBIA = "+57";
    static public final String CODE_GERMANY = "+49";
    static public final String CODE_EGYPT = "+20";
    static public final String CODE_SPAIN = "+34";
    static public final String CODE_GHANA = "+233";
    static public final String CODE_INDONESIA = "+62";
    static public final String CODE_INDIA = "+91";
    static public final String CODE_IRAN = "+98";
    static public final String CODE_MALAWI = "+265";
    static public final String CODE_MEXICO = "+52";
    static public final String CODE_NETHERLANDS = "+31";
    static public final String CODE_NORWAY = "+47";
    static public final String CODE_NEPAL = "+977";
    static public final String CODE_OMAN = "+968";
    static public final String CODE_PAKISTAN = "+92";
    static public final String CODE_TURKEY = "+90";
    static public final String CODE_UKRAINE = "+380";

    static public final String CODE_ANGOLA = "+244";
    static public final String CODE_AFGHANISTAN = "+93";
    static public final String CODE_BANGLADESH = "+880";
    static public final String CODE_BOLIVIA = "+591";
    static public final String CODE_CANADA = "+1";
    static public final String CODE_SWITZERLAND = "+41";
    static public final String CODE_CHILE = "+56";
    static public final String CODE_CAMEROON = "+237";
    static public final String CODE_DENMARK = "+45";
    static public final String CODE_DOMINICAN = "+1";
    static public final String CODE_ALGERIA = "+213";
    static public final String CODE_FINLAND = "+358";
    static public final String CODE_FRANCE = "+33";
    static public final String CODE_IRAQ = "+964";
    static public final String CODE_JAPAN = "+81";
    static public final String CODE_KENYA = "+254";
    static public final String CODE_KAZAKHSTAN = "+7";
    static public final String CODE_LEBANON = "+961";
    static public final String CODE_MOROCCO = "+212";
    static public final String CODE_MOLDOVA = "+373";
    static public final String CODE_MAURITIUS = "+230";
    static public final String CODE_MALDIVES = "+960";
    static public final String CODE_PALESTINIAN = "+970";
    static public final String CODE_ROMANIA = "+40";
    static public final String CODE_SUDAN = "+249";
    static public final String CODE_SWEDEN = "+46";
    static public final String CODE_SWAZILAND = "+268";
    static public final String CODE_UZBEKISTAN = "+998";
    static public final String CODE_SAINT_VINCENT = "+1";
    static public final String CODE_SOUTH_AFRICA = "+27";
    static public final String CODE_ZIMBABWE = "+263";


    static public final String[] COUNTRY_LIST = {
            AppDefines.COUNTRY_VIETNAM,
            AppDefines.COUNTRY_SAUDI_ARABIA,
            AppDefines.COUNTRY_KOREA,
            AppDefines.COUNTRY_SINGAPORE,
            AppDefines.COUNTRY_MALAYSIA,
            AppDefines.COUNTRY_TAIWAN,
            AppDefines.COUNTRY_HONGKONG,
            AppDefines.COUNTRY_LAOS,
            AppDefines.COUNTRY_MYANMA,
            AppDefines.COUNTRY_THAILAND,
            AppDefines.COUNTRY_PHILIPINS,
            AppDefines.COUNTRY_ISREAL,
            AppDefines.COUNTRY_AUSTRALIA,
            AppDefines.COUNTRY_UNITED_KINGDOM,
            AppDefines.COUNTRY_RUSSIA,
            AppDefines.COUNTRY_POLAND,
            AppDefines.COUNTRY_UNITED_STATES,
            AppDefines.COUNTRY_BRAZIL,
            AppDefines.COUNTRY_ARGENTINA,
            AppDefines.COUNTRY_COLOMBIA,
            AppDefines.COUNTRY_GERMANY,
            AppDefines.COUNTRY_EGYPT,
            AppDefines.COUNTRY_SPAIN,
            AppDefines.COUNTRY_GHANA,
            AppDefines.COUNTRY_INDONESIA,
            AppDefines.COUNTRY_INDIA,
            AppDefines.COUNTRY_IRAN,
            AppDefines.COUNTRY_MALAWI,
            AppDefines.COUNTRY_MEXICO,
            AppDefines.COUNTRY_NETHERLANDS,
            AppDefines.COUNTRY_NORWAY,
            AppDefines.COUNTRY_NEPAL,
            AppDefines.COUNTRY_OMAN,
            AppDefines.COUNTRY_PAKISTAN,
            AppDefines.COUNTRY_TURKEY,
            AppDefines.COUNTRY_UKRAINE,

            AppDefines.COUNTRY_ANGOLA,
            AppDefines.COUNTRY_AFGHANISTAN,
            AppDefines.COUNTRY_BANGLADESH,
            AppDefines.COUNTRY_BOLIVIA,
            AppDefines.COUNTRY_CANADA,
            AppDefines.COUNTRY_SWITZERLAND,
            AppDefines.COUNTRY_CHILE,
            AppDefines.COUNTRY_CAMEROON,
            AppDefines.COUNTRY_DENMARK,
            AppDefines.COUNTRY_DOMINICAN,
            AppDefines.COUNTRY_ALGERIA,
            AppDefines.COUNTRY_FINLAND,
            AppDefines.COUNTRY_FRANCE,
            AppDefines.COUNTRY_IRAQ,
            AppDefines.COUNTRY_JAPAN,
            AppDefines.COUNTRY_KENYA,
            AppDefines.COUNTRY_KAZAKHSTAN,
            AppDefines.COUNTRY_LEBANON,
            AppDefines.COUNTRY_MOROCCO,
            AppDefines.COUNTRY_MOLDOVA,
            AppDefines.COUNTRY_MAURITIUS,
            AppDefines.COUNTRY_MALDIVES,
            AppDefines.COUNTRY_PALESTINIAN,
            AppDefines.COUNTRY_ROMANIA,
            AppDefines.COUNTRY_SUDAN,
            AppDefines.COUNTRY_SWEDEN,
            AppDefines.COUNTRY_SWAZILAND,
            AppDefines.COUNTRY_UZBEKISTAN,
            AppDefines.COUNTRY_SAINT_VINCENT,
            AppDefines.COUNTRY_SOUTH_AFRICA,
            AppDefines.COUNTRY_ZIMBABWE
    };

    /******************** INSTAGRAM APP ********************/
    static final String PAGE_INSTAGRAM_LOGIN = "PAGE_INSTAGRAM_LOGIN";
    static final String PAGE_INSTAGRAM_HOME = "PAGE_INSTAGRAM_HOME";
/******************** END INSTAGRAM APP ********************/


    /************************************* END PAGE_ID ************************************/
    public static final int SCREEN_STACK_SIZE = 30;
    public static final int VIPLIKE_MAX_POST = 5;


    /************************************* BROADCAST ACTION ************************************/
    public static final int ACTION_FOR = 30;


    /* Function defines */
    public static void SetLocalData(String packageName,String jsonData){
        SaveJsonToFile(jsonData, AUTOREG_DATA_FOLDER + packageName + ".json");
    }

    public static String GetLocalData(String packageName){
        try {
            File file = new File(AUTOREG_DATA_FOLDER + packageName + ".json");
            String ret = FileUtils.readFileToString(file);
            return  ret;
        }catch (Exception ex){}
        return "";
    }

    public static boolean SaveJsonToFile(String json, String filepath){
        try {
            File f = new File(filepath);
            if(f.exists() && f.isFile()) {
                f.delete();
            }
            FileUtils.writeStringToFile(f, json);
            return  true;
        } catch (Exception ex) { }
        return false;
    }

    public static List<String> getInstalledPackage(String appName){
        List<String> lstPackages= new ArrayList<String>();
        String searchPackage = "";
        String exceptionCase = "";
        switch (appName){
            case AppDefines.FACEBOOK_APPNAME:
                searchPackage = "com.facebook.kata";
                exceptionCase = "com.facebook.katana";
                break;
            case AppDefines.INSTAGRAM_APPNAME:
                searchPackage = "com.instagram.and";
                exceptionCase = "com.instagram.android";
                break;
            default:
                exceptionCase = "com.facebook.katana";
                searchPackage = "com.facebook.kata";
                break;

        }
        final PackageManager pm = App.getApplication().getApplicationContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            try {
                JSONObject jsonRet = new JSONObject();
                if (packageInfo.packageName.contains(searchPackage) && !packageInfo.packageName.contains(exceptionCase)) {
                    lstPackages.add(packageInfo.packageName);
                }
            }catch (Exception _ex){

            }
        }
		
        java.util.Collections.sort(lstPackages);
        return lstPackages;
    }

    public static boolean DownloadPicture(String src) {
        try {
            java.net.URL url = new java.net.URL(src);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            AppDefines.SaveImage2Gallery(App.getApplication().getContentResolver(), myBitmap, "avatar" , "uploading avatar later ");
            return  true;

        } catch (IOException e) {
            e.printStackTrace();

        }catch (Exception _ex){

        }
        return false;
    }

    public static final String SaveImage2Gallery (ContentResolver cr , Bitmap source, String title, String description){

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title );
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                AppDefines.StoreThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    private static final Bitmap StoreThumbnail( ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
    /* END Function defines */
}
