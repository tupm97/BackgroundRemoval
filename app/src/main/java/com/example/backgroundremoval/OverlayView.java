package com.example.backgroundremoval;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private DrawCallBack callBack;

    public OverlayView(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    public interface DrawCallBack{
        public void drawCallBack(final Canvas canvas);
    }

    public void setCallBack(DrawCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        super.draw(canvas);
        if(callBack!=null){
            callBack.drawCallBack(canvas);
        }
    }
}
