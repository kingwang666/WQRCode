package com.wang.qrcode.widget;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import com.wang.qrcode.camera.CameraManager;
import com.wang.qrcode.model.ScanResult;

import java.lang.ref.WeakReference;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Author: wangxiaojie6
 * Date: 2018/3/16
 */

final class ScanViewPresenter {

    private final WeakReference<IView> mView;
    private final WeakReference<CameraManager> mManager;

    private final MultiFormatReader mReader;

    ScanViewPresenter(CameraManager manager, Map<DecodeHintType, Object> hints, IView view) {
        mManager = new WeakReference<>(manager);
        mView = new WeakReference<>(view);
        mReader = new MultiFormatReader();
        mReader.setHints(hints);
    }

    @NonNull
    Disposable initSubject() {
        return Flowable.create(
                new FlowableOnSubscribe<byte[]>() {
                    @Override
                    public void subscribe(FlowableEmitter<byte[]> emitter) throws Exception {
                        IView view = mView.get();
                        if (view != null) {
                            view.setSubject(emitter);
                        }
                    }
                }, BackpressureStrategy.LATEST)
                .observeOn(Schedulers.io())
                .map(new Function<byte[], ScanResult>() {
                    @Override
                    public ScanResult apply(byte[] bytes) throws Exception {
                        CameraManager manager = mManager.get();
                        Result rawResult = null;
                        if (manager != null) {
                            PlanarYUVLuminanceSource source = manager.buildLuminanceSource(bytes);
                            if (source != null) {
                                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                                if (mReader != null) {
                                    try {
                                        rawResult = mReader.decodeWithState(bitmap);
                                    } catch (ReaderException re) {
                                        // continue
                                    } finally {
                                        mReader.reset();
                                    }
                                }
                            }
                        }
                        if (rawResult == null){
                            return new ScanResult(false, null);
                        }
                        return new ScanResult(true, ResultParser.parseResult(rawResult));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ScanResult>() {
                    @Override
                    public void accept(ScanResult result) throws Exception {
                        IView view = mView.get();
                        if (view != null) {
                            view.onScan(result);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        IView view = mView.get();
                        if (view != null) {
                            view.onScanError(throwable);
                        }
                    }
                });
    }

    interface IView {

        void setSubject(FlowableEmitter<byte[]> emitter);

        void onScan(ScanResult result);

        void onScanError(Throwable throwable);
    }
}
