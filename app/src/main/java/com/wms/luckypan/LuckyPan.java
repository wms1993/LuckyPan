package com.wms.luckypan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by wms1993 on 2016/4/12 0012.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    /**
     * 获取的Canvas
     */
    private Canvas mCanvas;
    /**
     * 绘制线程
     */
    private Thread mThread;
    /**
     * 线程是否在运行
     */
    private boolean isRunning;
    /**
     * 图片对应文字
     */
    private String[] mStrs = new String[]{"单反相机", "IPAD", "恭喜发财", "iPhone", "服装一套", "恭喜发财"};
    /**
     * 图片资源
     */
    private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.f040, R.drawable.iphone, R.drawable.meizi, R.drawable.f015};
    /**
     * 盘块的画笔
     */
    private Paint mArcPaint;
    /**
     * 文本的画笔
     */
    private Paint mTextPaint;
    /**
     * 保存图片的Bitmap
     */
    private Bitmap[] mImgBitmaps;
    /**
     * 转盘背景Bitmap
     */
    private Bitmap mBgBitmap;
    /**
     * 转盘的直径
     */
    private int mDiameter;
    /**
     * 左右上下边距
     */
    private int mPadding;
    /**
     * 开始的角度
     */
    private float mStartAngle;
    /**
     * 转盘转动的速度
     */
    private float mSpeed;
    /**
     * 盘块的颜色
     */
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};
    private RectF mRange;
    /**
     * 几个盘面
     */
    private int mItemCount;
    /**
     * 转盘中心点
     */
    private int mCenter;
    /**
     * 是否结束旋转
     */
    private boolean isShouldEnd;

    public LuckyPan(Context context) {
        this(context, null);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckyPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHolder = getHolder();
        mHolder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        initPaint();
        initBitmaps();

        mItemCount = mImgs.length;
        mRange = new RectF(getPaddingLeft(), getPaddingLeft(), mDiameter
                + getPaddingLeft(), mDiameter + getPaddingLeft());

        isRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * 初始化Bitmap
     */
    private void initBitmaps() {
        mImgBitmaps = new Bitmap[mImgs.length];

        for (int i = 0; i < mImgs.length; i++) {
            mImgBitmaps[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        //初始化背景Bitmap
        mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mArcPaint = new Paint();
        mArcPaint.setDither(true);
        mArcPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setDither(true);
        mTextPaint.setDither(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                getResources().getDisplayMetrics()));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        mPadding = getPaddingLeft();
        mDiameter = width - mPadding * 2;
        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    @Override
    public void run() {
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始绘制
     */
    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();

            if (mCanvas != null) {
                drawBg();
                drawArc();
            }
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 绘制抽奖文字
     *
     * @param tempAngle
     * @param sweepAngle
     * @param string
     */
    private void drawText(float tempAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRange, tempAngle, sweepAngle);
        float textLength = mTextPaint.measureText(string);
        float hOffset = (float) ((mDiameter * Math.PI / mItemCount - textLength) * 0.5);
        float vOffset = (float) (mDiameter / 2 / 6);
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    /**
     * 绘制盘面
     */
    private void drawArc() {
        float tempAngle = mStartAngle;
        float sweepAngle = 360 * 1.0f / mImgs.length;
        for (int i = 0; i < mImgs.length; i++) {
            mArcPaint.setColor(mColors[i]);
            mCanvas.drawArc(mRange, tempAngle, sweepAngle, true, mArcPaint);

            drawText(tempAngle, sweepAngle, mStrs[i]);
            drawIcon(tempAngle, mImgBitmaps[i]);

            tempAngle += sweepAngle;
        }

        mStartAngle += mSpeed;

        if (isShouldEnd) {
            mSpeed--;
        }

        if (mSpeed <= 0) {
            mSpeed = 0;
        }
    }

    /**
     * 绘制Icon
     *
     * @param tempAngle
     * @param mImgBitmap
     */
    private void drawIcon(float tempAngle, Bitmap mImgBitmap) {
        int imgWidth = mDiameter / 8;
        float angle = (float) ((tempAngle + 360 / mItemCount / 2) * Math.PI / 180);

        int x = (int) (mDiameter / 2 / 2 * Math.cos(angle) + mCenter);
        int y = (int) (mDiameter / 2 / 2 * Math.sin(angle) + mCenter);

        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(mImgBitmap, null, rect, null);
    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);
    }

    /**
     * 开始抽奖
     */
    public void start() {
        mSpeed = 50;
        isShouldEnd = false;
    }

    /**
     * 停止抽奖
     */
    public void stop() {
        isShouldEnd = true;
    }

    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }
}
