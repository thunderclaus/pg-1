package com.pku.pg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadAlertThread extends Thread{
	private String id;
	private int type;
	private String recordTime;
	private String mobile;
	private int alertState;
	private String patientName;
	private List<String> nursePhone;
	private int dataID;
	private String alertWebStr; 

	public UploadAlertThread(String ID, int type, String recordTime, String mobile, 
			int alertState, String patientName, List<String> nursePhone){
		this.id = ID;
		this.type = type;
		this.recordTime =recordTime;
		this.mobile = mobile;
		this.alertState=alertState;
		this.patientName=patientName;
		this.nursePhone=nursePhone;
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
		alertJson.put("mobile", mobile);
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
				System.out.println("POST������Ϣ���ݳɹ�");
				JSONObject jsonObj = new JSONObject(HttpRequest.reply);
				int device_exist = jsonObj.getInt("device_exist");
				int user_exist = jsonObj.getInt("user_exist");
				dataID = jsonObj.getInt("dataID");
				
				if(device_exist == 1&&user_exist == 1){
					//������ʾ
					MainActivity.SendMessage(MainActivity.handler, 13);
				
					//ͬJPush���������б���
					try {
						JSONObject jpushJson = new JSONObject();
						jpushJson.put("patientName", patientName);
						jpushJson.put("alertType", type);
						jpushJson.put("recordTime", recordTime);
						jpushJson.put("nursePhone",nursePhone);
						jpushJson.put("alertState", alertState);
						jpushJson.put("dataID", dataID);

						JSONObject newJpushJson = new JSONObject();
						newJpushJson.put("alert", jpushJson);
						String jpushStr = newJpushJson.toString();
						UploadJpush uploadJpush = new UploadJpush(jpushStr);
						uploadJpush.upload();
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


