package com.wang.wqrcode

import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.net.Uri
import com.trello.rxlifecycle2.LifecycleProvider
import com.wang.qrcode.decoder.CodeDecoder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import java.lang.ref.WeakReference

/**
 * Author: wangxiaojie6
 * Date: 2018/5/23
 */
class ScanActivityPresenter(private val mView: IView, private val mProvider: LifecycleProvider<Lifecycle.Event>) {


    fun parseQRCode(context: Context, uri: Uri) {
        val weakContext = WeakReference(context)
        Flowable.just(uri)
                .map { uri ->
                    weakContext.get()?.let {
                        CodeDecoder.decode(it, uri)
                    }?:let {
                        throw NullPointerException("no found qr code")
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mProvider.bindUntilEvent(Lifecycle.Event.ON_DESTROY))
                .subscribe(object : DisposableSubscriber<String>(){
                    override fun onComplete() {

                    }

                    override fun onNext(t: String) {
                        mView.parseQRCodeSuccess(t)
                    }

                    override fun onError(t: Throwable) {
                        mView.parseQRCodeError(t)
                    }

                })
    }



    public interface IView{
        fun parseQRCodeSuccess(t: String)

        fun parseQRCodeError(t: Throwable)
    }
}
