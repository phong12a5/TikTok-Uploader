package pdt.autoreg.devicefaker.helper;

import androidx.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import java.io.File;
import de.robv.android.xposed.XposedBridge;

public class FileHelper {

    public static void copy(String fromPath,String toPath){
        RootHelper.execute("cp " + fromPath + " " + toPath);
    }

    public static boolean exist(String path) throws Exception {
        final String ret = RootHelper.execute(String.format("(ls %s && echo __YES__) || echo __NO__", path));
        if(ret.contains("__YES__")) return true;
        else if(ret.contains("__NO__")) return false;
        else {
            throw new Exception() {
                @Nullable
                @Override
                public String getMessage() {
                    return "exec failed(" + ret + ")";
                }
            };
        }
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
