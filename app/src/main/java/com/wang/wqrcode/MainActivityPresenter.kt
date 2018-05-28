package com.wang.wqrcode

import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.text.TextUtils
import com.trello.rxlifecycle2.LifecycleProvider
import com.wang.qrcode.encoder.CodeEncoder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference

/**
 * Author: wangxiaojie6
 * Date: 2018/5/22
 */
class MainActivityPresenter(private val mView: IView, private val mProvider: LifecycleProvider<Lifecycle.Event>) {

    fun getHomeQRCode(context: Context, code: String) {
        val weak = WeakReference(context)
        Flowable.just(code)
                .map {
                    weak.get()?.let {
                        val op = CodeEncoder.Options().apply {
                            logo = BitmapFactory.decodeResource(it.resources, R.mipmap.ic_as)
                        }
                        CodeEncoder.createQRCode(false, code, SizeUtil.dp2pxSize(it, 222f), op)
                    } ?: throw Exception("context is null")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(object : DisposableSubscriber<Bitmap>() {
                    override fun onComplete() {

                    }

                    override fun onNext(t: Bitmap) {
                        mView.getQRCodeSuccess(t)
                    }

                    override fun onError(t: Throwable) {

                    }

                })
    }

    fun saveBitmap(bitmap: Bitmap?, name: String) {
        bitmap?.let {
            Flowable.just(it)
                    .map {
                        var baos: ByteArrayOutputStream? = null
                        try {
                            val dir = File(FileUtil.getInternalSDCardPath() + File.separator + "qrcode")
                            dir.mkdir()
                            baos = ByteArrayOutputStream()
                            it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val file = FileUtil.save(baos.toByteArray(), File(dir, name), true)
                                    ?: throw Exception("save failure")
                            file.absolutePath

                        } finally {
                            if (baos != null) {
                                baos.close()
                            }
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(mProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                    .subscribe(object : DisposableSubscriber<String>() {
                        override fun onComplete() {

                        }

                        override fun onNext(path: String) {
                            mView.saveBitmapSuccess(path)
                        }

                        override fun onError(t: Throwable) {

                        }

                    })
        }
    }

    interface IView {

        fun getQRCodeSuccess(bitmap: Bitmap)

        fun saveBitmapSuccess(path: String)

    }
}