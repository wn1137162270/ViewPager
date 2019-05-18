package myapp.lenovo.viewpager.camera;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * Created by wn on 2019/4/23.
 */

public class FocusView extends View {
    private Paint linePaint;
    private final int borderWidth = 4;

    private AnimatorSet animatorSet;
    private boolean isFocusing = false;
    private ObjectAnimator animatorC;

    public FocusView(Context context) {
        this(context, null);
    }

    public FocusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(borderWidth);
        linePaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - borderWidth / 2, linePaint);
    }

    void beginFocus(){
        isFocusing = true;
        if (animatorSet == null){
            animatorSet = new AnimatorSet();
            ObjectAnimator animatorX = ObjectAnimator.ofFloat(FocusView.this, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(FocusView.this, "scaleY", 1f, 1.3f, 1f);
            animatorSet.play(animatorX).with(animatorY);
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.setDuration(1000);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    FocusView.this.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setMainColor();
                    animatorC = ObjectAnimator.ofFloat(FocusView.this, "alpha", 1f, 0f);
                    animatorC.setInterpolator(new LinearInterpolator());
                    animatorC.setDuration(500);
                    animatorC.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {}

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            reset();
                            isFocusing = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {}

                        @Override
                        public void onAnimationRepeat(Animator animator) {}
                    });
                    animatorC.start();
                }

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            });
        }
        else {
            if (animatorSet.isRunning()){
                animatorSet.cancel();
            }
            if (animatorC != null && animatorC.isRunning()){
                animatorC.cancel();
            }
        }
        animatorSet.start();
    }

    private void setMainColor(){
        linePaint.setColor(Color.parseColor("#52ce90"));
        postInvalidate();
    }

    private void reset(){
        linePaint.setColor(Color.parseColor("#e0e0e0"));
        postInvalidate();
    }

    public boolean isFocusing(){
        return isFocusing;
    }
}
