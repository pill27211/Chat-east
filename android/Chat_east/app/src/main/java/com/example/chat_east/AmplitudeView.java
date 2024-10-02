package com.example.chat_east;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class AmplitudeView extends View {

    public AmplitudeView(Context context) {
        super(context);
    }

    public AmplitudeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AmplitudeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int amplitude = 0;

    public void setAmplitude(int amplitude) {
        this.amplitude = amplitude;
        invalidate();  // 뷰를 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FB9E65"));
        canvas.drawRect(0, getHeight() - amplitude, getWidth(), getHeight(), paint);
    }
}

