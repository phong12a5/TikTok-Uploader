package pdt.autoreg.cgblibrary.screendefinitions;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pdt.autoreg.cgblibrary.LOG;

public class ScreenInfo implements Parcelable {
    static final String TAG = "ScreenInfo";
    public static List<DefintionElement> sDefinitions = null;
    public String tartget_app;
    public String detected_screen_id = "SCREEN_UNKNOWN";
    public List<ScreenNode> nodes_in_screen = new ArrayList<>();

    public ScreenInfo(List<AccessibilityNodeInfo> nodes, String tartget_app) {
        this.tartget_app = tartget_app;
        for (AccessibilityNodeInfo node : nodes) {
            try {
                this.nodes_in_screen.add(new ScreenNode(node));
            } catch (Exception e) {
                LOG.printStackTrace(TAG, e);
            }
        }
        detectScreen();
    }

    protected ScreenInfo(Parcel in) {
        tartget_app = in.readString();
        detected_screen_id = in.readString();
        in.readList(nodes_in_screen, nodes_in_screen.getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tartget_app);
        dest.writeString(detected_screen_id);
        dest.writeList(nodes_in_screen);
    }

    public static final Creator<ScreenInfo> CREATOR = new Creator<ScreenInfo>() {
        @Override
        public ScreenInfo createFromParcel(Parcel in) {
            return new ScreenInfo(in);
        }

        @Override
        public ScreenInfo[] newArray(int size) {
            return new ScreenInfo[size];
        }
    };

    void detectScreen() {
        try {
            LOG.D(TAG, "sDefinitions: " + sDefinitions.size());
            for (DefintionElement defintionElement : sDefinitions) {
                LOG.D(TAG, "defintionElement.app_name: " + defintionElement.app_name);
                if (defintionElement.app_name.equals(tartget_app) ||
                        defintionElement.app_name.equalsIgnoreCase("common")) {
                    boolean debug = defintionElement.screen_id.equals("SCREEN_TIKTOK_HOME_FOR_YOU");

                    if(debug) LOG.D(TAG, "checking screen_Id: " + defintionElement.screen_id);

                    for (String langCode : defintionElement.definitions.keySet()) {
                        List<List<DefinitionNode>> groupsByLang = defintionElement.definitions.get(langCode);
                        if(debug) LOG.D(TAG, "checking in: " + langCode);
                        for (List<DefinitionNode> evidenceGrp : groupsByLang) {
                            boolean detected = true;
                            for (DefinitionNode evidence : evidenceGrp) {
                                boolean existedPartern = false;
                                for (ScreenNode node : nodes_in_screen) {
                                    DefinitionNode.COMPARE_RESULT compare = evidence.compare(node);
                                    if(debug) LOG.D(TAG, "compare " + compare + "-- node: " + node.text + "|" + node.contentDescription + " -- evidence: " + evidence.text + "|" + evidence.contentDescription);
                                    if (compare == DefinitionNode.COMPARE_RESULT.MATCH ||
                                            compare == DefinitionNode.COMPARE_RESULT.CONTAIN) {
                                        existedPartern = true;
                                        break;
                                    }
                                }
                                if (!existedPartern) {
                                    detected = false;
                                    break;
                                }
                            }

                            if (detected) {
                                detected_screen_id = defintionElement.screen_id;
                                detectKeywords(defintionElement);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    void detectKeywords(DefintionElement defintionElement) {
        try {
            for (ScreenNode logNode : nodes_in_screen) {
                for (String langCode : defintionElement.keywords.keySet()) {
                    List<DefinitionNode> keywordByLange = defintionElement.keywords.get(langCode);
                    for (DefinitionNode keywordNode : keywordByLange) {
                        DefinitionNode.COMPARE_RESULT compare_result = keywordNode.compare(logNode);
                        if (compare_result == DefinitionNode.COMPARE_RESULT.MATCH ||
                                (logNode.keyword == null) &&
                                        (compare_result == DefinitionNode.COMPARE_RESULT.CONTAIN)) {
                            logNode.keyword = keywordNode.keyword;
                            logNode.match = compare_result == DefinitionNode.COMPARE_RESULT.MATCH;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.printStackTrace(TAG, e);
        }
    }

    public static void updateDefinitions(JSONArray defArr) {
        if(defArr == null || defArr.length() == 0) return;

        if(sDefinitions == null)
            sDefinitions = new ArrayList<>();
        else
            sDefinitions.clear();

        for (int i = 0; i < defArr.length(); i++) {
            try {
                String str = defArr.getString(i);
                JSONObject object = new JSONObject(str);
                sDefinitions.add(new DefintionElement(object));
                LOG.I(TAG, object + "");
            } catch (Exception e) {
                LOG.printStackTrace(TAG, e);
            }
        }
    }

    public static List<String> getListPageInDefinition() {
        List<String> list = new ArrayList<>();
        if(sDefinitions != null) {
            for (DefintionElement defintionElement : sDefinitions) {
                list.add(defintionElement.screen_id);
            }
        }
        return list;
    }

        public JSONArray toJson() {
        JSONArray screenInfo = new JSONArray();
        for (ScreenNode node : nodes_in_screen) {
            screenInfo.put(node.toJson());
        }
        return screenInfo;
    }
}
