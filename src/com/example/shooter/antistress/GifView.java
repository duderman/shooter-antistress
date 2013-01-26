package com.example.shooter.antistress;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class GifView extends SurfaceView {

	public static final int IMAGE_TYPE_UNKNOWN = 0;
	public static final int IMAGE_TYPE_STATIC = 1;
	public static final int IMAGE_TYPE_DYNAMIC = 2;

	public static final int DECODE_STATUS_UNDECODE = 0;
	public static final int DECODE_STATUS_DECODING = 1;
	public static final int DECODE_STATUS_DECODED = 2;

	public static final int FPS = 24;
	public static final int LOOPS = 1;

	private GifDecoder decoder;
	private Bitmap currBitmap;

	public int imageType = IMAGE_TYPE_UNKNOWN;
	public int decodeStatus = DECODE_STATUS_UNDECODE;

	private int width;
	private float minX, stepX, totalX, totalTimeGif;
	private float x, y;
	long lastStepTimeX, timeStepX, lastStepTimeGif;

	private int currFrame;

	private int resId;

	public boolean playFlag = false;
	Thread drawingThread = null;
	Thread framingThread = null;

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor
	 */
	public GifView(Context context) {
		super(context);

	}

	private InputStream getInputStream() {
		if (resId > 0)
			return getContext().getResources().openRawResource(resId);
		return null;
	}

	/**
	 * set gif resource id
	 * 
	 * @param resId
	 */
	public void setGif(int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
		setGif(resId, bitmap);
	}

	/**
	 * set gif resource id and cache image
	 * 
	 * @param resId
	 * @param cacheImage
	 */
	public void setGif(int resId, Bitmap cacheImage) {
		this.resId = resId;
		imageType = IMAGE_TYPE_UNKNOWN;
		decodeStatus = DECODE_STATUS_UNDECODE;
		lastStepTimeGif = lastStepTimeX = 0;
		playFlag = false;
		currBitmap = cacheImage;
		width = currBitmap.getScaledWidth(currBitmap.getDensity());
		x = this.getWidth();
		y = this.getHeight();
		minX = (x - width) / 2;
		totalX = x - minX;

		Log.d("Coordinates obtained",
				"(" + Float.toString(x) + "; " + Float.toString(y) + ")");
	}

	private void decode() {
		release();
		currFrame = 0;

		decoder = new GifDecoder();
		decoder.read(getInputStream());
		if (decoder.width == 0 || decoder.height == 0) {
			imageType = IMAGE_TYPE_STATIC;
		} else {
			imageType = IMAGE_TYPE_DYNAMIC;
		}
		totalTimeGif = decoder.getDuration();
		stepX = totalX / ((totalTimeGif * LOOPS) / 1000);
		Log.d("Dimensions",
				"totalTimeGif=" + Float.toString(totalTimeGif) + "; stepX="
						+ Float.toString(stepX) + "; totalX="
						+ Float.toString(totalX));
		decodeStatus = DECODE_STATUS_DECODED;
	}

	public void release() {
		decoder = null;
	}

	protected void Draw() {
		updateCoordinates();
		
		Canvas canvas = null;
		canvas = getHolder().lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		if (currBitmap != null) {
			currBitmap = decoder.getFrame(currFrame);
			canvas.drawBitmap(currBitmap, x, y, null);
		}
		getHolder().unlockCanvasAndPost(canvas);
	}

	private void incrementFrameIndex() {
		currFrame++;
		if (currFrame >= decoder.getFrameCount() - 1) {
			currFrame = 0;
		}
	}

	public void play() {
		// TODO two threads for changing coordinates by FPS and frames by delay

		playFlag = true;

		if (decodeStatus == DECODE_STATUS_UNDECODE) {
			if (playFlag) {
				decode();
			}
		}
		new Thread() {
			@Override
			public void run() {
				while (playFlag) {
					try {
						incrementFrameIndex();
						sleep(decoder.getDelay(currFrame));
						Log.d("next frame", Integer.toString(currFrame));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();

		while (playFlag) {
			Draw();
			try {
				Thread.sleep(1000 / FPS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void pause() {
		playFlag = false;
		invalidate();
	}

	public void stop() {
		playFlag = false;

		currFrame = decoder.getFrameCount() - 1;
	}

	private void updateCoordinates() {
		x -= stepX / FPS;
		y = this.getHeight() - 2 * (-x + this.getWidth());
		Log.d("Coordinates", "(" + Float.toString(x) + "; " + Float.toString(y)
				+ ")");

		if (x <= minX) {
			Log.d("Stopping", "because x(" + Float.toString(x) + ")<=minX("
					+ Float.toString(minX) + ")");
			stop();
		}
	}

	public void getFinalBitmap(Canvas c) {
		c.drawBitmap(currBitmap, x, y, null);
	}

	public void clear() {
		Canvas c = this.getHolder().lockCanvas();
		c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		this.getHolder().unlockCanvasAndPost(c);
	}
}