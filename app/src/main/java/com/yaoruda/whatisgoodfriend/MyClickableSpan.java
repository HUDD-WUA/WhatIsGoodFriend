package com.yaoruda.whatisgoodfriend;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by yaoruda on 2017/5/8.
 */
public class MyClickableSpan extends ClickableSpan {

    int color = -1;
    private Context context;
    private Intent intent;

    public MyClickableSpan(Context context, Intent intent) {
        this(-1, context, intent);
    }

    public MyClickableSpan(int color, Context context, Intent intent) {
        if (color!=-1) {
            this.color = color;
        }
        this.context = context;
        this.intent = intent;
    }

    /**
     * Performs the click action associated with this span.
     */
    public void onClick(View widget){
        context.startActivity(intent);
    }

    /**
     * Makes the text without underline.
     */
    @Override
    public void updateDrawState(TextPaint ds) {
        if (color == -1) {
            ds.setColor(ds.linkColor);
        } else {
            ds.setColor(color);
        }
        ds.setUnderlineText(false);
    }
}