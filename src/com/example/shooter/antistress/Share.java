package com.example.shooter.antistress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class Share extends Activity {

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	ImageView finalImageImageView;
	String imagePath;
	ImageButton shareButton;
	ImageButton saveButton;
	ImageButton shootAgainButton;

	MediaScannerConnection mScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.share);
		finalImageImageView = (ImageView) findViewById(R.id.finalImageImageView);
		imagePath = getIntent().getStringExtra("finalImagePath");
		finalImageImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
		shootAgainButton = (ImageButton) this
				.findViewById(R.id.shootAgainImageButton);
		shootAgainButton.setOnClickListener(myShootAgainBtnOnClickListener);
		shareButton = (ImageButton) this.findViewById(R.id.shareImageButton);
		shareButton.setOnClickListener(myShareBtnOnClickListener);
		saveButton = (ImageButton) this.findViewById(R.id.saveImageButton);
		saveButton.setOnClickListener(mySaveBtnOnClickListener);

	}

	OnClickListener myShareBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("image/jpeg");
			sharingIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + imagePath));
			startActivity(Intent.createChooser(sharingIntent,
					getString(R.string.title_share_camera)));
		}
	};

	OnClickListener mySaveBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Uri fileUri = saveFinalImage();
			if (fileUri != Uri.EMPTY) {
				saveButton.setEnabled(false);
				Toast.makeText(getApplicationContext(), R.string.save_success,
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	OnClickListener myShootAgainBtnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private Uri saveFinalImage() {
		Uri uri = Uri.EMPTY;

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Bitmap tmpBmp = BitmapFactory.decodeFile(imagePath);
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long bytesAvailable = (long) stat.getBlockSize()
					* (long) stat.getAvailableBlocks();
			if (bytesAvailable > tmpBmp.getRowBytes() * tmpBmp.getHeight()) {
				try {
					String fileName = JPEG_FILE_PREFIX
							+ new SimpleDateFormat("yyyyMMdd_HHmmss")
									.format(new Date()) + JPEG_FILE_SUFFIX;
					File saveDir = new File(Environment
							.getExternalStoragePublicDirectory(
									Environment.DIRECTORY_PICTURES).getPath()
							+ "/" + getString(R.string.app_name) + "/");
					if (!saveDir.exists()) {
						saveDir.mkdirs();
					}

					final File imageFile = new File(saveDir, fileName);

					FileOutputStream fos = new FileOutputStream(imageFile);
					tmpBmp.compress(CompressFormat.JPEG, 100, fos);
					fos.flush();
					fos.close();
					mScanner = new MediaScannerConnection(
							getApplicationContext(),
							new MediaScannerConnectionClient() {
								@Override
								public void onScanCompleted(String path, Uri uri) {
									mScanner.disconnect();
								}

								@Override
								public void onMediaScannerConnected() {
									mScanner.scanFile(
											imageFile.getAbsolutePath(),
											"image/jpeg");
								}
							});
					if (mScanner != null)
						mScanner.connect();
					uri = Uri.fromFile(imageFile);
				} catch (FileNotFoundException e) {
					Log.e("Exception", "Exception while saving", e);
					Toast.makeText(getApplicationContext(),
							R.string.create_error, Toast.LENGTH_LONG)
							.show();
				} catch (Exception e) {
					Log.e("Exception", "Exception while saving", e);
					Toast.makeText(getApplicationContext(),
							R.string.write_error, Toast.LENGTH_LONG)
							.show();
				} finally {
					tmpBmp.recycle();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						R.string.free_space_error, Toast.LENGTH_LONG)
						.show();
				tmpBmp.recycle();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					R.string.storage_error, Toast.LENGTH_LONG)
					.show();
		}

		return uri;
	}
}
