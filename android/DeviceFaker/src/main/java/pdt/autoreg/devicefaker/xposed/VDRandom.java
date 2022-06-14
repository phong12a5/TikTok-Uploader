package pdt.autoreg.devicefaker.xposed;

import java.util.Random;

public class VDRandom {

    private static final String CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
    private static final String NUMBERS = "0123456789";

    public static final Random random = new Random();

    public static int randomNumber(int start,int end){
        return start + random.nextInt(end - start);
    }

    public static String randomString(int sizeOfRandomString){
        Random random = new Random();
        StringBuilder builder = new StringBuilder(sizeOfRandomString);
        for(int i=0; i < sizeOfRandomString; i++)
            builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        return builder.toString();
    }

    public static String randomNumber(int size){
        StringBuilder builder = new StringBuilder(size);
        for(int i=0; i < size; i++)
            builder.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        return builder.toString();
    }

    public static boolean randomRatio(int ratio){
        return random.nextInt(100) < ratio;
    }
}
