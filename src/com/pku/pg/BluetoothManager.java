package com.pku.pg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

public class BluetoothManager implements Runnable {
	private String deviceID;
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothAdapter adapter;
	private SimpleDateFormat format;

	private boolean connectting;
	private final int CONNECT_INTERVAL = 1000; // 每隔500ms重新开始建立蓝牙连接
	private final int INTERVAL = 3000; // 每次连接成功通信结束后，休眠3000ms开始下一次连接

	public BluetoothManager(String deviceID) {
		this.deviceID = deviceID;
		this.adapter = BluetoothAdapter.getDefaultAdapter();
		this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		connectting = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (connectting) {
			connect();
			Log.e("BluetoothManager", "循环结束");
		}
	}

	private void connect() {
		BluetoothSocket socket = null;
		if (!deviceID.equals(null)) {
			Log.e("BluetoothManager", "开始连接");
			if (!adapter.isEnabled())
				adapter.enable();
			BluetoothDevice device = adapter.getRemoteDevice(deviceID);
			try {
				socket = createBluetoothSocket(device);
				socket.connect();
				MainActivity.SendMessage(MainActivity.handler, 7);
				Log.e("BluetoothManager", "连接完成");
				initStream(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (Info.dialog != null && Info.dialog.isShowing()) {
					sleep(10000);
					Info.dialog.cancel();
					setConnectting(false);
					MainActivity.SendMessage(MainActivity.handler, 11);
				}
				close(socket);
				Log.e("BluetoothManager", "休眠500ms");
				sleep(CONNECT_INTERVAL);
			}
		}
	}

	private void initStream(BluetoothSocket socket) {
		try {
			InputStream is = socket.getInputStream();
			// InputStreamReader isreader = new InputStreamReader(is);
			// BufferedReader bReader = new BufferedReader(isreader);
			OutputStream os = socket.getOutputStream();
			boolean checkFlag = MainActivity.sp.getBoolean("checkFlag", false);
			if (checkFlag) {
				MainActivity.sp.edit().putBoolean("checkFlag", false).commit();
				checkProgress(socket, is, os);
				if (Info.dialog != null && Info.dialog.isShowing()) {
					Info.dialog.cancel();
					MainActivity.SendMessage(MainActivity.handler, 10);
				}
			} else
				communicationProcess(socket, is, os);
			close(socket);
			Log.e("BluetoothManager", "休眠2000ms");
			sleep(INTERVAL);
			MainActivity.SendMessage(MainActivity.handler, 8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close(socket);
			MainActivity.SendMessage(MainActivity.handler, 8);
			Log.e("BluetoothManager", "休眠500ms");
			sleep(CONNECT_INTERVAL);
		}
	}

	private void checkProgress(BluetoothSocket socket, InputStream is,
			OutputStream os) throws IOException {
		Log.e("BluetoothManager", "验证过程");
		String reply;
		os.write("GetID\r\n".getBytes());
		String ID = readOneLine(is);
		Log.e("BluetoothManager", "ID:" + ID);
		os.write("OK\r\n".getBytes());
		os.write("SetSysTime\r\n".getBytes());
		reply = readOneLine(is);
		Log.e("BluetoothManager", "reply:" + reply);
		if (reply.equals("OK")) {
			String sysTime = format.format(System.currentTimeMillis());
			os.write((sysTime + "\r\n").getBytes());
			reply = readOneLine(is);
			Log.e("BluetoothManager", "reply:" + reply);
			if (reply.equals("SysTimeOK")) {
				String batteryInfo = readOneLine(is);
				Log.e("BluetoothManager", "batteryInfo:" + batteryInfo);
				if (batteryInfo.contains("BatteryLevel")) {
					handleBattery(batteryInfo);
					os.write("BatteryLevelOK\r\n".getBytes());
					MainActivity.sp.edit().putBoolean("checkSucFlag", true)
							.commit();
				}
			}
		}
	}

	private void communicationProcess(BluetoothSocket socket, InputStream is,
			OutputStream os) throws IOException {
		Log.e("BluetoothManager", "通信过程");
		String reply;
		os.write("SetSysTime\r\n".getBytes());
		reply = readOneLine(is);
		Log.e("BluetoothManager", "reply:" + reply);
		if (reply.equals("OK")) {
			String sysTime = format.format(System.currentTimeMillis());
			os.write((sysTime + "\r\n").getBytes());
			reply = readOneLine(is);
			Log.e("BluetoothManager", "reply:" + reply);
			if (reply.equals("SysTimeOK")) {
				String alertInfo = readOneLine(is);
				Log.e("BluetoothManager", "alertInfo:" + alertInfo);
				if (alertInfo.contains("Alert")) {
					handleAlert(alertInfo);
					os.write("AlertOK\r\n".getBytes());
					String batteryInfo = readOneLine(is);
					Log.e("BluetoothManager", "batteryInfo:" + batteryInfo);
					if (batteryInfo.contains("BatteryLevel")) {
						handleBattery(batteryInfo);
						os.write("BatteryLevelOK\r\n".getBytes());
					}
				}
			}
		}
	}

	private void handleAlert(String alertInfo) {
		String userTel = MainActivity.sp.getString("userPhone", "");
		String[] alertInfoArray = alertInfo.split(",");
		int type = Integer.parseInt(alertInfoArray[1], 10);
		String alertTime = alertInfoArray[2];
		int alertState = 0;
		String patientName = MainActivity.sp.getString("userName", "");
		List<String> nurseCheckedList = analysisList(Info.mListItemRelatives);
		//sendMessage(type, alertTime, Info.mListItemNurses);
		sendMessage(type, alertTime, Info.mListItemRelatives);
		
		//将数据传给报警线程
		UploadAlertThread thread = new UploadAlertThread(deviceID, type, alertTime, userTel, 
				alertState, patientName, nurseCheckedList);
		thread.start();
		
		
	}

	private void handleBattery(String batteryInfo) {
		String[] batteryInfoArray = batteryInfo.split(",");
		int battery = Integer.parseInt(batteryInfoArray[1], 10);
		MainActivity.sp.edit().putInt("battery", battery).commit();
		MainActivity.SendMessage(MainActivity.handler, 9);
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void close(BluetoothSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendMessage(int type, String alertTime,
			ArrayList<HashMap<String, String>> list) {
		String userName = MainActivity.sp.getString("userName", "");
		String alert1 = "您好，用户" + userName + "尿湿报警；" + "时间：" + alertTime;
		String alert2 = "您好，用户" + userName + "按键报警；" + "时间：" + alertTime;
		String alert3 = "您好，用户" + userName + "脱落报警；" + "时间：" + alertTime;
		String alert4 = "您好，用户" + userName + "低电量报警；" + "时间：" + alertTime;
		String alert5 = "您好，用户" + userName + "穿戴正常；" + "时间：" + alertTime;
		String alert = null;
		switch (type) {
		case 1:
			alert = alert1;
			MainActivity.wetAlertTime = alertTime;
			MainActivity.SendMessage(MainActivity.handler, 1);
			MainActivity.sp.edit().putString("wetAlertTime", alertTime)
					.commit();
			break;
		case 2:
			alert = alert2;
			MainActivity.keyAlertTime = alertTime;
			MainActivity.SendMessage(MainActivity.handler, 2);
			MainActivity.sp.edit().putString("keyAlertTime", alertTime)
					.commit();
			break;
		case 3:
			alert = alert3;
			MainActivity.dropAlertTime = alertTime;
			MainActivity.SendMessage(MainActivity.handler, 3);
			MainActivity.sp.edit().putString("dropAlertTime", alertTime)
					.commit();
			break;
		case 4:
			alert = alert4;
			// MainActivity.wetAlertTime = alertTime;
			// MainActivity.SendMessage(MainActivity.handler, 1);
			break;
		case 5:
			alert = alert5;
			// MainActivity.wetAlertTime = alertTime;
			// MainActivity.SendMessage(MainActivity.handler, 1);
			break;
		}
		SmsManager smsManager = SmsManager.getDefault();
		List<String> text1 = smsManager.divideMessage(alert);
		List<String> telList = analysisList(list);
		for (String tel : telList) {
			for (String text : text1) {
				smsManager.sendTextMessage(tel, null, text, null, null);
			}
		}
	}

	private List<String> analysisList(ArrayList<HashMap<String, String>> list) {
		List<String> telList = new ArrayList<String>();
		for (HashMap<String, String> map : list) {
			if (map.get("ItemCheckbox").equals("true"))
				telList.add(map.get("ItemText"));
		}
		Log.e("BluetoothManager", "telList:" + telList.toString());
		return telList;
	}

	private String readOneLine(InputStream is) {
		String reply = null;
		byte[] buffer = new byte[1024];
		int readCount = 0; // 已经成功读取的字节的个数
		while (readCount < buffer.length) {
			try {
				readCount += is.read(buffer, readCount, buffer.length
						- readCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reply = new String(buffer, 0, readCount);
			if (reply.indexOf("\r\n") != -1) // 遇到"\r\n"结束 跳出循环
				break;
		}
		return reply.substring(0, reply.length() - 2);
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, uuid);
			} catch (Exception e) {
				Log.e("BluetoothManager",
						"Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(uuid);
	}

	protected void setConnectting(boolean connectting) {
		this.connectting = connectting;
	}
}
