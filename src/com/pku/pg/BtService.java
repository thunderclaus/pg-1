package com.pku.pg;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BtService extends Service{
	private BluetoothManager bluetoothManager = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);
        Bundle bundle = intent.getExtras();
        String userName = bundle.getString("userName");
        String deviceID = bundle.getString("deviceID");
        String nurseTel = bundle.getString("nurseTel");
        String relativesTel = bundle.getString("relativesTel");
        String userTel = bundle.getString("userTel");
        if(bluetoothManager!=null)
			bluetoothManager.setConnectting(false);
		bluetoothManager = new BluetoothManager(deviceID,userName,nurseTel,relativesTel,userTel);
		Thread thread = new Thread(bluetoothManager);
		thread.start();
        return START_REDELIVER_INTENT;
    }
	public void onDestroy(){
		super.onDestroy();
		bluetoothManager.setConnectting(false);
		Log.e("BtService", "onDestroy");
	}
}
