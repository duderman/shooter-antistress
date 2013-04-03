package com.example.shooter.antistress;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class GifView extends SurfaceView {

	public static final int DECODE_STATUS_UNDECODE = 0;
	public static final int DECODE_STATUS_DECODING = 1;
	public static final int DECODE_STATUS_DECODED = 2;

	public static final int FPS = 30;

	private GifDecoder decoder;

	SoundPool sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	private int startSound;
	private int endSound;

	public int decodeStatus = DECODE_STATUS_UNDECODE;

	private int startX, startY;
	public int finalX, finalY;
	private int currentX, currentY;
	private int currentW, currentH;
	private double speed;
	private double angle;

	private boolean playFlag = false;
	private int currFrame;
	private int currRow;
	private int loops = 2;
	private int initialBitmapScale = 2;
	private int resId;

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifView(Context context) {
		super(context);
	}

	public void setGif(int resId) {
		this.resId = resId;
		decodeStatus = DECODE_STATUS_UNDECODE;
		setDimAndDecode();
	}

	private void setDimAndDecode() {
		startX = startY = finalX = finalY = 0;
		int columns = 1;
		int rows = 2;
		int duration = 600;

		startSound = sounds.load(getContext(), R.raw.throwing, 1);

		switch (resId) {
		default:
		case R.id.tomatoImageButton:
			this.resId = R.drawable.animate_tomato;
			endSound = sounds.load(getContext(), R.raw.tomato, 2);
			columns = 11;
			break;
		case R.id.bottleImageButton:
			this.resId = R.drawable.animate_bottle;
			endSound = sounds.load(getContext(), R.raw.bottle, 2);
			columns = 11;
			break;
		case R.id.axeImageButton:
			this.resId = R.drawable.animate_axe;
			endSound = sounds.load(getContext(), R.raw.axe, 2);
			columns = 6;
//			duration = 500;
			break;
		case R.id.eggImageButton:
			this.resId = R.drawable.animate_egg;
			endSound = sounds.load(getContext(), R.raw.egg, 2);
			columns = 9;
			duration = 800;
			break;
		case R.id.knifeImageButton:
			this.resId = R.drawable.animate_knife;
			endSound = sounds.load(getContext(), R.raw.knife, 2);
			columns = 7;
//			duration = 500;
			break;
		case R.id.hunterKnifeImageButton:
			this.resId = R.drawable.animate_hunter_knife;
			endSound = sounds.load(getContext(), R.raw.hunter_knife, 2);
			columns = 5;
			duration = 300;
			break;
		}

		release();
		currFrame = currRow = 0;
		decoder = new GifDecoder();
		decoder.init(this.getWidth(), this.getHeight(), duration, rows, columns);
		new Thread() {
			@Override
			public void run() {
				decoder.readFile(getResources(), resId);
				decodeStatus = DECODE_STATUS_DECODED;
			}
		}.start();
	}

	public void release() {
		if (decoder != null) {
			decoder.clear();
			decoder = null;
		}
		decodeStatus = DECODE_STATUS_UNDECODE;
	}

	protected void Draw() {
		Canvas canvas = null;
		canvas = getHolder().lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(decoder.mainBitmap, decoder.getFrame(currFrame,
				currRow), new Rect(currentX, currentY, currentX + currentW,
				currentY + currentH), null);
		getHolder().unlockCanvasAndPost(canvas);
		Log.d("Drawed", "Canvas rendered");
	}

	private void updateCoordinates() {
		if (currentH > decoder.height)
			currentH -= ((decoder.height * initialBitmapScale - decoder.height)
					/ ((decoder.duration * loops) / 1000) / FPS);
		if (currentW > decoder.width)
			currentW -= ((decoder.width * initialBitmapScale - decoder.width)
					/ ((decoder.duration * loops) / 1000) / FPS);
		currentX -= Math.round(speed * Math.cos(angle));
		if (startY > finalY)
			currentY -= Math.round(speed * Math.sin(angle));
		else
			currentY += Math.round(speed * Math.sin(angle));
		Log.d("Dimensions", "currentXY = { " + currentX + " ; " + currentY
				+ " }");
		if (startX > finalX && startY > finalY) {
			if (currentX <= finalX && currentY <= finalY) {
				stop();
			}
		}
		if (startX < finalX && startY > finalY) {
			if (currentX >= finalX && currentY <= finalY) {
				stop();
			}
		}
		if (startX > finalX && startY < finalY) {
			if (currentX <= finalX && currentY >= finalY) {
				stop();
			}
		}
		if (Math.round(speed * Math.cos(angle)) == 0)
			if (startY > finalY) {
				if (currentY <= finalY) {
					stop();
				}
			} else {
				if (currentY > finalY) {
					stop();
				}
			}
		if (Math.round(speed * Math.sin(angle)) == 0)
			if (startX > finalX) {
				if (currentX <= finalX) {
					stop();
				}
			} else {
				if (currentX >= finalX) {
					stop();
				}
			}
	}

	private void incrementFrameIndex() {
		currFrame++;
		if (currFrame >= decoder.getFrameCount()) {
			currFrame = 0;
		}
		Log.d("nextFrame", "" + currFrame);
	}

	public void play() {
		while (decodeStatus != DECODE_STATUS_DECODED) {
		}
		if (finalX == 0 || finalY == 0) {
			finalX = (this.getWidth()) / 2;
			finalY = this.getHeight() / 3 - decoder.height / 2;
		} else {
			finalX = finalX / 2;
			finalY = finalY - (decoder.height) / 2;
		}

		this.startX = this.getWidth() - decoder.width;
		this.startY = this.getHeight();

		this.currentX = this.startX;
		this.currentY = this.startY;
		this.currentH = decoder.height * initialBitmapScale;
		this.currentW = decoder.width * initialBitmapScale;
		int vectX = finalX - startX;
		int vectY = finalY - startY;
		int secondVectX = startX - 10 - startX;
		int secondVectY = startY - startY;
		double cos = (double) (vectX * secondVectX + vectY * secondVectY)
				/ (Math.sqrt(Math.pow(vectX, 2) + Math.pow(vectY, 2)) * Math
						.sqrt(Math.pow(secondVectX, 2)
								+ Math.pow(secondVectY, 2)));
		this.angle = Math.acos(cos);
		double vectLength = Math.sqrt(Math.pow(vectX, 2) + Math.pow(vectY, 2));
		double durby1000 = (double) (decoder.duration) / (double) 1000;
		double lenbydur = vectLength / durby1000;
		speed = Math.round(lenbydur / FPS)/loops;

		Log.d("Dimensions", "startXY= { " + startX + " ; " + startY
				+ " }; finalXY={ " + finalX + " ; " + finalY + " }; speed= "
				+ Math.round(speed) + " angel= " + angle);

		playFlag = true;
		new Thread() {
			@Override
			public void run() {
				while (playFlag || currFrame != 0) {
					try {
						sleep(decoder.getDelay());
					} catch (InterruptedException e) {
						showError();
					}
					incrementFrameIndex();
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				try {
					while (playFlag) {
						sleep(1000 / FPS);
						updateCoordinates();
					}
				} catch (InterruptedException e) {
					Log.e("Exception", "Exception while drawing", e);
					showError();
				}
			}
		}.start();
		try {
			sounds.play(startSound, 1.0f, 1.0f, 1, 0, 1.5f);
			while (playFlag || currFrame != 0) {
				Draw();
				Thread.sleep(1000/FPS);
			}
		} catch (InterruptedException e) {
			Log.e("Exception", "Exception while drawing", e);
			showError();
		}
		currentH = decoder.height;
		currentW = decoder.width;
		sounds.play(endSound, 1.0f, 1.0f, 2, 0, 1.5f);
		currRow = 1;
		for (currFrame = 0; currFrame < decoder.getFrameCount(); currFrame++) {
			Draw();
			try {
				Thread.sleep(decoder.getDelay());
			} catch (InterruptedException e) {
				Log.e("Exception", "Exception while final drawing", e);
				showError();
			}
		}
	}

	public void stop() {
		playFlag = false;
	}

	public void getFinalBitmap(Canvas c) {
		float propX = ((float) c.getWidth() / (float) this.getWidth());
		float propY = ((float) c.getHeight() / (float) this.getHeight());
		c.drawBitmap(decoder.mainBitmap, decoder.getFrame(
				decoder.getFrameCount() - 1, 1), new Rect(
					Math.round(currentX * propX), 
					Math.round(currentY * propY),
					Math.round(currentX * propX + decoder.width * propX),
					Math.round(currentY * propY + decoder.height * propY)),
				null);
		release();
	}

	public void clear() {
		Canvas c = this.getHolder().lockCanvas();
		c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		this.getHolder().unlockCanvasAndPost(c);
	}

	private void showError() {
		Toast.makeText(getContext(), "Error while drawing animation. Sorry!",
				Toast.LENGTH_LONG).show();
		((Activity) getContext()).finish();
	}
}