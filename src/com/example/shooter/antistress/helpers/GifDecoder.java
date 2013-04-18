package com.example.shooter.antistress.helpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;

public class GifDecoder {

	public int width;
	public int height;
	private int screenWidth;
	private int columnsCount;
	private int rowsCount;
	private int frameCount;
	private int delay;
	public int duration;
	public Bitmap mainBitmap;

	private final int STATUS_OK = 0;
	private final int STATUS_ERROR = 1;
	private final int STATUS_CLEAN = 3;
	protected int status = STATUS_OK;

	public int getDelay() {
		return delay;
	}

	public int getDuration() {
		return duration;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public Rect getFrame(int n, int row) {
		if (frameCount <= 0)
			return null;
		if(n>columnsCount-1 || row > rowsCount-1)
			return new Rect((columnsCount-1)*width, (rowsCount-1)*height, (columnsCount-1)*width + width, (rowsCount-1)*height + height);
		int srcX = n * width;
		int srcY = row * height;
		return new Rect(srcX, srcY, srcX + width, srcY + height);
	}

	public void clear() {
		if (err())
			return;
		if (mainBitmap != null) {
			mainBitmap.recycle();
			width = height = rowsCount = columnsCount = frameCount = delay = duration = 0;
			status = STATUS_CLEAN;
		}
	}

	public int readFile(Resources res, int srcId) {
		clear();
		BitmapFactory.Options opts = new Options();
		 opts.inJustDecodeBounds = true;
		 BitmapFactory.decodeResource(res, srcId, opts);
		 opts.inSampleSize = calculateInSampleSize(opts, (this.screenWidth/3)*columnsCount);
		opts.inJustDecodeBounds = false;
		mainBitmap = BitmapFactory.decodeResource(res, srcId, opts);
		if (mainBitmap == null) {
			status = STATUS_ERROR;
		} else {
			this.width = mainBitmap.getWidth() / this.columnsCount;
			this.height = mainBitmap.getHeight() / this.rowsCount;
			this.frameCount = this.columnsCount;
			this.delay = this.duration / this.frameCount;
		}
		return status;
	}

	public void init(int width, int height, int duration, int rows, int columns) {
		this.screenWidth = width;
		this.duration = duration;
		this.rowsCount = rows;
		this.columnsCount = columns;
	}

	protected boolean err() {
		return status != STATUS_OK || mainBitmap == null || width == 0
				|| height == 0 || rowsCount == 0 || duration == 0;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (width > reqWidth) {
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}

		return inSampleSize;
	}

}