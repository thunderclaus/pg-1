package com.pku.pg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private ImageView enterBtn;
	static TextView tv_wetAlert;
	static TextView tv_keyAlert;
	static TextView tv_dropAlert;
	static String wetAlertTime;
	static String keyAlertTime;
	static String dropAlertTime;
	static SharedPreferences sp;
	static Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		enterBtn = (ImageView)findViewById(R.id.imageView_enter_Main);
		enterBtn.setOnClickListener(this);	
		sp = getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
		tv_wetAlert = (TextView)findViewById(R.id.wetAlertTime);
		tv_keyAlert = (TextView)findViewById(R.id.keyAlertTime);
		tv_dropAlert = (TextView)findViewById(R.id.dropAlertTime);
		boolean checkSucFlag = sp.getBoolean("checkSucFlag", false);
		if(checkSucFlag){
			Intent intent = new Intent(this,BtService.class);
			String deviceID = Info.infoSharedPreferences.getString("deviceID", "");
			Bundle bundle = new Bundle();
			bundle.putString("deviceID", deviceID);
			intent.putExtras(bundle);
			this.startService(intent);
		}
		context = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void onStart(){
		super.onStart();
		tv_wetAlert.setText(sp.getString("wetAlertTime", ""));
		tv_keyAlert.setText(sp.getString("keyAlertTime", ""));
		tv_dropAlert.setText(sp.getString("dropAlertTime", ""));
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
	public static void SendMessage(Handler handler, int i){
		Message msg = handler.obtainMessage();
		msg.what = i;
		handler.sendMessage(msg);
		}
	public static Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:		
				tv_wetAlert.setText(wetAlertTime);
				break;
			case 2:
				tv_keyAlert.setText(keyAlertTime);
				break;
			case 3:
				tv_dropAlert.setText(dropAlertTime);
				break;			
			case 4:
				Toast.makeText(context, "×¢²áÓÃ»§Ê§°Ü", Toast.LENGTH_LONG).show();
				break;
			case 5:
				Toast.makeText(context, "×¢²á»¤¹¤Ê§°Ü", Toast.LENGTH_LONG).show();
				break;
			case 6:
				Toast.makeText(context, "×¢²áÇ×ÊôÊ§°Ü", Toast.LENGTH_LONG).show();
				break;
				}
			}
	};
}
