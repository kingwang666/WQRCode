#include <jni.h>
#include <string>
#include <math.h>
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG "ImageUtil"

int rgb2Grey(int pixel);

int argb2Grey(int pixel);

int otsu(int *pixels, int *binaryPixels, int size);

void grey(int *pixels, int *greyPixels, int size);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_wang_imageutil_ImageUtil_nativeGrey(JNIEnv *env, jclass type, jintArray pixels_,
                                             jintArray greyPixels_) {

    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    jint *greyPixels = env->GetIntArrayElements(greyPixels_, NULL);
    int size = env->GetArrayLength(pixels_);

    if (size == 0 || size > env->GetArrayLength(greyPixels_)) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "the pixels size is %d, is > greyPixels size", size);
        env->ReleaseIntArrayElements(pixels_, pixels, 0);
        env->ReleaseIntArrayElements(greyPixels_, greyPixels, 0);
        return 0;
    }
    grey(pixels, greyPixels, size);

    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    env->ReleaseIntArrayElements(greyPixels_, greyPixels, 0);
    return 1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_wang_imageutil_ImageUtil_grey__Landroid_graphics_Bitmap_2(JNIEnv *env, jclass type,
                                                                   jobject bitmap) {

    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap get info error");
        return 0;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap format: %d not support",
                            info.format);
        return 0;
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap lock pixels error");
        return 0;
    }
    if (pixels == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap pixels is error");
        AndroidBitmap_unlockPixels(env, bitmap);
        return 0;
    }
    grey((int *) pixels, (int *) pixels, info.width * info.height);
    AndroidBitmap_unlockPixels(env, bitmap);
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_wang_imageutil_ImageUtil_nativeOTSU(JNIEnv *env, jclass type, jintArray pixels_,
                                             jintArray binaryPixels_) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    jint *binaryPixels = env->GetIntArrayElements(binaryPixels_, NULL);

    int size = env->GetArrayLength(pixels_);
    if (size == 0 || size > env->GetArrayLength(binaryPixels_)) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "the pixels size is %d, is > greyPixels size", size);
        env->ReleaseIntArrayElements(pixels_, pixels, 0);
        env->ReleaseIntArrayElements(binaryPixels_, binaryPixels, 0);
        return -1;
    }

    int thresholdValue = otsu(pixels, binaryPixels, size);

    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    env->ReleaseIntArrayElements(binaryPixels_, binaryPixels, 0);
    return thresholdValue;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_wang_imageutil_ImageUtil_OTSU__Landroid_graphics_Bitmap_2(JNIEnv *env, jclass type,
                                                                   jobject bitmap) {

    AndroidBitmapInfo info;
    int *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap get info error");
        return -1;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap format: %d not support",
                            info.format);
        return -1;
    }
    if (AndroidBitmap_lockPixels(env, bitmap, (void **)&pixels) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap lock pixels error");
        return -1;
    }
    if (pixels == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "bitmap pixels is error");
        AndroidBitmap_unlockPixels(env, bitmap);
        return -1;
    }
    int thresholdValue = otsu(pixels, pixels, info.width * info.height);
    AndroidBitmap_unlockPixels(env, bitmap);
    return thresholdValue;

}

void grey(int *pixels, int *greyPixels, int size) {

    for (int i = 0; i < size; i++) {
        int pixel = *(pixels + i);
        int grey = rgb2Grey(pixel) & 0xFF;
        int a = (pixel >> 24) & 0xFF;
        *(greyPixels + i) = (a << 24) | (grey << 16) | (grey << 8) | grey;
    }

}

int otsu(int *pixels, int *binaryPixels, int size) {

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

    int hist[256] = {0};

    for (int i = 0; i < size; i++) {
        int pixel = *(pixels + i);
        int grey = argb2Grey(pixel) & 0xFF;
        *(binaryPixels + i) = grey;
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
        if (*(binaryPixels + i) >= thresholdValue) {
            *(binaryPixels + i) = 0xFFFFFFFF;
        } else {
            *(binaryPixels + i) = 0xFF000000;
        }
    }
    return thresholdValue;
}

int rgb2Grey(int pixel) {
    int r = (pixel >> 16) & 0xFF;
    int g = (pixel >> 8) & 0xFF;
    int b = pixel & 0xFF;
    int grey = (int) (0.30 * r + 0.59 * g + 0.11 * b);
    if (grey < 0) {
        return 0;
    } else if (grey > 255) {
        return 255;
    } else {
        return grey;
    }
}

int argb2Grey(int pixel) {
    int a = (pixel >> 24) & 0xFF;
    int r = (pixel >> 16) & 0xFF;
    int g = (pixel >> 8) & 0xFF;
    int b = pixel & 0xFF;
    r = (255 - a) + a * r / 255;
    g = (255 - a) + a * g / 255;
    b = (255 - a) + a * b / 255;
    int grey = (int) (0.30 * r + 0.59 * g + 0.11 * b);
    if (grey < 0) {
        return 0;
    } else if (grey > 255) {
        return 255;
    } else {
        return grey;
    }
}

int argbToRgb(int pixel) {
    int a = (pixel >> 24) & 0xFF;
    int r = (pixel >> 16) & 0xFF;
    int g = (pixel >> 8) & 0xFF;
    int b = pixel & 0xFF;

    r = (255 - a) + a * r / 255;
    g = (255 - a) + a * g / 255;
    b = (255 - a) + a * b / 255;
    return 0xff000000 | (r << 16) | (g << 8) | b;
}

