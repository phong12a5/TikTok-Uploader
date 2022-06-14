package pdt.autoreg.devicefaker;

import com.stericson.RootShell.RootShell;

public class FakerInterface {
    static private FakerInterface instance = null;

    private FakerInterface() {

    }

    public static boolean isRootAccess() {
        return  RootShell.isAccessGiven();
    }

    public static void changeDeviceInfo() {

    }

    public static String rootExecute(String command) {
        return VDRoot.execute(command);
    }
}
