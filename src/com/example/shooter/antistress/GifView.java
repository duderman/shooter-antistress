package com.example.shooter.antistress;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class GifView extends SurfaceView {

	public static final int DECODE_STATUS_UNDECODE = 0;
	public static final int DECODE_STATUS_DECODING = 1;
	public static final int DECODE_STATUS_DECODED = 2;

	public static final int FPS = 24;
	public static final int LOOPS = 1;

	private GifDecoder decoder;

	public int decodeStatus = DECODE_STATUS_UNDECODE;

	private int width, height;
	private float x, y;

	private boolean playFlag = false;
	private int currFrame;
	private int resId;

	private Bitmap lastBitmap;

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
		decodeStatus = DECODE_STATUS_UNDECODE;
		setResIdAndDimensions(this.getWidth(), this.getHeight());
		decode();
	}

	private void setResIdAndDimensions(int targetWidth, int targetHeight) {
		width = targetWidth;
		height = targetHeight;
		x = y = 0;
		switch (resId) {
		case R.id.tomatoImageButton:
			this.resId = R.drawable.gif_tomato;
			break;
		case R.id.bottleImageButton:
			this.resId = R.drawable.gif_bottle;
			break;
		case R.id.axeImageButton:
			this.resId = R.drawable.gif_axe;
			width = (int) (targetWidth * 1.3);
			y -= (int) (targetHeight * 0.2);
			break;
		case R.id.eggImageButton:
			this.resId = R.drawable.gif_egg;
			break;
		case R.id.knifeImageButton:
			this.resId = R.drawable.gif_knife;
			width = (int) (targetWidth * 1.1);
			y -= (int) (targetHeight * 0.1);
			break;
		case R.id.hunterKnifeImageButton:
			this.resId = R.drawable.gif_hunter_knife;
			width = (int) (targetWidth * 1.2);
			y -= (int) (targetHeight * 0.2);
			x += (int) (targetWidth * 0.2);
			break;
		default:
			this.resId = R.drawable.gif_tomato;
			break;
		}
	}

	private void decode() {
		release();
		currFrame = 0;

		new Thread() {
			@Override
			public void run() {
				decoder = new GifDecoder();
				decoder.setFinalDimensions(width, height);
				decoder.read(getInputStream());
				decodeStatus = DECODE_STATUS_DECODED;
			}
		}.start();
	}

	public void release() {
		if (decoder != null) {
			decoder.clear();
			decoder = null;
			decodeStatus = DECODE_STATUS_UNDECODE;
		}
	}

	protected void Draw() {
		Canvas canvas = null;
		canvas = getHolder().lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(decoder.getFrame(currFrame), x, y, null);
		getHolder().unlockCanvasAndPost(canvas);
	}

	private void incrementFrameIndex() {
		currFrame++;
		if (currFrame >= decoder.getFrameCount()) {
			currFrame = decoder.getFrameCount() - 1;
			stop();
		}
	}

	public void play() {
		try {
			while (decodeStatus != DECODE_STATUS_DECODED) {
			}
			playFlag = true;
			while (playFlag) {
				Draw();
				Thread.sleep(decoder.getDelay(currFrame));
				incrementFrameIndex();
			}
			lastBitmap = decoder.getFrame(currFrame);
			release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void pause() {
		playFlag = false;
		invalidate();
	}

	public void stop() {
		playFlag = false;
	}

	public void getFinalBitmap(Canvas c) {
		setResIdAndDimensions(com.example.shooter.antistress.Main.PHOTO_WIDTH,
				com.example.shooter.antistress.Main.PHOTO_HEIGHT);
		c.drawBitmap(lastBitmap, x, y, null);
		lastBitmap.recycle();
		release();
	}

	public void clear() {
		Canvas c = this.getHolder().lockCanvas();
		c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		this.getHolder().unlockCanvasAndPost(c);
	}
}