package com.example.shooter.antistress;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Menu extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
        
        this.findViewById(R.id.tomatoImageButton).setOnClickListener(this);
        this.findViewById(R.id.exitImageButton).setOnClickListener(this);
        this.findViewById(R.id.moreAppsImageButton).setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.exitImageButton){
			finish();
		} else if (v.getId() == R.id.moreAppsImageButton){
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=i-Free+Innovations"));
			startActivity(browserIntent);
		} else {
			Intent intent = new Intent();
			intent.setClass(this, Main.class);
			switch (v.getId()) {
			case R.id.tomatoImageButton:
				intent.putExtra("objectId", v.getId());
				break;
			}
		    startActivity(intent);	
		}
	}
    
    

}
