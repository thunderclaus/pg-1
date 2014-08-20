package com.pku.pg;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener{

	private ImageView enterBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		enterBtn = (ImageView)findViewById(R.id.imageView_enter_Main);
		enterBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imageView_enter_Main:
			Intent intent=new Intent();  
            intent.setClass(MainActivity.this,Info.class);  
            startActivity(intent);
			break;

		default:
			break;
		}
	}
	public void onDestroy(){
		super.onDestroy();
		this.stopService(new Intent(this,BtService.class));
	}

}
