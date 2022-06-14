package pdt.autoreg.devicefaker.xposed;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import pdt.autoreg.devicefaker.VDRoot;

public class VDFileHelper {

    public static String[] listFileName(String path){

        String result = VDRoot.execute("ls \"" + path + "\"");

        if (result == null){
            return new String[]{};
        }

        return result.split("\n");
    }

    public static String[] listFilePath(String path){

        String endPath = path.endsWith("/") ? "" : "/";

        String[] listFileName = listFileName(path);

        String[] listFilePath = new String[listFileName.length];

        for (int i=0; i < listFileName.length; i++){
            listFilePath[i] = path + endPath + listFileName[i];
        }
        return listFilePath;
    }

    public static File[] listFile(String path){

        String[] filePaths = listFilePath(path);

        if (filePaths.length == 0){
            return new File[]{};
        }

        File[] files = new File[filePaths.length];

        for (int i=0; i < filePaths.length; i++){
            String endPath = path.endsWith("/") ? "" : "/";
            files[i] = new File(path + endPath + filePaths[i]);
        }

        return files;
    }

    public static void createFolder(String... paths){

        for (String path : paths){

            if(!isExists(path)) {

                if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
                    mount(path);
                }

                String[] parts = path.split("/");

                StringBuilder newPath = new StringBuilder();

                for (String part : parts) {

                    newPath.append(part).append("/");

                    VDRoot.execute("mkdir \"" + newPath.toString() + "\"");
                }

                if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
                    unMount(path);
                }
            }
        }
    }

    public static void writeFile(String content,String path){

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            mount(path);
        }

        VDRoot.execute("echo \"" + content + "\" > \"" + path + "\"");

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            unMount(path);
        }
    }

    public static void deleteFile(String path) {

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            mount(path);
        }

        VDRoot.execute("rm -r \"" + path + "\"");

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            unMount(path);
        }
    }

    public static void copy(String fromPath,String toPath){

        if (!fromPath.startsWith("/sdcard/") && !fromPath.startsWith("/storage/emulated/0/")) {
            mount(fromPath);
        }

        if (!toPath.startsWith("/sdcard/") && !toPath.startsWith("/storage/emulated/0/")) {
            mount(toPath);
        }

        VDRoot.execute("cp -r \"" + fromPath + "\" \"" + toPath + "\"");

        if (!fromPath.startsWith("/sdcard/") && !fromPath.startsWith("/storage/emulated/0/")) {
            unMount(fromPath);
        }

        if (!toPath.startsWith("/sdcard/") && !toPath.startsWith("/storage/emulated/0/")) {
            unMount(toPath);
        }
    }

    public static boolean isExists(String path){

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            mount(path);
        }

        boolean exists = new File(path).exists();

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            unMount(path);
        }

        return exists;
    }

    public static String readTextFile(String path){

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            mount(path);
        }

        String content = VDRoot.execute("cat \"" + path + "\"");

        if (!path.startsWith("/sdcard/") && !path.startsWith("/storage/emulated/0/")) {
            unMount(path);
        }

        return content;
    }

        public static ArrayList<String> readTextFiles(String path){

            String content = readTextFile(path);

            String[] contents = content.split("\n");

            return new ArrayList<>(Arrays.asList(contents));
        }

        public static void mount(String path){
            fileMode(path,"rw");
        }

        public static void unMount(String path){
            fileMode(path,"ro");
        }

        private static void fileMode(String path,String mode){

            String[] parts = path.split("/");

            StringBuilder pathTemp = new StringBuilder();

            for (String part : parts){

                if (part.length() == 0){
                    VDRoot.execute("mount -o " + mode + ",remount /");
                    continue;
                }

                pathTemp.append("/").append(part);

                VDRoot.execute("mount -o " + mode + ",remount " + pathTemp.toString());
            }
        }
}
