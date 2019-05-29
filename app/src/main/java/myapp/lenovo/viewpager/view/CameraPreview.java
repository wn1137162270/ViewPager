package myapp.lenovo.viewpager.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import myapp.lenovo.viewpager.utils.Utils;

/**
 * Created by wn on 2019/4/22.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback{
    private static final String TAG = "CameraPreview";

    private SurfaceHolder holder;
    private Camera camera;
    private FocusView focusView;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private OnCameraStatusListener listener;

    public interface OnCameraStatusListener{
        void onCameraStopped(byte[] data);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    int width = focusView.getWidth();
                    int height = focusView.getHeight();
                    focusView.setX(motionEvent.getX() - width / 2.0f);
                    focusView.setY(motionEvent.getY() - height / 2.0f);
                    focusView.beginFocus();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    setFocusOnTouch(motionEvent);
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!Utils.hasCameraHardware(getContext())){
            Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        camera = getCameraInstance();
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "拍照失败！", Toast.LENGTH_SHORT).show();
            camera.release();
            camera = null;
        }
        updateCameraParameters();
        camera.setDisplayOrientation(90);
        if (camera != null){
            camera.startPreview();
            setAutoFocus();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
            camera.stopPreview();
        }
        catch (Exception e){
            Log.e(TAG, "surfaceChanged");
        }
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "拍照失败！", Toast.LENGTH_SHORT).show();
            camera.release();
            camera = null;
        }
        updateCameraParameters();
        camera.setDisplayOrientation(90);
        if (camera != null){
            camera.startPreview();
            setAutoFocus();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.release();
        camera = null;
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {}

    private Camera getCameraInstance(){
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        try {
            for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex ++){
                Camera.getCameraInfo(cameraIndex, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                    c = Camera.open(cameraIndex);
                }
            }
            if (c == null){
                c = Camera.open(0);
            }
        }
        catch (Exception e){
            Log.e(TAG,"getCameraInstance open camera failed");
            Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    private void updateCameraParameters(){
        if (camera != null){
            Camera.Parameters parameters = camera.getParameters();
            setCameraParameters(parameters);
            try {
                Camera.Size size = getPreviewSizeByScreen();
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPictureSize(size.width, size.height);
                camera.setParameters(parameters);
            }
            catch (Exception e){
                Camera.Size size = getBestPreviewSize(parameters);
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPictureSize(size.width, size.height);
                camera.setParameters(parameters);
            }
        }
    }

    private void setCameraParameters(Camera.Parameters parameters){
        List<String> focusModes = parameters.getSupportedFocusModes();
        long time = new Date().getTime();
        parameters.setGpsTimestamp(time);
        parameters.setPictureFormat(PixelFormat.JPEG);
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        else {
            Log.d(TAG, "don't have FOCUS_MODE_AUTO");
            Toast.makeText(getContext(), "自动聚焦失败！", Toast.LENGTH_SHORT).show();
        }
    }

    private Camera.Size getPreviewSizeByScreen(){
        if (viewWidth != 0 && viewHeight != 0){
            return camera.new Size(viewHeight, viewWidth);
        }
        else {
            return camera.new Size(Utils.getScreenWidthHeight(getContext()).heightPixels,
                    Utils.getScreenWidthHeight(getContext()).widthPixels);
        }
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters){
        String previewSizeValueString = null;
        previewSizeValueString = parameters.get("preview-size-values");

        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return camera.new Size(Utils.getScreenWidthHeight(getContext()).widthPixels,
                    Utils.getScreenWidthHeight(getContext()).heightPixels);
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRadio = 0;
        float viewRadio = 0;

        if (viewWidth != 0 && viewHeight != 0) {
            viewRadio = Math.min((float) viewWidth, (float) viewHeight)
                    / Math.max((float) viewWidth, (float) viewHeight);
        }

        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String prewsizeString : COMMA_PATTERN) {
            prewsizeString = prewsizeString.trim();

            int dimPosition = prewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
                newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRadio == 0) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return camera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    public void setFocusView(FocusView focusView){
        this.focusView = focusView;
    }

    public void setAutoFocus(){
        if(!focusView.isFocusing()){
            try {
                camera.autoFocus(this);
            }
            catch (Exception e){
                Log.e(TAG, "auto focus failed");
                Toast.makeText(getContext(), "自动聚焦失败！", Toast.LENGTH_SHORT).show();
            }
            focusView.setX(Utils.getScreenWidthHeight(getContext()).widthPixels / 2 - focusView.getWidth() / 2);
            focusView.setY(Utils.getScreenWidthHeight(getContext()).heightPixels / 2 - focusView.getHeight() / 2);
            focusView.beginFocus();
        }
    }

    private void setFocusOnTouch(MotionEvent motionEvent){
        int[] location = new int[2];
        RelativeLayout relativeLayout = (RelativeLayout) getParent();
        relativeLayout.getLocationOnScreen(location);
        Rect meteringRect = Utils.calculateTapArea(focusView.getWidth(),
                focusView.getHeight(), 1.5f, motionEvent.getRawX(), motionEvent.getRawY(),
                location[0], location[0] + relativeLayout.getWidth(),
                location[1], location[1] + relativeLayout.getHeight());
        Rect focusRect = Utils.calculateTapArea(focusView.getWidth(),
                focusView.getHeight(), 1f, motionEvent.getRawX(), motionEvent.getRawY(),
                location[0], location[0] + relativeLayout.getWidth(),
                location[1], location[1] + relativeLayout.getHeight());
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getMaxNumMeteringAreas() > 0){
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }
        if (parameters.getMaxNumFocusAreas() > 0){
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(focusAreas);
        }
        try {
            camera.setParameters(parameters);
        }
        catch (Exception e){
            Toast.makeText(getContext(), "聚焦失败1！", Toast.LENGTH_SHORT).show();
        }
        try {
            camera.autoFocus(this);
        }
        catch (Exception e){
            Toast.makeText(getContext(), "聚焦失败2 ！", Toast.LENGTH_SHORT).show();
        }
    }

    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }

    public void takePicture(){
        if (camera != null){
            try {
                camera.takePicture(null, null,  new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        try {
                            camera.stopPreview();
                        }
                        catch (Exception e){
                            Log.e(TAG, "takePicture---onPictureTaken");
                        }
                        if (listener != null){
                            listener.onCameraStopped(bytes);
                        }
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(getContext(), "拍照失败！", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), "拍照失败", Toast.LENGTH_SHORT).show();
        }
    }
}
