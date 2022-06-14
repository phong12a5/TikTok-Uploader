package pdt.autoreg.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import pdt.autoreg.app.AppDefines;

import com.chilkatsoft.CkEmail;
import com.chilkatsoft.CkEmailBundle;
import com.chilkatsoft.CkHttp;
import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkMessageSet;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.GrayscaleTransformation;
import jp.wasabeef.picasso.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.ToonFilterTransformation;
import pdt.autoreg.cgblibrary.CGBInterface;
import pdt.autoreg.cgblibrary.CGBUtils;
import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.devicefaker.Constants;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static pdt.autoreg.app.AppDefines.AUTOREG_DATA_FOLDER;

public class TiktokAppService extends BaseService {
    private static String TAG = "TiktokAppService";

    List<String> m_listFirstName = new ArrayList<>();
    List<String> m_listLastName = new ArrayList<>();
    List<JSONObject> m_listSSH = new ArrayList<>();
    String m_country = AppDefines.COUNTRY_VIETNAM;
    boolean resetPackage = true;
    boolean useTempMailInsteadOf = false;
    private JSONObject hotmailObj = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void mainOperations() {
        LOG.D(TAG, "mainOperations");
        showToastMessage("Start NEW CYCLE");

        AdvertisingIdClient.Info idInfo;

        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(App.getContext());
            String advertisingId = idInfo.getId();
            showToastMessage("advertisingId: " + advertisingId);
            //do sth with the id

//            SafetyNet.getClient(this).attest(nonce, API_KEY)
//                    .addOnSuccessListener(this,
//                            new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
//                                @Override
//                                public void onSuccess(SafetyNetApi.AttestationResponse response) {
//                                    // Indicates communication with the service was successful.
//                                    // Use response.getJwsResult() to get the result data.
//                                }
//                            })
//                    .addOnFailureListener(this, new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // An error occurred while communicating with the service.
//                            if (e instanceof ApiException) {
//                                // An error with the Google Play services API contains some
//                                // additional details.
//                                ApiException apiException = (ApiException) e;
//                                // You can retrieve the status code using the
//                                // apiException.getStatusCode() method.
//                            } else {
//                                // A different, unknown type of error occurred.
//                                Log.d(TAG, "Error: " + e.getMessage());
//                            }
//                        }
//                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (changeIp()) {
//            generateCloneInfo();
            if(regClone()){
                // Submit clone
                try {
                    String username = getTiktokId();
                    String password = "Pdt1794@#";
                    String uid = null;
                    try {
                        uid = getTiktokNumericId(username);
                    } catch (IOException e) {
                        uid = getTiktokNumericId(username);
                    }

                    CloneSubmitter.submitClone(getApplicationContext(), AppDefines.TIKTOK_APPNAME, uid, username, password, null, null, null, null, null, false, null);

                    // Update Counter
                    Intent showCounter = new Intent();
                    showCounter.setAction(FloatingWindow.ACTION_REGISTER_SUCCESS);
                    App.getContext().sendBroadcast(showCounter);
                } catch (Exception e) {

                }
            }

            execute("pm clear " + Constants.REG_PACKAGE);
        }

        resetNetwork();
    }

    private boolean changeIp() {
        boolean success = false;
        switch (AppModel.instance().networkType()) {
            case AppDefines.MOBILE_NETWORK:
                onOffAirPlanemode();
                delay(5000);
                success = true;
                break;
            case AppDefines.PROXY_NETWORK:
                break;
            case AppDefines.SSHTUNNEL_NETWORK:
                break;
            default:
                break;
        }

        showToastMessage("public ip: " + getPuclicIP());
        return success;
    }
    private void resetNetwork() {
        switch (AppModel.instance().networkType()) {
            case AppDefines.PROXY_NETWORK:
                stopProxySwitch();
                break;
            case AppDefines.SSHTUNNEL_NETWORK:
//                closeTunnel();
                break;
            case AppDefines.MOBILE_NETWORK:
                disableAirplane();
                break;
            default:
                break;
        }
    }

    private boolean checkGrantedPermission(String packageName, String per) {
        PackageManager pm = getPackageManager();
        int hasPerm = pm.checkPermission(per, packageName);
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            // do stuff
            LOG.D(TAG, "packageName: " + packageName + " -- requestedPerm: " + per + "-- GRANTED");
            return true;
        } else {
            LOG.E(TAG, "packageName: " + packageName + " -- requestedPerm: " + per + "-- DENIED");
            return false;
        }
    }

    private boolean regClone() {
        LOG.D(TAG, "regClone");
        boolean result = false;

        delay(2000);

        String prevScreenID = new String();
        int loopUnknownCount = 0;

        for (int i = 0; i < 1000000; i++) {
            if (detectScreen((false))) {
                try {
                    switch (AppModel.instance().getCurrentScreenID()) {
                        case AppDefines.SCREEN_TIKTOK_HOME_FOR_YOU:
                            CGBUtils.findAndClick("ID_BTN_PROFILE", AppModel.instance().getCurrentScreenInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_FOR_TIKTOK:
                            CGBUtils.findAndClick("ID_USE_PHONE_OR_EMAIL", AppModel.instance().getCurrentScreenInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_BIRTHDAY:
                            if(CGBUtils.findAndClick("ID_ENTER_BIRTHDAY_FIELD", AppModel.instance().getCurrentScreenInfo())) {
                                delay(2000);
                                CGBInterface.getInstance().inputText("March 15, 1994", null, false);
                            } else {
                                CGBUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().getCurrentScreenInfo());
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_PHONE:
                            CGBUtils.findAndClick("ID_BTN_EMAIL", AppModel.instance().getCurrentScreenInfo());
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_EMAIL:
                            if(CGBUtils.findAndClick("ID_INPUT_EMAIL_FIELD", AppModel.instance().getCurrentScreenInfo())) {
                                delay(1000);
                                CGBInterface.getInstance().inputText("testme" + new Random().nextInt(1000) + "@inboxbear.com", null, false);

                                // hide softkeyboard
                                execute("input keyevent 111");
                            } else {
                                CGBUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().getCurrentScreenInfo());
                            }
                            break;
                        case AppDefines.SCREEN_TIKTOK_VERIFY_CAPTCHA:
                            break;
                        case AppDefines.SCREEN_TIKTOK_SIGN_UP_CREATE_PASSWORD:
                            if(CGBUtils.findAndClick("ID_ENTER_PASSWORD_FIELD", AppModel.instance().getCurrentScreenInfo())) {
                                delay(1000);
                                CGBInterface.getInstance().inputText("PDt1794@#", null, false);
                            } else {
                                CGBUtils.findAndClick("ID_NEXT_BTN", AppModel.instance().getCurrentScreenInfo());
                            }
                            break;
                        default:
                            if (!CGBInterface.getInstance().getCurrentForgroundPkg().equals(Constants.REG_PACKAGE)) {
                                CGBInterface.getInstance().openPackage(Constants.REG_PACKAGE);
                            }
                            break;
                    }
                } catch (Exception e) {
                    LOG.printStackTrace(TAG, e);
                }
            }
            if (prevScreenID == null || AppModel.instance().getCurrentScreenID() == null || AppModel.instance().getCurrentScreenID().equals(prevScreenID)) {
                loopUnknownCount++;
            } else {
                loopUnknownCount = 0;
            }

//            if (loopUnknownCount >= 20) {
//                LOG.E(TAG, " Loop unknown");
//                return result;
//            }
            prevScreenID = AppModel.instance().getCurrentScreenID();
            delayRandom(4000, 5000);
        }
        return result;
    }

    private String generateGmail() {
        String email = new String();

        try {

            int lengthOfChars = new Random().nextInt(4) + 6;
            for (int i = 0; i < lengthOfChars; i++) {
                email += String.valueOf((char) (random.nextInt(22) + 'a'));
            }

            int lengthOfNumber = new Random().nextInt(4) + 3;
            for (int i = 0; i < lengthOfNumber; i++) {
                email += String.valueOf((char) (random.nextInt(9) + '0'));
            }

            email += "@gmail.com";
            email = VNCharacterUtils.removeAccent(email).replace(" ", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return email;
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
                List<String> listUids = regex(source, "(?<=\\\"authorId\\\":\\\")(.*?)(?=\\\")");
                return listUids.get(0);
            } catch (Exception e) { }
        }
        return uid;
    }


    public static String getTiktokId() {
        String id = null;
        try {
            String content = execute("cat /data/data/com.ss.android.ugc.trill/shared_prefs/aweme_user.xml");
            List<String> userNames = regex(content, "(?<=&quot;unique_id&quot;:&quot;)(.*?)(?=&quot;)");
            if (userNames != null && userNames.size() > 0) {
                id = userNames.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static String getFacebookVeriCode(String email, String password) {
        //std::string WebAPI::getFacebookCodeFromHotmail(JNIEnv* env, const char * email, const char * password) const {
        //    PLOGD(env, "email: %s -- passwd: %s", email, password);
        //    return getCodeFromImap(env, "outlook.office365.com", 993, "Inbox", "Facebook", email, email, password);
        //}
        String code = null;
        LOG.D(TAG, String.format("email: %s -- passwd: %s", email, password));
        CkImap imap = new CkImap();

        imap.put_Port(993);
        imap.put_Ssl(true);
        //outlook: "outlook.office365.com"
        boolean success = imap.Connect("outlook.office365.com");
        if (!success)
        {
            LOG.E(TAG, "imap.Connect: " + imap.lastErrorText());
            return code;
        }
        // Send the non-standard ID command...
        imap.sendRawCommand("ID (\"GUID\" \"1\")");
        if (!imap.get_LastMethodSuccess())
        {
            LOG.E(TAG, "imap.sendRawCommand: " + imap.lastErrorText());
            return code;
        }

        // Login
        success = imap.Login(email, password);
        if (!success) {
            LOG.E(TAG, "imap.Login: " + imap.lastErrorText());
            return "";
        }

        LOG.D(TAG, "Login Success!");

        //outlook: "Inbox"
        success = imap.SelectMailbox("Inbox");
        if (!success) {
            LOG.E(TAG, "imap.SelectMailbox: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "SelectMailbox success!");
        }

        // We can choose to fetch UIDs or sequence numbers.
        CkMessageSet messageSet;
        boolean fetchUids = true;
        // Get the message IDs of all the emails in the mailbox
        messageSet = imap.Search("ALL", fetchUids);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.Search: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "Search ALL mail box success!");
        }

        // Fetch the emails into a bundle object:
        CkEmailBundle bundle = imap.FetchBundle(messageSet);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.FetchBundle: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "FetchBundle success!");
        }

        // Loop over the bundle and display the FROM and SUBJECT of each.
        int i = 0;
        int numEmails = bundle.get_MessageCount();
        while (i < numEmails) {
            CkEmail ckEmail = bundle.GetEmail(i);
            LOG.D(TAG, "email from: " + ckEmail.ck_from());
            LOG.D(TAG, "email to: " + ckEmail.getToAddr(0));
            LOG.D(TAG, "email subject: " + ckEmail.subject());

            if (ckEmail.ck_from().equals("Facebook")||
                    ckEmail.getToAddr(0).equals(email)) {
                LOG.D(TAG, "body: " + ckEmail.body());
                Pattern pattern = Pattern.compile("\\d{4,7}");
                Matcher matcher = pattern.matcher(ckEmail.subject());

                while (matcher.find()) {
                    code = matcher.group();
                    LOG.D(TAG, "code: " + code);
                    // s now contains "BAR"
                }
            }
            i = i + 1;
        }

        // Expunge and close the mailbox.
        success = imap.ExpungeAndClose();

        // Disconnect from the IMAP server.
        success = imap.Disconnect();
        LOG.D(TAG, "code: " +  code);
        return code;
    }

    public static String getTiktokCode(String email) {
        String code = null;
        LOG.D(TAG, String.format("email: %s", email));
        CkImap imap = new CkImap();

        imap.put_Port(993);
        imap.put_Ssl(true);

        boolean success = imap.Connect("imap.yandex.com");
        if (!success)
        {
            LOG.E(TAG, "imap.Connect: " + imap.lastErrorText());
            return code;
        }
        // Send the non-standard ID command...
        imap.sendRawCommand("ID (\"GUID\" \"1\")");
        if (!imap.get_LastMethodSuccess())
        {
            LOG.E(TAG, "imap.sendRawCommand: " + imap.lastErrorText());
            return code;
        }

        // Login
        success = imap.Login("admin@bobolala.xyz", "ecstipxneiopwyvx");
        if (!success) {
            LOG.E(TAG, "imap.Login: " + imap.lastErrorText());
            return "";
        }

        LOG.D(TAG, "Login Success!");

        //outlook: "Inbox"
        success = imap.SelectMailbox("Inbox");
        if (!success) {
            LOG.E(TAG, "imap.SelectMailbox: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "SelectMailbox success!");
        }

        // We can choose to fetch UIDs or sequence numbers.
        CkMessageSet messageSet;
        boolean fetchUids = true;
        // Get the message IDs of all the emails in the mailbox
        messageSet = imap.Search("ALL", fetchUids);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.Search: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "Search ALL mail box success!");
        }

        // Fetch the emails into a bundle object:
        CkEmailBundle bundle = imap.FetchBundle(messageSet);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.FetchBundle: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "FetchBundle success!");
        }

        // Loop over the bundle and display the FROM and SUBJECT of each.
        int i = 0;
        int numEmails = bundle.get_MessageCount();
        while (i < numEmails) {
            CkEmail ckEmail = bundle.GetEmail(i);
            LOG.D(TAG, "email from: " + ckEmail.ck_from());
            LOG.D(TAG, "email to: " + ckEmail.getToAddr(0));
            LOG.D(TAG, "email subject: " + ckEmail.subject());

            if (ckEmail.ck_from().contains("TikTok")||
                    ckEmail.getToAddr(0).equals(email)) {
                LOG.D(TAG, "body: " + ckEmail.body());
                Pattern pattern = Pattern.compile("\\d{4,7}");
                Matcher matcher = pattern.matcher(ckEmail.body());

                while (matcher.find()) {
                    code = matcher.group();
                    LOG.D(TAG, "code: " + code);
                    // s now contains "BAR"
                }
            }
            i = i + 1;
        }

        // Expunge and close the mailbox.
        success = imap.ExpungeAndClose();

        // Disconnect from the IMAP server.
        success = imap.Disconnect();
        LOG.D(TAG, "code: " +  code);
        return code;
    }

    private boolean doUploadAvatar() {
        LOG.D(TAG, "doUploadAvatar");
        deletePicInPicFolderRecursive(new File(AppDefines.PICTURES_FOLDER));
        try {
            String avtImageCloudPath = "/Avatar/Avartar/" + new Random().nextInt(30194) + ".jpg";
            String avtImageLocalPath = AppDefines.AUTOREG_FOLDER + "avatar.png";
            DropboxAPI.downloadFileFromDropbox(avtImageCloudPath, AppDefines.AUTOREG_FOLDER + "avatar.png");
            File avtImageLocalFile = new File(avtImageLocalPath);
            if (avtImageLocalFile != null && avtImageLocalFile.exists() && avtImageLocalFile.isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(avtImageLocalPath);
                saveImageToExternal(avtImageLocalPath, bitmap);
                fakeImage(avtImageLocalPath);
                boolean clickedSave = false;
            } else {
                LOG.E("doUploadAvatar", "Avatar image not exist!");
            }
        } catch (Exception e) {
            LOG.E("doUploadAvatar", e.getMessage());
        }
        return false;
    }

    private final Random random = new Random();

    public int getRandom(int start,int end){
        return start + (random.nextInt(end - start));
    }


    public static void acceptPermission(String permission, String packageName) {
        String command = "pm grant " + packageName + " " + permission;
        execute(command);
    }

    private void revokePermission(String permission, String packageName) {
        String command = "pm revoke " + packageName + " " + permission;
        execute(command);
    }

    /*
    private void generateCloneInfo() {
        LOG.D(TAG, "generateCloneInfo country: " + m_country);
        JSONObject ressult = new JSONObject();
        try {
            // Get country
            ressult.put("country", m_country);

            // Gen email
            if (AppDefines.USE_TEMPMAIL) {
                ressult.put("email", NadaMailAPI.generateRandomEmail());
            }

            // Gen gender
            boolean isMale = true;
            if (new Random().nextInt(2) != 0)
                ressult.put("gender", "male");
            else {
                isMale = false;
                ressult.put("gender", "female");
            }

            String fullName = getRandomName(isMale? "male" : "female", m_country);
            LOG.D(TAG, "fullName: " + fullName);
            String firstName = null;
            String lastName = null;
            if(fullName == null) {
                if (m_listFirstName == null || m_listFirstName.isEmpty()) {
                    getFirstName();
                }
                if (m_listLastName == null || m_listLastName.isEmpty()) {
                    getLastName();
                }

                // Gen First name
                if (m_listFirstName != null && !m_listFirstName.isEmpty()) {
                    firstName = m_listFirstName.get(new Random().nextInt(m_listFirstName.size()));
                } else {
                    firstName = "Phuong";
                }

                // Gen last name
                if (m_listLastName != null && !m_listLastName.isEmpty()) {
                    lastName = m_listLastName.get(new Random().nextInt(m_listLastName.size())) + " " + m_listFirstName.get(new Random().nextInt(m_listFirstName.size()));
                } else {
                    lastName = "Đặng";
                }
            } else {
                String[] params= fullName.split(" ",2);
                if(params.length > 1) {
                    firstName = params[0];
                    lastName = params[1];
                } else {
                    firstName = fullName;
                    lastName = fullName;
                }
            }

            ressult.put("firstname", firstName);
            ressult.put("lastname", lastName);

            // Gen password
            String password = null;
            Random random = new Random();
            String[] specialChar = new String[]{"@","#","!","*", String.valueOf(random.nextInt(1000))};
            password = String.valueOf((char) (random.nextInt(26) + 'A'));
            int passLenth = random.nextInt(3) + 10;
            for (int i = 0; i < passLenth; i++) {
                if(random.nextInt(6) == 5) {
                    password += specialChar[random.nextInt(specialChar.length)];
                } else {
                    password += String.valueOf((char) (random.nextInt(22) + 'a'));
                }
            }

            password = VNCharacterUtils.removeAccent(password).replace(" ", "");
            ressult.put("password", password);

            String phoneNumber = getRandomPhoneNumber(m_country);
            ressult.put("phone_number", phoneNumber);
            FileUtils.writeStringToFile(new File(Constants.AUTOREG_DATA_FOLDER + "phone_number.txt"), phoneNumber);

            String gmail = generateGmail();
            ressult.put("gmail", gmail);


            ressult.put("isRegistering", true);
            LOG.D(TAG, "generateCloneInfo: " + ressult);
            m_packageData.setCloneInfo(ressult);
        } catch (Exception e) {
            LOG.E("generateCloneInfo", e.getMessage());
        }
    }
     */

    private static void saveOutput(String uid, String passWord, final String email, final String passmail, final String cookie, final String twoFa) {
        LOG.D("saveOutput", "uid: " + uid + " -- passWord:" + passWord);
        try {
            File file = new File(AUTOREG_DATA_FOLDER + "FBCLoneException.json");
            String fileData = "";
            if (file != null && file.exists() && file.isFile()) {
                fileData = FileUtils.readFileToString(file);
            }

            fileData += (uid + "|" +
                    passWord + "|" +
                    email + "|" +
                    passmail + "|" +
                    cookie + "|" +
                    twoFa + "\n");
            FileUtils.writeStringToFile(file, fileData);
        } catch (Exception ex) {
            LOG.E("saveOutput", ex.getMessage());
        }
    }

    private void changeIdentify() {
        try {
            Intent intent = new Intent();
            intent.setAction("com.applisto.appcloner.api.action.NEW_IDENTITY");
            intent.putExtra("package_name", Constants.REG_PACKAGE);
            intent.putExtra("delete_app_data", true);
            intent.setPackage("com.applisto.appcloner");
            App.getContext().sendBroadcast(intent);
        } catch (Exception e) {
            LOG.E(TAG, "changeIdentify exception: " + e);
        }
    }

    private void onOffAirPlanemode() {
        enableAirplane();
        delay(1000);
        disableAirplane();
    }

    private void getFirstName() {
        BufferedReader reader = null;

        if (m_listFirstName == null)
            m_listFirstName = new ArrayList<String>();

        try {
            String mLine;
            if (AppDefines.US_CLONE) {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("data/firstname_us_male.txt")));
            } else {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("data/firstname_vn.txt")));
            }
            while ((mLine = reader.readLine()) != null) {
                m_listFirstName.add(mLine);
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void getLastName() {
        BufferedReader reader = null;
        if (m_listLastName == null)
            m_listLastName = new ArrayList<String>();
        try {
            String mLine;
            if (AppDefines.US_CLONE) {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("data/lastname_us.txt")));
            } else {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("data/lastname_vn.txt")));
            }
            while ((mLine = reader.readLine()) != null) {
                m_listLastName.add(mLine);
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private String getRandomPhoneNumber() {
        String phoneNumber = null;
        try {
            String[] prefixList = new String[]{"90", "91", "92", "93", "94", "96", "97", "98", "99"};
            if (prefixList != null) {
                phoneNumber = prefixList[new Random().nextInt(prefixList.length)] + (new Random().nextInt(9000000) + 1000000);
            }
        } catch (Exception e) { }
        LOG.D(TAG, "getRandomPhoneNumber: " + phoneNumber);
        return phoneNumber;
    }


    private String getRandomPhoneNumber(String country) {
        LOG.D(TAG, "getMobileRandom country: " + country);
        String mobileNumber = new String();
        String countryCode = null;
        switch (country) {
            case AppDefines.COUNTRY_VIETNAM: {
                countryCode = AppDefines.CODE_VIETNAM;
                String[] networkcarrier = {"90", "91", "92", "93", "94", "96", "97", "98", "99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_KOREA: {
                countryCode = AppDefines.CODE_KOREA;
                String[] networkcarrier = {"16", "17", "18", "19"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);

            }
            break;
            case AppDefines.COUNTRY_SINGAPORE: {
                countryCode = AppDefines.CODE_SINGAPORE;
                String[] networkcarrier = {"8", "9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_MALAYSIA: {
                countryCode = AppDefines.CODE_MALAYSIA;
                String[] networkcarrier = {"12", "13", "14", "16", "17", "18", "19"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_TAIWAN: {
                countryCode = AppDefines.CODE_TAIWAN;
                String[] networkcarrier = {"96"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_HONGKONG: {
                countryCode = AppDefines.CODE_HONGKONG;
                String[] networkcarrier = {"51", "54"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;
            case AppDefines.COUNTRY_LAOS: {
                countryCode = AppDefines.CODE_LAOS;
                String[] networkcarrier = {"202", "207", "205", "209"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_MYANMA: {
                countryCode = AppDefines.CODE_MYANMA;
                String[] networkcarrier = {"96", "97", "99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_THAILAND: {
                countryCode = AppDefines.CODE_THAILAND;
                String[] networkcarrier = {"91", "92", "61", "64"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_PHILIPINS: {
                countryCode = AppDefines.CODE_PHILIPINS;
                String[] networkcarrier = {"90", "91", "92", "93", "94", "95", "96", "97", "98", "99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_ISREAL: {
                countryCode = AppDefines.CODE_ISREAL;
                String[] networkcarrier = {"5"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_UNITED_KINGDOM: {
                countryCode = AppDefines.CODE_UNITED_KINGDOM;
                String[] networkcarrier = {"71", "73", "74", "75", "77", "78", "79"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_UNITED_STATES: {
                countryCode = AppDefines.CODE_UNITED_STATES;
                String[] networkcarrier = {"518", "410", "404", "207", "512", "225", "701", "208", "617", "775", "843", "202"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_AUSTRALIA: {
                countryCode = AppDefines.CODE_AUSTRALIA;
                String[] networkcarrier = {"4", "5"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_RUSSIA: {
                countryCode = AppDefines.CODE_RUSSIA;
                String[] networkcarrier = {"978"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_POLAND: {
                countryCode = AppDefines.CODE_POLAND;
                String[] networkcarrier = {"45", "50", "51", "53", "57", "60", "66", "69", "72", "73", "78", "79", "88"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_SAUDI_ARABIA: {
                countryCode = AppDefines.CODE_SAUDI_ARABIA;
                String[] networkcarrier = {"50", "51", "53", "54", "55", "56", "57", "58", "59"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);

            }
            break;
            case AppDefines.COUNTRY_BRAZIL: {
                countryCode = AppDefines.CODE_BRAZIL;
                String[] networkcarrier = {"11", "21"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + "9" + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_ARGENTINA: {
                countryCode = AppDefines.CODE_ARGENTINA;
                String[] networkcarrier = {"911"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_COLOMBIA: {
                countryCode = AppDefines.CODE_COLOMBIA;
                String[] networkcarrier = {"30", "31", "32"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_GERMANY: {
                countryCode = AppDefines.CODE_GERMANY;
                String[] networkcarrier = {"151", "152", "155", "157", "159"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_EGYPT: {
                countryCode = AppDefines.CODE_EGYPT;
                String[] networkcarrier = {"10", "11", "12"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_SPAIN: {
                countryCode = AppDefines.CODE_SPAIN;
                String[] networkcarrier = {"6", "7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_GHANA: {
                countryCode = AppDefines.CODE_GHANA;
                String[] networkcarrier = {"20","50","23","24","54","55","59","26","56","27","57","28"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_INDONESIA: {
                String[] networkcarrier = {"811", "818"};
                countryCode = AppDefines.CODE_INDONESIA;
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);
            }
            break;
            case AppDefines.COUNTRY_INDIA: {
                countryCode = AppDefines.CODE_INDIA;
                String[] networkcarrier = {"7", "8", "9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);
            }
            break;
            case AppDefines.COUNTRY_IRAN: {
                countryCode = AppDefines.CODE_IRAN;
                String[] networkcarrier = {"91"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_MALAWI: {
                countryCode = AppDefines.CODE_MALAWI;
                String[] networkcarrier = {"88","99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_MEXICO: {
                countryCode = AppDefines.CODE_MEXICO;
                String[] networkcarrier = {"1"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);
            }
            break;
            case AppDefines.COUNTRY_NETHERLANDS: {
                countryCode = AppDefines.CODE_NETHERLANDS;
                String[] networkcarrier = {"6"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_NORWAY: {
                countryCode = AppDefines.CODE_NORWAY;
                String[] networkcarrier = {"4", "9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(9000000) + 1000000);
            }
            break;
            case AppDefines.COUNTRY_NEPAL: {
                countryCode = AppDefines.CODE_NEPAL;
                String[] networkcarrier = {"98"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_OMAN: {
                countryCode = AppDefines.CODE_OMAN;
                String[] networkcarrier = {"9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);
            }
            break;
            case AppDefines.COUNTRY_PAKISTAN: {
                countryCode = AppDefines.CODE_PAKISTAN;
                String[] networkcarrier = {"30","31","32","33","34"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
            case AppDefines.COUNTRY_TURKEY: {
                countryCode = AppDefines.CODE_TURKEY;
                String[] networkcarrier = {"50", "53", "54", "55"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(900000000) + 100000000);
            }
            break;
            case AppDefines.COUNTRY_UKRAINE: {
                countryCode = AppDefines.CODE_UKRAINE;
                String[] networkcarrier = {"9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;

            // ---------------------------------------
            case AppDefines.COUNTRY_ANGOLA: {
                countryCode = AppDefines.CODE_ANGOLA;
                String[] networkcarrier = {"91","92"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_AFGHANISTAN:
            {
                countryCode = AppDefines.CODE_AFGHANISTAN;
                String[] networkcarrier = {"75", "77","78"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_BANGLADESH:
            {
                countryCode = AppDefines.CODE_BANGLADESH;
                String[] networkcarrier = {"11", "12","13", "14", "15", "16", "17", "18", "19"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_BOLIVIA:
            {
                countryCode = AppDefines.CODE_BOLIVIA;
                String[] networkcarrier = {"2", "3","4", "5", "6", "7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_CANADA:
            {
                countryCode = AppDefines.CODE_CANADA;
                String[] networkcarrier = {"418", "902","250", "780", "506", "709", "902", "867", "613", "418", "306", "709"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_SWITZERLAND:
            {
                countryCode = AppDefines.CODE_SWITZERLAND;
                String[] networkcarrier = {"75", "76","77", "78", "79"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_CHILE:
            {
                countryCode = AppDefines.CODE_CHILE;
                String[] networkcarrier = {"9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_CAMEROON:
            {
                countryCode = AppDefines.CODE_CAMEROON;
                String[] networkcarrier = {"745","746","747","748","749","940","941","942","943","944"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(5);
            }
            break;
            case AppDefines.COUNTRY_DENMARK:
            {
                countryCode = AppDefines.CODE_DENMARK;
                String[] networkcarrier = {"20","31","40","42","50","53","60","61","71","81", "91", "93"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;
            case AppDefines.COUNTRY_DOMINICAN:
            {
                countryCode = AppDefines.CODE_DOMINICAN;
                String[] networkcarrier = {"809","829","849"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_ALGERIA:
            {
                countryCode = AppDefines.CODE_ALGERIA;
                String[] networkcarrier = {"790","791","792", "793", "794", "795", "796", "697", "698", "699"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;
            case AppDefines.COUNTRY_FINLAND:
            {
                countryCode = AppDefines.CODE_FINLAND;
                String[] networkcarrier = {"457","50"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_FRANCE:
            {
                countryCode = AppDefines.CODE_FRANCE;
                String[] networkcarrier = {"7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_IRAQ:
            {
                countryCode = AppDefines.CODE_IRAQ;
                String[] networkcarrier = {"73", "74" , "75", "76", "77", "78", "79"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_JAPAN:
            {
                countryCode = AppDefines.CODE_JAPAN;
                String[] networkcarrier = {"70", "80" , "90"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_KENYA:
            {
                countryCode = AppDefines.CODE_KENYA;
                String[] networkcarrier = {"7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_KAZAKHSTAN:
            {
                countryCode = AppDefines.CODE_KENYA;
                String[] networkcarrier = {"700","701", "702", "705", "707"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_LEBANON:
            {
                countryCode = AppDefines.CODE_LEBANON;
                String[] networkcarrier = {"760","761", "763", "764", "766", "767", "768", "769"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(5);
            }
            break;
            case AppDefines.COUNTRY_MOROCCO:
            {
                countryCode = AppDefines.CODE_MOROCCO;
                String[] networkcarrier = {"40","41", "42", "44", "45", "48", "50", "51"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;
            case AppDefines.COUNTRY_MOLDOVA:
            {
                countryCode = AppDefines.CODE_MOLDOVA;
                String[] networkcarrier = {"6","7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_MAURITIUS:
            {
                countryCode = AppDefines.CODE_MAURITIUS;
                String[] networkcarrier = {"5"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_MALDIVES:
            {
                countryCode = AppDefines.CODE_MALDIVES;
                String[] networkcarrier = {"77","78", "76", "79", "73", "91", "96", "97", "98", "99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(5);
            }
            break;
            case AppDefines.COUNTRY_PALESTINIAN:
            {
                countryCode = AppDefines.CODE_PALESTINIAN;
                String[] networkcarrier = {"59","56"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_ROMANIA:
            {
                countryCode = AppDefines.CODE_ROMANIA;
                String[] networkcarrier = {"7885","7886"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(5);
            }
            break;
            case AppDefines.COUNTRY_SUDAN:
            {
                countryCode = AppDefines.CODE_SUDAN;
                String[] networkcarrier = {"91","92"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_SWEDEN: {
                countryCode = AppDefines.CODE_SWEDEN;
                String[] networkcarrier = {"70", "72", "73", "76", "79"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7) + getRandNumber(4);
            }
            break;
            case AppDefines.COUNTRY_SWAZILAND:
            {
                countryCode = AppDefines.CODE_SWAZILAND;
                String[] networkcarrier = {"76"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;
            case AppDefines.COUNTRY_UZBEKISTAN:
            {
                countryCode = AppDefines.CODE_UZBEKISTAN;
                String[] networkcarrier = {"9"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(8);
            }
            break;
            case AppDefines.COUNTRY_SAINT_VINCENT:
            {
                countryCode = AppDefines.CODE_SAINT_VINCENT;
                String[] networkcarrier = {"784454", "784455", "784593"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(4);
            }
            break;
            case AppDefines.COUNTRY_SOUTH_AFRICA:
            {
                countryCode = AppDefines.CODE_SOUTH_AFRICA;
                String[] networkcarrier = {"5","6","7"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(7);
            }
            break;
            case AppDefines.COUNTRY_ZIMBABWE:
            {
                countryCode = AppDefines.CODE_ZIMBABWE;
                String[] networkcarrier = {"712","713","714", "715","716","717","718","719"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + getRandNumber(6);
            }
            break;

            default: {
                countryCode = AppDefines.CODE_PHILIPINS;
                String[] networkcarrier = {"90", "91", "92", "93", "94", "95", "96", "97", "98", "99"};
                mobileNumber = countryCode + networkcarrier[new Random().nextInt(networkcarrier.length)] + (new Random().nextInt(90000000) + 10000000);
            }
            break;
        }
        return mobileNumber;
    }

    static private String getRandNumber(int length) {
        int base = (int)Math.pow(10,length - 1);
        return String.valueOf(new Random().nextInt(base * 9) + base);
    }

    protected static boolean checkUidLive(String uid) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(String.format("https://graph.facebook.com/%s/picture?redirect=false", uid))
                    .method("GET", null)
                    .build();
            com.squareup.okhttp.Response response = client.newCall(request).execute();
            String respStr = response.body().string();
            if (response.code() == 200) {
                JSONObject responseJson = new JSONObject(respStr);
                if(responseJson.has("data")) {
                    JSONObject data = responseJson.getJSONObject("data");
                    boolean status = data.has("height");
                    LOG.D(TAG, "checkUidLive -- " + uid + ": " + (status? "live" : "checkpoint"));
                    return status;
                } else {
                    Exception e = new Exception() {
                        @Override
                        public String getMessage() {
                            return "Data field not found!";
                        }
                    };
                    throw e;
                }
            } else {
                Exception e = new Exception() {
                    @Override
                    public String getMessage() {
                        return "Request failed: " + response.message();
                    }
                };
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void fakeImage(String imagePath) {
        try {
            List<Transformation> listTransf = new ArrayList<>();
            listTransf.add(new BlurTransformation(MainActivity.context, 10, 1));
            listTransf.add(new SketchFilterTransformation(MainActivity.context));
            listTransf.add(new GrayscaleTransformation());
            listTransf.add(new ToonFilterTransformation(MainActivity.context));
            listTransf.add(new SepiaFilterTransformation(MainActivity.context));

            Bitmap bm = Picasso.get()
                    .load(new File(imagePath))
                    .transform(listTransf.get(new Random().nextInt(listTransf.size())))
                    .get();

            File file = new File(imagePath);
            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                LOG.E(TAG, "" + e);
            }
        } catch (Exception e) {
            LOG.E(TAG, "" + e);
        }
    }
}

