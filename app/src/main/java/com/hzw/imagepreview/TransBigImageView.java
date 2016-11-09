package com.hzw.imagepreview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 有移动动画的ImageView
 * Created by 何志伟 on 2016/4/19.
 */
public class TransBigImageView extends ImageView {
    private Context mContext;
    private float tX, tY, sX, sY;
    private static long time1;
    private static long lastClickTime1;

    public TransBigImageView(Context context) {
        super(context);
        this.mContext = context;
        setVisibility(GONE);
    }

    public TransBigImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setVisibility(GONE);
    }

    public TransBigImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setVisibility(GONE);
    }

    public void init(int x, int y, float width, float height) {
        FrameLayout.MarginLayoutParams margin = new FrameLayout.MarginLayoutParams(getLayoutParams());
        margin.width = (int) width;
        margin.height = (int) height;
        margin.setMargins(x, y, x + (int) width, y + (int) height);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        setLayoutParams(layoutParams);
        //// TODO: 2016/11/9 修改为长图与宽图判断，但是还没判断大小是不是超过屏幕
        if (height > width) {
            float imgHeight;
            float imgWidth;
            float screenScale = (float) getScreenHeight() / getScreenWidth();
            float imtScale = height /width;
            if (screenScale < imtScale) {
                imgHeight = getScreenHeight();
                imgWidth = getScreenHeight() * width / height;
            } else {
                imgHeight = getScreenWidth() * height / width;
                imgWidth = getScreenWidth();
            }
            tX = (getScreenWidth() - width) / 2.0f - x;
            tY = (getScreenHeight() - height) / 2.0f - getStatusBarHeight() / 2.0f - y;
            sX = imgWidth / width;
            sY = imgHeight / height;
        } else {
            float imgHeight = getScreenWidth() * height / width;
            tX = (getScreenWidth() - width) / 2.0f - x;
            tY = (getScreenHeight() - height) / 2.0f - getStatusBarHeight() / 2.0f - y;
            sX = getScreenWidth() / width;
            sY = imgHeight / height;
        }
    }


    public void startTrans() {
        if (isFastClick()) {
            return;
        }
        setVisibility(VISIBLE);
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofInt(getParent(), "backgroundColor",
                Color.TRANSPARENT, Color.parseColor("#000000"));
        animator.setEvaluator(new ArgbEvaluator());
        set.playTogether(
                ObjectAnimator.ofFloat(this, "translationX", 0, tX),
                ObjectAnimator.ofFloat(this, "translationY", -getStatusBarHeight(), tY),
                ObjectAnimator.ofFloat(this, "scaleX", 1, sX),
                ObjectAnimator.ofFloat(this, "scaleY", 1, sY), animator
        );
        set.setDuration(400);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mTransEnd != null) {
                    setVisibility(GONE);
                    mTransEnd.end();

                }
            }
        });
    }


    private synchronized static boolean isFastClick() {
        lastClickTime1 = System.currentTimeMillis();
        if (lastClickTime1 - time1 < 500) {
            return true;
        }
        time1 = lastClickTime1;
        return false;
    }

    //动画开始时的监听
    public interface TransEnd {
        void end();
    }

    private TransEnd mTransEnd;

    public void setTransEnd(TransEnd end) {
        this.mTransEnd = end;
    }

    public int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            java.lang.reflect.Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
        }
        return statusBarHeight;
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }

}
