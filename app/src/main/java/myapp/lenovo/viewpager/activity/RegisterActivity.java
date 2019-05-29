package myapp.lenovo.viewpager.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import myapp.lenovo.viewpager.R;
import myapp.lenovo.viewpager.bmob.MyBmob;

public class RegisterActivity extends Activity implements MyBmob.OperateDoneListener{
    private static final String TAG = "RegisterActivity";

    private EditText regAccount;
    private EditText regEmail;
    private EditText regPassword;
    private ImageButton regBack;
    private TextView regExit;
    private Button register;

    private List<String> groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        groupName=new ArrayList<>();
        groupName.add("我的文档");
        groupName.add("未归档");
        groupName.add("文件");
        groupName.add("备忘录");
        groupName.add("名片");
        groupName.add("证件");
    }

    public void initView(){
        regAccount= (EditText) findViewById(R.id.account_et);
        regEmail= (EditText) findViewById(R.id.email_et);
        regPassword= (EditText) findViewById(R.id.password_et);
        regBack= (ImageButton) findViewById(R.id.register_back_ib);
        regExit= (TextView) findViewById(R.id.exit_tv);
        register= (Button) findViewById(R.id.register_btn);
        regAccount.setHint("用户名");
        regEmail.setHint("邮箱（可不填）");
        regPassword.setHint("密码");
        regBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        regExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=regAccount.getText().toString();
                String email=regEmail.getText().toString();
                String password=regPassword.getText().toString();
                if(password.length()<6){
                    Toast.makeText(RegisterActivity.this,"密码不能少于六位，请重新输入",Toast.LENGTH_SHORT).show();
                    regAccount.setText(null);
                    regEmail.setText(null);
                    regPassword.setText(null);
                }
                else {
                    MyBmob.registerBmobUserWithEmail(username, email, password, RegisterActivity.this);
                }
            }
        });
    }

    @Override
    public void onoperateDone(int errorCode, int which) {
        if (which == MyBmob.REGISTER){
            if(errorCode == -1){
                Log.d(TAG, "create success");
                Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Log.d(TAG, "create fail");
                if(errorCode==202){
                    Toast.makeText(RegisterActivity.this,"该用户名已注册，请重新输入",Toast.LENGTH_SHORT).show();
                }
                else if(errorCode==203){
                    Toast.makeText(RegisterActivity.this,"该邮箱已注册，请重新输入",Toast.LENGTH_SHORT).show();
                }
                else if(errorCode==301){
                    Toast.makeText(RegisterActivity.this,"邮箱输入有误，请重新输入",Toast.LENGTH_SHORT).show();
                }
                else if(errorCode==304){
                    Toast.makeText(RegisterActivity.this,"用户名或密码不能为空，请重新输入",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegisterActivity.this,"注册失败，请重新输入",Toast.LENGTH_SHORT).show();
                }
                regAccount.setText(null);
                regEmail.setText(null);
                regPassword.setText(null);
            }
        }
    }

//    public void createBombUserWithEmail(String username,String email,String password){
//        MyUser bmobUser=new MyUser();
//        bmobUser.setUsername(username);
//        bmobUser.setPassword(password);
//        if(email!=null&& !TextUtils.isEmpty(email)){
//            bmobUser.setEmail(email);
//        }
//        //bmobUser.setGroupName(groupName);
//        bmobUser.signUp(new SaveListener<MyUser>() {
//            @Override
//            public void done(MyUser myUser, BmobException e) {
//                if(e==null){
//                    Log.d("create success",myUser.toString());
//                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
//                    if(myUser!=null){
//                        BmobUser.logOut();
//                    }
//                    Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//                else{
//                    Log.d("create fail",e.toString());
//                    if(e.getErrorCode()==202){
//                        Toast.makeText(RegisterActivity.this,"该用户名已注册，请重新输入",Toast.LENGTH_SHORT).show();
//                        regAccount.setText(null);
//                        regEmail.setText(null);
//                        regPassword.setText(null);
//                    }
//                    else if(e.getErrorCode()==203){
//                        Toast.makeText(RegisterActivity.this,"该邮箱已注册，请重新输入",Toast.LENGTH_SHORT).show();
//                        regAccount.setText(null);
//                        regEmail.setText(null);
//                        regPassword.setText(null);
//                    }
//                    else if(e.getErrorCode()==301){
//                        Toast.makeText(RegisterActivity.this,"邮箱输入有误，请重新输入",Toast.LENGTH_SHORT).show();
//                        regAccount.setText(null);
//                        regEmail.setText(null);
//                        regPassword.setText(null);
//                    }
//                    else if(e.getErrorCode()==304){
//                        Toast.makeText(RegisterActivity.this,"用户名或密码不能为空，请重新输入",Toast.LENGTH_SHORT).show();
//                        regAccount.setText(null);
//                        regEmail.setText(null);
//                        regPassword.setText(null);
//                    }
//                }
//            }
//        });
//    }
}
