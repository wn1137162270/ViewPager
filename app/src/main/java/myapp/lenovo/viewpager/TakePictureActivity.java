package myapp.lenovo.viewpager;


import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import myapp.lenovo.viewpager.camera.CameraPreview;
import myapp.lenovo.viewpager.camera.FocusView;
import myapp.lenovo.viewpager.utils.Utils;

public class TakePictureActivity extends AppCompatActivity implements CameraPreview.OnCameraStatusListener, SensorEventListener{
    private static final String TAG = "TakePictureActivity";
    private static final boolean IS_TRANSVERSE = true;
    private static final String PATH = Environment.getExternalStorageDirectory() + "/AndroidMedia/";

    private CameraPreview cameraPreview;
    private FocusView focusView;
    private TextView hint;
    private RelativeLayout takePictureLayout;
    private Button album;
    private ImageButton shutter;
    private ImageButton close;

    private CropImageView cropImageView;
    private LinearLayout cropLayout;
    private ImageButton closeCrop;
    private ImageButton confirmCrop;

    private boolean isRotated = false;

    private float lastX;
    private float lastY;
    private float lastZ;
    private boolean sensorChangeFirst = true;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.album_btn:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 1);
                    break;
                case R.id.shutter_ib:
                    cameraPreview.takePicture();
                    break;
                case R.id.close_ib:
                    finish();
                    break;
                case R.id.close_crop_ib:
                    showTakePictureLayout();
                    break;
                case R.id.confirm_crop_ib:
                    Bitmap croppedBitmap = cropImageView.getCroppedImage();
                    croppedBitmap = Utils.rotate(croppedBitmap, -90);
                    long time = System.currentTimeMillis();
                    String fileName = DateFormat.format("yyyy-MM-dd kk.mm.ss", time) + ".jpg";
                    Uri uri = saveImage(time, fileName, croppedBitmap);
                    croppedBitmap.recycle();
                    Intent showCroppedIntent = new Intent();
                    showCroppedIntent.setData(uri);
                    showCroppedIntent.putExtra("path", PATH + fileName);
                    showCroppedIntent.putExtra("width", croppedBitmap.getWidth());
                    showCroppedIntent.putExtra("height", croppedBitmap.getHeight());
                    setResult(3, showCroppedIntent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_picture);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
    }

    private void init(){
        cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        focusView = (FocusView) findViewById(R.id.focus_view);
        hint = (TextView) findViewById(R.id.hint_tv);
        takePictureLayout = (RelativeLayout) findViewById(R.id.take_picture_layout);
        album = (Button) findViewById(R.id.album_btn);
        shutter = (ImageButton) findViewById(R.id.shutter_ib);
        close = (ImageButton) findViewById(R.id.close_ib);

        cropImageView = (CropImageView) findViewById(R.id.crop_iv);
        cropLayout = (LinearLayout) findViewById(R.id.crop_layout);
        closeCrop = (ImageButton) findViewById(R.id.close_crop_ib);
        confirmCrop = (ImageButton) findViewById(R.id.confirm_crop_ib);

        album.setOnClickListener(onClickListener);
        shutter.setOnClickListener(onClickListener);
        close.setOnClickListener(onClickListener);
        closeCrop.setOnClickListener(onClickListener);
        confirmCrop.setOnClickListener(onClickListener);

        cropImageView.setGuidelines(2);

        cameraPreview.setFocusView(focusView);
        cameraPreview.setOnCameraStatusListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (IS_TRANSVERSE){
            if (!isRotated){
                ObjectAnimator animatorHint = ObjectAnimator.ofFloat(hint, "rotation", 0f, 90f);
                animatorHint.setInterpolator(new LinearInterpolator());
                animatorHint.setDuration(500);
                animatorHint.setStartDelay(800);
                animatorHint.start();
                ObjectAnimator animatorShutter = ObjectAnimator.ofFloat(shutter, "rotation", 0f, 90f);
                animatorShutter.setInterpolator(new LinearInterpolator());
                animatorShutter.setDuration(500);
                animatorShutter.setStartDelay(800);
                animatorShutter.start();
                isRotated = true;
            }
        }
        else {
            if (!isRotated){
                //code
                isRotated = true;
            }
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            Uri uri = data.getData();
            ContentResolver contentResolver = getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                bitmap = Utils.rotate(bitmap, 90);
                cropImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        showCropLayout();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        if (sensorChangeFirst){
            lastX = x;
            lastY = y;
            lastZ = z;
            sensorChangeFirst = false;
        }
        float subX = Math.abs(x - lastX);
        float subY = Math.abs(y - lastY);
        float subZ = Math.abs(z - lastZ);
        if (subX > 0.8 ||subY > 0.8 ||subZ > 0.8){
            cameraPreview.setAutoFocus();
        }
        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onCameraStopped(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        long time = System.currentTimeMillis();
        String fileName = DateFormat.format("yyyy-MM-dd kk.mm.ss", time) + ".jpg";
        saveImage(time, fileName, bitmap);
        bitmap = Utils.rotate(bitmap, 90);
        cropImageView.setImageBitmap(bitmap);
        showCropLayout();
    }

    private void showCropLayout(){
        takePictureLayout.setVisibility(View.GONE);
        cropLayout.setVisibility(View.VISIBLE);
        //code
    }

    private void showTakePictureLayout(){
        takePictureLayout.setVisibility(View.VISIBLE);
        cropLayout.setVisibility(View.GONE);
    }

    private Uri saveImage(long time, String fileName, Bitmap bitmap){
        FileOutputStream fos = null;
        try {
            File dir = new File(PATH);
            if (!dir.exists()){
                dir.mkdir();
            }
            File file = new File(PATH, fileName);
            if (file.createNewFile()){
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        String filePath = PATH + fileName;
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, filePath);
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}

