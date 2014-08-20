package com.pku.pg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Info extends Activity implements OnClickListener {

	private TextView tv_userName;
	private TextView tv_userPhone;
	private TextView tv_userHospital;
	private Context infoContext;
	static Activity activity;

	private String deviceID;
	private String userStr;

	private EditText regUserMac;
	private EditText regUserName;
	private EditText regUserPhone;
	private EditText regUserHospital;
	private EditText regNurseName;
	private EditText regNursePhone;
	private EditText regRelativeName;
	private EditText regRelativePhone;

	private ScrollView infoScrollView;
	private ListViewForScrollView mListviewNurses;
	private ListViewForScrollView mListviewRelatives;

	static SharedPreferences infoSharedPreferences;
	private Editor editor;

	static ArrayList<HashMap<String, String>> mListItemNurses;
	static ArrayList<HashMap<String, String>> mListItemRelatives;

	private JSONArray nursesJsonArray;
	private JSONArray relativesJsonArray;
	private JSONArray userJsonArray;
	private NurseAdapter mAdapter_Nurses;
	private RelativeAdapter mAdapter_Relatives;
	
	static boolean checkFlag;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);

		infoScrollView = (ScrollView) findViewById(R.id.info_scrollview);
		infoScrollView.smoothScrollTo(0, 0);

		findViewById(R.id.info_btn_DeviceChanging).setOnClickListener(this);
		findViewById(R.id.info_btn_UserChange).setOnClickListener(this);
		findViewById(R.id.info_imageview_AddNurses).setOnClickListener(this);
		findViewById(R.id.info_imageview_AddRelative).setOnClickListener(this);
		findViewById(R.id.info_btn_MacCheck).setOnClickListener(this);
		findViewById(R.id.commit).setOnClickListener(this);
		
		
		regUserMac = (EditText)findViewById(R.id.info_textview_MacAddrss);
		
		ActionBar infoActionBar = this.getActionBar();

		infoActionBar.setHomeButtonEnabled(true);
		infoActionBar.setDisplayHomeAsUpEnabled(true);
		infoActionBar.setDisplayShowHomeEnabled(true);
		
		userJsonArray = new JSONArray();
		infoContext = Info.this;
		activity = this;
		infoSharedPreferences = infoContext.getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
		editor = infoSharedPreferences.edit();
		// spclear();

		tv_userName = (TextView) findViewById(R.id.info_textview_UserName);
		tv_userPhone = (TextView) findViewById(R.id.info_textview_UserPhone);
		tv_userHospital = (TextView) findViewById(R.id.info_textview_UserHospital);
		tv_userName.setText(infoSharedPreferences.getString("userName", "数据尚未写入"));
		tv_userPhone.setText(infoSharedPreferences.getString("userPhone", "数据尚未写入"));
		tv_userHospital.setText(infoSharedPreferences.getString("geracomium","数据尚未写入"));
		/*
		 * 护工
		 * *******************************************************************
		 * **********************************************************护工OnCreate
		 */
		mListviewNurses = (ListViewForScrollView) findViewById(R.id.info_list_Nurses); /* 定义一个动态数组 */
		Set nurseSet = infoSharedPreferences.getStringSet("nurseSet", null);
		if(nurseSet!=null)
			mListItemNurses = (ArrayList<HashMap<String, String>>) nurseSet.iterator().next();
		else mListItemNurses = new ArrayList<HashMap<String, String>>();
		
		mAdapter_Nurses = new NurseAdapter(mListItemNurses,this);
		mListviewNurses.setAdapter(mAdapter_Nurses);

		mListviewNurses.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// TODO Auto-generated method stub
				setTitle("你选择了护理人员" + arg2);// 设置标题栏显示点击的行
				com.pku.pg.NurseAdapter.ViewHolder holder = (com.pku.pg.NurseAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                HashMap<String,String> map = mListItemNurses.get(arg2);
                map.put("ItemCheckbox", ""+holder.cb.isChecked());
                mListItemNurses.remove(arg2);
                mListItemNurses.add(arg2, map);
                mAdapter_Nurses.notifyDataSetChanged();                
			}
		});
		mListviewNurses.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, final int arg2, long arg3) {
						// TODO Auto-generated method stub
						new AlertDialog.Builder(infoContext)
								.setTitle("确认删除?")
								.setPositiveButton("确定",new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub												
												mListItemNurses.remove(arg2);
												mAdapter_Nurses.notifyDataSetChanged();
											}
										}).setNegativeButton("取消", null).show();

						return false;
					}
				});

		// Log.i("护工注册sp",
		// infoSharedPreferences.getString("NursesInfo","Nothing Doh!l"));

		/*
		 * 亲属 ************************************************************
		 * ******
		 * *******************************************************Relatives
		 * onCreate
		 */
		mListviewRelatives = (ListViewForScrollView) findViewById(R.id.info_list_Relatives); /* 定义一个动态数组 */
		Set relativeSet = infoSharedPreferences.getStringSet("relativeSet", null);
		if(relativeSet!=null)
			mListItemRelatives = (ArrayList<HashMap<String, String>>) relativeSet.iterator().next();
		else mListItemRelatives = new ArrayList<HashMap<String, String>>();
		
		mAdapter_Relatives = new RelativeAdapter(mListItemRelatives,this);
		mListviewRelatives.setAdapter(mAdapter_Relatives);
		
		mListviewRelatives.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				setTitle("你选择了亲友" + arg2);// 设置标题栏显示点击的行
				com.pku.pg.RelativeAdapter.ViewHolder holder = (com.pku.pg.RelativeAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                HashMap<String,String> map = mListItemRelatives.get(arg2);
                map.put("ItemCheckbox", ""+holder.cb.isChecked());
                mListItemRelatives.remove(arg2);
                mListItemRelatives.add(arg2, map);
                mAdapter_Relatives.notifyDataSetChanged();
			}

		});
		mListviewRelatives.setOnItemLongClickListener(new OnItemLongClickListener() {
					private int position;

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, final int arg2, long arg3) {
						// TODO Auto-generated method stub
						position = arg2;
						new AlertDialog.Builder(infoContext)
								.setTitle("确认删除?")
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												mListItemRelatives.remove(arg2);
												mAdapter_Relatives.notifyDataSetChanged();
											}
										}).setNegativeButton("取消", null).show();

						return false;
					}
				});

		// Log.i("亲属注册sp",
		// infoSharedPreferences.getString("RelativesInfo","Nothing Doh!l"));

		// onCreate结束*******************************************onCreate结束*******************************************onCreate结束

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return false;

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.info_btn_DeviceChanging:
			// ======================================================================更换设备
			// 按钮点击
			// 弹出对话框
			LayoutInflater inflater_DeviceChange = getLayoutInflater();
			View layout_DeviceChange = inflater_DeviceChange.inflate(
					R.layout.dialog_devicechange,
					(ViewGroup) findViewById(R.id.dialog_DeviceChange));
			
			new AlertDialog.Builder(this)
					.setTitle("警告")
					.setView(layout_DeviceChange)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// 保存进SharedPreferences
									
									editor.putString("Mac", "");
									editor.putString("userName", "");
									editor.putString("userPhone", "");
									editor.putString("geracomium", "");
									editor.commit();

									refresh();

								}
							}).setNegativeButton("取消", null).show();

			break;

		case R.id.info_btn_UserChange:
			// ======================================================================用户注册
			// 按钮点击
			// 弹出对话框
			LayoutInflater inflater_UserReg = getLayoutInflater();
			View layout_UserReg = inflater_UserReg.inflate(
					R.layout.user_reg_dialog,
					(ViewGroup) findViewById(R.id.user_reg_v01));
			regUserName = (EditText)layout_UserReg.findViewById(R.id.user_reg_EditText_name);
			regUserPhone = (EditText)layout_UserReg.findViewById(R.id.user_reg_EditText_phone);
			regUserHospital = (EditText)layout_UserReg.findViewById(R.id.user_reg_EditText_hospital);
			new AlertDialog.Builder(this)
					.setTitle("请填写注册信息")
					.setView(layout_UserReg)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// 保存进SharedPreferences
									JSONObject jsonObject = new JSONObject();
									try {
										jsonObject.put("ID", regUserMac.getText().toString());
										jsonObject.put("userName", regUserName.getText().toString());
										jsonObject.put("userPhone", regUserPhone.getText().toString());
										jsonObject.put("geracomium", regUserHospital.getText().toString());
										jsonObject.put("IMSI", "1111111111111111");
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									editor.putString("userStr", jsonObject.toString());
									editor.putString("userName",regUserName.getText().toString());
									/*
									 * editor.putInt("UserPhone",
									 * Integer.parseInt
									 * (regPhone.getText().toString()));
									 */
									editor.putString("userPhone", regUserPhone
											.getText().toString());
									editor.putString("geracomium",
											regUserHospital.getText()
													.toString());
									editor.commit();

									refresh();

								}
							}).setNegativeButton("取消", null).show();

			break;

		case R.id.info_imageview_AddNurses:
			/*
			 * ==================================================================
			 * ==== 护工注册
			 */
			LayoutInflater inflater_NurseReg = getLayoutInflater();
			View layout_NurseReg = inflater_NurseReg.inflate(
					R.layout.nurses_reg_dialog,
					(ViewGroup) findViewById(R.id.nurse_reg_v01));
			regNurseName = (EditText) layout_NurseReg
					.findViewById(R.id.reg_NurseName_EditText_name);
			regNursePhone = (EditText) layout_NurseReg
					.findViewById(R.id.reg_NursePhone_EditText_phone);

			new AlertDialog.Builder(this)
					.setTitle("请填写注册信息")
					.setView(layout_NurseReg)
					.setNegativeButton("取消", null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
														
										HashMap<String, String> map_nurses = new HashMap<String, String>();
										map_nurses.put("ItemTitle", regNurseName.getText().toString());
										map_nurses.put("ItemText", regNursePhone.getText().toString());
										map_nurses.put("ItemCheckbox", ""+false);
										mListItemNurses.add(map_nurses);
										mAdapter_Nurses.notifyDataSetChanged();
									

								}
							}).show();

			break;

		case R.id.info_imageview_AddRelative:
			/*
			 * ==================================================================
			 * ==== 护工注册
			 */
			LayoutInflater inflater_RelativeReg = getLayoutInflater();
			View layout_RelativeReg = inflater_RelativeReg.inflate(
					R.layout.relatives_reg_dialog,
					(ViewGroup) findViewById(R.id.relative_reg_v01));
			regRelativeName = (EditText) layout_RelativeReg
					.findViewById(R.id.reg_RelativeName_EditText_name);
			regRelativePhone = (EditText) layout_RelativeReg
					.findViewById(R.id.reg_RelativePhone_EditText_phone);

			new AlertDialog.Builder(this)
					.setTitle("请填写注册信息")
					.setView(layout_RelativeReg)
					.setNegativeButton("取消", null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,int which) {
									// TODO Auto-generated method stub
									
									HashMap<String, String> map_relatives = new HashMap<String, String>();
									map_relatives.put("ItemTitle", regRelativeName.getText().toString());
									map_relatives.put("ItemText", regRelativePhone.getText().toString());
									map_relatives.put("ItemCheckbox", ""+false);
									mListItemRelatives.add(map_relatives);
									mAdapter_Relatives.notifyDataSetChanged();
									
									

								}
							}).show();

			break;
		case R.id.info_btn_MacCheck:
			checkFlag = true;
			Intent intent = new Intent(infoContext,BtService.class);
			String inputID = regUserMac.getText().toString();
			if(inputID.length()==12&&inputID.matches("\\w{11}")){
				deviceID = insertChar(inputID);				
				Bundle bundle = new Bundle();
//				bundle.putString("userName", userName.getText().toString());	
				bundle.putString("deviceID", deviceID);
//				bundle.putString("nurseTel", nursesStr);
//				bundle.putString("relativesTel", relativesStr);
//				bundle.putString("userTel", userPhone.getText().toString());
				intent.putExtras(bundle);
				infoContext.startService(intent);							
			}					
			break;
		case R.id.commit:
			//注册用户
			String registerUserStr = infoSharedPreferences.getString("registerUserStr", "registerNurseStr");
			String userStr = infoSharedPreferences.getString("userStr", "userStr");
			String newUserStr = null;
			try {
				JSONObject obj1 = new JSONObject(userStr);
				JSONObject obj2 = new JSONObject();
				obj2.put("user", obj1);
				newUserStr = obj2.toString();
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if(userStr!= "userStr"&&registerUserStr!= newUserStr){		
				RegisterUserThread thread = new RegisterUserThread(newUserStr,infoSharedPreferences);
				thread.start();	
			}
			
			//注册护工
			String registerNurseStr = infoSharedPreferences.getString("registerNurseStr", "registerNurseStr");
			JSONArray nurseArray = new JSONArray();
			for(HashMap<String,String> map: mListItemNurses){
				if(map.get("ItemCheckbox").equals("true")){
					JSONObject obj = new JSONObject();
					try {
						obj.put("nurseName", map.get("ItemTitle"));
						obj.put("nursePhone", map.get("ItemText"));
						nurseArray.put(obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			Log.e("nurseArray", nurseArray.toString());
			String newNurseStr = null;			
			try {
				JSONObject obj3 = new JSONObject();
				obj3.put("ID", deviceID);
				obj3.put("nursesInfo", nurseArray);
				JSONObject obj4 = new JSONObject();
				obj4.put("nurse", obj3);
				newNurseStr = obj4.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			if(registerNurseStr!= newNurseStr){				
				RegisterNurseThread thread = new RegisterNurseThread(newNurseStr,infoSharedPreferences);
				thread.start();
			}
			
			//注册亲属
			String registerRelativesStr = infoSharedPreferences.getString("registerRelativesStr", "registerRelativesStr");
			JSONArray relativeArray = new JSONArray();
			for(HashMap<String,String> map: mListItemRelatives){
				if(map.get("ItemCheckbox").equals("true")){
					JSONObject obj = new JSONObject();
					try {
						obj.put("relativeName", map.get("ItemTitle"));
						obj.put("relativePhone", map.get("ItemText"));
						relativeArray.put(obj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			Log.e("relativeArray", relativeArray.toString());
			String newRelativeStr = null;			
			try {
				JSONObject obj3 = new JSONObject();
				obj3.put("ID", deviceID);
				obj3.put("relativesInfo", relativeArray);
				JSONObject obj4 = new JSONObject();
				obj4.put("relative", obj3);
				newRelativeStr = obj4.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			if(registerRelativesStr!= newRelativeStr){				
				RegisterRelativesThread thread = new RegisterRelativesThread(newRelativeStr,infoSharedPreferences);
				thread.start();
			}
			break;
		default:
			break;
		}
	}
	private String insertChar(String str){
		String newStr;
		StringBuffer sb = new StringBuffer(str);
		int i = 0;
		while((i+=2) < sb.length()){
			sb.insert(i, ':');
			i++;
		}
		newStr = sb.toString();
		return newStr;
	}
	private void refresh() {
		finish();
		Intent intent = new Intent(infoContext, Info.class);
		startActivity(intent);
	}

	public void onStop(){
		super.onStop();
		Set nurseSet = new HashSet();
		nurseSet.add(mListItemNurses);
		Set relativeSet = new HashSet();
		relativeSet.add(mListItemRelatives);
		infoSharedPreferences.edit().putStringSet("nurseSet", nurseSet).commit();
		infoSharedPreferences.edit().putStringSet("relativeSet", relativeSet).commit();
	}
}
