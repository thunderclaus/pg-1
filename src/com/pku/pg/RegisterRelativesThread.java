package com.pku.pg;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class RegisterRelativesThread extends Thread{
	private String relativesStr;
	private SharedPreferences sp;
	private boolean regRelativesFlag;

	public RegisterRelativesThread(String relativesStr,SharedPreferences sp){
		this.relativesStr = relativesStr;
		this.sp = sp;
	}
	private void setRegRelativesFlag(boolean regRelativesFlag){
		this.regRelativesFlag = regRelativesFlag;
	}
	protected boolean getRegRelativesFlag(){
		return this.regRelativesFlag;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String path = "http://162.105.76.252:8082/api/insertRelative";
		String enc = "UTF-8";
		Map<String, String> params = new HashMap<String,String>();
		params.put("relative", relativesStr);
		System.out.println("relativesStr:"+params.toString());
		try {
			System.out.println("注册家属");
			if(HttpRequest.sendPostRequest(path, params, enc)){
				
				System.out.println("POST家属数据成功");
				JSONObject jsonObj = new JSONObject(HttpRequest.reply);
				int device_exist = jsonObj.getInt("device_exist");
				int user_exist = jsonObj.getInt("user_exist");
				if(device_exist == 1&&user_exist == 1){
					sp.edit().putString("registerRelativesStr", relativesStr).commit();
					//界面显示
					
					
				}
				setRegRelativesFlag(true);
			}else{
				MainActivity.SendMessage(MainActivity.handler, 6);
				setRegRelativesFlag(false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainActivity.SendMessage(MainActivity.handler, 6);
			setRegRelativesFlag(false);
		}
		
	}

}


