package com.wang.imageutil.java;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created on 2017/8/11.
 * Author: wang
 */

public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    public static boolean grey(@NonNull int[] pixels){
        return grey(pixels, pixels);
    }

    public static boolean grey(@NonNull int[] pixels, @NonNull int[] greyPixels ) {
        int length = pixels.length;
        if (length == 0 || length > greyPixels.length) {
            Log.e(TAG, "the pixel length is " + length + ", is > greyPixels length");
            return false;
        }
        grey(pixels, greyPixels, length);
        return true;
    }

    public static boolean grey(@NonNull Bitmap bitmap){
        if (bitmap.isRecycled()){
            Log.e(TAG, "the bitmap is recycled");
            return false;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == 0 || height == 0){
            Log.e(TAG, "the bitmap size is 0");
            return false;
        }
        int length = width * height;
        int[] pixels = new int[length];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        grey(pixels, pixels, length);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return true;
    }

    /**
     * 大津法 进行二值化
     *
     * @param pixels       输入像素
     * @param binaryPixels 输出的二值像素
     * @return 阈值
     */
    public static int OTSU(@NonNull int[] pixels, @NonNull int[] binaryPixels) {
        int length = pixels.length;
        if (length == 0 || length > binaryPixels.length) {
            Log.e(TAG, "the pixel length is " + length + ", is > binaryPixels length");
            return -1;
        }
        return otsu(pixels, binaryPixels, length);
    }

    public static int OTSU(@NonNull int[] pixels) {
        return OTSU(pixels, pixels);
    }

    public static int OTSU(@NonNull Bitmap bitmap){
        if (bitmap.isRecycled()){
            Log.e(TAG, "the bitmap is recycled");
            return -1;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == 0 || height == 0){
            Log.e(TAG, "the bitmap size is 0");
            return -1;
        }
        int length = width * height;
        int[] pixels = new int[length];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int thresholdValue = otsu(pixels, pixels, length);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return thresholdValue;
    }

    private static void grey(int[] pixels, int[] greyPixels, int size) {

        for (int i = 0; i < size; i++) {
            int pixel = pixels[i];
            int grey = rgb2Grey(pixel) & 0xFF;
            int a = (pixel >> 24) & 0xFF;
            greyPixels[i] = (a << 24) | (grey << 16) | (grey << 8) | grey;
        }

    }

    private static int otsu(int[] pixels, int[] binaryPixels, int size) {

        /**
         * 总的灰度值
         */
        int sumGrey = 0;
        /**
         * 总的前景灰度值
         */
        int sumFrontGrey = 0;
        /**
         * 阈值
         */
        int thresholdValue = 0;

        /**
         * 前景(像素小于阈值)像素点个数
         */
        int n0 = 0;
        /**
         * 背景(像素大于阈值)像素点个数
         */
        int n1 = 0;
        /**
         * 前景平均灰度值
         */
        double u0 = 0;
        /**
         * 背景平均灰度值
         */
        double u1 = 0;
        /**
         *最大类间方差
         */
        double maxG = 0;
        /**
         * 类间方差
         */
        double g = 0;

        int[] hist = new int[256];

        for (int i = 0; i < size; i++) {
            int pixel = pixels[i];
            int grey = argb2Grey(pixel) & 0xFF;
            binaryPixels[i] = grey;
            sumGrey += grey;
            hist[grey]++;
        }

        for (int i = 0; i < 256; i++) {
            n0 += hist[i];
            if (n0 == 0) {
                continue;
            }
            n1 = size - n0;
            if (n1 == 0) {
                break;
            }
            sumFrontGrey += i * hist[i];
            u0 = (double) sumFrontGrey / n0;
            u1 = (double) (sumGrey - sumFrontGrey) / n1;
            g = (double) n0 * n1 * (u0 - u1) * (u0 - u1);
//            类间方差最大的分割意味着错分概率最小
            if (g > maxG) {
                maxG = g;
                thresholdValue = i;
            }
        }

        for (int i = 0; i < size; i++) {
            if (binaryPixels[i] >= thresholdValue){
                binaryPixels[i] = 0xFFFFFFFF;
            } else{
                binaryPixels[i] = 0xFF000000;
            }
        }
        return thresholdValue;
    }

    /**
     * 加权平均法 进行灰度化
     *
     * @param pixel 像素点
     * @return 灰度值
     */
    private static int rgb2Grey(int pixel) {
        final int r = (pixel >> 16) & 0xFF;
        final int g = (pixel >> 8) & 0xFF;
        final int b = pixel & 0xFF;
        return (int) (0.30 * r + 0.59 * g + 0.11 * b);
    }

    private static int argb2Grey(int pixel) {
        final int a = pixel >>> 24;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        r = (255 - a) + a * r / 255;
        g = (255 - a) + a * g / 255;
        b = (255 - a) + a * b / 255;
        return (int) (0.30 * r + 0.59 * g + 0.11 * b);
    }

    private static int argb2Rgb(int pixel) {
        final int a = pixel >>> 24;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;

        r = (255 - a) + a * r / 255;
        g = (255 - a) + a * g / 255;
        b = (255 - a) + a * b / 255;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }
}
