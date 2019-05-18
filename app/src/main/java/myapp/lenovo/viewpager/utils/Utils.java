package myapp.lenovo.viewpager.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by wn on 2019/4/23.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static DisplayMetrics getScreenWidthHeight(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics;
    }

    public static boolean hasCameraHardware(Context context){
        return context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static Rect calculateTapArea(int focusWidth, int focusHeight, float coefficient,
                                        float x, float y, int previewLeft, int previewRight,
                                        int previewTop, int previewBottom){
        float areaWidth = focusWidth * coefficient;
        float areaHeight = focusHeight * coefficient;
        float previewCenterX = (previewLeft + previewRight) / 2f;
        float previewCenterY = (previewTop + previewBottom) / 2f;
        float unitX = (previewRight - previewLeft) / 2000f;
        float unitY = (previewBottom - previewTop) / 2000f;
        int left = clamp((int)((x - areaWidth / 2 - previewCenterX) / unitX), 1000, -1000);
        int top = clamp((int)((y - areaHeight / 2 - previewCenterY) / unitY), 1000, -1000);
        int right = clamp((int)((x + areaWidth / 2 - previewCenterX) / unitX), 1000, -1000);
        int bottom = clamp((int)((y + areaHeight / 2 - previewCenterY) / unitY), 1000, -1000);
        return new Rect(left, top, right, bottom);
    }

    private static int clamp(int x, int max, int min){
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    public static Bitmap rotate(Bitmap bitmap, int degrees){
        if (degrees != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                Log.e(TAG, "rotate");
            }
        }
        return bitmap;
    }
}
