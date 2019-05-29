package myapp.lenovo.viewpager.activity;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import myapp.lenovo.viewpager.fragment.MyDocumentFragment;
import myapp.lenovo.viewpager.adapter.MyFragmentPagerAdapter;
import myapp.lenovo.viewpager.fragment.MyMeFragment;
import myapp.lenovo.viewpager.fragment.MyOcrFragment;
import myapp.lenovo.viewpager.R;

public class MainActivity extends AppCompatActivity implements MyOcrFragment.OcrContent {
    private ViewPager viewPager;
    private RadioGroup radioGroup;
    private RadioButton ocr,document,me;
    private Drawable[] drawables=new Drawable[6];
    private ColorStateList[] csls=new ColorStateList[2];
    private MyDocumentFragment myDocumentFragment;
    public List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setRadioGroup();
        setViewPager();
    }

    private void initView(){
        viewPager= (ViewPager) findViewById(R.id.view_pager);
        radioGroup= (RadioGroup) findViewById(R.id.radio_group);
        ocr= (RadioButton) findViewById(R.id.ocr_rb);
        document= (RadioButton) findViewById(R.id.document_rb);
        me= (RadioButton) findViewById(R.id.me_rb);

        csls[0]=MainActivity.this.getResources().getColorStateList(R.color.colorDarkGray);
        csls[1]=MainActivity.this.getResources().getColorStateList(R.color.colorDarkBlue);
    }

    private void setRadioGroup() {

        drawables[0]=MainActivity.this.getResources().getDrawable(R.drawable.ocr_off);
        drawables[1]=MainActivity.this.getResources().getDrawable(R.drawable.my_document_off);
        drawables[2]=MainActivity.this.getResources().getDrawable(R.drawable.tab_off);
        drawables[3]=MainActivity.this.getResources().getDrawable(R.drawable.ocr_on);
        drawables[4]=MainActivity.this.getResources().getDrawable(R.drawable.my_document_on);
        drawables[5]=MainActivity.this.getResources().getDrawable(R.drawable.tab_on);
        for(int i=0;i<6;i++)
            drawables[i].setBounds(0,0,85,85);
        ocr.setCompoundDrawables(null,drawables[0],null,null);
        document.setCompoundDrawables(null,drawables[1],null,null);
        me.setCompoundDrawables(null,drawables[2],null,null);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                resetRadioGroupDrawableColor();
                switch (i){
                    case R.id.ocr_rb:
                        ocr.setTextColor(csls[0]);
                        ocr.setCompoundDrawables(null,drawables[3],null,null);
                        viewPager.setCurrentItem(0,false);
                        break;
                    case R.id.document_rb:
                        document.setTextColor(csls[0]);
                        document.setCompoundDrawables(null,drawables[4],null,null);
                        viewPager.setCurrentItem(1,false);
                        break;
                    case R.id.me_rb:
                        me.setTextColor(csls[0]);
                        me.setCompoundDrawables(null,drawables[5],null,null);
                        viewPager.setCurrentItem(2,false);
                        break;
                }
            }

        });
    }

    private void setViewPager() {

        MyOcrFragment myOcrFragment=new MyOcrFragment();
        myDocumentFragment=new MyDocumentFragment();
        MyMeFragment myMeFragment=new MyMeFragment();
        fragments=new ArrayList<>();
        fragments.add(myOcrFragment);
        fragments.add(myDocumentFragment);
        fragments.add(myMeFragment);

        MyFragmentPagerAdapter fpg=new MyFragmentPagerAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(fpg);
        ocr.setTextColor(csls[0]);
        ocr.setCompoundDrawables(null, drawables[3], null, null);
        viewPager.setCurrentItem(0, false);
        radioGroup.check(R.id.ocr_rb);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                resetRadioGroupDrawableColor();
                switch (position) {
                    case 0:
                        ocr.setTextColor(csls[0]);
                        ocr.setCompoundDrawables(null, drawables[3], null, null);
                        viewPager.setCurrentItem(0, false);
                        radioGroup.check(R.id.ocr_rb);
                        break;
                    case 1:
                        document.setTextColor(csls[0]);
                        document.setCompoundDrawables(null, drawables[4], null, null);
                        viewPager.setCurrentItem(1, false);
                        radioGroup.check(R.id.document_rb);
                        break;
                    case 2:
                        me.setTextColor(csls[0]);
                        me.setCompoundDrawables(null, drawables[5], null, null);
                        viewPager.setCurrentItem(2, false);
                        radioGroup.check(R.id.me_rb);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void resetRadioGroupDrawableColor(){
        ocr.setCompoundDrawables(null,drawables[0],null,null);
        document.setCompoundDrawables(null,drawables[1],null,null);
        me.setCompoundDrawables(null,drawables[2],null,null);
        ocr.setTextColor(csls[1]);
        document.setTextColor(csls[1]);
        me.setTextColor(csls[1]);
    }

    @Override
    protected void onDestroy() {
        Log.d("destroy","haha");
        BmobUser.logOut();
        super.onDestroy();
    }

    @Override
    public void getOcrContent(Uri uri,String html) {
        myDocumentFragment.getOcrContent(uri,html);
    }

}
