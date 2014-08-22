package com.pku.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

public class BluetoothManager implements Runnable{
	private String deviceID;
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothAdapter adapter;
	private SimpleDateFormat format;

	private boolean connectting;
	private final int CONNECT_INTERVAL = 500;  //每隔500ms重新开始建立蓝牙连接
	private final int INTERVAL = 2000;    //每次连接成功通信结束后，休眠3000ms开始下一次连接
	
	public BluetoothManager(String deviceID){
		this.deviceID = deviceID;		
		this.adapter = BluetoothAdapter.getDefaultAdapter();
		this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		connectting = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(connectting){
			connect();
			Log.e("BluetoothManager", "循环结束");
		}
	}
	private void connect(){
		BluetoothSocket socket = null;
		if(!deviceID.equals(null)){
			Log.e("BluetoothManager", "开始连接");
			if(!adapter.isEnabled())
				adapter.enable();
			BluetoothDevice device = adapter.getRemoteDevice(deviceID);
			try {
				socket= createBluetoothSocket(device);
				socket.connect();
				Log.e("BluetoothManager", "连接完成");
				initStream(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				close(socket);
				Log.e("BluetoothManager", "休眠500ms");
				sleep(CONNECT_INTERVAL);				
			}			
		}
	}
	private void initStream(BluetoothSocket socket){
		try {
			InputStream is = socket.getInputStream();
			InputStreamReader isreader = new InputStreamReader(is);
			BufferedReader bReader = new BufferedReader(isreader);
			OutputStream os = socket.getOutputStream();
			boolean checkFlag = MainActivity.sp.getBoolean("checkFlag", false);
			if(checkFlag){
				MainActivity.sp.edit().putBoolean("checkFlag", false).commit();
				checkProgress(socket,bReader,os);				
			}
			else communicationProcess(socket,bReader,os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close(socket);
			Log.e("BluetoothManager", "休眠500ms");
			sleep(CONNECT_INTERVAL);
		}			
	}
	
	private void checkProgress(BluetoothSocket socket,BufferedReader bReader,OutputStream os) throws IOException{
		Log.e("BluetoothManager", "验证过程");
		String reply;
		os.write("GetID\r\n".getBytes());
		String ID = bReader.readLine();
		Log.e("BluetoothManager", "ID:"+ID);
		os.write("OK\r\n".getBytes());
		os.write("SetSysTime\r\n".getBytes());
		reply = bReader.readLine();
		Log.e("BluetoothManager", "reply:"+reply);
		if(reply.equals("OK")){
			String sysTime = format.format(System.currentTimeMillis());
			os.write((sysTime+"\r\n").getBytes());
			reply = bReader.readLine();
			Log.e("BluetoothManager", "reply:"+reply);
			if(reply.equals("SysTimeOK")){
				String batteryInfo = bReader.readLine();
				Log.e("BluetoothManager", "batteryInfo:"+batteryInfo);
				if(batteryInfo.contains("BatteryLevel")){
					handleBattery(batteryInfo);
					os.write("BatteryLevelOK\r\n".getBytes());
					MainActivity.sp.edit().putBoolean("checkSucFlag", true).commit();
				}
			}
		}
		close(socket);
		Log.e("BluetoothManager", "休眠2000ms");
		sleep(INTERVAL);
	}
	private void communicationProcess(BluetoothSocket socket,BufferedReader bReader,OutputStream os) throws IOException{
		Log.e("BluetoothManager", "通信过程");
		String reply;
		os.write("SetSysTime\r\n".getBytes());
		reply = bReader.readLine();
		Log.e("BluetoothManager", "reply:"+reply);
		if(reply.equals("OK")){
			String sysTime = format.format(System.currentTimeMillis());
			os.write((sysTime+"\r\n").getBytes());
			reply = bReader.readLine();
			Log.e("BluetoothManager", "reply:"+reply);
			if(reply.equals("SysTimeOK")){
				String alertInfo = bReader.readLine();
				Log.e("BluetoothManager", "alertInfo:"+alertInfo);
				if(alertInfo.contains("Alert")){
					handleAlert(alertInfo);
					os.write("AlertOK\r\n".getBytes());
					String batteryInfo = bReader.readLine();
					Log.e("BluetoothManager", "batteryInfo:"+batteryInfo);
					if(batteryInfo.contains("BatteryLevel")){
//						handleBattery(batteryInfo);
						os.write("BatteryLevelOK\r\n".getBytes());						
					}
				}				
			}
		}
		close(socket);		
		Log.e("BluetoothManager", "休眠2000ms");
		sleep(INTERVAL);		
	}
	private void handleAlert(String alertInfo){
		String userTel = Info.infoSharedPreferences.getString("userPhone", "");
		String[] alertInfoArray = alertInfo.split(",");
		int type = Integer.parseInt(alertInfoArray[1], 10);
		String alertTime = alertInfoArray[2];
		sendMessage(type,alertTime,Info.mListItemNurses);
		sendMessage(type,alertTime,Info.mListItemRelatives);
		try {
			JSONObject alertJson = new JSONObject();
			alertJson.put("ID", deviceID);
			alertJson.put("type", type);
			alertJson.put("mobile", userTel);
			alertJson.put("recordTime", alertTime);
			JSONObject newAlertJson = new JSONObject();
			newAlertJson.put("alert", alertJson);
			String alertStr = newAlertJson.toString();
			UploadAlertThread thread = new UploadAlertThread(alertStr);
			thread.start();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void handleBattery(String batteryInfo){
		String[] batteryInfoArray = batteryInfo.split(",");
		int battery = Integer.parseInt(batteryInfoArray[1], 10);	
	}
	private void sleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void close(BluetoothSocket socket){
		if(socket!=null){
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	private void sendMessage(int type, String alertTime, ArrayList<HashMap<String,String>> list){
		String userName = Info.infoSharedPreferences.getString("userName", "");
		String alert1 = "您好，用户"+userName+"尿湿报警；"+"时间："+alertTime;
		String alert2 = "您好，用户"+userName+"按键报警；"+"时间："+alertTime;
		String alert3 = "您好，用户"+userName+"脱落报警；"+"时间："+alertTime;
		String alert4 = "您好，用户"+userName+"低电量报警；"+"时间："+alertTime;
		String alert5 = "您好，用户"+userName+"穿戴正常；"+"时间："+alertTime;
		String alert = null;
		switch(type){
			case 1:		
				alert = alert1;
				MainActivity.wetAlertTime = alertTime;
				MainActivity.SendMessage(MainActivity.handler, 1);
				MainActivity.sp.edit().putString("wetAlertTime", alertTime).commit();
				break;
			case 2:		
				alert = alert2;
				MainActivity.keyAlertTime = alertTime;
				MainActivity.SendMessage(MainActivity.handler, 2);
				MainActivity.sp.edit().putString("keyAlertTime", alertTime).commit();
				break;
			case 3:
				alert = alert3;
				MainActivity.dropAlertTime = alertTime;
				MainActivity.SendMessage(MainActivity.handler, 3);
				MainActivity.sp.edit().putString("dropAlertTime", alertTime).commit();
				break;
			case 4:
				alert = alert4;
//				MainActivity.wetAlertTime = alertTime;
//				MainActivity.SendMessage(MainActivity.handler, 1);
				break;
			case 5:
				alert = alert5;
//				MainActivity.wetAlertTime = alertTime;
//				MainActivity.SendMessage(MainActivity.handler, 1);
				break;
		}
		SmsManager smsManager = SmsManager.getDefault();
		List<String> text1 = smsManager.divideMessage(alert);
		List<String> telList = analysisList(list);
		for(String tel: telList){
			for (String text : text1) {
				smsManager.sendTextMessage(tel, null, text, null, null);
			}
		}		
	}
	private List<String> analysisList(ArrayList<HashMap<String,String>> list){
		List<String> telList = new ArrayList<String>();
		for(HashMap<String,String> map: list){
			if(map.get("ItemCheckbox").equals("true"))
				telList.add(map.get("ItemText"));
		}
		Log.e("BluetoothManager", "telList:"+telList.toString());
		return telList;
	}
	
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		if(Build.VERSION.SDK_INT >= 10){
			try{
				final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
	            return (BluetoothSocket) m.invoke(device, uuid);
			}catch (Exception e) {
				Log.e("BluetoothManager", "Could not create Insecure RFComm Connection",e);
	        } 
		}
	      return  device.createRfcommSocketToServiceRecord(uuid);
	}
	protected void setConnectting(boolean connectting){
		this.connectting = connectting;
	}
}
