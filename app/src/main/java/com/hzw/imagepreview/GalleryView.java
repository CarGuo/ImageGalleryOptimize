package com.hzw.imagepreview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.hzw.imagepreview.CommonUtil.dip2px;
import static com.hzw.imagepreview.CommonUtil.getScreenWidth;
import static com.hzw.imagepreview.CommonUtil.sp2px;

/**
 * 特殊的瀑布流
 * Created by 何志伟 on 2016/4/18.
 */
public class GalleryView extends ViewGroup {

    private List<Integer> imgWidthList = new ArrayList<>();
    private List<Integer> imgHeightList = new ArrayList<>();
    private int loadMoreHeight;
    private Context mContext;
    private int lineSpaceWidth = 0;//每行子View间的分割线宽度
    private int lineSpaceHeight = 0;//两行间的分割线高度
    private float parentHeight;
    private float updateHeight;
    private int updateSize;
    private int updateTimes;
    private boolean isLoadMoreEnable = true;
    private static long time;
    private static long lastClickTime;
    private TextView mLoadMore;

    public GalleryView(Context context) {
        super(context);
        this.mContext = context;
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public GalleryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    /**
     * 初始化
     *
     * @param wList  子View的宽度List
     * @param hList  子View的高度List
     * @param spaceW 每行子View分割线宽
     * @param spaceH 两行间分割线高
     */
    public void init(List<Integer> wList, List<Integer> hList, int spaceW, int spaceH, int loadMoreHeight) {
        this.loadMoreHeight = loadMoreHeight;
        createLoadMore();
        this.imgWidthList.addAll(wList);
        this.imgHeightList.addAll(hList);
        this.imgWidthList.add(getScreenWidth(mContext));
        this.imgHeightList.add(loadMoreHeight);
        this.lineSpaceWidth = spaceW;
        this.lineSpaceHeight = spaceH;
        updateSize = wList.size() + 1;

        //当高度是warp_content情况下，计算具体的高度
        float height = 0;//父View的高度
        int childCount = imgWidthList.size();//子View的个数
        float lineHeight;//每行的实际高度
        int lineWidth = 0;//每行子View的宽度相加的和
        int index = imgWidthList.size() - wList.size() - 1;//上一行结束时的view

        //遍历每个子View,计算父View的高
        for (int i = imgHeightList.size() - wList.size() - 1; i < childCount; i++) {
            //最后一行时，无论怎样都让它的值超出屏宽
            lineWidth = i >= imgWidthList.size() - 2 ? getScreenWidth(mContext) : lineWidth;
            //计算每一行的固定高度，以便计算ViewGroup的高度，这里减去多余的一个分割线宽
            if (lineWidth + imgWidthList.get(i) - lineSpaceWidth >= getScreenWidth(mContext)) {
                //每行子View的宽度相加的和大于屏宽时
                float childWH = 0;//初始化宽高比之和
                for (int j = index; j <= i; j++) {//计算该行每张图的宽高比之和
                    childWH += imgWidthList.get(j) / (float) imgHeightList.get(j);
                }
                //计算每行的实际高度
                lineHeight = (getScreenWidth(mContext) - (i - index) * lineSpaceWidth) / childWH;
                //父View的高等于每一行的高度和两行间的分割线高度相加
                height += lineHeight + lineSpaceHeight;
                //重置lineWidth(每行子View的宽度相加的和)
                lineWidth = 0;
                index = i + 1;//记录上一行结束时的view
            } else {
                lineWidth += imgWidthList.get(i) + lineSpaceWidth;
            }
        }
        parentHeight += height;
        updateHeight = height;
        updateTimes++;
    }

    private void createLoadMore() {
        mLoadMore = new TextView(mContext);
        mLoadMore.setBackground(new ColorDrawable(Color.parseColor("#40000000")));
        mLoadMore.setText("查看更多...");
        addView(mLoadMore);
        mLoadMore.getLayoutParams().width = getScreenWidth(mContext);
        mLoadMore.getLayoutParams().height = loadMoreHeight;
        mLoadMore.setGravity(Gravity.CENTER);
        mLoadMore.setPadding(0, loadMoreHeight / 2 - sp2px(mContext, 8), 0, 0);

        mLoadMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClick != null) {
                    removeLoadMore();
                    mClick.load();
                }
            }
        });
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    //测量子view的宽高，设置自己的宽和高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int height = parentHeight - updateHeight == 0 ? (int) parentHeight - lineSpaceHeight :
                (int) parentHeight - lineSpaceHeight * (updateTimes) - (updateTimes - 1) *
                        loadMoreHeight - (updateTimes - 1) * (int) dip2px(mContext, 2.0f);
        height = isLoadMoreEnable ? height : height - loadMoreHeight - lineSpaceHeight;
        //实际高度减去多余一条分割线高
        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth : getScreenWidth(mContext),
                (heightMode == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //设置子view的位置，这里通过View的margin属性设置View在屏幕上的位置
        int height = parentHeight - updateHeight == 0 ? 0 : //上几行的总高度
                (int) (parentHeight - lineSpaceHeight * (updateTimes - 1) - (updateTimes - 1) *
                        loadMoreHeight - updateHeight) - (updateTimes - 1) * (int) dip2px(mContext, 2.0f);
        float lineHeight;//每行的实际高度
        int lineWidth = 0;//每行子View的宽度相加的和
        int index = imgWidthList.size() - (isLoadMoreEnable ? updateSize : updateSize - 1);//上一行结束时的view

        //遍历每个子 View 确定 View 的位置
        for (int i = index; i < imgWidthList.size(); i++) {
            //最后一行时，无论怎样都让它的值超出屏宽
            lineWidth = i >= (isLoadMoreEnable ? imgWidthList.size() - 2
                    : imgWidthList.size() - 1) ? getScreenWidth(mContext) : lineWidth;
            //还是计算每行的实际高度
            if (lineWidth + imgWidthList.get(i) - lineSpaceWidth >= getScreenWidth(mContext)) {
                //每行子View的宽度相加的和大于屏宽时
                float childWH = 0;//初始化宽高比之和
                for (int j = index; j <= i; j++) {//计算该行每张图的宽高比之和
                    childWH += imgWidthList.get(j) / (float) imgHeightList.get(j);
                }
                //计算每行的实际高度
                lineHeight = (getScreenWidth(mContext) - (i - index) * lineSpaceWidth) / childWH;
                //获取了每行的高度后设置childView的实际宽高
                for (int j = index; j <= i; j++) {
                    if (getChildAt(j).getVisibility() != GONE) {
                        int childWidth = (int) (imgWidthList.get(j) * lineHeight / imgHeightList.get(j));
                        //设置每个子View的位置
                        if (j > index) {
                            lineWidth += getChildAt(j - 1).getWidth() + lineSpaceWidth;
                        } else {
                            lineWidth = 0;
                        }
                        int left = lineWidth;
                        int top = height;
                        int right = j == i ? getScreenWidth(mContext) : left + childWidth;
                        int bottom = height + (int) lineHeight;
                        getChildAt(j).layout(left, top, right, bottom);
                    }
                    if (j == imgWidthList.size() - 1) {
                        //防止覆盖最后一个Item的点击事件
                        break;
                    }
                    //设置Item的点击事件
                    final int finalJ = j;
                    getChildAt(j).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null && !isFastClick()) {
                                int[] location = new int[2];
                                v.getLocationOnScreen(location);
                                mListener.click(location[0], location[1],
                                        v.getWidth(), v.getHeight(), finalJ);
                            }
                        }
                    });
                }
                //父View的高等于每一行的高度和两行间的分割线高度相加
                height += lineHeight + lineSpaceHeight;
                lineWidth = 0;//重置lineWidth(每行子View的宽度相加的和)
                index = i + 1;//记录上一行结束时的`
            } else {
                lineWidth += imgWidthList.get(i) + lineSpaceWidth;
            }
        }
    }

    private void removeLoadMore() {
        removeViewAt(imgWidthList.size() - 1);
        imgWidthList.remove(imgWidthList.size() - 1);
        imgHeightList.remove(imgHeightList.size() - 1);
    }

    private synchronized static boolean isFastClick() {
        lastClickTime = System.currentTimeMillis();
        if (lastClickTime - time < 500) {
            return true;
        }
        time = lastClickTime;
        return false;
    }

    //常用公用方法
    public void setLoadMoreEnable(boolean isLoadMoreEnable) {
        this.isLoadMoreEnable = isLoadMoreEnable;
        requestLayout();
        mLoadMore.setVisibility(GONE);
    }

    //Item的点击回调
    public interface ItemClickListener {
        void click(int x, int y, float width, float height, int position);
    }

    private ItemClickListener mListener;

    public void setItemClickListener(ItemClickListener listener) {
        this.mListener = listener;
    }

    //加载更多的点击回调
    public interface LoadMoreClick {
        void load();
    }

    private LoadMoreClick mClick;

    public void setLoadMoreClick(LoadMoreClick click) {
        this.mClick = click;
    }

    public List<Float> getChildInfo(int position) {
        int[] location = new int[2];
        getChildAt(position).getLocationOnScreen(location);
        List<Float> info = new ArrayList<>();
        info.add((float) location[0]);//x
        info.add((float) location[1]);//y
        info.add((float) getChildAt(position).getWidth());//宽
        info.add((float) getChildAt(position).getHeight());//高
        return info;
    }


}
