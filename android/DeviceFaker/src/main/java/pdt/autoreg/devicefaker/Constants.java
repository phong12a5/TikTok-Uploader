package pdt.autoreg.devicefaker;

import android.os.Environment;

public class Constants {

    public static final String AUTOREG_FOLDER = Environment.getExternalStorageDirectory() + "/pdt.autoreg.app/";
    public static final String AUTOREG_DATA_FOLDER = AUTOREG_FOLDER + "data/";
    public static final String REG_PACKAGE = "com.zhiliaoapp.musically";

    public static final String CARRIER_VIETTEL = "Viettel";
    public static final String CARRIER_VINAPHONE = "VinaPhone";
    public static final String CARRIER_MOBIFONE = "MobiFone";

    public static final String[] PHONE_PREFIX_VIETTEL = new String[]{"096","097","098","086","032","033","034", "035","036","037","038","039"};
//    public static final String[] PHONE_PREFIX_VIETTEL = new String[]{"098"};

    public static final String[] PHONE_PREFIX_VINAPHONE = new String[]{"088","091","094","081","082","083","084","085"};
//    public static final String[] PHONE_PREFIX_VINAPHONE = new String[]{"094"};

    //    public static final String[] PHONE_PREFIX_MOBIFONE = new String[]{"090", "093", "089", "070", "079", "077", "076", "078"};
    public static final String[] PHONE_PREFIX_MOBIFONE = new String[]{"090"};

    public static final String[] CARRIER_LIST = new String[] {Constants.CARRIER_VINAPHONE};
}
