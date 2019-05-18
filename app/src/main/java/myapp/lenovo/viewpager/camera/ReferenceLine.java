package myapp.lenovo.viewpager.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import myapp.lenovo.viewpager.utils.Utils;

/**
 * Created by wn on 2019/4/23.
 */

public class ReferenceLine extends View {
    private Paint linePaint;

    public ReferenceLine(Context context) {
        this(context, null);
    }

    public ReferenceLine(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReferenceLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int screenWidth = Utils.getScreenWidthHeight(getContext()).widthPixels;
        int screenHeight = Utils.getScreenWidthHeight(getContext()).heightPixels;

        double width = screenWidth / 3.0;
        double height = screenHeight / 3.0;

        canvas.drawLine((float) width, 0, (float) width, screenHeight, linePaint);
        canvas.drawLine((float) width * 2, 0, (float) width * 2, screenHeight, linePaint);

        canvas.drawLine(0, (float) height, screenWidth, (float) height, linePaint);
        canvas.drawLine(0, (float) height * 2, screenWidth, (float) height * 2, linePaint);

    }
}
