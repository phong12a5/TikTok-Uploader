package pdt.autoreg.devicefaker.helper;

import org.apache.commons.io.FileUtils;
import java.io.File;
import de.robv.android.xposed.XposedBridge;

public class FileHelper {

    public static void copy(String fromPath,String toPath){
        RootHelper.execute("cp " + fromPath + " " + toPath);
    }

    public static String readFile(String path){
        File file = new File(path);
        try {
            return FileUtils.readFileToString(file);
        } catch (Exception e){
            XposedBridge.log("readFile error: " +  e);
        }
        return null;
    }

    public static void deleteFile(String path){
        RootHelper.execute("rm -r " + path);
    }

    public static void writeFile(String content,String path) {
        RootHelper.execute("echo " + content + " > " + path);
    }
}
