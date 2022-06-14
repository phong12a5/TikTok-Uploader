package pdt.autoreg.cgblibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;

import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import java.nio.ByteBuffer;

public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "ImageTransmogrifier";
    private final int width;
    private final int height;
    private final ImageReader imageReader;
    private Bitmap latestBitmap = null;
    Context context;
    WindowManager windowManager;
    String outputPath = null;
    private boolean alreadyProcessed = false;

    public ImageTransmogrifier(Context _context, WindowManager _windowManager, String _outputPath) {
        context = _context;
        windowManager = _windowManager;
        outputPath = _outputPath;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);

        this.width = metrics.widthPixels;
        this.height = metrics.heightPixels;

        imageReader = ImageReader.newInstance(width, height,
                PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, null);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        if(!alreadyProcessed) {
            alreadyProcessed = true;
            final Image image = imageReader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                int bitmapWidth = width + rowPadding / pixelStride;

                if (latestBitmap == null ||
                        latestBitmap.getWidth() != bitmapWidth ||
                        latestBitmap.getHeight() != height) {
                    if (latestBitmap != null) {
                        latestBitmap.recycle();
                    }

                    latestBitmap = Bitmap.createBitmap(bitmapWidth,
                            height, Bitmap.Config.ARGB_8888);
                }

                latestBitmap.copyPixelsFromBuffer(buffer);
                image.close();
                final Bitmap cropped = Bitmap.createBitmap(latestBitmap, 0, 0,
                        width, height);
                new Thread() {
                    @Override
                    public void run() {
                        String path = GallerySaveExpert.writePhotoFile(cropped, outputPath, context.getApplicationContext());
                        Intent intent = new Intent();
                        intent.setAction(ModuleDefines.PROCESS_IMAGE_DONE);
                        context.sendBroadcast(intent);
                    }
                }.start();
            }
        }
    }

    public Surface getSurface() {
        return (imageReader.getSurface());
    }

    public int getWidth() {
        return (width);
    }

    public int getHeight() {
        return (height);
    }

    public void close() {
        imageReader.close();
    }
}
