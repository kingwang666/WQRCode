/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wang.qrcode.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
public final class CameraManager implements Camera.AutoFocusCallback {

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 675;
    private static final int MAX_FRAME_HEIGHT = 675;

    private final CameraConfigurationManager configManager;
    private OnLightChangeListener mOnLightChangeListener;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int cameraId = -1;
    private boolean userScreen = true;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;

    public CameraManager(Context context, boolean userScreen) {
        this.configManager = new CameraConfigurationManager(context);
        this.userScreen = userScreen;
    }

    public void setOnLightChangeListener(OnLightChangeListener onLightChangeListener) {
        mOnLightChangeListener = onLightChangeListener;
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder    The surface object which the camera will draw preview frames into.
     * @param autoFocus
     * @param lightMode @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void open(SurfaceHolder holder, int width, int height, boolean autoFocus, int lightMode) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            int numCameras = Camera.getNumberOfCameras();
            if (numCameras == 0) {
                throw new IOException("no camera!!");
            }
            boolean explicitRequest = cameraId >= 0;
            int index;
            if (explicitRequest) {
                index = cameraId;

            } else {
                index = 0;
                while (index < numCameras) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(index, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        break;
                    }
                    index++;
                }
            }

            if (index < numCameras) {
                Log.i(TAG, "Opening camera #" + index);
                theCamera = Camera.open(index);
                cameraId = index;
            } else {
                if (explicitRequest) {
                    throw new IOException("Requested camera does not exist: " + cameraId);
                } else {
                    Log.i(TAG, "No camera facing back; returning camera #0");
                    theCamera = Camera.open(0);
                    cameraId = 0;
                }
            }
            this.camera = theCamera;
        }

        if (!initialized) {
            initialized = true;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (!configManager.initFromCameraParameters(theCamera, cameraInfo.facing, cameraInfo.orientation, width, height)) {
                throw new IOException("the camera init error");
            }
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
//                requestedFramingRectWidth = 0;
//                requestedFramingRectHeight = 0;
            }
        }
        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
        try {
            configManager.setDesiredCameraParameters(theCamera, lightMode, false, autoFocus, false, false, false, false, false);
        } catch (RuntimeException re) {
            // Driver failed
            Log.e(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            if (parametersFlattened != null) {
                parameters = theCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    theCamera.setParameters(parameters);
                    configManager.setDesiredCameraParameters(theCamera, lightMode, false, autoFocus, false, false, false, false, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }
        theCamera.setPreviewDisplay(holder);

    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void close(boolean clearRect) {
        if (camera != null) {
            camera.release();
            camera = null;
            initialized = false;
            previewing = false;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            if (clearRect) {
                framingRect = null;
                framingRectInPreview = null;

            }
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview(Camera.PreviewCallback callback) {
        Camera theCamera = camera;
        if (theCamera != null) {
            if (!previewing) {
                theCamera.startPreview();
                previewing = true;
            }
            theCamera.setOneShotPreviewCallback(callback);
        }
    }

    public synchronized void setOneShotPreviewCallback(Camera.PreviewCallback callback) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            theCamera.setOneShotPreviewCallback(callback);
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewing = false;
        }
    }

    /**
     * @param on if {@code true}, light should be turned on if currently off. And vice versa.
     */
    public synchronized void setTorch(boolean on) {
        if (previewing) {
            Camera theCamera = camera;
            if (theCamera != null && on != configManager.getTorchState(theCamera)) {
                configManager.setTorch(camera, on, false, true);
                if (mOnLightChangeListener != null){
                    mOnLightChangeListener.onLightChange(on);
                }
            }
        }
    }


    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    @Nullable
    public synchronized Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point size = userScreen ? configManager.getScreenResolution() : configManager.getScanViewSize();
            if (size == null) {
                // Called early, before init even finished
                return null;
            }
            int width;
            int height;
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                if (requestedFramingRectWidth > size.x) {
                    width = size.x;
                } else {
                    width = requestedFramingRectWidth;
                }
                if (requestedFramingRectHeight > size.y) {
                    height = size.y;
                } else {
                    height = requestedFramingRectHeight;
                }
            } else {
                width = findDesiredDimensionInRange(size.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
                height = findDesiredDimensionInRange(size.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);
            }
            int leftOffset = (size.x - width) / 2;
            int topOffset = (size.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    public static int findDesiredWidthInRange(int width) {
        int dim = 5 * width / 8; // Target 5/8 of each dimension
        if (dim < MIN_FRAME_WIDTH) {
            return MIN_FRAME_WIDTH;
        }
        if (dim > MAX_FRAME_WIDTH) {
            return MAX_FRAME_WIDTH;
        }
        return dim;
    }

    public static int findDesiredHeightInRange(int height) {
        int dim = 5 * height / 8; // Target 5/8 of each dimension
        if (dim < MIN_FRAME_HEIGHT) {
            return MIN_FRAME_HEIGHT;
        }
        if (dim > MAX_FRAME_HEIGHT) {
            return MAX_FRAME_HEIGHT;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point previewSizeOnScreen = configManager.getPreviewSizeOnScreen();
            Point size = userScreen ? configManager.getScreenResolution() : configManager.getScanViewSize();
            if (previewSizeOnScreen == null || size == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * previewSizeOnScreen.x / size.x;
            rect.right = rect.right * previewSizeOnScreen.x / size.x;
            rect.top = rect.top * previewSizeOnScreen.y / size.y;
            rect.bottom = rect.bottom * previewSizeOnScreen.y / size.y;

            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    public synchronized void autoFocus() {
        Camera camera = this.camera;
        if (camera != null && previewing) {
            camera.autoFocus(this);
        }
    }

    public synchronized boolean isUserScreen() {
        return userScreen;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }


    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    public synchronized void setManualCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width  The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public synchronized void setManualFramingRect(int width, int height) {
        if (initialized) {
            Point size = userScreen ? configManager.getScreenResolution() : configManager.getScanViewSize();
            if (width > size.x) {
                width = size.x;
            }
            if (height > size.y) {
                height = size.y;
            }
            int leftOffset = (size.x - width) / 2;
            int topOffset = (size.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated manual framing rect: " + framingRect);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data) {
        Point previewSizeOnScreen = configManager.getPreviewSizeOnScreen();
        return buildLuminanceSource(data, previewSizeOnScreen.x, previewSizeOnScreen.y);
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        //add by tancolo
        if (width < height) {
            // portrait
            byte[] rotatedData = new byte[data.length];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++)
                    rotatedData[y * width + width - x - 1] = data[y + x * height];
            }
            data = rotatedData;
        }
        //end add
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    public interface OnLightChangeListener {

        void onLightChange(boolean open);

    }
}
