package myapp.lenovo.viewpager.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import myapp.lenovo.viewpager.bmob.MyBmob;
import myapp.lenovo.viewpager.entity.MyUser;
import myapp.lenovo.viewpager.R;

public class  PhoneLoginActivity extends Activity implements MyBmob.OperateDoneListener{
    private static final String TAG = "PhoneLoginActivity";

    private EditText logPhone;
    private EditText logPhoneSMS;
    private ImageButton logBack;
    private TextView logExit;
    private Button requestPhoneSMS;
    private Button entryPhone;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initView();
    }

    public void initView(){
        logPhone= (EditText) findViewById(R.id.phone_et);
        logPhoneSMS= (EditText) findViewById(R.id.phone_sms_et);
        logBack= (ImageButton) findViewById(R.id.phone_back_ib);
        logExit= (TextView) findViewById(R.id.exit_tv);
        requestPhoneSMS= (Button) findViewById(R.id.request_sms_btn);
        entryPhone= (Button) findViewById(R.id.register_entry_btn);
        logPhone.setHint("手机");
        logPhoneSMS.setHint("验证码");
        logBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        logExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        requestPhoneSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone=logPhone.getText().toString();
                if(phone.length()!=11){
                    Toast.makeText(PhoneLoginActivity.this,"请输入有效的11位手机号",Toast.LENGTH_SHORT).show();
                }
                else {
                    MyBmob.requestPhoneSMS(phone ,PhoneLoginActivity.this);
                }
            }
        });
        entryPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone=logPhone.getText().toString();
                String phoneSMS=logPhoneSMS.getText().toString();
                MyBmob.LoginBmobUserByPhone(phone,phoneSMS,PhoneLoginActivity.this);
            }
        });
    }

//    public void requestPhoneSMSByPhone(String phone){
//        BmobSMS.requestSMSCode(phone, "验证码", new QueryListener<Integer>() {
//            @Override
//            public void done(Integer integer, BmobException e) {
//                if(e==null){
//                    Log.d("SMS success",integer+"");
//                    requestPhoneSMS.setClickable(false);
//                    requestPhoneSMS.setBackgroundColor(Color.LTGRAY);
//                    Toast.makeText(PhoneLoginActivity.this,"验证码发送成功，请尽快使用",Toast.LENGTH_SHORT).show();
//                    countDownTimer=new CountDownTimer(60000, 1000) {
//                        @Override
//                        public void onTick(long l) {
//                            requestPhoneSMS.setText(l/1000+"秒");
//                            requestPhoneSMS.setPadding(3,3,3,3);
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            requestPhoneSMS.setClickable(true);
//                            requestPhoneSMS.setBackgroundResource(R.drawable.phone_sms_background);
//                            requestPhoneSMS.setText("重新发送");
//                        }
//                    }.start();
//                }
//                else{
//                    Log.d("SMS fail",e.getMessage());
//                }
//            }
//        });
//    }

//    public void signOrLoginByPhone(String phone,String phoneSMS){
//        MyUser bmobUser=new MyUser();
//        bmobUser.setMobilePhoneNumber(phone);
//        bmobUser.setPassword("123456");
//        bmobUser.signOrLogin(phoneSMS, new SaveListener<MyUser>() {
//            @Override
//            public void done(MyUser myUser, BmobException e) {
//                if(e==null){
//                    Log.d("login success",myUser.toString());
//                    Toast.makeText(PhoneLoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
//                    Intent intent=new Intent(PhoneLoginActivity.this,MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//                else{
//                    Toast.makeText(PhoneLoginActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
//                    Log.d("login fail","登录失败：code="+e.getErrorCode()+"，错误描述："+e.getLocalizedMessage());
//                }
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
    }

    @Override
    public void onoperateDone(int errorCode, int which) {
        if (which == MyBmob.REQUEST_LOGIN_PHONESMS){
            if(errorCode > 0){
                Log.d(TAG,"request succeed");
                requestPhoneSMS.setClickable(false);
                requestPhoneSMS.setBackgroundColor(Color.LTGRAY);
                Toast.makeText(PhoneLoginActivity.this,"验证码发送成功，请尽快使用",Toast.LENGTH_SHORT).show();
                countDownTimer=new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long l) {
                        requestPhoneSMS.setText(l/1000+"秒");
                        requestPhoneSMS.setPadding(3,3,3,3);
                    }

                    @Override
                    public void onFinish() {
                        requestPhoneSMS.setClickable(true);
                        requestPhoneSMS.setBackgroundResource(R.drawable.phone_sms_background);
                        requestPhoneSMS.setText("重新发送");
                    }
                }.start();
            }
            else{
                Log.d(TAG,"request fail2");
                Toast.makeText(PhoneLoginActivity.this,"验证码发送失败",Toast.LENGTH_SHORT).show();
            }
        }
        else if (which == MyBmob.LOGIN_BY_PHONESMS){
            if(errorCode > 0){
                Log.d(TAG,"login success2");
                Toast.makeText(PhoneLoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(PhoneLoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Log.d(TAG,"request fail2");
                Toast.makeText(PhoneLoginActivity.this,"登录失败，请重新输入验证码",Toast.LENGTH_SHORT).show();
                logPhoneSMS.setText("");
            }
        }
    }
}
