package pdt.autoreg.app.services;

import android.content.Context;

import pdt.autoreg.accessibility.ASInterface;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pdt.autoreg.accessibility.ASUtils;
import pdt.autoreg.accessibility.LOG;
import pdt.autoreg.app.AppDefines;
import pdt.autoreg.app.api.DBPApi;
import pdt.autoreg.app.model.AppModel;
import pdt.autoreg.app.common.Utils;
import pdt.autoreg.app.helpers.ProxyHelper;
import pdt.autoreg.devicefaker.helper.RootHelper;
import pdt.autoreg.devicefaker.Constants;

public class TiktokAppService extends BaseService {
    private static String TAG = "TiktokAppService";

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
        LOG.D(TAG, "mainOperations");
        Utils.showToastMessage(this, "Start NEW CYCLE");

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

                        if(!ASUtils.findByTextOrDes(username, AppModel.instance().currScrInfo())) {
                            if (ASUtils.findAndClick("ID_EDT_EMAIL_USERNAME", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText(username, null, true);
                            }
                        } else {
                            if (ASUtils.findAndClick("ID_EDT_PASSWORD", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText(password, null, true);
                            }

                            ASUtils.findAndClick("ID_BTN_LOGIN", AppModel.instance().currScrInfo());
                        }
                    }
                default:
                    break;

            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    private void resetNetwork() {
        switch (AppModel.instance().networkType()) {
            case AppDefines.PROXY_NETWORK:
                ProxyHelper.stopProxySwitch();
                break;
            case AppDefines.SSHTUNNEL_NETWORK:
//                closeTunnel();
                break;
            case AppDefines.MOBILE_NETWORK:
                RootHelper.disableAirplane();
                break;
            default:
                break;
        }
    }

    private boolean regClone() {
        LOG.D(TAG, "regClone");
        boolean result = false;

        Utils.delay(2000);

        String prevScreenID = new String();
        int loopUnknownCount = 0;

        for (int i = 0; i < 1000000; i++) {
            if (detectScreen((false))) {
                try {
                    switch (AppModel.instance().currScrID()) {
                        case AppDefines.SCREEN_TIKTOK_HOME_FOR_YOU:
                            ASUtils.findAndClick("ID_BTN_PROFILE", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK:
                            ASUtils.findAndClick("ID_USE_PHONE_OR_EMAIL", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_BIRTHDAY:
                            if(ASUtils.findAndClick("ID_ENTER_BIRTHDAY_FIELD", AppModel.instance().currScrInfo())) {
                                Utils.delay(2000);
                                ASInterface.instance().inputText("March 15, 1994", null, false);
                            } else {
                                ASUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().currScrInfo());
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_PHONE:
                            ASUtils.findAndClick("ID_BTN_EMAIL", AppModel.instance().currScrInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_EMAIL:
                            if(ASUtils.findAndClick("ID_INPUT_EMAIL_FIELD", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText("testme" + new Random().nextInt(1000) + "@inboxbear.com", null, false);

                                // hide softkeyboard
                                RootHelper.execute("input keyevent 111");
                            } else {
                                ASUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().currScrInfo());
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_VERIFY_CAPTCHA:
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD:
                            if(ASUtils.findAndClick("ID_ENTER_PASSWORD_FIELD", AppModel.instance().currScrInfo())) {
                                Utils.delay(1000);
                                ASInterface.instance().inputText("PDt1794@#", null, false);
                            } else {
                                ASUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().currScrInfo());
                            }
                            break;
                        default:
                            if (!ASInterface.instance().getCurrentForgroundPkg().equals(Constants.REG_PACKAGE)) {
                                ASInterface.instance().openPackage(Constants.REG_PACKAGE);
                            }
                            break;
                    }
                } catch (Exception e) {
                    LOG.printStackTrace(TAG, e);
                }
            }
            if (prevScreenID == null || AppModel.instance().currScrID() == null || AppModel.instance().currScrID().equals(prevScreenID)) {
                loopUnknownCount++;
            } else {
                loopUnknownCount = 0;
            }

//            if (loopUnknownCount >= 20) {
//                LOG.E(TAG, " Loop unknown");
//                return result;
//            }
            prevScreenID = AppModel.instance().currScrID();
            Utils.delayRandom(4000, 5000);
        }
        return result;
    }

    public void getClone() {
        JSONObject retval = DBPApi.instance().getClone();
        LOG.I(TAG, "retval: " + retval);
        try {
            if (retval != null && retval.getBoolean("success")) {
                AppModel.instance().currPackage().setCloneInfo(retval.getString("clone_info"));
            }
        } catch (JSONException e){}
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
}

