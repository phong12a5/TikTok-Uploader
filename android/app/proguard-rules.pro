# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Signature,InnerClasses
-keepattributes *Annotation*

# Samsung Knox SDK
-dontwarn com.samsung.**
-dontwarn com.sec.enterprise.**
-dontwarn android.app.enterprise.**
-dontwarn com.sec.enterprise.**
-keep class com.sec.enterprise.** { *; }
-keep class android.app.enterprise.** { *; }
-keep class com.samsung.** { *; }
-keep interface com.samsung.** { *; }
-keep enum com.samsung.** { *; }
-keepclassmembers class com.samsung.** { *; }


# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep class okio.** { *;}
-dontwarn okio.**

-verbose
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
#-keep,includedescriptorclasses class com.google.android.gms.internal.** { *; }
#-keep class com.google.android.gms.** { *; }

-keep class com.google.android.gms.common.api.internal.BasePendingResult$ReleasableResultGuardian

-keepnames class com.firebase.** { *; }
-keepnames class com.shaded.fasterxml.jackson.** { *; }
-keepnames class org.shaded.apache.** { *; }
-keepnames class javax.servlet.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.commons.logging.impl.**

-keep class com.google.code.** { *; }
-dontwarn com.google.code.**

-keep class pdt.autoreg.devicefaker** { *; }
-dontwarn pdt.autoreg.devicefaker.**

-keep class com.chilkatsoft.** { *; }
-dontwarn com.chilkatsoft.**

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**


-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.common.**
-keepclassmembers class com.google.android.gms.common.api.internal.BasePendingResult { *; }

#-keep class pdt.autoreg.cgblibrary.** { *; }
#-dontwarn pdt.autoreg.cgblibrary.**
##-keepclassmembers class pdt.autoreg.cgblibrary.** { *; }

#-keep interface pdt.autoreg.cgblibrary.** { *; }
-keep interface pdt.autoreg.cgblibrary.AccessibilityEventHandlerBase
-dontwarn pdt.autoreg.cgblibrary.AccessibilityEventHandlerBase
-keepclassmembers interface pdt.autoreg.cgblibrary.AccessibilityEventHandlerBase { *; }

-keep class pdt.autoreg.cgblibrary.DeviceInfo
-dontwarn pdt.autoreg.cgblibrary.DeviceInfo
-keepclassmembers class pdt.autoreg.cgblibrary.DeviceInfo { *; }

-keep class pdt.autoreg.cgblibrary.LOG
-dontwarn pdt.autoreg.cgblibrary.LOG
-keepclassmembers class pdt.autoreg.cgblibrary.LOG { *; }

-keep class pdt.autoreg.cgblibrary.services.ApiAccessibilityService
-dontwarn pdt.autoreg.cgblibrary.ApiAccessibilityService
-keepclassmembers class pdt.autoreg.cgblibrary.services.ApiAccessibilityService { *; }

-keep class pdt.autoreg.cgblibrary.CGBInterface
-dontwarn pdt.autoreg.cgblibrary.CGBInterface
-keepclassmembers class pdt.autoreg.cgblibrary.CGBInterface { *; }