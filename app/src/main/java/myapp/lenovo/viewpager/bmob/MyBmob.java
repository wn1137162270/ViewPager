
package myapp.lenovo.viewpager.bmob;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

import myapp.lenovo.viewpager.entity.MyUser;

/**
 * Created by wn on 2019/5/18.
 */

public class MyBmob {
    private static final String TAG ="MyBmob";
    private static final String qqAppId ="1105782685";
    public static final int REGISTER = 0;
    public static final int LOGIN_BY_ACCOUNT = 1;
    public static final int LOGIN_BY_QQ = 2;
    public static final int REQUEST_LOGIN_PHONESMS = 3;
    public static final int LOGIN_BY_PHONESMS = 4;

    private static IUiListener iUiListener;
    private static Tencent tencent;

    public static void initBmob(Context context){
        cn.bmob.v3.Bmob.initialize(context,"167be2b330d3485eaad70348455b3853");
        BmobConfig bmobConfig=new BmobConfig.Builder(context)
                .setApplicationId("167be2b330d3485eaad70348455b3853")
                .build();
        cn.bmob.v3.Bmob.initialize(bmobConfig);
    }

    public static void registerBmobUserWithEmail(String username, String email, String password, final OperateDoneListener listener){
        MyUser bmobUser=new MyUser();
        bmobUser.setUsername(username);
        bmobUser.setPassword(password);
        if(email != null && !TextUtils.isEmpty(email)){
            bmobUser.setEmail(email);
        }
        bmobUser.signUp(new SaveListener<MyUser>() {
            @Override
            public void done(MyUser myUser, BmobException e) {
                int errorCode;
                if(e == null){
                    errorCode = -1;
                }
                else{
                    errorCode = e.getErrorCode();
                }
                listener.onoperateDone(errorCode, REGISTER);
            }
        });
    }

    public static void loginBmobUserByAccount(String account, String password, final OperateDoneListener listener){
        final boolean[] isSucceed = new boolean[1];
        BmobUser.loginByAccount(account, password, new LogInListener<MyUser>() {
            @Override
            public void done(MyUser o, BmobException e) {
                isSucceed[0] = (e == null);
                listener.onoperateDone(isSucceed[0] ? 1 : 0, LOGIN_BY_ACCOUNT);
            }
        });
    }

    public static void requestPhoneSMS(String phone, final OperateDoneListener listener){
        BmobSMS.requestSMSCode(phone, "验证码", new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                boolean isSucceed = (e == null);
                listener.onoperateDone(isSucceed ? 1 : 0, REQUEST_LOGIN_PHONESMS);
            }
        });
    }

    public static void LoginBmobUserByPhone(String phone, String phoneSMS, final OperateDoneListener listener){
        BmobUser.loginBySMSCode(phone, phoneSMS, new LogInListener<MyUser>() {
            @Override
            public void done(MyUser bmobUser, BmobException e) {
                boolean isSucceed = (e == null);
                listener.onoperateDone(isSucceed ? 1 : 0, LOGIN_BY_PHONESMS);
            }
        });
    }

    public static void bmobThirdLoginByQQ(final Activity activity, final OperateDoneListener listener){
        iUiListener=new IUiListener() {
            @Override
            public void onComplete(Object o) {
                if (o != null) {
                    JSONObject jsonObject = (JSONObject) o;
                    try {
                        String accessToken = jsonObject.getString(com.tencent.
                                connect.common.Constants.PARAM_ACCESS_TOKEN);
                        String expires = jsonObject.getString(com.tencent.
                                connect.common.Constants.PARAM_EXPIRES_IN);
                        String openId = jsonObject.getString(com.tencent.
                                connect.common.Constants.PARAM_OPEN_ID);
                        Log.d("accessToken", accessToken);
                        Log.d("expires", expires);
                        Log.d("openId", openId);
                        BmobUser.BmobThirdUserAuth authInfo = new BmobUser.BmobThirdUserAuth(BmobUser.
                                BmobThirdUserAuth.SNS_TYPE_QQ, accessToken, expires, openId);
                        BmobUser.loginWithAuthData(authInfo, new LogInListener<JSONObject>() {
                            @Override
                            public void done(JSONObject jsonObject, BmobException e) {
                                boolean isSucceed = (e == null);
                                listener.onoperateDone(isSucceed ? 1 : 0, LOGIN_BY_QQ);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onError(UiError uiError) {
                Toast.makeText(activity,"QQ登录出错"+uiError.errorCode+uiError.errorDetail,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(activity,"取消QQ绑定",Toast.LENGTH_SHORT).show();
            }
        };

        if(tencent==null){
            tencent= Tencent.createInstance(qqAppId,activity);
        }
        tencent.logout(activity);
        tencent.login(activity,"all", iUiListener);
    }

    public static void handleActivityResult(int requestCode, int resultCode, Intent data){
        Tencent.onActivityResultData(requestCode, resultCode, data, iUiListener);

        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == 65) {
                tencent.handleLoginData(data, iUiListener);
            }
        }
    }


    public interface OperateDoneListener{
        void onoperateDone(int errorCode, int which);
    }

}