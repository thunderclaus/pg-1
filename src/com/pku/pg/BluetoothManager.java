package com.pku.pg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	private String userName;
	private String nurseTel;
	private String relativesTel;
	private String userTel;

	private boolean connectting;
	private final int CONNECT_INTERVAL = 500;  //每隔500ms重新开始建立蓝牙连接
	private final int INTERVAL = 3000;    //每次连接成功通信结束后，休眠3000ms开始下一次连接
	
	public BluetoothManager(String deviceID,String userName,String nurseTel,String relativesTel,String userTel){
		this.deviceID = deviceID;
		this.nurseTel = nurseTel;
		this.userName = userName;
		this.userTel = userTel;
		this.relativesTel = relativesTel;
		this.adapter = BluetoothAdapter.getDefaultAdapter();
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
				try {
					close(socket);
					Thread.sleep(CONNECT_INTERVAL);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}			
		}
	}
	private void initStream(BluetoothSocket socket){
		try {
			InputStream is = socket.getInputStream();
			InputStreamReader isreader = new InputStreamReader(is);
			BufferedReader bReader = new BufferedReader(isreader);
			OutputStream os = socket.getOutputStream();

			if(Info.checkFlag){
				Info.checkFlag = false;
				checkProgress(socket,bReader,os);
				
			}
			else communicationProcess(socket,bReader,os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				close(socket);
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
				
	}
	
	private void checkProgress(BluetoothSocket socket,BufferedReader bReader,OutputStream os) throws IOException{
		Log.e("BluetoothManager", "验证过程");
		os.write("GetID\r\n".getBytes());
		Log.e("BluetoothManager", "ID:"+bReader.readLine());
		String ID = bReader.readLine();
		os.write("OK\r\n".getBytes());
		os.write("SetSysTime\r\n".getBytes());
		if(bReader.readLine().equals("OK")){
			int sysTime = (int) (System.currentTimeMillis()/1000);
			os.write((sysTime+"\r\n").getBytes());
			Log.e("BluetoothManager", "sysTime:"+bReader.readLine());
		}
		socket.close();		
	}
	private void communicationProcess(BluetoothSocket socket,BufferedReader bReader,OutputStream os) throws IOException{
		Log.e("BluetoothManager", "通信过程");
		os.write("SetSysTime\r\n".getBytes());
		if(bReader.readLine().equals("OK")){
			int sysTime = (int) (System.currentTimeMillis()/1000);
			os.write((sysTime+"\r\n").getBytes());
			if(bReader.readLine().equals("SysTimeOK")){
				String alertInfo = bReader.readLine();
				if(alertInfo.contains("Alert")){
					handleAlert(alertInfo);
					os.write("AlertOK\r\n".getBytes());
					String batteryInfo = bReader.readLine();
					if(batteryInfo.contains("BatteryLevel")){
						handleBattery(batteryInfo);
						os.write("BatteryLevelOK\r\n".getBytes());
						close(socket);
					}else close(socket);
				}else close(socket);				
			}else close(socket);
		}else close(socket);
	}
	private void handleAlert(String alertInfo){
		String[] alertInfoArray = alertInfo.split(",");
		int type = Integer.parseInt(alertInfoArray[1], 10);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String alertTime = format.format(Long.parseLong(alertInfoArray[2], 10)*1000);
		sendMessage(type,alertTime,userName,nurseTel);
		sendMessage(type,alertTime,userName,relativesTel);
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

/*	private void sendData(BluetoothSocket socket){
		
		try {
			InputStream is = socket.getInputStream();
			InputStreamReader isReader = new InputStreamReader(is);
			BufferedReader bReader = new BufferedReader(isReader);
			OutputStream os = socket.getOutputStream();
			while(true){
				if(bReader.ready())
					break;
			}
			os.write("SetSysTime\r\n".getBytes());
			if(bReader.readLine() == "OK"){
				int sysTime = (int) (System.currentTimeMillis()/1000);
				os.write((sysTime+"\r\n").getBytes());
				if(bReader.readLine() == "SysTimeOK"){
					String info = bReader.readLine();
					String[] infoArray = info.split(",");
					if(infoArray.length == 3){
						int type = Integer.parseInt(infoArray[1], 10);
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String alertTime = format.format(Long.parseLong(infoArray[2], 10)*1000);
						sendMessage(type,alertTime,userName,nurseTel);
						sendMessage(type,alertTime,userName,relativesTel);
					}else close(socket);					
				}
			}else close(socket);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}*/
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
	private void sendMessage(int type, String alertTime, String userName, String telArrayString){
		String alert1 = "您好，用户"+userName+"尿湿报警；"+"时间："+alertTime;
		String alert2 = "您好，用户"+userName+"按键报警；"+"时间："+alertTime;
		String alert3 = "您好，用户"+userName+"脱落报警；"+"时间："+alertTime;
		String alert4 = "您好，用户"+userName+"低电量报警；"+"时间："+alertTime;
		String alert5 = "您好，用户"+userName+"穿戴正常；"+"时间："+alertTime;
		String alert = null;
		switch(type){
			case 1:		alert = alert1; break;
			case 2:		alert = alert2; break;
			case 3:		alert = alert3; break;
			case 4:		alert = alert4; break;
			case 5:		alert = alert5; break;
		}
		SmsManager smsManager = SmsManager.getDefault();
		List<String> text1 = smsManager.divideMessage(alert);
		List<String> telList = analysisJSON(telArrayString);
		for(String tel: telList){
			for (String text : text1) {
				smsManager.sendTextMessage(tel, null, text, null, null);
			}
		}		
	}
	private List<String> analysisJSON(String jsonStr){
		List<String> telList = new ArrayList<String>();
		try {
			JSONArray array = new JSONArray(jsonStr);
			JSONObject obj;
			for(int i = 0;i<array.length();i++){
				obj = (JSONObject) array.get(i);
				String tel = obj.getString("NursePhone");
				telList.add(i, tel);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
