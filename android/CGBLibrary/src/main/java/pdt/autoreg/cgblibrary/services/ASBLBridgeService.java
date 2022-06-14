package pdt.autoreg.cgblibrary.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.Nullable;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import pdt.autoreg.cgblibrary.CGBInterface;
import pdt.autoreg.cgblibrary.IASBLInterface;
import pdt.autoreg.cgblibrary.LOG;
import pdt.autoreg.cgblibrary.screendefinitions.DefinitionNode;
import pdt.autoreg.cgblibrary.screendefinitions.DefintionElement;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenInfo;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenNode;

public class ASBLBridgeService extends Service {
    private static final String TAG = "ASBLBridgeService";

    private static AccessibilityService asblService = null;

    private int widthOfScreen = 0;
    private int heightOfScreen = 0;
    private Rect tempRect = new Rect();

    private IASBLInterface.Stub mBinder = new IASBLInterface.Stub() {

        @Override
        public boolean clickByPos(int x, int y, boolean longPress) throws RemoteException {
            return ASBLBridgeService.this.do_click(x, y, longPress);
        }

        @Override
        public boolean clickByComp(String screenID, String compId) throws RemoteException {
            return ASBLBridgeService.this.do_click(screenID, compId);
        }

        @Override
        public boolean swipe(int x1, int y1, int x2, int y2, int duration) throws RemoteException {
            return ASBLBridgeService.this.swipe(x1, y1, x2, y2, duration);
        }

        @Override
        public boolean openPackage(String pckg) throws RemoteException {
            return ASBLBridgeService.this.openPacakge(pckg);
        }

        @Override
        public boolean inputText(String txt, ScreenNode target, boolean delay) throws RemoteException {
            return ASBLBridgeService.this.inputText(txt, target, delay);
        }

        @Override
        public boolean scrollForward() throws RemoteException {
            return ASBLBridgeService.this.scrollForward();
        }

        @Override
        public boolean scrollBackward() throws RemoteException {
            return ASBLBridgeService.this.scrollBackward();
        }

        @Override
        public boolean globalBack() throws RemoteException {
            return ASBLBridgeService.this.globalBack();
        }

        @Override
        public String getCurrentForgroundPkg() throws RemoteException {
            return ASBLBridgeService.this.getCurrentForgroundPkg();
        }

        @Override
        public void updateKeywordDefinitions() throws RemoteException {
            ASBLBridgeService.this.updateKeywordDefinitions();
        }

        @Override
        public ScreenInfo detectScreen(String appName) throws RemoteException {
            return ASBLBridgeService.this.detectScreen(appName);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ApiAccessibilityService.getInstance(this);

        WindowManager wmgr = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wmgr.getDefaultDisplay().getRealMetrics(metrics);
        widthOfScreen = metrics.widthPixels;
        heightOfScreen = metrics.heightPixels;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static void setASBLInstance(AccessibilityService accessibilityService) {
        asblService = accessibilityService;
    }

    private boolean do_click(int x, int y, boolean longPress) {
        LOG.I(TAG, String.format("[%d,%d]",x,y));
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        Random rand = new Random();
        int pointCount  = rand.nextInt(3) + 1;
        for (int i = 0; i < pointCount; i++) {
            clickPath.lineTo(x + rand.nextInt(6) - 3, y + rand.nextInt(6) - 3);
        }

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, longPress? 1000 : 100));
        return asblService.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
    }

    private boolean do_click(String screenId, String compId) {
        for (int i = 0; i < ScreenInfo.sDefinitions.size(); i++) {
            try {
                DefintionElement obj = ScreenInfo.sDefinitions.get(i);
                if (obj != null && obj.screen_id.equals(screenId)) {
                    Iterator<String> keysItr = obj.keywords.keySet().iterator();
                    AccessibilityNodeInfo root = asblService.getRootInActiveWindow();

                    while (keysItr.hasNext()) {
                        String langCode = keysItr.next();
                        List<DefinitionNode> keywordsByLang = obj.keywords.get(langCode);
                        for (int j = 0; j < keywordsByLang.size(); j++) {
                            DefinitionNode keyword = keywordsByLang.get(j);
                            String keywordID = keyword.keyword;

                            if (keywordID.equals(compId)) {
                                String keywordDes = keyword.contentDescription;
                                String keywordText = keyword.text;

                                List<AccessibilityNodeInfo> nodes = null;
                                if (keywordText != null && !keywordText.equalsIgnoreCase("null"))
                                    nodes = findAccessibilityNodeInfosByTextDes(root, keywordText, true);
                                else if (keywordDes != null && !keywordDes.equalsIgnoreCase("null"))
                                    nodes = findAccessibilityNodeInfosByTextDes(root, keywordDes, true);

                                if (nodes != null) {
                                    for (AccessibilityNodeInfo node : nodes) {
                                        LOG.D(TAG, "node: " + node);
                                        List<AccessibilityNodeInfo.AccessibilityAction> actions = node.getActionList();
                                        if (actions != null && actions.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                                            LOG.D(TAG, "performAction: ACTION_CLICK");
                                            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return false;
    }


    private boolean do_double_click(final int x, final int y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        Random rand = new Random();
        clickPath.lineTo(x + rand.nextInt(6) - 3, y + rand.nextInt(6) - 3);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));
        asblService.dispatchGesture(gestureBuilder.build(),null, null);

        sleep(200);

        clickPath.lineTo(x + rand.nextInt(6) - 3, y + rand.nextInt(6) - 3);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));
        asblService.dispatchGesture(gestureBuilder.build(),null, null);

        return true;
    }

    private boolean swipe(int x1, int y1, int x2, int y2, int delay) {
        try {
            Path clickPath = new Path();
            clickPath.moveTo(x1, y1);
            clickPath.lineTo(x2, y2);
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, delay));
            boolean res = asblService.dispatchGesture(gestureBuilder.build(), null, null);
            sleep(delay);
            return true;
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
            return false;
        }
    }

    private boolean inputText(String text, ScreenNode targetObj, boolean delay) {
        LOG.D(TAG, "inputText text: " + text + " -- targetObj: " + targetObj + " -- delay: " + delay);
        AccessibilityNodeInfo root = asblService.getRootInActiveWindow();
        if (root != null) {
            AccessibilityNodeInfo focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (focus == null || !focus.isFocused() || !focus.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)) {
                focus = findInputFocus(root);
            }

            if (focus != null) {
                if (targetObj != null) {
                    try {
                        String contentDesFocused = focus.getContentDescription() != null ? focus.getContentDescription().toString() : "null";
                        String textFocused = focus.getText() != null ? focus.getText().toString() : "null";
                        String classNameFocused = focus.getClassName() != null ? focus.getClassName().toString() : "null";

                        if (targetObj.contentDescription.equalsIgnoreCase(contentDesFocused) &&
                                targetObj.text.equalsIgnoreCase(textFocused) &&
                                targetObj.className.equalsIgnoreCase(classNameFocused)) {
                            // Do nothing
                        } else {
                            focus.recycle();
                            root.recycle();
                            return false;
                        }
                    } catch (Exception e) {
                        LOG.E(TAG, "inputText Error: " + e);
                        focus.recycle();
                        root.recycle();
                        return false;
                    }
                }

                String test = "";
                Bundle arguments = new Bundle();
                if (delay) {
                    for (char c : text.toCharArray()) {
                        test += c;
                        arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, test);
                        focus.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
                        sleep(200);
                    }
                } else {
                    arguments.putString(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                    focus.performAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT, arguments);
                }
                focus.recycle();
                root.recycle();
                return true;
            } else {
                LOG.E(TAG, "inputText: Focused node is not found");
            }
            root.recycle();
        }
        return false;
    }

    private AccessibilityNodeInfo findInputFocus(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();

        if (root == null) {
            return null;
        }

        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node != null) {
                if (node.isFocused() && node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)) {
                    return node;
                }
                for (int i = 0; i < node.getChildCount(); i++) {
                    deque.addLast(node.getChild(i));
                }
            }
        }
        return null;
    }

    private List<AccessibilityNodeInfo> findScrollables(AccessibilityNodeInfo root, int action) {
        List<AccessibilityNodeInfo> results = new ArrayList<>();
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        AccessibilityNodeInfo.AccessibilityAction accessibilityAction;
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            accessibilityAction = AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD;
        else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            accessibilityAction = AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD;
        else
            return results;

        if (root == null) {
            return results;
        }
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            boolean isCollectedNode = false;
            if (node != null) {
                if (node.getActionList().contains(accessibilityAction)) {
                    if (node.getClassName() == null || !node.getClassName().equals("androidx.viewpager.widget.ViewPager")) {
                        isCollectedNode = true;
                        results.add(node);
                    }
                }
                for (int i = 0; i < node.getChildCount(); i++) {
                    deque.addLast(node.getChild(i));
                }
                if (!isCollectedNode) {
                    node.recycle();
                }
            }
        }
        return results;
    }

    private boolean scrollMainScrollable(int action) {
        try {
            AccessibilityNodeInfo root = asblService.getRootInActiveWindow();
            if (root != null) {
                AccessibilityNodeInfo mainScrollable = findMainScrollable(root, action);
                if (mainScrollable != null) {
                    synchronized (mainScrollable) {
                        mainScrollable.performAction(action);
                        mainScrollable.recycle();
                    }
                    root.recycle();
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "scrollMainScrollable: " + e);
        }
        return false;
    }

    private AccessibilityNodeInfo findMainScrollable(AccessibilityNodeInfo root, int action) {
        AccessibilityNodeInfo mainScrollable = null;
        try {
            if (root != null) {
                List<AccessibilityNodeInfo> scrollables = findScrollables(asblService.getRootInActiveWindow(), action);
                int mainScrollableHeight = 0;
                for (AccessibilityNodeInfo node : scrollables) {
                    if (node != null) {
                        Rect boundsInSCreen = new Rect();
                        node.getBoundsInScreen(boundsInSCreen);
                        if (mainScrollable == null || (boundsInSCreen.height() >= mainScrollableHeight &&
                                boundsInSCreen.left <= 0 && boundsInSCreen.right >= widthOfScreen)) {
                            mainScrollableHeight = boundsInSCreen.height();
                            if (mainScrollable != null) {
                                mainScrollable.recycle();
                            }
                            mainScrollable = node;
                        } else {
                            node.recycle();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.E(TAG, "findMainScrollable: " + e);
        }
        return mainScrollable;
    }

    public String getCurrentForgroundPkg() {
        String currentPkg = null;
        try {
            if (asblService.getRootInActiveWindow() != null) {
                currentPkg = asblService.getRootInActiveWindow().getPackageName().toString();
            }
            LOG.D(TAG, "Current package: " + (currentPkg == null ? "NULL" : currentPkg));
        } catch (Exception e) {
        }
        return currentPkg;
    }

    public void updateKeywordDefinitions() {
        String content = "";
        try {
            InputStream stream = getAssets().open("definitions.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            content = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        try {
            JSONArray defArr = new JSONArray(content);
            if(defArr == null || defArr.length() <= 0) {
                LOG.E(TAG,"Invalid database");
            } else {
                ScreenInfo.updateDefinitions(defArr);
            }
        } catch (JSONException e) {
            LOG.E(TAG,"updateKeywordDefinitions: " + e);
        }
    }

    public ScreenInfo detectScreen(String currAppName) {
        LOG.D(TAG, "detectScreen: " + currAppName);
        try {
            /** Try to detect page on all window **/

            List<AccessibilityWindowInfo> windowList = asblService.getWindows();
            List<AccessibilityNodeInfo> nodesOnScreen = new ArrayList<AccessibilityNodeInfo>();

            LOG.D(TAG, "windowList: " + windowList.size());

            for (AccessibilityWindowInfo window : windowList) {
                if (window.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD ||
                        (window.isActive() && window.isFocused())) {
                    List<AccessibilityNodeInfo> nodesOnWindow = this.getNodeListOnTree(window.getRoot());
                    if (nodesOnWindow != null) {
                        nodesOnScreen.addAll(nodesOnWindow);
                    }
                }
            }

            LOG.D(TAG, "nodesOnScreen: " + nodesOnScreen.size());
            for (AccessibilityNodeInfo nodeInfo : nodesOnScreen) {
                LOG.D(TAG, "node: " + nodeInfo);
            }
            long startTime = System.currentTimeMillis();
            ScreenInfo screenInfo = new ScreenInfo(nodesOnScreen, currAppName);
            long stopTime = System.currentTimeMillis();
            LOG.D(TAG, "time in detecting: " + (stopTime-startTime));


            try {
                LOG.D(TAG, "SCREEN_ID: " + screenInfo.detected_screen_id);
                LOG.D(TAG, "SCREEN_INFO: " + screenInfo.toJson());
            } catch (Exception e) { }

            return screenInfo;
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        return null;
    }

    private List<AccessibilityNodeInfo> getNodeListOnTree(AccessibilityNodeInfo mNodeInfo) {
        if (mNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> result = new ArrayList<AccessibilityNodeInfo>();


        for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
            List<AccessibilityNodeInfo> subList = this.getNodeListOnTree(mNodeInfo.getChild(i));
            if (subList != null) {
                result.addAll(subList);
            }
        }

        boolean isCollected = false;
        if (isValidNodeOnScreen(mNodeInfo)) {
            if(!result.contains(mNodeInfo)) {
                result.add(mNodeInfo);
            }
            isCollected = true;
        }

        if (!isCollected) {
            mNodeInfo.recycle();
        }

        return result;
    }

    private boolean isValidNodeOnScreen(AccessibilityNodeInfo node) {
        if (node != null) {
            node.getBoundsInScreen(tempRect);
            if (node.isVisibleToUser() &&
                    tempRect.left >= 0 &&
                    tempRect.top >= 0 &&
                    tempRect.top <= heightOfScreen &&
                    tempRect.left <= widthOfScreen) {
                if (node.getText() != null ||
                        node.getContentDescription() != null ||
                        node.getHintText() != null)
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean openPacakge(String packageName) {
        try {
            Intent launchIntent = asblService.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.setFlags(launchIntent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_FROM_BACKGROUND);
                asblService.startActivity(launchIntent);
                return true;
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        return false;
    }

    private boolean globalBack() {
        return asblService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public boolean scrollForward() {
        return scrollMainScrollable(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    public boolean scrollBackward() {
        return scrollMainScrollable(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTextDes(AccessibilityNodeInfo root, String textOrDes, boolean match) {
        if (root == null) {
            return null;
        }

        List<AccessibilityNodeInfo> subNodes = new ArrayList<>();

        boolean isCollected = false;
        if ((match && root.getText() != null && String.valueOf(root.getText()).equals(textOrDes)) ||
                (match && root.getContentDescription() != null && root.getContentDescription().toString().equals(textOrDes)) ||
                (!match && root.getText() != null && root.getText().toString().contains(textOrDes)) ||
                (!match && root.getContentDescription() != null && root.getContentDescription().toString().contains(textOrDes))) {
            isCollected = true;
            subNodes.add(root);
        }

        if (root.getChildCount() < 1) {
            if (!isCollected) {
                root.recycle();
                return null;
            } else {
                return subNodes;
            }
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            List<AccessibilityNodeInfo> nodes = findAccessibilityNodeInfosByTextDes(child, textOrDes, match);
            if (nodes != null && !nodes.isEmpty()) {
                subNodes.addAll(nodes);
            }
        }

        return subNodes;
    }



    private static void sleep(int dur) {
        try {
            Thread.sleep(dur);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
