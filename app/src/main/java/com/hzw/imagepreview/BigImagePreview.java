package com.hzw.imagepreview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * 大图预览界面
 * Created by 何志伟 on 2016/4/19.
 */
public class BigImagePreview extends RelativeLayout {

    private Context mContext;
    private GalleryView galleryView;
    private ViewPager pager;
    private TransSmallImageView mTransSmallImageView;
    private TransBigImageView transBigImageView;
    private FrameLayout previewParent;
    private List urlList = new ArrayList();
    private int lineWidth, lineHeight, loadMoreHeight;
    private ViewPagerAdapter mAdapter;
    private ScrollView galleryScroll;

    public BigImagePreview(Context context) {
        super(context);
        this.mContext = context;
        findView();
    }

    public BigImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        findView();
    }

    public BigImagePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        findView();
    }

    private void findView() {
        //获取布局
        View preview = LayoutInflater.from(mContext).inflate(R.layout.gallery_preview, this);
        galleryView = (GalleryView) preview.findViewById(R.id.gallery);
        pager = (ViewPager) preview.findViewById(R.id.preview_viewPager);
        mTransSmallImageView = (TransSmallImageView) preview.findViewById(R.id.transImage);
        previewParent = (FrameLayout) preview.findViewById(R.id.previewParent);
        galleryScroll = (ScrollView) preview.findViewById(R.id.gallery_scroll);
        transBigImageView= (TransBigImageView) preview.findViewById(R.id.transBigImage);
    }

    public void init(final List<?> list, List<Integer> widthList, List<Integer> heightList,
                     int lineWidth, int lineHeight, int loadMoreHeight) {
        this.lineWidth = lineWidth;
        this.lineHeight = lineHeight;
        this.loadMoreHeight = loadMoreHeight;
        //设置Gallery数据
        setGalleryLoadMoreData(list, widthList, heightList);
        mTransSmallImageView.setExitEnd(new TransSmallImageView.ExitEnd() {
            @Override
            public void end() {
                previewParent.setVisibility(GONE);
            }
        });
        galleryView.setItemClickListener(new GalleryView.ItemClickListener() {
            @Override
            public void click(int x, int y, float width, float height, int position) {
                galleryItemClick(x, y, width, height, position);
            }
        });
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int w = galleryView.getChildAt(position).getWidth();
                int h = galleryView.getChildAt(position).getHeight();
                Glide.with(mContext).load(urlList.get(position))
                        .override(w, h).dontAnimate().into(mTransSmallImageView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //Gallery加载更多的回调事件
        galleryView.setLoadMoreClick(new GalleryView.LoadMoreClick() {
            @Override
            public void load() {
                if (mLoadMore != null) {
                    mLoadMore.loadMore();
                }
            }
        });

    }

    public void setGalleryLoadMoreData(final List<?> list, List<Integer> widthList, List<Integer> heightList) {
        if(list.size()==0){
            galleryView.setLoadMoreEnable(false);
            return;
        }
        //初始化GalleryView
        for (int i = 0; i < list.size(); i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            galleryView.addView(imageView);
        }
        galleryView.init(widthList, heightList, lineWidth, lineHeight, loadMoreHeight);
        //当最后一个子View绘制完成后，加载子View的图片
        galleryView.getChildAt(urlList.size() + list.size() - 1).post(new Runnable() {
            @Override
            public void run() {
                urlList.addAll(list);
                for (int i = urlList.size() - list.size(); i < urlList.size(); i++) {
                    //指定Gallery中的加载的图片大小，防止OOM
                    int w = galleryView.getChildAt(i).getWidth();
                    int h = galleryView.getChildAt(i).getHeight();
                    Glide.with(mContext).load(urlList.get(i)).override(w, h)
                            .into((ImageView) galleryView.getChildAt(i));
                }
                //设置ViewPager的适配器
                if (mAdapter == null) {
                    mAdapter = new ViewPagerAdapter(mContext, urlList);
                    pager.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void galleryItemClick(final int x, final int y, final float width, final float height, final int position) {
        previewParent.setVisibility(VISIBLE);
        Glide.with(mContext).load(urlList.get(position)).asBitmap().dontAnimate().into(transBigImageView);
        transBigImageView.init(x, y, width, height);
        transBigImageView.startTrans();
        transBigImageView.setTransEnd(new TransBigImageView.TransEnd() {
            @Override
            public void end() {
                pager.setCurrentItem(position, false);
                pager.setVisibility(View.VISIBLE);
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {
        private Context mContext;
        private List<?> list;

        public ViewPagerAdapter(Context context, List<?> list) {
            this.mContext = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int i) {
            ImageView imageView = new ImageView(mContext);
            container.addView(imageView);
            //指定要加载的图片大小，防止OOM
            List<Float> info = galleryView.getChildInfo(i);
            int w = getScreenWidth();
            int h = (int) (getScreenWidth() * info.get(3) / info.get(2));
            //使用GalleryView子View上的图片作为占位图，不然会出现加载闪白
            galleryView.getChildAt(i).setDrawingCacheEnabled(true);
            Drawable drawable = new BitmapDrawable(galleryView.getChildAt(i).getDrawingCache());
            Glide.with(mContext).load(list.get(i)).override(w, h)
                    .dontAnimate().placeholder(drawable).into(imageView);
            //Item点击事件
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setVisibility(View.GONE);
                    List<Float> info = galleryView.getChildInfo(i);
                    mTransSmallImageView.exit((int) (info.get(0) / 1),
                            (int) (info.get(1) / 1), info.get(2), info.get(3));
                }
            });
            return imageView;
        }
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public interface GalleryLoadMore {
        void loadMore();
    }

    private GalleryLoadMore mLoadMore;

    public void setGalleryLoadMore(GalleryLoadMore loadMore) {
        this.mLoadMore = loadMore;
    }


}
