package com.wang.wqrcode

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri

/**
 * Author: wangxiaojie6
 * Date: 2017/11/22
 */
class PhotoScannerManager private constructor(context: Context): MediaScannerConnection.MediaScannerConnectionClient{

    private var mScanner: MediaScannerConnection? = MediaScannerConnection(context, this)

    private var mCurrentPhotoPath: String = ""

    companion object {

        @Volatile private var sInstance: PhotoScannerManager? = null

        @JvmStatic
        fun get(context: Context): PhotoScannerManager {
            var manager = sInstance
            return manager?:let {
                synchronized(PhotoScannerManager::class.java){
                    manager = sInstance
                    manager?:let {
                        val temp = PhotoScannerManager(context.applicationContext)
                        sInstance = temp
                        temp
                    }
                }
            }
        }
    }

    fun connect(path: String){
        mCurrentPhotoPath = path
        mScanner?.connect()
    }

    fun disconnect(){
        mScanner?.disconnect()
        mScanner?.onServiceDisconnected(null)
        mScanner = null
    }

    override fun onMediaScannerConnected() {
        mScanner?.scanFile(mCurrentPhotoPath, "image/*")
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        mScanner?.disconnect()
    }
}