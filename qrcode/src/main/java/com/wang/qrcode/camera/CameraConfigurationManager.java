package com.wang.qrcode.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.zxing.client.android.camera.CameraConfigurationUtils;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */

final class CameraConfigurationManager {


    private static final String TAG = "CameraConfiguration";

    private final Context context;
    private int cwNeededRotation;
    private int cwRotationFromDisplayToCamera;
    private Point screenResolution;
//    private Point cameraResolution;
    private Point scanViewSize;
    private Point bestPreviewSize;
    private Point previewSizeOnScreen;

    CameraConfigurationManager(@NonNull Context context) {
        this.context = context;
    }

    /**
     * 初始化相机
     */
    boolean initFromCameraParameters(@NonNull Camera camera, int facing, int orientation, int scanWidth, int scanHeight) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) {
            Log.e(TAG, "the camera init error");
            return false;
        }
        Display display = manager.getDefaultDisplay();

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    Log.e(TAG, "Bad rotation: " + displayRotation);
                    return false;
                }
        }
        Log.i(TAG, "Display at: " + cwRotationFromNaturalToDisplay);

        int cwRotationFromNaturalToCamera = orientation;
        Log.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

        // Still not 100% sure about this. But acts like we need to flip this:
        if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
            Log.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
        }

        cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
        Log.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);
        if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Log.i(TAG, "Compensating rotation for front camera");
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }
        Log.i(TAG, "Clockwise rotation from display to camera: " + cwNeededRotation);

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
        Log.i(TAG, "Screen resolution in current orientation: " + screenResolution);
//        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, scanViewSize);
//        Log.i(TAG, "Camera resolution: " + cameraResolution);
        this.scanViewSize = new Point(scanWidth, scanHeight);
        Log.i(TAG, "Scan view size: " + this.scanViewSize);
        bestPreviewSize = /*new Point(cameraResolution);*/CameraConfigurationUtils.findBestPreviewSizeValue(parameters, this.scanViewSize);
        Log.i(TAG, "Best available preview size: " + bestPreviewSize);

        boolean isScreenPortrait = screenResolution.x < screenResolution.y;
        boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;

        if (isScreenPortrait == isPreviewSizePortrait) {
            previewSizeOnScreen = bestPreviewSize;
        } else {
            previewSizeOnScreen = new Point(bestPreviewSize.y, bestPreviewSize.x);
        }
        Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
        return true;
    }

    /**
     * 设置相机参数
     *
     * @param camera            相机
     * @param lightModel        闪光灯模式
     * @param exposure          曝光
     * @param autoFocus         自动对焦
     * @param disableContinuous 是否持续自动对焦
     * @param invertColor       反色
     * @param barcodeSceneMode  条形码场景匹配
     * @param metering          距离测量
     * @param safeMode          安全模式
     */
    void setDesiredCameraParameters(Camera camera,
                                    @Mode int lightModel,
                                    boolean exposure,
                                    boolean autoFocus,
                                    boolean disableContinuous,
                                    boolean invertColor,
                                    boolean barcodeSceneMode,
                                    boolean metering,
                                    boolean safeMode) {

        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }


        initializeTorch(parameters, lightModel, exposure, safeMode);

        CameraConfigurationUtils.setFocus(parameters, autoFocus, disableContinuous, safeMode);

        if (!safeMode) {
            if (invertColor) {
                CameraConfigurationUtils.setInvertColor(parameters);
            }

            if (barcodeSceneMode) {
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }

            if (metering) {
                CameraConfigurationUtils.setVideoStabilization(parameters);
                CameraConfigurationUtils.setFocusArea(parameters);
                CameraConfigurationUtils.setMetering(parameters);
            }

            //SetRecordingHint to true also a workaround for low framerate on Nexus 4
            //https://stackoverflow.com/questions/14131900/extreme-camera-lag-on-nexus-4
            parameters.setRecordingHint(true);

        }

        parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y);

        camera.setParameters(parameters);

        camera.setDisplayOrientation(cwRotationFromDisplayToCamera);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null && (bestPreviewSize.x != afterSize.width || bestPreviewSize.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size " + bestPreviewSize.x + 'x' + bestPreviewSize.y +
                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            bestPreviewSize.x = afterSize.width;
            bestPreviewSize.y = afterSize.height;
            boolean isScreenPortrait = screenResolution.x < screenResolution.y;
            boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;

            if (isScreenPortrait == isPreviewSizePortrait) {
                previewSizeOnScreen = bestPreviewSize;
            } else {
                previewSizeOnScreen = new Point(bestPreviewSize.y, bestPreviewSize.x);
            }
            Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
        }
    }

    Point getBestPreviewSize() {
        return bestPreviewSize;
    }

    Point getPreviewSizeOnScreen() {
        return previewSizeOnScreen;
    }

//    Point getCameraResolution() {
//        return cameraResolution;
//    }

    Point getScanViewSize() {
        return scanViewSize;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    int getCWNeededRotation() {
        return cwNeededRotation;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null &&
                        (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
                                Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera, boolean on, boolean exposure, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, on, exposure, safeMode);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters, int lightModel, boolean exposure, boolean safeMode) {
        boolean on = lightModel == Light.ON;
        doSetTorch(parameters, on, exposure, safeMode);
    }


    private void doSetTorch(Camera.Parameters parameters, boolean on, boolean exposure, boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, on);
        if (!safeMode && exposure) {
            CameraConfigurationUtils.setBestExposure(parameters, on);
        }
    }

}
