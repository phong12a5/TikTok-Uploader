package pdt.autoreg.app.services;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.squareup.okhttp.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pdt.autoreg.accessibility.ASInterface;
import pdt.autoreg.accessibility.ASUtils;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.accessibility.screendefinitions.ScreenNode;
import pdt.autoreg.app.App;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.R;
import pdt.autoreg.app.api.DBPApi;
import pdt.autoreg.app.api.DropboxAPI;
import pdt.autoreg.app.model.AppModel;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.app.helpers.ProxyHelper;
import pdt.autoreg.app.model.CloneInfo;
import pdt.autoreg.devicefaker.helper.RootHelper;
import pdt.autoreg.devicefaker.Constants;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TiktokAppService extends BaseService {
    private static String TAG = "TiktokAppService";

    static {
        LOG.D(TAG, "Load openCV: " + OpenCVLoader.initDebug());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initEnv(Context context) {
        LOG.D(TAG,"initEnv");
        super.initEnv(context);
        ProxyHelper.stopProxySwitch();
        RootHelper.disableAirplane();
    }

    @Override
    protected void mainOperations() {
        try {
            switch (AppModel.instance().currScrID()) {
                case AppDefines.SCREEN_TIKTOK_AGREE_TOS:
                    ASUtils.findAndClick("ID_AGREE_BTN", AppModel.instance().currScrInfo());
                    break;

                case AppDefines.SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK:
                    ASUtils.findAndClick("ID_LOGIN", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_CHOOSE_INTERESTS:
                    ASUtils.findAndClick("ID_SKIP_BTN", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_SWIPE_UP:
                    ASUtils.findAndClick("ID_START_WATCHING_BTN", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_HOME_FOR_YOU:
                    if(ASUtils.find("ID_BTN_SWIPE_FOR_MORE", AppModel.instance().currScrInfo(), false)) {
                        ASInterface.instance().swipe(widthOfScreen/2, heightOfScreen - 100, widthOfScreen/2, 200, 400);
                    } else if(AppModel.instance().currPackage().getCloneInfo() == null || !AppModel.instance().currPackage().isVerifiedLogin()) {
                        ASUtils.findAndClick("ID_BTN_PROFILE", AppModel.instance().currScrInfo());
                    } else {
                        LOG.I(TAG, "do job ...........");
                        if(AppModel.instance().currPackage().getActions() == null) {
                            getActions();
                        } else if(AppModel.instance().currPackage().getActions().length() == 0) {
                            changePackage();
                        } else {
                            JSONObject action = AppModel.instance().currPackage().takeAction();
                            String actionCode = action.getString("action");
                            switch (actionCode) {
                                case "feed_like":
                                    feedLike();
                                    break;
                                case "feed_comment":
                                    feedComment();
                                    break;
                                case "post_video":
                                    postVideo();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                case AppDefines.SCREEN_TIKTOK_LOGIN_TO_TIKTOK:
                    ASUtils.findAndClick("ID_LOGIN_EMAIL", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_LOGIN_TAB_PHONE:
                    ASUtils.findAndClick("ID_TAB_EMAIL_USERNAME", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_LOGIN_TAB_EMAIL:
                    if(AppModel.instance().currPackage().getCloneInfo() == null) {
                        getClone();
                    } else {
                        String username = AppModel.instance().currPackage().getCloneInfo().username();
                        String password = AppModel.instance().currPackage().getCloneInfo().password();
                        String hiddend = "";
                        for (int i =0; i < password.length(); i++) {
                            hiddend += "•";
                        }
                        if(!ASUtils.findByTextOrDes(username, AppModel.instance().currScrInfo())) {
                            if (ASUtils.findAndClick("ID_EDT_EMAIL_USERNAME", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText(username, null, true);
                            }
                        } else if(!ASUtils.findByTextOrDes(hiddend, AppModel.instance().currScrInfo())){
                            if (ASUtils.findAndClick("ID_EDT_PASSWORD", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText(password, null, true);
                            }
                        } else if(ASUtils.findByTextOrDes("Too many attempts. Try again later.", AppModel.instance().currScrInfo())) {
                            changePackage();
                        } else {
                            ASUtils.findAndClick("ID_BTN_LOGIN", AppModel.instance().currScrInfo());
                            Utils.delayRandom(3000, 5000);
                        }
                    }
                    break;
                case AppDefines.SCREEN_TIKTOK_VERIFY_CAPTCHA:
                    if(AppModel.instance().currPackage().getCloneInfo() != null) {
                        String outputPath = AppDefines.PDT_FOLDER + "screen.png";
                        RootHelper.screenCapture(outputPath);
                        Map<String, Rect> result = scanPuzzlePiece(outputPath);
                        if (result != null) {
                            try {
                                Rect start = result.get("start");
                                Rect end = result.get("end");
                                ASInterface.instance().swipe(start.x + start.width / 2, start.y + start.height / 2, end.x + end.width / 2, end.y + end.height / 2, 1000);
                            } catch (Exception e) {
                                LOG.printStackTrace(TAG, e);
                            }
                        }
                    } else {
                        ASInterface.instance().globalBack();
                    }
                    break;
                case AppDefines.SCREEN_TIKTOK_MY_PROFILE:
                    if(AppModel.instance().currPackage().getCloneInfo() != null) {
                        AppModel.instance().currPackage().setIsVerifiedLogin(true);
                        AppModel.instance().currPackage().getCloneInfo().setStatus(CloneInfo.CLONE_STATUS_STORED);
                        Utils.showToastMessage(this, "Verified login");
                        while (!backupPackage(AppModel.instance().currPackage().getCloneInfo().username(), AppModel.instance().currPackage().getPackageName())) {
                            Utils.showToastMessage(this, "Backup failed -> retry");
                            Utils.delay(1000);
                        }
                        ASInterface.instance().globalBack();
                    } else {
                        RootHelper.closePackage(AppModel.instance().currPackage().getPackageName());
                        RootHelper.clearPackage(AppModel.instance().currPackage().getPackageName());
                        generateNewDeviceInfo(AppModel.instance().currPackage().getPackageName());
                    }
                    LOG.I(TAG, "Logged in");
                    break;
                case AppDefines.SCREEN_TIKTOK_ME_SIGN_UP:
                    //ID_BTN_SIGN_UP
                    ASUtils.findAndClick("ID_BTN_SIGN_UP", AppModel.instance().currScrInfo());
                    break;
                case AppDefines.SCREEN_TIKTOK_ACCOUNT_BE_BANNED:
                    if(AppModel.instance().currPackage().getCloneInfo() != null) {
                        AppModel.instance().currPackage().getCloneInfo().setStatus(CloneInfo.CLONE_STATUS_BANNED);
                        RootHelper.execute("rm -rf " + getBackupFolderPath(AppModel.instance().currPackage().getCloneInfo().username()));
                    }
                    changePackage();
                    break;
                case AppDefines.SCREEN_TIKTOK_SYNC_CONTACTS:
                    ASUtils.findAndClick("ID_BTN_DONT_ALLOW", AppModel.instance().currScrInfo());
                    break;
                default:
                    if(!AppModel.instance().currPackage().getPackageName().equals(ASInterface.instance().getCurrentForgroundPkg())) {
                        ASInterface.instance().openPackage(AppModel.instance().currPackage().getPackageName());
                    } else {
                        ASInterface.instance().globalBack();
                    }
                    Utils.delayRandom(2000, 4000);
                    break;

            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    private void feedLike() {
        LOG.I(TAG, "feedLike");
        int lineCount = 0;
        for (int i = 0; i < 40; i++) {
            try {
                Utils.delayRandom(3000, 15000);

                if(lineCount == 0 && new Random().nextInt(20) == 0) {
                    int x = widthOfScreen/2 + (new Random().nextInt(100));
                    int y = heightOfScreen/2 + (new Random().nextInt(100));
                    RootHelper.execute(String.format("input tap %d %d ", x, y));
                    Utils.delay(100);
                    RootHelper.execute(String.format("input tap %d %d ", x + new Random().nextInt(10) , y + new Random().nextInt(10)));
                    Utils.delay(2000);
                    lineCount ++;
                }

                Random random = new Random();
                RootHelper.execute(String.format("input swipe %d %d %d %d %d",
                        widthOfScreen/2 + random.nextInt(100),
                        heightOfScreen - 300 + random.nextInt(100),
                        widthOfScreen/2 + random.nextInt(100),
                        100 + random.nextInt(100),
                        400 + random.nextInt(100)));
            } catch (Exception e){
                LOG.printStackTrace(TAG, e);
            }
        }
    }

    private void feedComment() {
        LOG.I(TAG, "feedComment");
        for (int i = 0; i < 40; i++) {
            try {
                Utils.delayRandom(3000, 15000);

                Random random = new Random();
                RootHelper.execute(String.format("input swipe %d %d %d %d %d",
                        widthOfScreen/2 + random.nextInt(100),
                        heightOfScreen - 300 + random.nextInt(100),
                        widthOfScreen/2 + random.nextInt(100),
                        100 + random.nextInt(100),
                        400 + random.nextInt(100)));
            } catch (Exception e){
                LOG.printStackTrace(TAG, e);
            }
        }
    }

    private void postVideo() {
        LOG.I(TAG, "postVideo");
        long current = System.currentTimeMillis();
        long last_upload = AppModel.instance().currPackage().getCloneInfo().lastUploadTime();
        if(current - last_upload < (8 * 60 * 60 * 1000)) {
            LOG.E(TAG, "waiting for uploading next video");
        } else {
            String videoPath = null, videoId = null;
            String videoLocalPath = AppDefines.MOVIES_FOLDER + "video.mp4";
            JSONObject video_info = null;

            try {
                JSONObject ret = DBPApi.instance().getVideoPath(AppModel.instance().currPackage().getCloneInfo().clonedFrom());
                video_info = ret.getJSONObject("video_info");
                videoPath = video_info.getString("video_path");
                videoId = video_info.getString("video_id");
                LOG.I(TAG, "video_info: " + video_info);
            } catch (Exception e) {
                LOG.E(TAG, e.getMessage());
            }

            if(videoPath == null || videoPath.isEmpty()) {
                LOG.E(TAG, "postVideo exit (reason: get video_path failed!)");
                return;
            } else if(!DropboxAPI.downloadFileFromDropbox(videoPath, videoLocalPath)) {
                LOG.E(TAG, "postVideo exit (reason: download video failed!)");
                return;
            }

            MediaScannerConnection.scanFile(this,new String[] { videoLocalPath }, null, null);


            boolean selectedVideo = false;
            boolean clickedPost = false;
            String describe = getRandomVideoDescribe();

            for (int i = 0; i < 30; i ++) {
                try {
                    detectScreen(false);
                    switch (AppModel.instance().currScrID()) {
                        case AppDefines.SCREEN_TIKTOK_HOME_FOR_YOU:
                            if(clickedPost) {
                                Utils.delay(3000);
                                continue;
                            }
                            if(!ASUtils.findAndClick("ID_BTN_POST", AppModel.instance().currScrInfo())) {
                                ScreenNode homeBtn = ASUtils.getComponentInfo("ID_BTN_HOME", AppModel.instance().currScrInfo());
                                ScreenNode profileBtn = ASUtils.getComponentInfo("ID_BTN_PROFILE", AppModel.instance().currScrInfo());
                                if(homeBtn != null && profileBtn != null) {
                                    ASInterface.instance().clickByPos(homeBtn.x + (profileBtn.x - homeBtn.x)/2 + homeBtn.width/2, homeBtn.y + (homeBtn.height/2), false);
                                }
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_STORIES_INTRODUCE:
                        case AppDefines.SCREEN_TIKTOK_INTRODUCE_10MINS_VIDEO:
                        case AppDefines.SCREEN_TIKTOK_ENGLISH_AUTO_GENERATE:
                        case AppDefines.SCREEN_TIKTOK_ADJUST_CLIPS_FEATURE_ENHANCED:
                        case AppDefines.SCREEN_TIKTOK_VIEW_YOUR_FRIENDS:
                            ASUtils.findAndClick("ID_BTN_OK", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_UPLOAD:
                            ASUtils.findAndClick("ID_BTN_UPLOAD", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SELECT_MEDIA:
                            if(!selectedVideo) {
                                selectedVideo = ASUtils.findAndClickByTextOrDes(":", AppModel.instance().currScrInfo(), false);
                            } else {
                                ASUtils.findAndClick("ID_BTN_NEXT", AppModel.instance().currScrInfo());
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_POST:
                            if(!ASUtils.findByTextOrDes(describe, AppModel.instance().currScrInfo())) {
                                if(ASUtils.findAndClick("ID_EDT_DESCRIBE", AppModel.instance().currScrInfo())) {
                                    Utils.delay(1000);
                                    ASInterface.instance().inputText(describe, null, true);
                                    Utils.delay(500);
                                    ASInterface.instance().globalBack();
                                }
                            } else {
                                ASUtils.findAndClick("ID_BTN_POST", AppModel.instance().currScrInfo());
                                clickedPost = true;
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_VIDEO_EDITING:
                            ASUtils.findAndClick("ID_BTN_NEXT", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_POST_VIDEO_PUBLICLY:
                            if(ASUtils.findAndClick("ID_BTN_POST_NOW", AppModel.instance().currScrInfo())) {
                                return;
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_SYNC_CONTACTS:
                            ASUtils.findAndClick("ID_BTN_DONT_ALLOW", AppModel.instance().currScrInfo());
                            break;
                        default:
                            break;
                    }

                    Utils.delayRandom(1000, 3000);
                } catch (Exception e) {
                    LOG.printStackTrace(TAG, e);
                }
            }

            if(clickedPost) {
                LOG.I(TAG, "updateVideoStatus: " + DBPApi.instance().updateVideoStatus(videoId, "used"));
                AppModel.instance().currPackage().getCloneInfo().setLastUploadTime(current);
            }
        }
    }

    public void getClone() {
        JSONObject retval = DBPApi.instance().getClone();
        LOG.I(TAG, "retval: " + retval);
        try {
            if (retval != null && retval.getBoolean("success")) {
                AppModel.instance().currPackage().setCloneInfo(retval.getString("clone_info"));
                AppModel.instance().currPackage().getCloneInfo().setStatus(CloneInfo.CLONE_STATUS_GETTING);
            }
        } catch (Exception e){}
    }

    public void getActions() {
        JSONObject retval = DBPApi.instance().getActions(AppModel.instance().currPackage().getCloneInfo().username());
        LOG.I(TAG, "retval: " + retval);
        try {
            if (retval != null && retval.getBoolean("success")) {
                AppModel.instance().currPackage().setActions(retval.getJSONArray("actions"));
            }
        } catch (JSONException e){}
    }

    private String getRandomVideoDescribe() {
        String[] listDes = new String[] {
                "Hi everyone!"
                ,"Are you ready ...?"
                ,"Love you <3"
                ,"Boom boom..."
                ,"Good morning!"
                ,"Good afternoon~"
                ,"Yeah yeah."
                ,"Being happy :))"
                ,"So sad ..."
                ,"Tell me something .."
                ,"Say something???"
                ,"Say oh yeah :v"
                ,"It's ok????"
                ,"Good?"
                ,"Let me see your feeling? :)))))))))))"
                ,"Don't leave me :(("
                ,"Love me????"
                ,"Just look in my eyes? :D"
                ,"Wowwwwwwwwwwwwwww ...."
                ,"Oh my god"
                ,"Beautiful girl?"
                ,"Cute or something????"
                ,"Don't leave me alone..."
                ,"Feeling sad ..."
                ,"So deep :)))))"
                ,"Yessss .... again... yes..."
                ,"Why? tell me why?..."
                ,"Tried my best~"
                ,"Don't let me say bad words ..."
                ,"Broken heart ... :(("};
        
        return listDes[new Random().nextInt(listDes.length)] +  " #trending #xuhuong.";
    }

    public static String getTiktokNumericId(String tiktokId) throws  IOException{
        OkHttpClient client = new OkHttpClient();
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                .url("https://www.tiktok.com/@" + tiktokId)
                .method("GET", null)
                .addHeader("authority", "www.tiktok.com")
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("accept-language", "en-US,en;q=0.9,vi;q=0.8")
                .addHeader("cache-control", "max-age=0")
                .addHeader("referer", "https://www.tiktok.com/live")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "document")
                .addHeader("sec-fetch-mode", "navigate")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-user", "?1")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36")
                .build();
        com.squareup.okhttp.Response response = client.newCall(request).execute();

        String uid = null;
        if (response.code() == 200) {
            try {
                String source = response.body().string();
                List<String> listUids = Utils.regex(source, "(?<=\\\"authorId\\\":\\\")(.*?)(?=\\\")");
                return listUids.get(0);
            } catch (Exception e) { }
        }
        return uid;
    }


    public static String getTiktokId() {
        String id = null;
        try {
            String content = RootHelper.execute(String.format("cat /data/data/%s/shared_prefs/aweme_user.xml", Constants.REG_PACKAGE));
            List<String> userNames = Utils.regex(content, "(?<=&quot;unique_id&quot;:&quot;)(.*?)(?=&quot;)");
            if (userNames != null && userNames.size() > 0) {
                id = userNames.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    static private String getRandNumber(int length) {
        int base = (int)Math.pow(10,length - 1);
        return String.valueOf(new Random().nextInt(base * 9) + base);
    }

    public static Map<String, Rect> scanPuzzlePiece(String inputImage) {
        try {
            Mat src = Imgcodecs.imread(inputImage);
            if (src.empty()) {
                LOG.W(TAG, "empty image");
                return null;
            } else {
                LOG.D(TAG, "Load image success");
                //Converting the source image to binary

                float threshold = 100;
                Mat srcGray = new Mat();
                Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.blur(srcGray, srcGray, new Size(3, 3));
                Mat cannyOutput = new Mat();
                Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);


                Imgcodecs.imwrite(AppDefines.PDT_FOLDER + "screen_binary.png", cannyOutput);

                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchey = new Mat();
                Imgproc.findContours(cannyOutput, contours, hierarchey, Imgproc.RETR_TREE,
                        Imgproc.CHAIN_APPROX_SIMPLE);
                Mat draw = Mat.zeros(src.size(), CvType.CV_8UC3);
                MatOfPoint2f[] contoursPoly = new MatOfPoint2f[contours.size()];
                Rect[] boundRect = new Rect[contours.size()];

                for (int i = 0; i < contours.size(); i++) {
                    Imgproc.drawContours(draw, contours, i, new Scalar(255, 255, 255), 2, Imgproc.LINE_8,
                            hierarchey, 2, new Point());
                }

                List<Rect> filtered_list = new ArrayList<>();
                for (int i = 0; i < contours.size(); i++) {
                    contoursPoly[i] = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
                    boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));

                    if (boundRect[i].width > 50 && boundRect[i].width < 200 &&
                            boundRect[i].height > 50 && boundRect[i].height < 200) {

                        filtered_list.add(boundRect[i]);

                        Scalar green_color = new Scalar(0, 255, 0);
                        Imgproc.rectangle(draw, boundRect[i].tl(), boundRect[i].br(), green_color, 2);
                        LOG.D(TAG, "boundRect[i]: " + boundRect[i]);
                    } else {
//                        Imgproc.rectangle(draw, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 0, 255), 2);
                    }
                }

                Map<String, Rect> result = null;
                for (int m = 0; m < filtered_list.size(); m++) {
                    for (int n = m + 1; n < filtered_list.size(); n++) {
                        Rect rectM = filtered_list.get(m);
                        Rect rectN = filtered_list.get(n);
                        if (Math.abs((rectM.y + rectM.height / 2) - (rectN.y + rectN.height / 2)) < 20) {
                            if (result == null) {
                                result = new HashMap<String, Rect>();
                                result.put("start", rectM.x < rectN.x ? rectM : rectN);
                                result.put("end", rectM.x < rectN.x ? rectN : rectM);
                            } else {
                                try {
                                    Rect oldStart = result.get("start");
                                    Rect oldEnd = result.get("end");
                                    if ((Math.abs((rectM.x + rectM.width / 2) - (rectN.x + rectN.width / 2)))
                                            > (Math.abs((oldEnd.x + oldEnd.width / 2) - (oldStart.x + oldStart.width / 2)))) {
                                        result.put("start", rectM.x < rectN.x ? rectM : rectN);
                                        result.put("end", rectM.x < rectN.x ? rectN : rectM);
                                    }
                                } catch (Exception e) {
                                    LOG.printStackTrace(TAG, e);
                                }
                            }
                        }
                    }
                }
                LOG.D(TAG, "result: " + result);

                Imgcodecs.imwrite(AppDefines.PDT_FOLDER + "screen_contours.png", draw);
                return result;
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return null;
        }
    }
}

