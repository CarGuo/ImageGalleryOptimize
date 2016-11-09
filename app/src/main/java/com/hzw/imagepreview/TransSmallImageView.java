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
import android.widget.ImageView;

/**
 * 有移动动画的ImageView
 * Created by 何志伟 on 2016/4/19.
 */
public class TransSmallImageView extends ImageView {

    private Context mContext;
    private int x, y;
    private float width, height;
    private static long time1;
    private static long lastClickTime1;

    public TransSmallImageView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public TransSmallImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public TransSmallImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        setVisibility(GONE);
    }


    public void startTrans(int x, int y, float width, float height) {
        if (isFastClick()) {
            return;
        }
        setVisibility(VISIBLE);
        AnimatorSet set = new AnimatorSet();
        float imgHeight = getScreenWidth() * height / width;
        float dy = getResources().getDisplayMetrics().density * 4.0f + 0.5f;
        ObjectAnimator animator = ObjectAnimator.ofInt(getParent(), "backgroundColor",
                Color.TRANSPARENT, Color.parseColor("#000000"));
        animator.setEvaluator(new ArgbEvaluator());
        set.playTogether(
                ObjectAnimator.ofFloat(this, "translationX", x - (getScreenWidth() - width) / 2.0f, 0),
                ObjectAnimator.ofFloat(this, "translationY", y - (getScreenHeight() - height) / 2.0f
                        - getStatusBarHeight() / 3.0f - dy, 0),
                ObjectAnimator.ofFloat(this, "scaleX", width / getScreenWidth(), 1),
                ObjectAnimator.ofFloat(this, "scaleY", height / imgHeight, 1), animator
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
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public void exit() {
        exit(x, y, width, height);
    }

    public void exit(int x, int y, float width, float height) {
        if (isFastClick()) {
            return;
        }
        setVisibility(VISIBLE);


        //// TODO: 2016/11/9 修改为长图与宽图判断，但是还没判断大小是不是超过屏幕
        float imgHeight;
        float imgWidth;
        if (height > width) {
            float screenScale = (float) getScreenHeight() / getScreenWidth();
            float imtScale = height /width;
            if (screenScale <  imtScale) {
                imgHeight = getScreenHeight();
                imgWidth = getScreenHeight() * width / height;
            } else {
                imgHeight = getScreenWidth() * height / width;
                imgWidth = getScreenWidth();
            }
        } else {
            imgHeight = getScreenWidth() * height / width;
            imgWidth = getScreenWidth();
        }


        float dy = getResources().getDisplayMetrics().density * 4.0f + 0.5f;
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofInt(getParent(), "backgroundColor",
                Color.parseColor("#000000"), Color.TRANSPARENT);
        animator.setEvaluator(new ArgbEvaluator());
        set.playTogether(
                ObjectAnimator.ofFloat(this, "translationX", 0, x - (getScreenWidth() - width) / 2.0f),
                ObjectAnimator.ofFloat(this, "translationY", 0, y - (getScreenHeight() - height) / 2.0f
                        - getStatusBarHeight() / 3.0f - dy),
                ObjectAnimator.ofFloat(this, "scaleX", 1, width / imgWidth),
                ObjectAnimator.ofFloat(this, "scaleY", 1, height / imgHeight), animator
        );
        set.setDuration(400);
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mEnd != null) {
                    mEnd.end();
                    setVisibility(GONE);
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

    //动画结束时的监听
    public interface ExitEnd {
        void end();
    }

    private ExitEnd mEnd;

    public void setExitEnd(ExitEnd end) {
        this.mEnd = end;
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
