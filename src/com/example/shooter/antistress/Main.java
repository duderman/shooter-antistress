package com.example.shooter.antistress;

import java.io.IOException;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity implements SurfaceHolder.Callback,
		Camera.AutoFocusCallback, Camera.PreviewCallback,
		Camera.PictureCallback {

	private SurfaceView cameraView;
	private GifView weaponView;
	private SurfaceHolder cameraHolder;
	private SurfaceHolder weaponHolder;
	private Camera camera;
	private Bitmap weaponBmp;
	private Button throwButton;

	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.ERROR;
	private enum CameraViewStatusCodes {  ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED };

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
		weaponView.setGif(R.drawable.apple);
		throwButton = (Button)findViewById(R.id.throwButton);
		throwButton.setOnClickListener(myBtnOnClickListener);
		weaponHolder.addCallback(weaponView);
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
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setRotation(90);

			camera.setParameters(parameters);

			camera.setDisplayOrientation(90);
			camera.setPreviewCallback(this);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			cameraViewStatus = CameraViewStatusCodes.WAITING;
		} catch (IOException e) {
			Log.d("Throwy", "Exception");
			e.printStackTrace();
			cameraViewStatus = CameraViewStatusCodes.ERROR;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	protected void onResume() {
		super.onResume();
		camera = Camera.open();
	}

	OnClickListener myBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (cameraViewStatus) {
			case WAITING: {
				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
				throwButton.setVisibility(View.INVISIBLE);
				camera.autoFocus(myAutoFocusCallback);
			}break;
			case DRAWING_ENDED: {
				Canvas c = weaponHolder.lockCanvas();
				c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				weaponHolder.unlockCanvasAndPost(c);

				cameraViewStatus = CameraViewStatusCodes.WAITING;
				camera.startPreview();
			}break;
			case ERROR: {
				Toast.makeText(getApplicationContext(),
						"Something wrong with the camera", Toast.LENGTH_LONG).show();
			}break;
			default: {
				Toast.makeText(getApplicationContext(),
						"Have no idea what this is :(", Toast.LENGTH_LONG).show();
			}break;
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
		}
	};
	
	private void Throw() {
		weaponView.setGif(R.drawable.apple);
		weaponView.play();
	}
	
}
