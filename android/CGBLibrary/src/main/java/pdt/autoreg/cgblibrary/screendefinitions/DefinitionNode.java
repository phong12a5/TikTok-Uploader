package pdt.autoreg.cgblibrary.screendefinitions;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;

import pdt.autoreg.cgblibrary.LOG;

public class DefinitionNode {
    public String className = null;
    public boolean isSelected = false;
    public boolean isChecked = false;
    public String text = null;
    public String contentDescription = null;
    public String keyword = null;
    public String hintText = null;

    enum COMPARE_RESULT {
        DIFF,
        MATCH,
        CONTAIN
    }

    private DefinitionNode(){}
    public DefinitionNode(JSONObject node) throws Exception {
        try {
            if (node.has("className")) className = node.getString("className");
            if (node.has("text")) text = node.getString("text").toLowerCase();
            if (node.has("contentDescription")) contentDescription = node.getString("contentDescription").toLowerCase();
            if (node.has("hintText")) className = node.getString("hintText").toLowerCase();
            if (node.has("keyword")) keyword = node.getString("keyword");
            if (node.has("selected")) isSelected = node.getBoolean("selected");
            if (node.has("checked")) isChecked = node.getBoolean("checked");
        } catch (Exception e) {
            throw new Exception() {
                @Override
                public String getMessage(){
                    return "Definition node is NULL";
                }
            };
        }
    }

    COMPARE_RESULT compare(ScreenNode node) {
        try {
            if (node.isSelected == isSelected &&
                    node.isChecked == isChecked &&
                    node.className.equals(className)) {
                if (((node.textLower.equals(text) || (text.contains("@") && diffSepcialString(node.textLower, text))) &&
                        (node.contentDescriptionLower.equals(contentDescription) || (contentDescription.contains("@") && diffSepcialString(node.contentDescriptionLower, contentDescription))))) {
                    return COMPARE_RESULT.MATCH;
                } else if (node.textLower.contains(text) && node.contentDescriptionLower.contains(contentDescription)) {
                    return COMPARE_RESULT.CONTAIN;
                }
            }
        } catch (Exception e) {}
        return COMPARE_RESULT.DIFF;
    }

    COMPARE_RESULT compare(ScreenNode node, boolean debug) {
        if(debug) {
            LOG.D("compare", "node: " + node.toString());
            LOG.D("compare", "this: " + this.toString());
        }
        try {
            if (node.isSelected == isSelected &&
                    node.className.equals(className)) {
                if (((node.textLower.equals(text) || (text.contains("@") && diffSepcialString(node.textLower, text))) &&
                        (node.contentDescriptionLower.equals(contentDescription) || (contentDescription.contains("@") && diffSepcialString(node.contentDescriptionLower, contentDescription))))) {
                    return COMPARE_RESULT.MATCH;
                } else if (node.textLower.contains(text) && node.contentDescriptionLower.contains(contentDescription)) {
                    return COMPARE_RESULT.CONTAIN;
                }
            }
        } catch (Exception e) {}
        return COMPARE_RESULT.DIFF;
    }

    public static boolean diffSepcialString(String str1, String str2) {
        if(str1 == null || str2 == null) return false;
        List<DiffMatchPatch.Diff> diffs = new DiffMatchPatch().diffMain(str1, str2);
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation == DiffMatchPatch.Operation.DELETE) {
                str2 = str2.replaceFirst("@", Matcher.quoteReplacement(diff.text));
            }
        }
        return str2.equals(str1);
    }


        @Override
    public String toString() {
        return "DefinitionNode{" +
                "className='" + className + '\'' +
                ", isSelected=" + isSelected +
                ", isChecked=" + isChecked +
                ", text='" + text + '\'' +
                ", contentDescription='" + contentDescription + '\'' +
                ", keyword='" + keyword + '\'' +
                ", hintText='" + hintText + '\'' +
                '}';
    }
}
