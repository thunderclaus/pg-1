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

import com.pku.pg.MyAdapter.ViewHolder;
import com.pku.pg.RegisterUserThread.OnSucRegisterListener;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Info extends Activity implements OnClickListener {

	private TextView tv_userName;
	private TextView tv_userPhone;
	private TextView tv_userHospital;
	private Context infoContext;
	static Activity activity;
	static ProgressDialog dialog = null;

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
	private Button bt_submit;

	private ScrollView infoScrollView;
	private ListViewForScrollView mListviewNurses;
	private ListViewForScrollView mListviewRelatives;

	static ArrayList<HashMap<String, String>> mListItemNurses;
	static ArrayList<HashMap<String, String>> mListItemRelatives;

	private JSONArray nursesJsonArray;//没有生效!!!
	private JSONArray relativesJsonArray;
	private JSONArray userJsonArray;
	private MyAdapter nurseAdapter;
	private MyAdapter relativeAdapter;
	
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
		bt_submit = (Button)findViewById(R.id.submit);
		bt_submit.setOnClickListener(this);
		
		
		regUserMac = (EditText)findViewById(R.id.info_textview_MacAddrss);
		
		ActionBar infoActionBar = this.getActionBar();

		infoActionBar.setHomeButtonEnabled(true);
		infoActionBar.setDisplayHomeAsUpEnabled(true);
		infoActionBar.setDisplayShowHomeEnabled(true);
		//添加了infoAB,
		
		userJsonArray = new JSONArray();//没有生效!!!
		infoContext = Info.this;
		activity = this;
		
		// spclear();
		regUserMac.setText(MainActivity.sp.getString("inputID", ""));
		tv_userName = (TextView) findViewById(R.id.info_textview_UserName);
		tv_userPhone = (TextView) findViewById(R.id.info_textview_UserPhone);
		tv_userHospital = (TextView) findViewById(R.id.info_textview_UserHospital);
		tv_userName.setText(MainActivity.sp.getString("userName", ""));
		tv_userPhone.setText(MainActivity.sp.getString("userPhone", ""));
		tv_userHospital.setText(MainActivity.sp.getString("geracomium",""));
		/*
		 * 护工
		 * *******************************************************************
		 * **********************************************************护工OnCreate
		 */
		mListviewNurses = (ListViewForScrollView) findViewById(R.id.info_list_Nurses); /* 定义一个动态数组 */
		Set nurseSet = MainActivity.sp.getStringSet("nurseSet", null);
		if(nurseSet!=null)
			mListItemNurses = (ArrayList<HashMap<String, String>>) nurseSet.iterator().next();
		else mListItemNurses = new ArrayList<HashMap<String, String>>();
		//nurseSer如果为空,新建填充,如果不为空,填入下一项.
		
		nurseAdapter = new MyAdapter(mListItemNurses,this,"看  护  人");
		mListviewNurses.setAdapter(nurseAdapter);

		mListviewNurses.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// TODO Auto-generated method stub
				setTitle("你选择了护理人员" + arg2);// 设置标题栏显示点击的行
				ViewHolder holder = (ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                HashMap<String,String> map = mListItemNurses.get(arg2);
                map.put("ItemCheckbox", ""+holder.cb.isChecked());
                mListItemNurses.remove(arg2);
                mListItemNurses.add(arg2, map);
                nurseAdapter.notifyDataSetChanged();
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
												nurseAdapter.notifyDataSetChanged();
//												mAdapter_Nurses.notifyDataSetChanged();
											}
										}).setNegativeButton("取消", null).show();

						return false;
					}
				});

		// Log.i("护工注册sp",

		/*
		 * 亲属 ************************************************************
		 * ******
		 * *******************************************************Relatives
		 * onCreate
		 */
		mListviewRelatives = (ListViewForScrollView) findViewById(R.id.info_list_Relatives); /* 定义一个动态数组 */
		Set relativeSet = MainActivity.sp.getStringSet("relativeSet", null);
		if(relativeSet!=null)
			mListItemRelatives = (ArrayList<HashMap<String, String>>) relativeSet.iterator().next();
		else mListItemRelatives = new ArrayList<HashMap<String, String>>();
		
		relativeAdapter = new MyAdapter(mListItemRelatives,this,"亲     友");
		mListviewRelatives.setAdapter(relativeAdapter);
		
		mListviewRelatives.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				setTitle("你选择了亲友" + arg2);// 设置标题栏显示点击的行
				ViewHolder holder = (ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                HashMap<String,String> map = mListItemRelatives.get(arg2);
                map.put("ItemCheckbox", ""+holder.cb.isChecked());
                mListItemRelatives.remove(arg2);
                mListItemRelatives.add(arg2, map);
                relativeAdapter.notifyDataSetChanged();
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
												relativeAdapter.notifyDataSetChanged();
											}
										}).setNegativeButton("取消", null).show();

						return false;
					}
				});

		// Log.i("亲属注册sp",
		Log.d("SP", "sp.getAll().toString()");
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
									
									MainActivity.sp.edit().putString("Mac", "")
									.putString("userName", "")
									.putString("userPhone", "")
									.putString("geracomium", "").commit();
									tv_userName.setText(null);
									tv_userPhone.setText(null);
									tv_userHospital.setText(null);
									regUserMac.setText(null);
									if(BtService.bluetoothManager!=null)
										BtService.bluetoothManager.setConnectting(false);
									MainActivity.sp.edit().putBoolean("checkFlag", false)
									.putBoolean("checkSucFlag", false).commit();
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
										jsonObject.put("ID", insertChar(regUserMac.getText().toString()));
										jsonObject.put("userName", regUserName.getText().toString());
										jsonObject.put("userPhone", regUserPhone.getText().toString());
										jsonObject.put("geracomium", regUserHospital.getText().toString());
										jsonObject.put("IMSI", regUserPhone.getText().toString());
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									MainActivity.sp.edit().putString("userStr", jsonObject.toString())
									.putString("userName",regUserName.getText().toString())								
									.putString("userPhone", regUserPhone.getText().toString())
									.putString("geracomium",regUserHospital.getText().toString()).commit();
									tv_userName.setText(regUserName.getText().toString());
									tv_userPhone.setText(regUserPhone.getText().toString());
									tv_userHospital.setText(regUserHospital.getText().toString());
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
										nurseAdapter.notifyDataSetChanged();
									

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
									relativeAdapter.notifyDataSetChanged();
//									mAdapter_Relatives.notifyDataSetChanged();									
								}
							}).show();

			break;
		case R.id.info_btn_MacCheck:
			MainActivity.sp.edit().putBoolean("checkFlag", true).commit();
			String inputID = regUserMac.getText().toString();
			if(inputID.length()==12&&inputID.matches("\\w{12}")){
				deviceID = insertChar(inputID);	
				MainActivity.sp.edit().putString("deviceID", deviceID).commit();
				Intent intent = new Intent(infoContext,BtService.class);
				Bundle bundle = new Bundle();
				bundle.putString("deviceID", deviceID);
				intent.putExtras(bundle);
				infoContext.startService(intent);	
				dialog = new ProgressDialog(this);
				dialog.setTitle("检测");
				dialog.setMessage("正在连接设备");
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(false);
				dialog.show();
			}else Toast.makeText(this, "对不起，您输入的序列号格式有误，请重新输入", Toast.LENGTH_LONG).show();					
			break;
		case R.id.submit:
			//注册用户
			boolean checkSucFlag = MainActivity.sp.getBoolean("checkSucFlag", false);
			if(checkSucFlag){
//				String registerUserStr = MainActivity.sp.getString("registerUserStr", "registerUserStr");
				String userStr = MainActivity.sp.getString("userStr", "userStr");
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
//				if(userStr!= "userStr"&&registerUserStr!= newUserStr){		
					RegisterUserThread thread = new RegisterUserThread(newUserStr,MainActivity.sp);
					thread.start();
					thread.setOnSucRegisterListener(new OnSucRegisterListener(){
						@Override
						public void onSucRegister() {
							// TODO Auto-generated method stub
							//注册护工
//							String registerNurseStr = MainActivity.sp.getString("registerNurseStr", "registerNurseStr");
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
							String newNurseStr = null;			
							try {
								JSONObject obj3 = new JSONObject();
								deviceID = MainActivity.sp.getString("deviceID", "");
								obj3.put("ID", deviceID);
								obj3.put("nursesInfo", nurseArray);
								JSONObject obj4 = new JSONObject();
								obj4.put("nurse", obj3);
								newNurseStr = obj4.toString();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}			
//							if(registerNurseStr!= newNurseStr){				
								RegisterNurseThread registerNurseThread = new RegisterNurseThread(newNurseStr,MainActivity.sp);
								registerNurseThread.start();
//							}
							
							//注册亲属
//							String registerRelativesStr = MainActivity.sp.getString("registerRelativesStr", "registerRelativesStr");
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
							String newRelativeStr = null;			
							try {
								JSONObject obj3 = new JSONObject();
								deviceID = MainActivity.sp.getString("deviceID", "");
								obj3.put("ID", deviceID);
								obj3.put("relativesInfo", relativeArray);
								JSONObject obj4 = new JSONObject();
								obj4.put("relative", obj3);
								newRelativeStr = obj4.toString();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}			
//							if(registerRelativesStr!= newRelativeStr){				
								RegisterRelativesThread registerRelativesThread = new RegisterRelativesThread(newRelativeStr,MainActivity.sp);
								registerRelativesThread.start();
//							}			
								
							//注册成功后提示，注册为线程，此时做判断时还没有把标识位置为true，所以没有提示	
//							if(registerNurseThread.getRegNurseFlag()&&registerRelativesThread.getRegRelativesFlag())
//								MainActivity.SendMessage(MainActivity.handler, 12);
						}						
					});
//				}								
			}else Toast.makeText(this, "对不起，您输入的序列号需验证，验证成功后方可注册", Toast.LENGTH_LONG).show();
			
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


	public void onStop(){
		super.onStop();
		Set nurseSet = new HashSet();
		nurseSet.add(mListItemNurses);
		Set relativeSet = new HashSet();
		relativeSet.add(mListItemRelatives);
		MainActivity.sp.edit().putStringSet("nurseSet", nurseSet)
		.putStringSet("relativeSet", relativeSet)
		.putString("inputID", regUserMac.getText().toString()).commit();
	}
}
