package com.heyblack.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * 画板视图
 * @author Administrator
 *
 * public class DrawView {
 * }
 */

public class DrawView  extends View{
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint paint = null;
    public int sta1 = 2;
    private Mat firstMask = null;
    public Bitmap rawImg;
    public Bitmap resultMap;
    public ImageView imageView;
    private boolean isTouchEnd = false;



    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inite();
        if(!OpenCVLoader.initDebug())
        {
            Log.d("opencv","初始化失败");
        }


        firstMask = new Mat();
    }

    /**
     * 初始化
     */
    private void inite(){
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        mCanvas = new Canvas();
        mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕
    }

    /**
     * 设置颜色
     * @param color 颜色
     */
    public void setcolor(int color){
        if(paint!=null){
            paint.setColor(color);
        }
    }
    /**
     * 设置笔刷大小
     * @param size 笔刷大小值
     */
    public void setPenSize(float size){
        if(paint!=null){
            paint.setStrokeWidth(size);
        }
    }
    /**
     * 清屏
     */
    public void clearall(){
        if(mCanvas!=null){
            mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕
            invalidate();
        }
    }
    /**
     * 设置类型
     */
    public void changeSta(int A){
        sta1 = A;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        super.onSizeChanged(w, h, oldw, oldh);
    } //屏幕尺寸改变时调用

    //核心函数
    private float startX = 0;
    private float startY = 0;
    private float stopX = 0;
    private float stopY = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if(sta1==0){
            int action=event.getAction();
            switch(action){
                case MotionEvent.ACTION_DOWN://点击
                    //在这里获取起始点坐标
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE://移动
                    //在这里获取移动点坐标
                    stopX = event.getX();
                    stopY = event.getY();
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    mCanvas.drawLine(startX, startY, stopX, stopY, paint);//绘图
                    //把末点赋值给始点
                    startX = stopX;
                    startY = stopY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP://收笔
                    stopX = event.getX();
                    stopY = event.getY();


                    invalidate();
                    break;
                default:
                    break;
            }
        }
        if(sta1 == 2){
            int action=event.getAction();
            switch(action){
                case MotionEvent.ACTION_DOWN://点击
                    //在这里获取起始点坐标
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE://移动
                    //在这里获取移动点坐标
                    stopX = event.getX();
                    stopY = event.getY();
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP://收笔
                    stopX = event.getX();
                    stopY = event.getY();
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    mCanvas.drawLine(startX, startY, startX, stopY, paint);
                    mCanvas.drawLine(startX, startY, stopX, startY, paint);
                    mCanvas.drawLine(startX, stopY, stopX, stopY, paint);
                    mCanvas.drawLine(stopX, startY, stopX, stopY, paint);
                    isTouchEnd = true;
                    invalidate();
//                    onDrawImage( startX, startY, stopX, stopY);
                    break;
                default:
                    break;
            }
        }
        return true;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
//		mCanvas.drawLine(startX, startY, stopX, stopY, paint);
        canvas.drawBitmap(mBitmap,0 , 0, null);


    }

    public void onDrawImage(){
        if (isTouchEnd) {
            isTouchEnd = false;
//            double widtht1 = imageView.getWidth();
//            double height1 imageView.getHeight();
//            double width2 =  getWidth();
//            double height2 = getHeight();
//            WindowManager.LayoutParams lp = getLayoutParams().getAttributes();
            double ratio = (double) rawImg.getWidth()/(double) rawImg.getHeight();
            double tranStartX = startX - (getWidth()- getHeight()*ratio)/2.0;
            double tranStartY = getY() - startY;
            double tranEndX = stopX - (getWidth()- getHeight()*ratio)/2.0;
            double tranEndY = getY() - stopY;
            resultMap = beginGrabcut(rawImg,
                    tranStartX,
                    startY,
                    tranEndX,
                    stopY);

            if (imageView != null) {
                imageView.setImageBitmap(resultMap);
            }
        }
    }

    private Bitmap beginGrabcut(Bitmap bitmap, double beginX, double beginY, double endX, double endY)
    {
        Mat img = new Mat();
        Rect rect = new Rect(new Point(beginX, beginY), new Point(endX, endY));

        Utils.bitmapToMat(bitmap, img);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);

        //生成遮板
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Imgproc.grabCut(img, firstMask, rect,bgModel, fgModel,1, Imgproc.GC_INIT_WITH_RECT);
        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

        //抠图
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        img.copyTo(foreground, firstMask);

        //mat->bitmap
        Bitmap b = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(foreground,b);

        return b;
    }

}
