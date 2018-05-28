package com.wang.qrcode.decoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.io.InputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/16
 */

public class CodeDecoder {

    private static final String TAG = CodeDecoder.class.getSimpleName();

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
//        一维码商品
        decodeFormats.add(BarcodeFormat.UPC_A);
        decodeFormats.add(BarcodeFormat.UPC_E);
        decodeFormats.add(BarcodeFormat.EAN_13);
        decodeFormats.add(BarcodeFormat.EAN_8);
        decodeFormats.add(BarcodeFormat.RSS_14);
        decodeFormats.add(BarcodeFormat.RSS_EXPANDED);
//        一维码工业
        decodeFormats.add(BarcodeFormat.CODE_39);
        decodeFormats.add(BarcodeFormat.CODE_93);
        decodeFormats.add(BarcodeFormat.CODE_128);
        decodeFormats.add(BarcodeFormat.ITF);
        decodeFormats.add(BarcodeFormat.CODABAR);
//     二维码
        decodeFormats.add(BarcodeFormat.QR_CODE);
//        Data matrix
        decodeFormats.add(BarcodeFormat.DATA_MATRIX);
////        Aztec
//        decodeFormats.add(BarcodeFormat.AZTEC);
////        pdf417 测试
//        decodeFormats.add(BarcodeFormat.PDF_417);

//        HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    @Nullable
    public static String decode(@NonNull Context context, @NonNull Uri uri) {
        String path = UriUtil.getPath(context, uri);
        if (TextUtils.isEmpty(path)){
            return null;
        }
        Bitmap bitmap = getBitmap(path);
        if (bitmap == null){
            return null;
        }
        return decode(bitmap);
    }

    @Nullable
    public static String decode(@NonNull String path) {
        Bitmap bitmap = getBitmap(path);
        if (bitmap == null){
            return null;
        }
        return decode(bitmap);
    }

    @Nullable
    public static String decode(@Nullable Bitmap bitmap) {
        if (bitmap == null){
            return null;
        }
        Result result;
        RGBLuminanceSource source = null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            source = new RGBLuminanceSource(width, height, pixels);
            result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
            return result.getText();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            if (source != null) {
                try {
                    result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                    return result.getText();
                } catch (Throwable e2) {
                    Log.e(TAG, e2.getMessage(), e2);
                }
            }
            return null;
        }finally {
            bitmap.recycle();
        }
    }


    private static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            ExifInterface older = new ExifInterface(path);
            int degree = getBitmapDegree(older);
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, 1080, 1920);
            options.inJustDecodeBounds = false;
            bitmap =  BitmapFactory.decodeFile(path, options);
            if (degree != 0) {
                bitmap = rotateBitmapByDegree(bitmap, degree);
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            if (bitmap != null){
                bitmap.recycle();
            }
            return null;
        }
    }

    private static int getBitmapDegree(ExifInterface exifInterface) {
        int degree = 0;

        // 从指定路径下读取图片，并获取其EXIF信息
        // 获取图片的旋转信息
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (reqWidth == 0 && reqHeight == 0) {
            inSampleSize = 1;
        } else if (reqWidth == 0 && height > reqHeight) {
            inSampleSize = (int) Math.ceil((double) height / (double) reqHeight);
        } else if (reqHeight == 0 && width > reqWidth) {

            inSampleSize = (int) Math.ceil((double) width / (double) reqWidth);
        } else if (height > reqHeight || width > reqWidth) {

            final int heightRatio = (int) Math.ceil((double) height / (double) reqHeight);
            final int widthRatio = (int) Math.ceil((double) width / (double) reqWidth);

            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    private static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        try {
            // 根据旋转角度，生成旋转矩阵
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            if (returnBm == null) {
                returnBm = bm;
            }
            if (bm != returnBm) {
                bm.recycle();
            }
            return returnBm;
        }catch (Exception e){
            Log.e(TAG, e.getMessage(), e);
            if (returnBm != null){
                returnBm.recycle();
            }
            return bm;
        }
    }
}
