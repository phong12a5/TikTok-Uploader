package pdt.autoreg.cgblibrary;

import android.os.Environment;

public class ModuleDefines {

    /* Constant value */
    public static final String AUTOFARMER_APPLICATION_FOLDER = Environment.getExternalStorageDirectory() + "/Applications/pdt.autoreg.app/";
    public static final String AUTOREG_DATA_FOLDER = Environment.getExternalStorageDirectory() + "/pdt.autoreg.app/";
    public static final String AUTOFARMER_TESS_FOLDER = AUTOREG_DATA_FOLDER + "tessdata/";
    public static final String APPLICATION_FOLDER = Environment.getExternalStorageDirectory() + "/Applications/";
    public static final String AUTOFARMER_LOG_FOLDER = Environment.getExternalStorageDirectory() + "/pdt.autoreg.app/logs/";


    public static final String DO_CLICK = "xyz.autofarmer.cgblibrary.DO_CLICK";
    public static final String DO_DOUBLE_CLICK = "xyz.autofarmer.cgblibrary.DO_DOUBLE_CLICK";
    public static final String DO_CLICK_BY_SCREEN_ID = "xyz.autofarmer.cgblibrary.DO_CLICK_BY_SCREEN_ID";
    public static final String SWIPE = "xyz.autofarmer.cgblibrary.SWIPE";
    public static final String SCROLL_FORWARD = "xyz.autofarmer.cgblibrary.SCROLL_FORWARD";
    public static final String SCROLL_BACKWARD = "xyz.autofarmer.cgblibrary.SCROLL_BACKWORD";
    public static final String OPEN_PACKAGE = "xyz.autofarmer.cgblibrary.OPEN_PACKAGE";
    public static final String OPEN_PACKAGE_ACTIVITY = "xyz.autofarmer.cgblibrary.OPEN_PACKAGE_ACTIVITY";
    public static final String CLOSE_PACKAGE = "xyz.autofarmer.cgblibrary.CLOSE_PACKAGE";
    public static final String CLEAR_CACHE = "xyz.autofarmer.cgblibrary.CLEAR_CACHE";
    public static final String INPUT_TEXT = "xyz.autofarmer.cgblibrary.INPUT_TEXT";
    public static final String INPUT_KEY_EVENT = "xyz.autofarmer.cgblibrary.INPUT_KEY_EVENT";
    public static final String WIPE_PACKAGE = "xyz.autofarmer.cgblibrary.WIPE_PACKAGE";
    public static final String INSTALL_PACKAGE = "xyz.autofarmer.cgblibrary.INSTALL_PACKAGE";
    public static final String UNINSTALL_PACKAGE = "xyz.autofarmer.cgblibrary.UNINSTALL_PACKAGE";
    public static final String SCREEN_CAPTURE = "xyz.autofarmer.cgblibrary.SCREEN_CAPTURE";
    public static final String DONOTHING = "xyz.autofarmer.cgblibrary.DONOTHING";
    public static final String CAPTURE_SCREEN_RESPONSE = "xyz.autofarmer.cgblibrary.CAPTURE_SCREEN_RESPONSE";
    public static final String ON_OFF_WIFI = "xyz.autofarmer.cgblibrary.ON_OFF_WIFI";
    public static final String ON_OFF_MOBILE_NETWORK = "xyz.autofarmer.cgblibrary.ON_OFF_MOBILE_NETWORK";
    public static final String GLOBAL_BACK = "xyz.autofarmer.cgblibrary.GLOBAL_BACK";
    public static final String PROCESS_IMAGE_DONE = "xyz.autofarmer.cgblibrary.PROCESS_IMAGE";
    public static final String SETUP_DEVICE = "xyz.autofarmer.cgblibrary.SETUP_DEVICE";
    public static final String ENABLE_INSTALL_UNKNOWN_SRC = "xyz.autofarmer.cgblibrary.ENABLE_INSTALL_UNKNOWN_SRC";
    public static final String ENABLE_DND = "xyz.autofarmer.cgblibrary.ENABLE_DND";
    public static final String DISABLE_AUTOFILL_SERVICE = "xyz.autofarmer.cgblibrary.DISABLE_AUTOFILL_SERVICE";
    public static final String ON_OFF_AIRPLANE_MODE = "xyz.autofarmer.cgblibrary.ON_OFF_AIRPLANE_MODE";
    public static final String MOVE_SDCARD_TO_INTERNAL_STORAGE = "xyz.autofarmer.cgblibrary.MOVE_SDCARD_TO_INTERNAL_STORAGE";
    public static final String REMOVE_FACEBOOK_ACCOUNT = "xyz.autofarmer.cgblibrary.REMOVE_FACEBOOK_ACCOUNT";

    public static final String[] supportedLange = {"en_US","en_GB"};
    /* ------------------------- */

}
