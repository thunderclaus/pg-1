package com.pku.pg;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class RegisterUserThread extends Thread{
	private String userStr;
	private SharedPreferences sp;
	private OnSucRegisterListener onSucRegisterListener;

	public RegisterUserThread(String userStr,SharedPreferences sp){
		this.userStr = userStr;
		this.sp = sp;
	}
	public interface OnSucRegisterListener {
		public void onSucRegister();
	}
	public void setOnSucRegisterListener(OnSucRegisterListener listener) {
		onSucRegisterListener = listener;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(insertDevice()){
			String path = "http://162.105.76.252:8082/api/insertPatient";
			String enc = "UTF-8";
			Map<String, String> params = new HashMap<String,String>();
			params.put("user", userStr);	
			System.out.println("userStr:"+params.toString());
			try {
				System.out.println("注册用户");
				if(HttpRequest.sendPostRequest(path, params, enc)){
					System.out.println("POST用户数据成功");
					JSONObject jsonObj = new JSONObject(HttpRequest.reply);
					int device_exist = jsonObj.getInt("device_exist");
					int name_unique = jsonObj.getInt("name_unique");
					int mobile_unique = jsonObj.getInt("mobile_unique");
					int IMSI_unique = jsonObj.getInt("IMSI_unique");
					if(device_exist == 1&&name_unique == 1&&mobile_unique == 1&&IMSI_unique==1){
						sp.edit().putString("registerUserStr", userStr).commit();
					}			
					onSucRegisterListener.onSucRegister();
				}else MainActivity.SendMessage(MainActivity.handler, 4);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MainActivity.SendMessage(MainActivity.handler, 4);
			}
		}				
	}
	private boolean insertDevice(){
		String path = "http://162.105.76.252:8082/api/insertDevice";
		String enc = "UTF-8";
		Map<String, String> params = new HashMap<String,String>();
		String deviceID = sp.getString("deviceID", "");
		params.put("deviceSN", deviceID);
		params.put("deviceType", "11");
		try {
			System.out.println("注册设备");
			if(HttpRequest.sendPostRequest(path, params, enc)){
				System.out.println("POST设备数据成功");
				JSONObject jsonObj = new JSONObject(HttpRequest.reply);
				int deviceSN = jsonObj.getInt("deviceSN");				
				if(deviceSN==1||deviceSN==0)
					return true;				
			}
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}


