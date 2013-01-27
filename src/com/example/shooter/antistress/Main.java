package com.example.shooter.antistress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main extends Activity implements SurfaceHolder.Callback {

	public static final int PHOTO_WIDTH = 480;
	public static final int PHOTO_HEIGHT = 720;

	private SurfaceView cameraView;
	private GifView weaponView;
	private SurfaceHolder cameraHolder;
	private SurfaceHolder weaponHolder;
	private Camera camera;
	private ImageButton throwButton;

	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.STARTING;

	private int resId;

	private enum CameraViewStatusCodes {
		ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED, PAUSED, STARTING
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.main);

		resId = getIntent().getIntExtra(getString(R.string.intent_extra_name),
				-1);
		cameraView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
		cameraHolder = cameraView.getHolder();
		cameraHolder.addCallback(this);
		cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		weaponView = ((GifView) findViewById(R.id.throwingObjectSurfaceView));
		weaponHolder = weaponView.getHolder();
		weaponHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.d("watch", "SurfaceDestroyed wv");
				weaponView.release();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.d("watch", "SurfaceCreated wv");
				weaponView.setGif(resId);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.d("watch", "SurfaceChanged wv");
			}
		});
		weaponHolder.setFormat(PixelFormat.TRANSLUCENT);
		weaponView.setZOrderMediaOverlay(true);
		throwButton = (ImageButton) findViewById(R.id.shootImageButton);
		throwButton.setOnClickListener(myBtnOnClickListener);

		Log.d("watch", "onCreate");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (cameraViewStatus == CameraViewStatusCodes.WAITING) {
			camera.stopPreview();
			cameraViewStatus = CameraViewStatusCodes.PAUSED;
		}
		camera.release();
		Log.d("watch", "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
		if (cameraViewStatus == CameraViewStatusCodes.PAUSED) {
			setCameraParameters(camera);
			camera.startPreview();
			cameraViewStatus = CameraViewStatusCodes.WAITING;
		}
		Log.d("watch", "onResume");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("watch", "SurfaceDestroyed");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("watch", "SurfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setCameraParameters(camera);
		if (cameraViewStatus == CameraViewStatusCodes.STARTING) {
			camera.startPreview();
			cameraViewStatus = CameraViewStatusCodes.WAITING;
		} else if (cameraViewStatus == CameraViewStatusCodes.DRAWING_ENDED) {
			throwButton.performClick();
		}
		Log.d("watch", "SurfaceCreated");
	}

	OnClickListener myBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d("watch", "onClick " + cameraViewStatus.toString());
			switch (cameraViewStatus) {
			case WAITING: {
				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
				throwButton.setEnabled(false);
				camera.autoFocus(myAutoFocusCallback);
			}
				break;
			case DRAWING_ENDED: {
				cameraViewStatus = CameraViewStatusCodes.WAITING;
				setCameraParameters(camera);
				camera.startPreview();
			}
				break;
			case ERROR: {
				Toast.makeText(getApplicationContext(),
						"Something wrong with the camera", Toast.LENGTH_LONG)
						.show();
			}
				break;
			default: {
				Toast.makeText(getApplicationContext(),
						"Have no idea what this is :(", Toast.LENGTH_LONG)
						.show();
			}
				break;
			}
		}
	};

	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			camera.takePicture(null, null, null, myPictureCallback);
		}
	};

	PictureCallback myPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			cameraViewStatus = CameraViewStatusCodes.DRAWING;
			Throw();
			cameraViewStatus = CameraViewStatusCodes.DRAWING_ENDED;

			BitmapFactory.Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, opts);
			opts.inSampleSize = calculateInSampleSize(opts, PHOTO_WIDTH,
					PHOTO_HEIGHT);
			opts.inJustDecodeBounds = false;
			Bitmap fotoBitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length, opts);

			Bitmap finalBitmap = Bitmap.createScaledBitmap(fotoBitmap, PHOTO_WIDTH, PHOTO_HEIGHT, false);
			fotoBitmap.recycle();
			fotoBitmap = null;
			Canvas finalCanvas = new Canvas(finalBitmap);
			finalCanvas.setDensity(finalBitmap.getDensity());
			weaponView.getFinalBitmap(finalCanvas);
			finalCanvas.save();
			finalCanvas = null;

			try {
			File tmpFile = new File(getExternalCacheDir().getPath()
					+ "/image.jpg");
			tmpFile.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(tmpFile);
				finalBitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
				finalBitmap.recycle();
				finalBitmap = null;

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), Share.class);
				intent.putExtra("finalImagePath", tmpFile.getPath());
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: error message on saving fault
			}
			throwButton.setEnabled(true);

		}
	};

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	private void Throw() {
		weaponView.play();
	}

	private Camera getCamera() {
		Camera cam = null;
		try {
			cam = Camera.open();
		} catch (Exception e) {
			Log.e("Openin camera", e.getMessage());
			Toast.makeText(
					getApplicationContext(),
					"Error opening camera. May be it unavailible or doesn't exist",
					Toast.LENGTH_LONG).show();
			finish();
		}
		return cam;
	}

	private void setCameraParameters(Camera cam) {
		if (cam == null)
			cam = getCamera();
		Camera.Parameters parameters = cam.getParameters();
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(100);
		parameters.setRotation(90);
		cam.setParameters(parameters);
		cam.setDisplayOrientation(90);

		try {
			camera.setPreviewDisplay(cameraHolder);
		} catch (IOException e) {
			e.printStackTrace();
			cameraViewStatus = CameraViewStatusCodes.ERROR;
			camera.release();
		}
	}

}
