package com.dandian.admissiondemo.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import com.dandian.admissiondemo.CampusApplication;
import com.dandian.admissiondemo.R;
import com.dandian.admissiondemo.api.CampusAPI;
import com.dandian.admissiondemo.api.CampusException;
import com.dandian.admissiondemo.api.CampusParameters;
import com.dandian.admissiondemo.api.RequestListener;
import com.dandian.admissiondemo.base.Constants;
import com.dandian.admissiondemo.db.DatabaseHelper;
import com.dandian.admissiondemo.db.InitData;
import com.dandian.admissiondemo.entity.User;
import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.Base64;
import com.dandian.admissiondemo.util.DialogUtility;
import com.dandian.admissiondemo.util.PrefUtility;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedDelete;


public class LoginActivity extends Activity implements OnClickListener ,Callback {
	private static final String TAG = "LoginActivity";
	private EditText mUsernameView, mPasswordView;
	private Button loginButton,login_getcode;
	private String mUsername, mPassword,mUserType;
	private User user;
	private DatabaseHelper database;
	private Dialog mLoadingDialog;
	private Dao<User, Integer> userDao;
	private boolean ready;
	
	/** Called when the activity is first created. */
	@TargetApi(23)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		
		setContentView(R.layout.activity_login);

		PrefUtility.put(Constants.PREF_BAIDU_USERID, "");
		PrefUtility.put(Constants.PREF_CHECK_CODE,"");
		
		
		mUsername=PrefUtility.get(Constants.PREF_LOGIN_NAME, "");
		mPassword=PrefUtility.get(Constants.PREF_LOGIN_PASS, "");
		mUserType=PrefUtility.get(Constants.PREF_CHECK_USERTYPE, "");
		
		buildComponents();
		
		initSDK();
		
		mUsernameView.setText(mUsername);
		if(mUsername!=null && mUsername.length()>0 && mPassword!=null && mPassword.length()>0)
		{
			mPasswordView.setText(mPassword);
			doLogin();
		}
		
	}
	
	private void initSDK() {
		// 初始化短信SDK
		SMSSDK.initSDK(this, "1d813bc436cf4", "36c9f5b808719b99543c034b8e20c942",SMSSDK.InitFlag.DISABLE_CONTACT);
		final Handler handler = new Handler(this);
		EventHandler eventHandler = new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				handler.sendMessage(msg);
			}
		};
		// 注册回调监听接口
		SMSSDK.registerEventHandler(eventHandler);
		ready = true;
		

	}
	private void buildComponents() {
		final ImageView login_userphoto=(ImageView)findViewById(R.id.login_userphoto);
		mUsernameView = (EditText) findViewById(R.id.login_username);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		loginButton = (Button) findViewById(R.id.btn_login);
		login_getcode=(Button) findViewById(R.id.btn_getcode);
		RadioGroup genderGroup=(RadioGroup)findViewById(R.id.radioGroup1);
		genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { 
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) { 
                // TODO Auto-generated method stub 
            	mUsernameView.setText("");
            	mPasswordView.setText("");
                if(checkedId==R.id.radio0){ 
                	login_getcode.setVisibility(View.VISIBLE);
                	mUsernameView.setHint(R.string.hint_login_idcard);
                	mPasswordView.setHint(R.string.hint_login_password);
                	login_userphoto.setImageResource(R.drawable.shenfenzheng);
                	
                	mUserType="学生";
                }else if(checkedId==R.id.radio1){ 
                	login_getcode.setVisibility(View.GONE);
                	mUsernameView.setHint(R.string.hint_login_username);
                	mPasswordView.setHint(R.string.hint_login_password);
                	login_userphoto.setImageResource(R.drawable.login_userphoto);
                	mUserType="老师";
                } 
            } 
        }); 
		if(mUserType.equals("老师"))
			genderGroup.check(R.id.radio1);
		else
			genderGroup.check(R.id.radio0);
		mLoadingDialog = DialogUtility.createLoadingDialog(this, getString(R.string.logging));

		loginButton.setOnClickListener(this);
		login_getcode.setOnClickListener(this);
		TextView copyright=(TextView) findViewById(R.id.tv_copyright);
		String thisVersion = CampusApplication.getVersion();
		copyright.append(" Ver:"+thisVersion);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
	
		case R.id.btn_login:
			if(verifyInput())
			{
				doLogin();
			}
			break;
		case R.id.btn_getcode:
			//getSmsCode();
			if(verifyInput())
			{
				RegisterPage registerPage = new RegisterPage();
				registerPage.setRegisterCallback(new EventHandler() {
				public void afterEvent(int event, int result, Object data) {
					// 解析注册结果
					if (result == SMSSDK.RESULT_COMPLETE) {
						@SuppressWarnings("unchecked")
						HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
						String country = (String) phoneMap.get("country");
						String phone = (String) phoneMap.get("phone");
						// 提交用户信息
						registerUser(country, phone);
					}
				}
				});
				registerPage.show(this);
			}
			break;
		}
	}
	
	private void registerUser(String country,String phone)
	{
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "getPassword");
			jsonObj.put("身份证", mUsername);
			jsonObj.put("手机号", phone);
			jsonObj.put("language", language);
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		CampusAPI.loginCheck(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				
				Message msg = new Message();
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	private boolean verifyInput()
	{
		
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean pass = true;
		View focusView = null;
		if (TextUtils.isEmpty(mUsername)) {
			if(mUserType.equals("老师"))
				mUsernameView.setError(getString(R.string.error_username_required));
			else
				mUsernameView.setError(getString(R.string.error_idnumber_required));
			focusView = mUsernameView;
			pass = false;
		}
		
		/*
		else if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_username_required));
			focusView = mUsernameView;
			cancel = true;
		}*/
		if (!pass) {
			focusView.requestFocus();
		}
		return pass;
	}


	private void doLogin() {
		mLoadingDialog.show();
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "login");
			jsonObj.put("身份证", mUsername);
			jsonObj.put("密码", mPassword);
			jsonObj.put("language", language);
			jsonObj.put("用户类型",mUserType);
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		CampusAPI.loginCheck(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(LoginActivity.this,
						msg.obj.toString());
				break;
			case 0:
				
				String result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "--->  " + result);
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");
					
					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(LoginActivity.this, loginStatus,1);
						if(mLoadingDialog!=null)
							mLoadingDialog.dismiss();
					} else 
					{
						user = new User(jo.optJSONObject("用户信息"));
						PrefUtility.put(Constants.PREF_LOGIN_NAME, mUsername);
						PrefUtility.put(Constants.PREF_LOGIN_PASS, mPassword);
						PrefUtility.put(Constants.PREF_CHECK_CODE, user.getCheckCode());
						PrefUtility.put(Constants.PREF_CHECK_HOSTID, user.getUserNumber());
						PrefUtility.put(Constants.PREF_CHECK_USERTYPE, user.getUserType());
						
						PrefUtility.put(Constants.PREF_INIT_DATA_STR, jo.optJSONObject("用户信息").toString());
						PrefUtility.put(Constants.PREF_INIT_CONTACT_STR, jo.optString("显示字段"));
						
						((CampusApplication)getApplicationContext()).setLoginUserObj(user);

						getHelper().getEqmDao();
						userDao = getHelper().getUserDao();
						userDao.delete((PreparedDelete<User>) userDao.deleteBuilder().prepare());
						userDao.create(user);
						
						String baiduUserId=PrefUtility.get(Constants.PREF_BAIDU_USERID, "");
						if(baiduUserId.length()>0 && !TabHostActivity.ifpostuserid)
						{
							InitData initData = new InitData(LoginActivity.this, getHelper(), null,"postBaiDuUserId",user.getSortNumber());
							initData.postBaiduUserId();
						}
						jumpMain();
					}
				} catch (Exception e) {
					if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
					e.printStackTrace();
				}
				
				break;
			case 1:
				result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "--->  " + result);
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");
					
					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(LoginActivity.this, loginStatus,1);
						if(mLoadingDialog!=null)
							mLoadingDialog.dismiss();
					} else 
					{
						
						mPasswordView.setText(jo.optString("password"));
						mPassword=jo.optString("password");
						doLogin();
					}
				} catch (Exception e) {
					if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
					e.printStackTrace();
				}
				
				break;
			}
		};
	};

	

	private void jumpMain() {
		Intent intent = new Intent(this, TabHostActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		if(mLoadingDialog!=null)
			mLoadingDialog.dismiss();
		this.finish();
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (database != null) {
			OpenHelperManager.releaseHelper();
			database = null;
		}
		if (ready) {
			// 销毁回调监听接口
			SMSSDK.unregisterAllEventHandler();
		}
	}

	/**
     */
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, com.dandian.admissiondemo.db.DatabaseHelper.class);
		}
		return database;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		return false;
	}

}
