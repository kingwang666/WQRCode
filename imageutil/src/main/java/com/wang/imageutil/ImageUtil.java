package com.wang.imageutil;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/26
 */

public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    static {
        System.loadLibrary("image-util");
    }

    public static boolean grey(@NonNull int[] pixels){
        return grey(pixels, pixels);
    }

    public static boolean grey(@NonNull int[] pixels, @NonNull int[] greyPixels ) {
        return nativeGrey(pixels, greyPixels);
    }

    private static native boolean nativeGrey(int[] pixels, int[] greyPixels);

    public static native boolean grey(@NonNull Bitmap bitmap);

    /**
     * 大津法 进行二值化
     *
     * @param pixels       输入像素
     * @return 阈值
     */
    public static int OTSU(@NonNull int[] pixels) {
        return OTSU(pixels, pixels);
    }

    /**
     * 大津法 进行二值化
     *
     * @param pixels       输入像素
     * @param binaryPixels 输出的二值像素
     * @return 阈值
     */
    public static int OTSU(@NonNull int[] pixels, @NonNull int[] binaryPixels) {
        return nativeOTSU(pixels, binaryPixels);
    }

    private static native int nativeOTSU(int[] pixels, int[] binaryPixels);

    public static native int OTSU(@NonNull Bitmap bitmap);

}
