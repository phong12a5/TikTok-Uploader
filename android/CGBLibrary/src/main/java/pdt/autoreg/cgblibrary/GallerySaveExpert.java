package pdt.autoreg.cgblibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class GallerySaveExpert {

    private static final String TAG = "GallerySaveExpert";

    /**
     * This library write the file in the device storage or sdcard.
     * <p>
     *  @param bitmap                  the bitmap that you need to write in device
     * @param photoName               the photo name
     * @param directoryName           the directory that you need to create the picture
     * @param format                  the format of the photo, maybe png or jpeg
     * @param context
     */
    public static String writePhotoFile(Bitmap bitmap,String imagePath, Context context) {

        if (bitmap == null) {
            return null;
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            File f = new File(imagePath);
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + f.getAbsolutePath())));
                try {
                    //Update the System
                    Uri u = Uri.parse(f.getAbsolutePath());
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, u));
                } catch (Exception ex) {

                }
                return f.getAbsolutePath();
            } catch (Exception ev) {
                return null;
            }
        }
    }
}
