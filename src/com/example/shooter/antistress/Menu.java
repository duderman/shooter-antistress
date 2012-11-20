package com.example.shooter.antistress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Menu extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        this.findViewById(R.id.tomatoImageButton).setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setClass(this, Main.class);
		intent.putExtra("objectId", v.getId());
	    startActivity(intent);
	}
    
    

}
