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

    public static final String MOVIES_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "Movies" + File.separator;
    public static final String DCIM_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator;
    public static final String PICTURES_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator;
    public static final String PDT_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "PDT" + File.separator;
    public static final String PDT_DATABASE_FOLDER = PDT_FOLDER + "databases"+ File.separator;
    public static final String BACKUP_DATA_FOLDER = "/data/local/backup_data" + File.separator;

    public static final int MAX_PACKAGE_NUM = 10;
    public static final int SCREEN_STACK_SIZE = 30;

    public static final int MOBILE_NETWORK = 0;
    public static final int PROXY_NETWORK = 1;
    public static final int SSHTUNNEL_NETWORK = 2;

    public static final String TIKTOK_APPNAME = "tiktok";

    /******************** TIKTOK APP ********************/
    public static final String SCREEN_UNKNOWN = "SCREEN_UNKNOWN";
    public static final String SCREEN_TIKTOK_AGREE_TOS = "SCREEN_TIKTOK_AGREE_TOS";
    public static final String SCREEN_TIKTOK_CHOOSE_INTERESTS = "SCREEN_TIKTOK_CHOOSE_INTERESTS";
    public static final String SCREEN_TIKTOK_SWIPE_UP = "SCREEN_TIKTOK_SWIPE_UP";
    public static final String SCREEN_TIKTOK_HOME_FOR_YOU = "SCREEN_TIKTOK_HOME_FOR_YOU";
    public static final String SCREEN_TIKTOK_MY_PROFILE = "SCREEN_TIKTOK_MY_PROFILE";
    public static final String SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK = "SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK";
    public static final String SCREEN_TIKTOK_LOGIN_TO_TIKTOK = "SCREEN_TIKTOK_LOGIN_TO_TIKTOK";
    public static final String SCREEN_TIKTOK_LOGIN_TAB_PHONE = "SCREEN_TIKTOK_LOGIN_TAB_PHONE";
    public static final String SCREEN_TIKTOK_LOGIN_TAB_EMAIL = "SCREEN_TIKTOK_LOGIN_TAB_EMAIL";
    public static final String SCREEN_TIKTOK_SIGN_UP_BIRTHDAY = "SCREEN_TIKTOK_SIGN_UP_BIRTHDAY";
    public static final String SCREEN_TIKTOK_SIGN_UP_PHONE = "SCREEN_TIKTOK_SIGN_UP_PHONE";
    public static final String SCREEN_TIKTOK_SIGN_UP_EMAIL = "SCREEN_TIKTOK_SIGN_UP_EMAIL";
    public static final String SCREEN_TIKTOK_VERIFY_CAPTCHA = "SCREEN_TIKTOK_VERIFY_CAPTCHA";
    public static final String SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD = "SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD";
    public static final String SCREEN_TIKTOK_ME_SIGN_UP = "SCREEN_TIKTOK_ME_SIGN_UP";
    public static final String SCREEN_TIKTOK_ACCOUNT_BE_BANNED = "SCREEN_TIKTOK_ACCOUNT_BE_BANNED";
    public static final String SCREEN_TIKTOK_SYNC_CONTACTS = "SCREEN_TIKTOK_SYNC_CONTACTS";
    public static final String SCREEN_TIKTOK_STORIES_INTRODUCE = "SCREEN_TIKTOK_STORIES_INTRODUCE";
    public static final String SCREEN_TIKTOK_UPLOAD = "SCREEN_TIKTOK_UPLOAD";
    public static final String SCREEN_TIKTOK_SELECT_MEDIA = "SCREEN_TIKTOK_SELECT_MEDIA";
    public static final String SCREEN_TIKTOK_POST = "SCREEN_TIKTOK_POST";
    public static final String SCREEN_TIKTOK_INTRODUCE_10MINS_VIDEO = "SCREEN_TIKTOK_INTRODUCE_10MINS_VIDEO";
    public static final String SCREEN_TIKTOK_VIDEO_EDITING = "SCREEN_TIKTOK_VIDEO_EDITING";
    public static final String SCREEN_TIKTOK_ENGLISH_AUTO_GENERATE = "SCREEN_TIKTOK_ENGLISH_AUTO_GENERATE";
    public static final String SCREEN_TIKTOK_POST_VIDEO_PUBLICLY = "SCREEN_TIKTOK_POST_VIDEO_PUBLICLY";
    public static final String SCREEN_TIKTOK_ADJUST_CLIPS_FEATURE_ENHANCED = "SCREEN_TIKTOK_ADJUST_CLIPS_FEATURE_ENHANCED";
    public static final String SCREEN_TIKTOK_VIEW_YOUR_FRIENDS = "SCREEN_TIKTOK_VIEW_YOUR_FRIENDS";
    /******************** END TIKTOK APP ********************/
}
