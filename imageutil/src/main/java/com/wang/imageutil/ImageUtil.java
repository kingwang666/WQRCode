package com.wang.imageutil;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/26
 */

public class ImageUtil {

    static {
        System.loadLibrary("image-util");
    }


    @Nullable
    public static int[] grey(@NonNull int[] pixels) {
        int length = pixels.length;
        if (length == 0) {
            Log.e("Error", "the source pixel is length == 0");
            return null;
        }
        return nativeGrey(pixels);
    }

    @NonNull
    private static native int[] nativeGrey(@NonNull int[] pixels);

    /**
     * 大津法 进行二值化
     *
     * @param pixels       输入像素
     * @param greyPixels   输出的灰度像素
     * @param binaryPixels 输出的二值像素
     * @return 阈值
     */
    public static int OTSU(@NonNull int[] pixels, @Nullable int[] greyPixels, @NonNull int[] binaryPixels) {
        if (pixels.length == 0) {
            Log.e("Error", "the source pixel is length == 0");
            return -1;
        }
        return nativeOTSU(pixels, greyPixels, binaryPixels);
    }

    private static native int nativeOTSU(@NonNull int[] pixels, @Nullable int[] greyPixels, @NonNull int[] binaryPixels);

}
