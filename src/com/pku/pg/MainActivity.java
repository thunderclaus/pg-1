package com.pku.pg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
	static TextView tv_battery;
	static TextView tv_dropAlert;
	static String wetAlertTime;
	static String keyAlertTime;
	static String dropAlertTime;
	static ImageView imBtState;
	static ImageView imBattery;
	static SharedPreferences sp;
	static Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		enterBtn = (ImageView)findViewById(R.id.imageView_enter_Main);
		enterBtn.setOnClickListener(this);	
		sp = getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
		imBtState = (ImageView)findViewById(R.id.imageView_bluetooth);
		imBattery = (ImageView)findViewById(R.id.imageView_battery);
		tv_wetAlert = (TextView)findViewById(R.id.wetAlertTime);
		tv_keyAlert = (TextView)findViewById(R.id.keyAlertTime);
		tv_dropAlert = (TextView)findViewById(R.id.dropAlertTime);
		tv_battery = (TextView)findViewById(R.id.textview_battery);
		boolean checkSucFlag = sp.getBoolean("checkSucFlag", false);
		if(checkSucFlag){
			Intent intent = new Intent(this,BtService.class);
			String deviceID = sp.getString("deviceID", "");
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
		int battery = sp.getInt("battery", 0);
		tv_battery.setText(battery+"%");
		imBattery.getDrawable().setLevel(((battery/10+1)/2));
		imBtState.setBackgroundResource(R.drawable.btdisconnect);
		
		Log.d("SP", sp.getAll().toString());
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
				Toast.makeText(context, "注册用户失败，请您检查网络连接", Toast.LENGTH_LONG).show();
				break;
			case 5:
				Toast.makeText(context, "注册护工失败，请您检查网络连接", Toast.LENGTH_LONG).show();
				break;
			case 6:
				Toast.makeText(context, "注册亲属失败，请您检查网络连接", Toast.LENGTH_LONG).show();
				break;
			case 7:
				imBtState.setBackgroundResource(R.drawable.btconnect);
				break;
			case 8:
				imBtState.setBackgroundResource(R.drawable.btdisconnect);
				break;
			case 9:
				int battery = sp.getInt("battery", 0);
				tv_battery.setText(battery+"%");
				imBattery.getDrawable().setLevel(((battery/10+1)/2));
				break;
			case 10:
				Toast.makeText(context, "验证成功，您可以注册用户", Toast.LENGTH_LONG).show();
				break;
			case 11:
				Toast.makeText(context, "验证失败，请确定设备蓝牙是否开启，并检查输入序列号是否正确", Toast.LENGTH_LONG).show();
				break;
			case 12:
				Toast.makeText(context, "注册成功", Toast.LENGTH_LONG).show();
				break;
				}
			}
	};
}
