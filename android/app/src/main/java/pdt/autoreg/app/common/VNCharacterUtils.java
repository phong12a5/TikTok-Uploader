package pdt.autoreg.app.common;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VNCharacterUtils {
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        temp = temp.replaceAll("đ", "d");
        temp = temp.replaceAll("Đ", "D");
        return temp;
    }
}
