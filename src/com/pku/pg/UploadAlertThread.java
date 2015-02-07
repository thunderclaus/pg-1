package com.pku.pg;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UploadAlertThread extends Thread{
	private String id;
	private int type;
	private String recordTime;
	private String mobile;
	private int alertState;
	private String patientName;
	private String patientPhone;
	private JSONArray nursePhone;
	
	private int dataID;
	private String alertWebStr; 
	//ע��?
	public UploadAlertThread(String ID, int type, String recordTime, String mobile, 
			int alertState, String patientName, JSONArray nursePhone,String patientPhone){
		this.id = ID;
		this.type = type;
		this.recordTime =recordTime;
		this.mobile = mobile;
		this.alertState=alertState;
		this.patientName=patientName;
		this.nursePhone=nursePhone;
		this.patientPhone=patientPhone;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String path = "http://162.105.76.252:8082/api/insertUrineNotation";
		String enc = "UTF-8";
				
		//��װWeb��JSON
		JSONObject alertJson = new JSONObject();
		try {
			alertJson.put("ID", id);
			
				alertJson.put("type", type);
		alertJson.put("mobile", patientPhone);
		alertJson.put("recordTime", recordTime);
		alertJson.put("alertState", alertState);

		JSONObject newAlertJson = new JSONObject();
		newAlertJson.put("alert", alertJson);
		alertWebStr = newAlertJson.toString();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Map<String, String> params = new HashMap<String,String>();
		params.put("alert", alertWebStr);
		System.out.println("alertStr:"+params.toString());
		try {
			System.out.println("�ϴ�������Ϣ");
			if(HttpRequest.sendPostRequest(path, params, enc)){
				//ͨ������
				System.out.println("POST������Ϣ���ݳɹ�");
				JSONObject jsonObj = new JSONObject(HttpRequest.reply);
				int device_exist = jsonObj.getInt("device_exist");
				int user_exist = jsonObj.getInt("user_exist");
				
				dataID = jsonObj.getInt("dataID");
				//��������
//				dataID = 30;
				Log.e("WebServer����ֵ", "dataID"+dataID);
				//Test ȥ����IF д����dataID===============================
				//ͨ������
				if(device_exist == 1&&user_exist == 1){
					//������ʾ
					MainActivity.SendMessage(MainActivity.handler, 13);
				
					//ͬJPush���������б���
					try {
						JSONObject jpushJson = new JSONObject();
						jpushJson.put("patientName", patientName);
							Log.d("patientName", patientName);
						jpushJson.put("patientPhone", patientPhone);
							Log.d("patientPhone", patientPhone);
						jpushJson.put("alertType", type);		
							Log.d("type", ""+type);
						jpushJson.put("recordTime", recordTime);
							Log.d("recordTime", recordTime);
						jpushJson.put("nursePhone",nursePhone);
							Log.d("nursePhon", nursePhone.toString());
						jpushJson.put("alertState", alertState);
							Log.d("alertState", ""+alertState);
						jpushJson.put("dataID", dataID);		
							Log.d("dataID", ""+dataID);
						String jpushStr = jpushJson.toString();
							Log.d(" jpushJson", jpushJson.toString());
						UploadJpush uploadJpush = new UploadJpush(jpushStr);
						uploadJpush.start();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Jpush�ϴ�ʧ��");
					}
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("��̨������Ϣ�ϴ�ʧ��");
		}
		
	
		
		
	}

}


