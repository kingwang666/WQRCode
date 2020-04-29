package com.wang.wqrcode;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Author: wangxiaojie6
 * Date: 2018/1/29
 */

public class FileUtil  {

    public static boolean isHaveInternalSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取内置sd卡路径
     */
    @NonNull
    public static String getInternalSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Nullable
    public static File getUnExists(@Nullable String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return file;
        }
        String suffix = "";
        int dot = filePath.lastIndexOf('.');
        if (dot > -1) {
            suffix = filePath.substring(dot);
            filePath = filePath.substring(0, dot);
        }
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            file = new File(String.format(Locale.getDefault(), "%s(%d)%s", filePath, i, suffix));
            if (!file.exists()) {
                return file;
            }
        }
        file = new File(String.format(Locale.getDefault(), "%s%s", filePath, suffix));
        if (file.delete()) {
            return file;
        }
        return null;
    }


    /**
     * 保存文件到url路径下
     */
    @Nullable
    public static File save(byte[] buffer, @Nullable File file, boolean cover) {
        if (file == null) {
            return null;
        }
        if (file.exists()) {
            if (!cover) {
                file = getUnExists(file.getAbsolutePath());
            } else {
                if (!file.delete()) {
                    return null;
                }
            }
        }
        if (file == null) {
            return null;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(buffer);
            out.flush();
        } catch (IOException e) {
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    file = null;
                }
            }
        }
        return file;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    @NonNull
    public static String getPath(@NonNull final Context context, @NonNull final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else if (!isKitKat) {
            String filename = "";
            if (uri.getScheme().compareTo("content") == 0) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(0);
                }
            } else {
                if (uri.getScheme().compareTo("file") == 0) {         //file:///开头的uri{
                    filename = uri.toString().replace("file://", "");
                    //替换file://
                    if (!filename.startsWith("/mnt")) {
                        //加上"/mnt"头
                        filename += "/mnt";
                    }
                }
            }
            return filename;
        }

        return "";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
