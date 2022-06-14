package pdt.autoreg.cgblibrary;

import android.os.RemoteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pdt.autoreg.cgblibrary.screendefinitions.ScreenNode;

public class CGBUtils {
    private static String TAG = "CGBUtils";

    public static boolean findAndClick(String componentID, List<ScreenNode> nodes_in_screen) throws RemoteException {
        boolean retsult = false;
        ScreenNode targetItem = null;
        for (ScreenNode node : nodes_in_screen) {
            if (node.keyword != null && componentID.equals(node.keyword)) {
                if (targetItem == null || node.match) {
                    targetItem = node;
                }
            }
        }

        if (targetItem != null) {
            int x = targetItem.x;
            int y = targetItem.y;
            int width = targetItem.width;
            int height = targetItem.height;
            CGBInterface.getInstance().clickByPos(x + width / 2, y + height / 2, false);
            retsult = true;
        }

        LOG.D("findAndClick ", componentID + ":" + (retsult ? "SUCCESS" : "FAIL"));
        return retsult;
    }

    public static boolean findAndClickAll(String componentID, List<ScreenNode> nodes) {
        boolean retsult = false;
        try {
            for (ScreenNode node : nodes) {
                if (componentID.equals(node.keyword)) {
                    int x = node.x;
                    int y = node.y;
                    int width = node.y;
                    int height = node.y;
                    if (CGBInterface.getInstance().clickByPos(x + width / 2, y + height / 2 , false)) {
                        delay(200);
                        retsult = true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        LOG.D(TAG, "findAndClick " + componentID + ": " + (retsult ? "SUCCESS" : "FAIL"));
        return retsult;
    }

    public static boolean findAndClickWithOffset(String componentID, List<ScreenNode> nodes, int xOffset, int yOffset) {
        boolean retsult = false;
        try {
            for (ScreenNode node : nodes) {
                if (componentID.equals(node.keyword)) {
                    int x = node.x;
                    int y = node.y;
                    if (CGBInterface.getInstance().clickByPos(x + xOffset, y + yOffset, false)) {
                        delay(200);
                        retsult = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        LOG.D(TAG, "findAndClickWithOffset " + componentID + ": " + (retsult ? "SUCCESS" : "FAIL"));
        return retsult;
    }

    public static boolean findAndClickByTextOrDes(String textOrDes, List<ScreenNode> nodes) {
        boolean retsult = false;
        try {
            for (ScreenNode node : nodes) {
                if (textOrDes.equals(node.text) || textOrDes.equals(node.contentDescription)) {
                    if (CGBInterface.getInstance().clickByPos(node.x + node.width / 2, node.y + node.height / 2, false)) {
                        delay(200);
                        retsult = true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        LOG.D(TAG, "findAndClickByTextOrDes " + textOrDes + ": "  + (retsult ? "SUCCESS" : "FAIL"));
        return retsult;
    }

    public static boolean findByTextOrDes(String textOrDes, List<ScreenNode> nodes) {
        boolean retsult = false;
        try {
            if(textOrDes != null) {
                for (ScreenNode node : nodes) {
                    if (textOrDes.equals(node.text) || textOrDes.equals(node.contentDescription)) {
                        retsult = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
        LOG.D(TAG, "findByTextOrDes " + textOrDes + ": " + (retsult ? "SUCCESS" : "FAIL"));
        return retsult;
    }

    protected List<ScreenNode> getListComponentInfo(String idComponent, List<ScreenNode> nodes) {
        List<ScreenNode> retsult = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            ScreenNode item = nodes.get(i);
            if (idComponent.equals(item.keyword)) {
                retsult.add(item);
            }
        }
        LOG.D("getComponentInfoByText ", retsult == null ? "NULL" : retsult.toString());
        return retsult;
    }

    static public void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
        }
    }
}


