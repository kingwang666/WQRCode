#include <jni.h>
#include <string>
#include <math.h>
#include <android/log.h>

#define LOG_TAG "ImageUtil"

int rgb2Grey(int pixel);


int argb2Grey(int pixel);

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_wang_imageutil_ImageUtil_nativeGrey(JNIEnv *env, jclass type, jintArray pixels_) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    int size = env->GetArrayLength(pixels_);
    int *greyPixels = (int *)malloc(static_cast<size_t>(size));
    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                        "greyPixels is %p \n", greyPixels);
    for (int i = 0; i < size; ++i) {
        int pixel = *(pixels + i);
        int grey = rgb2Grey(pixel) & 0xFF;
        int a = (pixel >> 24) & 0xFF;
        *(greyPixels + i) = (a << 24) | (grey << 16) | (grey << 8) | grey;
    }
    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, greyPixels);
    free(greyPixels);
    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_wang_imageutil_ImageUtil_nativeOTSU(JNIEnv *env, jclass type, jintArray pixels_,
                                             jintArray greyPixels_, jintArray binaryPixels_) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);
    jint *greyPixels = NULL;
    if (greyPixels_ != NULL) {
        greyPixels = env->GetIntArrayElements(greyPixels_, NULL);
    }
    jint *binaryPixels = env->GetIntArrayElements(binaryPixels_, NULL);
    /**
     * 是否获取灰度图
     */
    bool getGreyPixel = false;
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
     * 总的像素点个数
     */
    int n = env->GetArrayLength(pixels_);
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

    int *greys = (int *)malloc(static_cast<size_t>(n));
    int hist[256] = {0};

    if (greyPixels != NULL) {
        if (env->GetArrayLength(greyPixels_) == n) {
            getGreyPixel = true;
        } else {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                                "the grey pixel length must = width * height");
        }
    }

    if (env->GetArrayLength(binaryPixels_) != n) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                            "the binary pixel length must = width * height");
        free(greys);
        return -1;
    }

    for (int i = 0; i < n; ++i) {
        int pixel = *(pixels + i);
        int grey = argb2Grey(pixel) & 0xFF;
        greys[i] = grey;
        sumGrey += grey;
        hist[grey]++;
        if (getGreyPixel) {
            greyPixels[i] = 0xff000000 | (grey << 16) | (grey << 8) | grey;
        }
    }

    for (int i = 0; i < 256; i++) {
        n0 += hist[i];
        if (n0 == 0) {
            continue;
        }
        n1 = n - n0;
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

    for (int i = 0; i < n; i++) {
        if (greys[i] >= thresholdValue) {
            binaryPixels[i] = 0xFFFFFFFF;
        } else {
            binaryPixels[i] = 0xFF000000;
        }
    }

    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    if (greyPixels != NULL) {
        env->ReleaseIntArrayElements(greyPixels_, greyPixels, 0);
    }
    env->ReleaseIntArrayElements(binaryPixels_, binaryPixels, 0);
    free(greys);
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