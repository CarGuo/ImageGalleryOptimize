package com.hzw.imagepreview;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import static com.hzw.imagepreview.CommonUtil.dip2px;

public class MainActivity extends AppCompatActivity {

    private BigImagePreview mPreview;
    private List<Integer> mUrlList;
    private List<Integer> mImgWidthList;
    private List<Integer> mImgHeightList;
    private int times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (BigImagePreview) findViewById(R.id.bigImagePreview);
        mImgWidthList = new ArrayList<>();
        mImgHeightList = new ArrayList<>();

        final int[] img = new int[]{R.mipmap.xxx0, R.mipmap.xxx1, R.mipmap.xxx2, R.mipmap.xxx3,
                R.mipmap.xxx4, R.mipmap.xxx5, R.mipmap.xxx6, R.mipmap.xxx7, R.mipmap.xxx8,
                R.mipmap.xxx9, R.mipmap.xxx10, R.mipmap.xxx11, R.mipmap.xxx12, R.mipmap.xxx13,
                R.mipmap.xxx14, R.mipmap.xxx15, R.mipmap.xxx16, R.mipmap.xxx17, R.mipmap.xxx18,
                R.mipmap.xxx19, R.mipmap.xxx20, R.mipmap.xxx21, R.mipmap.xxx22, R.mipmap.xxx23,
                R.mipmap.xxx24, R.mipmap.xxx25, R.mipmap.ffg, R.mipmap.ffh, R.mipmap.ffj};

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inSampleSize = 1;
        //opts.inJustDecodeBounds = false;
        mUrlList = new ArrayList<>();
        for (int i = 0; i < img.length; i++) {
            BitmapFactory.decodeResource(getResources(), img[i], opts);
            int width = opts.outWidth;
            int height = opts.outHeight;
            mImgWidthList.add(width / 3);
            mImgHeightList.add(height / 3);
            mUrlList.add(img[i]);
        }
        mPreview.init(mUrlList, mImgWidthList, mImgHeightList,
                (int) dip2px(this, 4), (int) dip2px(this, 4), (int) dip2px(this, 60));

        mPreview.setGalleryLoadMore(new BigImagePreview.GalleryLoadMore() {
            @Override
            public void loadMore() {
                if (times > 1) {
                    mUrlList.clear();
                    mPreview.setGalleryLoadMoreData(mUrlList, mImgWidthList, mImgHeightList);
                } else {
                    mPreview.setGalleryLoadMoreData(mUrlList, mImgWidthList, mImgHeightList);
                }
                times++;
            }
        });

    }

}
