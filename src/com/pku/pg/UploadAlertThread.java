package com.pku.pg;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class UploadAlertThread extends Thread{
	private String alertStr;

	public UploadAlertThread(String alertStr){
		this.alertStr = alertStr;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String path = "http://162.105.76.252:8082/api/insertUrineNotation";
		String enc = "UTF-8";
		Map<String, String> params = new HashMap<String,String>();
		params.put("alert", alertStr);
		System.out.println("alertStr:"+params.toString());
		try {
			System.out.println("�ϴ�������Ϣ");
			if(HttpRequest.sendPostRequest(path, params, enc)){
				System.out.println("POST������Ϣ���ݳɹ�");
				JSONObject jsonObj = new JSONObject(HttpRequest.reply);
				int device_exist = jsonObj.getInt("device_exist");
				int user_exist = jsonObj.getInt("user_exist");
				if(device_exist == 1&&user_exist == 1){
					//������ʾ
					MainActivity.SendMessage(MainActivity.handler, 13);
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			System.out.println("������Ϣ�ϴ�ʧ��");
		}
		
	}

}


