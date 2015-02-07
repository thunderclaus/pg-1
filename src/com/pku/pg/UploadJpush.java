package com.pku.pg;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadJpush extends Thread{
	private String jpushStr;
	
	public UploadJpush(String jpushStr) {
		this.jpushStr = jpushStr;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		upload() ;
	}

	public void upload() {
		try {
			Socket socket = new Socket("162.105.76.252", 2016);
			OutputStream output = socket.getOutputStream();
//			JSONObject sendJson = new JSONObject();
//			try {
//				sendJson.put("alert", jpushStr);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			String sendStr = sendJson.toString();
			byte[] bytes = jpushStr.getBytes("UTF-8");
			output.write(bytes);
			output.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
