package com.example.shooter.antistress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Main extends Activity implements SurfaceHolder.Callback,
		Camera.AutoFocusCallback, Camera.PreviewCallback,
		Camera.PictureCallback {
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private static final int PHOTO_WIDTH = 480;
	private static final int PHOTO_HEIGHT = 720;

	private SurfaceView cameraView;
	private GifView weaponView;
	private SurfaceHolder cameraHolder;
	private SurfaceHolder weaponHolder;
	private Camera camera;
	private Button throwButton;
	private Button saveButton;
	private Button shareButton;
	public Bitmap finalBitmap;
	public Uri fileUri = Uri.EMPTY;
	private RelativeLayout myLayout;

	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.ERROR;

	private enum CameraViewStatusCodes {
		ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.main);

		cameraView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
		cameraHolder = cameraView.getHolder();
		cameraHolder.addCallback(this);
		cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		weaponView = ((GifView) findViewById(R.id.throwingObjectSurfaceView));
		weaponHolder = weaponView.getHolder();
		weaponHolder.setFormat(PixelFormat.TRANSLUCENT);
		weaponView.setZOrderOnTop(true);
		// weaponView.setGif(R.drawable.apple);
		throwButton = (Button) findViewById(R.id.throwButton);
		throwButton.setOnClickListener(myBtnOnClickListener);
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
		shareButton = (Button) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(mySaveAndShareBtnOnClickListener);
		weaponHolder.addCallback(weaponView);
		myLayout = (RelativeLayout)getLayoutInflater().inflate(R.layout.main, null, false);
		Log.d("watch", "onCreate");
	}

	@Override
	protected void onPause() {//test
		super.onPause();
		
		Log.d("watch", "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//TODO onpause on resume causes saving and loading image (onSaveInstanceState)
		/*camera = Camera.open();
		if (cameraViewStatus == CameraViewStatusCodes.DRAWING_ENDED) {
			surfaceCreated(cameraHolder);
		}*/
		Log.d("watch", "onResume");
	}
	
	@Override
	protected void onPostResume() {
		super.onPostResume();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("watch", "SurfaceDestroyed");
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("watch", "SurfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("watch", "SurfaceCreated");

		try {
			camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setRotation(90);
			camera.setParameters(parameters);
			camera.setDisplayOrientation(90);
			camera.setPreviewCallback(this);
			camera.setPreviewDisplay(cameraHolder);

			if (cameraViewStatus != CameraViewStatusCodes.DRAWING_ENDED) {
				camera.startPreview();
				cameraViewStatus = CameraViewStatusCodes.WAITING;
			}
		} catch (IOException e) {
			Log.d("Throwy", "Exception");
			e.printStackTrace();
			cameraViewStatus = CameraViewStatusCodes.ERROR;
		}
	}

	OnClickListener myBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d("watch", "onClick " + cameraViewStatus.toString());
			switch (cameraViewStatus) {
			case WAITING: {
				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
				throwButton.setVisibility(View.INVISIBLE);
				camera.autoFocus(myAutoFocusCallback);
			}
				break;
			case DRAWING_ENDED: {
				weaponView.clear();
				fileUri = Uri.EMPTY;
				finalBitmap.recycle();
				saveButton.setVisibility(View.INVISIBLE);
				shareButton.setVisibility(View.INVISIBLE);
				throwButton.setText(getString(R.string.throw_button_caption));

				cameraViewStatus = CameraViewStatusCodes.WAITING;
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

	OnClickListener mySaveAndShareBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == saveButton.getId()) {
				fileUri = saveFinalImage();
				if (fileUri != Uri.EMPTY) {
					saveButton.setVisibility(View.INVISIBLE);
					Toast.makeText(getApplicationContext(), "Saving succeful",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				if (fileUri == Uri.EMPTY) {
					onClick(saveButton);
				}
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("image/jpeg");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
				startActivity(Intent.createChooser(sharingIntent,
						getString(R.string.title_share_camera)));
				shareButton.setVisibility(View.INVISIBLE);
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
			throwButton.setVisibility(View.VISIBLE);
			saveButton.setVisibility(View.VISIBLE);
			shareButton.setVisibility(View.VISIBLE);
			throwButton.setText(getString(R.string.throw_button_back_caption));

			try {
				Bitmap fotoBitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				/*
				 * int fotoBitmapWidth = fotoBitmap.getWidth(); int
				 * fotoBitmapHeight = fotoBitmap.getHeight(); File tmpFile = new
				 * File(getExternalCacheDir().getPath()+"tmpImage.dat");
				 * tmpFile.getParentFile().mkdirs(); RandomAccessFile
				 * randomAccessFile = new RandomAccessFile(tmpFile, "rw");
				 * FileChannel fileChannel = randomAccessFile.getChannel();
				 * MappedByteBuffer map = fileChannel.map(MapMode.READ_WRITE, 0,
				 * fotoBitmapWidth*fotoBitmapHeight*4);
				 * fotoBitmap.copyPixelsToBuffer(map); fotoBitmap.recycle();
				 * 
				 * finalBitmap = Bitmap.createBitmap(fotoBitmapWidth,
				 * fotoBitmapHeight, Config.ARGB_8888); map.position(0);
				 * finalBitmap.copyPixelsFromBuffer(map); fileChannel.close();
				 * randomAccessFile.close();
				 */
				if (!fotoBitmap.isMutable()) {
					finalBitmap = Bitmap.createScaledBitmap(fotoBitmap,
							PHOTO_WIDTH, PHOTO_HEIGHT, false);
					fotoBitmap.recycle();
				}

				Canvas finalCanvas = new Canvas(finalBitmap);
				weaponView.getFinalBitmap(finalCanvas);
				finalCanvas.save();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	private void Throw() {
		weaponView.setGif(R.drawable.apple);
		weaponView.play();
	}

	private Uri saveFinalImage() {
		Uri uri = Uri.EMPTY;

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				File saveDir = new File(Environment
						.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_PICTURES).getPath()
						+ "/" + getString(R.string.app_name) + "/");
				if (!saveDir.exists()) {
					saveDir.mkdirs();
				}
				File imageFile = new File(saveDir,
						JPEG_FILE_PREFIX
								+ new SimpleDateFormat("yyyyMMdd_HHmmss")
										.format(new Date()) + JPEG_FILE_SUFFIX);

				FileOutputStream fos = new FileOutputStream(imageFile);
				finalBitmap.compress(CompressFormat.JPEG, 95, fos);
				uri = Uri.fromFile(imageFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				// TODO: localize toasts
				Toast.makeText(getApplicationContext(),
						"Error creating file. Sorry :(", Toast.LENGTH_LONG)
						.show();
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						"Error writing file. Sorry :(", Toast.LENGTH_LONG)
						.show();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Your storage seems was unplugged", Toast.LENGTH_LONG)
					.show();
		}

		return uri;
	}

}
