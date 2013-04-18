package com.example.shooter.antistress;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Menu extends Activity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.menu);

		this.findViewById(R.id.tomatoImageButton).setOnClickListener(this);
		this.findViewById(R.id.pieImageButton).setOnClickListener(this);
		this.findViewById(R.id.axeImageButton).setOnClickListener(this);
		this.findViewById(R.id.eggImageButton).setOnClickListener(this);
		this.findViewById(R.id.knifeImageButton).setOnClickListener(this);
		this.findViewById(R.id.hunterKnifeImageButton).setOnClickListener(this);
		
		this.findViewById(R.id.exitImageButton).setOnClickListener(this);
		this.findViewById(R.id.moreAppsImageButton).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.exitImageButton) {
			finish();
		} else if (v.getId() == R.id.moreAppsImageButton) {
			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://play.google.com/store/apps/developer?id=i-Free+Innovations"));
			startActivity(browserIntent);
		} else {
			Intent intent = new Intent();
			intent.setClass(this, Main.class);
			intent.putExtra(getString(R.string.intent_extra_name), v.getId());
			startActivity(intent);
		}
	}

}
