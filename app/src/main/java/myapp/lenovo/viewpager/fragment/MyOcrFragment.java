package myapp.lenovo.viewpager.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

import myapp.lenovo.viewpager.R;
import myapp.lenovo.viewpager.activity.TakePictureActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyOcrFragment extends Fragment {

    private static final String TAG = "MyOcrFragment";
    private static final int REQUEST_CAMERA_MODE = 454;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private CollapsingToolbarLayout ctl;
    private FloatingActionButton recognize;
    private LinearLayout originalPictureLayout;
    private ImageView originalPicture;
    private TextView recognizeResult;

    private ProgressDialog dialog;

    private Uri uri;
    private String result;
    private TessBaseAPI tessBaseAPI;

    private OcrContent ocrContent;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tessBaseAPI = new TessBaseAPI();
            String dataPath = Environment.getExternalStorageDirectory() + "/tesseract/";
            String language = "eng+chi_sim";
            tessBaseAPI.init(dataPath, language);
            tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            tessBaseAPI.setImage(convertGray(getBitmapFromUri(uri)));
            result = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();
            Message message = new Message();
            message.arg1 = 0;
            handler.sendMessage(message);
        }
    };

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 0) {
                recognizeResult.setText(result);
                dialog.dismiss();
                ocrContent.getOcrContent(uri, result);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_my_ocr, container, false);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        if(context!=null){
            ocrContent=(OcrContent)context;
        }
        super.onAttach(context);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == 3) {
            uri = data.getData();
            originalPicture.setImageURI(uri);
            originalPictureLayout.setVisibility(View.VISIBLE);
            ctl.setTitle("文本识别");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_MODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(getContext(), TakePictureActivity.class);
                startActivityForResult(intent, 2);
            }
            else {
                Toast.makeText(getContext(), "请打开摄像头", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void init(View view){
//        Toolbar toolbar= (Toolbar) view.findViewById(R.id.tool_bar);
//        getActivity().setSupportActionBar(toolbar);
//        ActionBar actionBar=getSupportActionBar();
//        if(actionBar!=null){
//            actionBar.setTitle("文本识别");
//            actionBar.setDisplayHomeAsUpEnabled(false);
//        }
        ctl= (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        recognize = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        originalPictureLayout = (LinearLayout) view.findViewById(R.id.original_picture_ll);
        originalPicture = (ImageView) view.findViewById(R.id.original_picture_iv);
        recognizeResult = (TextView) view.findViewById(R.id.recognize_result_tv);

        ctl.setTitle("点击上传照片");
        ctl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });
        originalPictureLayout.setVisibility(View.GONE);
        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ProgressDialog(getContext());
                dialog.setMessage("识别中...");
                dialog.setCancelable(false);
                dialog.show();

                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    private Bitmap getBitmapFromUri(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromUri");
        }
        return bitmap;
    }

    private Bitmap convertGray(Bitmap originalBitmap){
        Bitmap result = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);
        return result;
    }

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(getContext(), PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSION_CAMERA}, REQUEST_CAMERA_MODE);
        }
        else {
            Intent intent = new Intent(getContext(), TakePictureActivity.class);
            startActivityForResult(intent, 2);
        }
    }

    public interface OcrContent{
        void getOcrContent(Uri uri,String html);
    }

    public class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
        }
    }

}