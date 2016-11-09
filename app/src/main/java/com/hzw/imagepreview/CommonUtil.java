package com.hzw.imagepreview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.WindowManager;

/**
 * Created by shuyu on 2016/11/9.
 */

public class CommonUtil {

    public static PointF getNeedSize(Context context, final float width, final float height) {
        float imgHeight;
        float imgWidth;
        if (height > width) {
            float screenScale = (float) getScreenHeight(context) / getScreenWidth(context);
            float imtScale = height / width;
            if (screenScale < imtScale) {
                imgHeight = getScreenHeight(context);
                imgWidth = getScreenHeight(context) * width / height;
            } else {
                imgHeight = getScreenWidth(context) * height / width;
                imgWidth = getScreenWidth(context);
            }
        } else {
            imgHeight = getScreenWidth(context) * height / width;
            imgWidth = getScreenWidth(context);
        }
        return new PointF(imgWidth, imgHeight);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }


    public static float dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


}
