package pdt.autoreg.devicefaker.xposed;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XCallback;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.findField;

public class HideChecking {

    private static ClassLoader classLoaders;
    private static final String[] APP_ROOT = {
            "supersu", "superuser", "Superuser",
            "noshufou", "xposed", "rootcloak",
            "chainfire", "titanium", "Titanium",
            "substrate", "greenify", "daemonsu",
            "root", "busybox", "titanium","magisk",
            ".tmpsu", "su", "rootcloak2",
            "XposedBridge.jar","framework",
    };

    private  ClassLoader getClassLoader(){
        return classLoaders;
    }

    public HideChecking(ClassLoader classLoader){
        classLoaders = classLoader;

        hideDebug();

        hideAdbDebug();

        enableSELinux();

        enableReleaseKey();

        preventProcessBuilder();

        preventRuntime();

        hideXposedClass();

        hideSearchRootXposedFile();

//        hidePackageManager();
//
//        hideActivityManager();
    }

    private void hideActivityManager() {
        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningServices", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                List<ActivityManager.RunningServiceInfo> services = (List<ActivityManager.RunningServiceInfo>) param.getResult();

                for (ActivityManager.RunningServiceInfo serviceInfo : services){
                    if (stringContainsFromSet(serviceInfo.process, APP_ROOT)) {
                        services.remove(serviceInfo);
                    }
                }
                param.setResult(services);
            }
        });

        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningTasks", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                List<ActivityManager.RunningTaskInfo> services = (List<ActivityManager.RunningTaskInfo>) param.getResult();

                for (ActivityManager.RunningTaskInfo taskInfo : services){
                    if (stringContainsFromSet(taskInfo.baseActivity.flattenToString(), APP_ROOT)) {
                        services.remove(taskInfo);
                    }
                }

                param.setResult(services);
            }
        });

        findAndHookMethod("android.app.ActivityManager", getClassLoader(), "getRunningAppProcesses", new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                List<ActivityManager.RunningAppProcessInfo> processes = (List<ActivityManager.RunningAppProcessInfo>) param.getResult();

                for (ActivityManager.RunningAppProcessInfo processInfo : processes){
                    if (stringContainsFromSet(processInfo.processName, APP_ROOT)) {
                        processes.remove(processInfo);
                    }
                }
                param.setResult(processes);
            }
        });
    }

    private void hidePackageManager() {
        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getInstalledApplications", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                List<ApplicationInfo> packages = (List<ApplicationInfo>) param.getResult();

                if (packages != null && packages.size() > 0) {
                    for (ApplicationInfo applicationInfo : packages) {
                        if (stringContainsFromSet(applicationInfo.packageName, APP_ROOT)) {
                            packages.remove(applicationInfo);
                        }
                    }
                }
                param.setResult(packages);
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getInstalledPackages", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                List<PackageInfo> packages = (List<PackageInfo>) param.getResult();

                if (packages != null && packages.size() > 0){
                    for (PackageInfo packageInfo : packages){
                        if (stringContainsFromSet(packageInfo.packageName, APP_ROOT)) {
                            packages.remove(packageInfo);
                        }
                    }
                }
                param.setResult(packages);
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                PackageInfo packages = (PackageInfo) param.getResult();

                if (packages != null) {
                    if (!stringContainsFromSet(packages.packageName, APP_ROOT)) {
                        param.setResult(packages);
                        return;
                    }
                }
                param.setResult(null);
            }
        });

        findAndHookMethod("android.app.ApplicationPackageManager", getClassLoader(), "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                ApplicationInfo applicationInfo = (ApplicationInfo) param.getResult();

                if (applicationInfo != null) {
                    if (!stringContainsFromSet(applicationInfo.packageName, APP_ROOT)) {
                        param.setResult(applicationInfo);
                        return;
                    }
                }
                param.setResult(null);
            }
        });
    }

    private void hideSearchRootXposedFile() {
        Constructor<?> constructLayoutParams = findConstructorExact(File.class, String.class);
        hookMethod(constructLayoutParams, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (((String) param.args[0]).endsWith("su")) {
                    param.args[0] = "/system/xbin/abc_xyz";
                } else if (((String) param.args[0]).endsWith("busybox")) {
                    param.args[0] = "/system/xbin/abc_xyz";
                } else if (stringContainsFromSet(((String) param.args[0]), APP_ROOT)) {
                    param.args[0] = "/system/app/abc_xyz.apk";
                }
            }
        });

        Constructor<?> extendedFileConstructor = findConstructorExact(File.class, String.class, String.class);
        hookMethod(extendedFileConstructor, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (((String) param.args[1]).equalsIgnoreCase("su")) {
                    param.args[1] = "abc_xyz";
                } else if (((String) param.args[1]).contains("busybox")) {
                    param.args[1] = "abc_xyz";
                } else if (stringContainsFromSet(((String) param.args[1]), APP_ROOT)) {
                    param.args[1] = "abc_xyz" + ".apk";
                }
            }
        });
    }

    private void hideXposedClass() {
        findAndHookMethod("java.lang.Class", getClassLoader(), "forName", String.class, boolean.class, ClassLoader.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String classname = (String) param.args[0];

                if (classname != null &&
                        (classname.equals("de.robv.android.xposed.XposedBridge") ||
                                classname.equals("de.robv.android.xposed.IXposedHookLoadPackage") ||
                                classname.equals("de.robv.android.xposed.IXposedHookZygoteInit") ||
                                classname.equals("de.robv.android.xposed.XC_MethodHook") ||
                                classname.equals("de.robv.android.xposed.XC_MethodReplacement") ||
                                classname.equals("de.robv.android.xposed.XSharedPreferences") ||
                                classname.equals("de.robv.android.xposed.XposedHelpers") ||
                                classname.equals("de.robv.android.xposed.IXposedHookInitPackageResources") ||
                                classname.equals("de.robv.android.xposed.SELinuxHelper") ||

                                classname.equals("de.robv.android.xposed.callbacks.XC_LoadPackage") ||
                                classname.equals("de.robv.android.xposed.callbacks.IXUnhook") ||
                                classname.equals("de.robv.android.xposed.callbacks.XC_InitPackageResources") ||
                                classname.equals("de.robv.android.xposed.callbacks.XC_LayoutInflated") ||
                                classname.equals("de.robv.android.xposed.callbacks.XCallback") ||

                                classname.equals("de.robv.android.xposed.services.BaseService") ||
                                classname.equals("de.robv.android.xposed.services.FileResult"))) {

                    param.setThrowable(new ClassNotFoundException());
                }
            }
        });
    }

    private void enableReleaseKey() {
        if (!Build.TAGS.equals("release-keys")) {
            XposedHelpers.setStaticObjectField(Build.class, "TAGS", "release-keys");
        }
    }

    private void enableSELinux() {
        findAndHookMethod("android.os.SystemProperties", getClassLoader(), "get", String.class , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals("ro.build.selinux")) {
                    param.setResult("1");
                }
            }
        });
    }

    private void hideDebug() {
        findAndHookMethod(
                "android.os.Debug",
                getClassLoader(),
                "isDebuggerConnected",
                XC_MethodReplacement.returnConstant(false)
        );
    }

    private void preventRuntime() {
        findAndHookMethod("java.lang.Runtime", getClassLoader(), "exec", String[].class, String[].class, File.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {

                String[] execArray = (String[]) param.args[0];

                if ((execArray != null) && (execArray.length >= 1)) {
                    String firstParam = execArray[0];

                    String[] command = {"su", "which", "busybox", "pm", "am", "sh", "ps","getprop"};

                    if (stringOfConstant(firstParam, command)) {
                        if (firstParam.equals("su") || firstParam.endsWith("/su")) {
                            param.setThrowable(new IOException());
                        } else if ((firstParam.equals("pm") || firstParam.endsWith("/pm"))) {
                            if (execArray.length >= 3 && execArray[1].equalsIgnoreCase("list") && execArray[2].equalsIgnoreCase("packages")) {
                                param.args[0] = buildGrepArraySingle(execArray, true);
                            } else if (execArray.length >= 3 && (execArray[1].equalsIgnoreCase("dump") || execArray[1].equalsIgnoreCase("path"))) {
                                String[] keywords = {"supersu", "superuser", "Superuser",
                                        "noshufou", "xposed", "rootcloak",
                                        "chainfire", "titanium", "Titanium",
                                        "substrate", "greenify", "daemonsu",
                                        "root", "busybox", "titanium",
                                        ".tmpsu", "su", "rootcloak2","vietdung"};

                                if (stringContainsFromSet(execArray[2],keywords)) {
                                    param.args[0] = new String[]{execArray[0], execArray[1], "com.zing.zalo"};
                                }
                            }
                        } else if ((firstParam.equals("ps") || firstParam.endsWith("/ps"))) { // This is a process list command
                            param.args[0] = buildGrepArraySingle(execArray, true);
                        } else if ((firstParam.equals("which") || firstParam.endsWith("/which"))) {
                            param.setThrowable(new IOException());
                        } else if (anyWordEndingWithKeyword("busybox", execArray)) {
                            param.setThrowable(new IOException());
                        } else if (firstParam.equals("sh") || firstParam.endsWith("/sh")) {
                            param.setThrowable(new IOException());
                        } else {
                            param.setThrowable(new IOException());
                        }
                    }
                }
            }
        });

        findAndHookMethod("java.lang.Runtime", getClassLoader(), "loadLibrary", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String libname = (String) param.args[0];

                String libKeyword = "tool-checker";

                if (libname != null && libname.equals(libKeyword)) {
                    param.setResult(null);
                }
            }
        });
    }

    private Boolean anyWordEndingWithKeyword(String keyword, String[] wordArray) {
        for (String tempString : wordArray) {
            if (tempString.endsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    public boolean stringContainsFromSet(String base,String[] keywords) {
        if (base != null) {
            for (String tempString : keywords) {
                if (base.matches(".*(\\W|^)" + tempString + "(\\W|$).*")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] buildGrepArraySingle(String[] original, boolean addSH) {
        StringBuilder builder = new StringBuilder();
        ArrayList<String> originalList = new ArrayList<>();
        if (addSH) {
            originalList.add("sh");
            originalList.add("-c");
        }
        for (String temp : original) {
            builder.append(" ");
            builder.append(temp);
        }

        String[] keyword = {"supersu", "superuser", "Superuser",
                "noshufou", "xposed", "rootcloak",
                "chainfire", "titanium", "Titanium",
                "substrate", "greenify", "daemonsu",
                "root", "busybox", "titanium",
                ".tmpsu", "su", "rootcloak2","vietdung"};

        for (String temp : keyword) {
            builder.append(" | grep -v ");
            builder.append(temp);
        }
        //originalList.addAll(Common.DEFAULT_GREP_ENTRIES);
        originalList.add(builder.toString());
        return originalList.toArray(new String[0]);
    }

    private void preventProcessBuilder() {
        Constructor<?> processBuilderConstructor2 = findConstructorExact(ProcessBuilder.class, String[].class);
        hookMethod(processBuilderConstructor2, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {

                String[] command = {"su", "which", "busybox", "pm", "am", "sh", "ps"};

                if (param.args[0] != null) {
                    String[] cmdArray = (String[]) param.args[0];
                    if (stringEndsWithFromSet(cmdArray[0], command)) {
                        cmdArray[0] = "abc";
                        param.args[0] = cmdArray;
                    }
                }
            }
        });
    }

    public boolean stringOfConstant(String base,String[] values){
        if (base != null && values != null) {
            for (String tempString : values) {
                if (base.contains(tempString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean stringEndsWithFromSet(String base, String[] values) {
        if (base != null && values != null) {
            for (String tempString : values) {
                if (base.endsWith(tempString)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void hideAdbDebug() {
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(0);
            }
        });

        findAndHookMethod(Settings.Secure.class, "getInt", ContentResolver.class, String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(0);
            }
        });
    }
}
