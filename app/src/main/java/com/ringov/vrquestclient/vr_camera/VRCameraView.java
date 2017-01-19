package com.ringov.vrquestclient.vr_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Сергей on 16.01.2017.
 */

public class VRCameraView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int POSITION_UPPER_LEFT = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_BEST_FIT = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private VRCameraThread thread;
    private VRCameraInputStream mIn = null;
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

    public class VRCameraThread extends Thread {
        private SurfaceHolder holderLeft;
        private SurfaceHolder holderRight;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;

        public VRCameraThread(SurfaceHolder leftHolder, SurfaceHolder rightHolder, Context context) {
            holderLeft = leftHolder;
            holderRight = rightHolder;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == VRCameraView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == VRCameraView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == VRCameraView.SIZE_FULLSCREEN)
                return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (holderLeft) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth = b.width() + 2;
            int bheight = b.height() + 2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left + 1, (bheight / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
            return bm;
        }

        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm = null;
            int width;
            int height;
            Rect destRect;
            Canvas c1 = null;
            Canvas c2 = null;
            Paint p = new Paint();
            String fps = "";

            while (mRun) {
                if (surfaceDone) {
                    try {

                        try {
                            bm = mIn.readMjpegFrame();
                        } catch (IOException e) {

                        }
                        c1 = holderLeft.lockCanvas();
                        synchronized (holderLeft) {
                            if(bm == null){
                                continue;
                            }
                            destRect = destRect(bm.getWidth(), bm.getHeight());
                            c1.drawColor(Color.BLACK);
                            c1.drawBitmap(bm, null, destRect, p);
                            if (showFps) {
                                p.setXfermode(mode);
                                if (ovl != null) {
                                    height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                    width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
                                    c1.drawBitmap(ovl, width, height, null);
                                }
                                p.setXfermode(null);
                                frameCounter++;
                                if ((System.currentTimeMillis() - start) >= 1000) {
                                    fps = String.valueOf(frameCounter) + "fps";
                                    frameCounter = 0;
                                    start = System.currentTimeMillis();
                                    ovl = makeFpsOverlay(overlayPaint, fps);
                                }
                            }
                        }
                        c2 = holderRight.lockCanvas();
                        synchronized (holderRight) {
                            if(bm == null){
                                continue;
                            }
                            destRect = destRect(bm.getWidth(), bm.getHeight());
                            c2.drawColor(Color.BLACK);
                            c2.drawBitmap(bm, null, destRect, p);
                            if (showFps) {
                                p.setXfermode(mode);
                                if (ovl != null) {
                                    height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                    width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
                                    c2.drawBitmap(ovl, width, height, null);
                                }
                                p.setXfermode(null);
//                                frameCounter++;
//                                if ((System.currentTimeMillis() - start) >= 1000) {
//                                    fps = String.valueOf(frameCounter) + "fps";
//                                    frameCounter = 0;
//                                    start = System.currentTimeMillis();
//                                    ovl = makeFpsOverlay(overlayPaint, fps);
//                                }
                            }
                        }
                    } finally {
                        if (c1 != null) holderLeft.unlockCanvasAndPost(c1);
                        if(c2 != null) holderRight.unlockCanvasAndPost(c2);
                    }
                }
            }
        }
    }

    private SurfaceView right;

    public void init(Context context, SurfaceView right) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        this.right = right;
        if(right != null) {
            thread = new VRCameraThread(holder, right.getHolder(), context);
        }
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = VRCameraView.POSITION_LOWER_RIGHT;
        displayMode = VRCameraView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    public void startPlayback() {
        if (mIn != null) {
            mRun = true;
            thread.start();
        }
    }

    public void stopPlayback() {
        mRun = false;
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public VRCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    public VRCameraView(Context context) {
        super(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    public void showFps(boolean b) {
        showFps = b;
    }

    public void setSource(VRCameraInputStream source) {
        mIn = source;
        startPlayback();
    }

    public void setOverlayPaint(Paint p) {
        overlayPaint = p;
    }

    public void setOverlayTextColor(int c) {
        overlayTextColor = c;
    }

    public void setOverlayBackgroundColor(int c) {
        overlayBackgroundColor = c;
    }

    public void setOverlayPosition(int p) {
        ovlPos = p;
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }
}
