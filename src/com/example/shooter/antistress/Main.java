package com.example.shooter.antistress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main extends Activity implements SurfaceHolder.Callback {

	public static final int PHOTO_WIDTH = 480;
	public static final int PHOTO_HEIGHT = 800;

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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
				if (resId != -1)
					weaponView.setGif(resId);
				else
					weaponView.setGif(R.id.tomatoImageButton);
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
		throwButton.setEnabled(true);
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

	ShutterCallback myShutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			Log.d("camera monitoring", "shutter callback");
			cameraViewStatus = CameraViewStatusCodes.DRAWING;
			Throw();
		}
	};

	PictureCallback myPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("camera monitoring", "picture taken");
			Throw();
			cameraViewStatus = CameraViewStatusCodes.DRAWING_ENDED;

			BitmapFactory.Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, opts);
			opts.inSampleSize = calculateInSampleSize(opts, PHOTO_WIDTH, PHOTO_HEIGHT);
			opts.inJustDecodeBounds = false;
			opts.inPurgeable = true;
			Bitmap finalBitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length, opts);

			int width = finalBitmap.getWidth();
			int height = finalBitmap.getHeight();

			if(width>height){
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				finalBitmap= Bitmap.createBitmap(finalBitmap, 0, 0, width, height,matrix, true);
			}
			//Matrix matrix = new Matrix();
			//matrix.postScale(PHOTO_WIDTH, PHOTO_HEIGHT);
			//finalBitmap = Bitmap.createScaledBitmap(finalBitmap, width, height, true);//(finalBitmap, 0, 0, width, height,matrix, false);
			try {
				// File file = new File(getExternalCacheDir().getPath()+
				// "/image.jpg");
				// file.getParentFile().mkdirs();
				// RandomAccessFile randomAccessFile = new
				// RandomAccessFile(file,"rw");
				// FileChannel channel = randomAccessFile.getChannel();
				// MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0,
				// width* height * 4);
				// finalBitmap.copyPixelsToBuffer(map);
				// finalBitmap.recycle();
				// finalBitmap = Bitmap.createBitmap(width,
				// height,Config.ARGB_8888);
				// map.position(0);
				// finalBitmap.copyPixelsFromBuffer(map);
				// channel.close();
				// randomAccessFile.close();

				Canvas finalCanvas = new Canvas(finalBitmap);
				finalCanvas.setDensity(finalBitmap.getDensity());
				weaponView.getFinalBitmap(finalCanvas);
				finalCanvas.save();
				finalCanvas = null;

				File tmpFile = new File(getExternalCacheDir().getPath()
						+ "/image.jpg");
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
				Log.e("Exception", "Exception while saving", e);
				Toast.makeText(getApplicationContext(), "Can't save final photo. Sorry", Toast.LENGTH_LONG).show();
				throwButton.performClick();
			}

		}
	};

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if((reqWidth<reqHeight && width>height) || (reqWidth>reqHeight && width<height)){
			int tmp;
			tmp = reqWidth;
			reqWidth = reqHeight;
			reqHeight = tmp;
		}

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
			if(cam == null)
				throw new Exception("Don't have back facing camera");
		} catch (Exception e) {
			Log.e("Exception", "Exception while opening camera", e);
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
		int degrees = 90;
		cam.setDisplayOrientation(degrees);
		Camera.Parameters parameters = cam.getParameters();
		Size optimalSize = getOptimalSize(parameters.getSupportedPictureSizes(), PHOTO_WIDTH, PHOTO_HEIGHT); 
		parameters.setPictureSize(optimalSize.width, optimalSize.height);
		optimalSize = getOptimalSize(parameters.getSupportedPreviewSizes(), cameraView.getWidth(), cameraView.getHeight());
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		int rotation = 0;
		for(int id = 0; id<Camera.getNumberOfCameras();id++){
			Camera.getCameraInfo(id, info);
			if(info.facing == CameraInfo.CAMERA_FACING_BACK){
				rotation = info.orientation;
			}
		}
		parameters.setRotation(rotation);
		parameters.set("orientation", "portrait");
		try {
			cam.setParameters(parameters);
			camera.setPreviewDisplay(cameraHolder);
		} catch (Exception e) {
			Log.e("Exception", "Exception while setting prev display", e);
			cameraViewStatus = CameraViewStatusCodes.ERROR;
			camera.release();
		}
	}

	public static Size getOptimalSize(List<Size> sizes, int screenWidth, int screenHeight) {
		final double epsilon = 0.17;
	  double aspectRatio = ((double)screenWidth)/screenHeight;
	  Size optimalSize = null;
	  for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
	    Size currSize =  iterator.next();
	    double curAspectRatio = ((double)currSize.width)/currSize.height;
	    if ( Math.abs( aspectRatio - curAspectRatio ) < epsilon ) {
	      if(optimalSize!=null) {
	        if(optimalSize.height<currSize.height && optimalSize.width<currSize.width) {
	          optimalSize = currSize;
	        }
	      } else {
	        optimalSize = currSize;
	      }
	    }
	  }
	  if(optimalSize == null) {
	  	if(screenWidth < screenHeight)
	  		optimalSize = getOptimalSize(sizes, screenHeight, screenWidth);
	  	else
			optimalSize = sizes.get(0);
	  }
	  return optimalSize;
	}

}
