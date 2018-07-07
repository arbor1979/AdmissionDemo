package com.dandian.admissiondemo.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dandian.admissiondemo.CampusApplication;
import com.dandian.admissiondemo.adapter.WaterfallAdapter;
import com.dandian.admissiondemo.api.CampusAPI;
import com.dandian.admissiondemo.api.CampusException;
import com.dandian.admissiondemo.api.CampusParameters;
import com.dandian.admissiondemo.api.RequestListener;
import com.dandian.admissiondemo.base.Constants;
import com.dandian.admissiondemo.db.DatabaseHelper;
import com.dandian.admissiondemo.entity.AlbumImageInfo;
import com.dandian.admissiondemo.entity.AlbumMsgInfo;
import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.Base64;
import com.dandian.admissiondemo.util.PrefUtility;
import com.dandian.admissiondemo.util.ZLibUtils;
import com.dandian.admissiondemo.widget.MultiColumnListView;
import com.dandian.admissiondemo.widget.SegmentedGroup;
import com.dandian.admissiondemo.R;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class AlbumFlowActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener,SwipeRefreshLayout.OnRefreshListener{

	public static LinearLayout layout_menu;
	public static final int REQUEST_CODE_TAKE_PICTURE = 2;// //璁剧疆鍥剧墖鎿嶄綔鐨勬爣蹇?
	public static final int REQUEST_CODE_TAKE_CAMERA = 1;// //璁剧疆鎷嶇収鎿嶄綔鐨勬爣蹇?
	private String picturePath;
	
	private LinearLayout loadingLayout;
	private SwipeRefreshLayout swipeLayout; 
	private RadioButton btn21,btn23;
	public ArrayList<AlbumMsgInfo> unreadList=new ArrayList<AlbumMsgInfo>();
	private ArrayList<AlbumImageInfo> imageList=new ArrayList<AlbumImageInfo>();
	private MultiColumnListView mclv = null;
	private WaterfallAdapter mAdapter = null;
	DatabaseHelper database;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_album_flow);

		SegmentedGroup segmented2 = (SegmentedGroup) findViewById(R.id.segmentedGroup2);
		segmented2.setTintColor(Color.DKGRAY);
		segmented2.setOnCheckedChangeListener(this);
		segmented2.setVisibility(View.GONE);
		
		btn21 = (RadioButton) findViewById(R.id.button21);
		btn23 = (RadioButton) findViewById(R.id.button23);
		
		mclv=(MultiColumnListView) findViewById(R.id.list);
		
		loadingLayout = (LinearLayout) findViewById(R.id.data_load);
		
		swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swip);  
        swipeLayout.setOnRefreshListener(this); 
       
        // 椤堕儴鍒锋柊鐨勬牱寮?  
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light,  
                android.R.color.holo_blue_bright, android.R.color.holo_orange_light);  
		
		if(!btn21.isChecked() && !btn23.isChecked())
			btn21.setChecked(true);
		LinearLayout relogin = (LinearLayout) findViewById(R.id.layout_btn_left);
		relogin.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((CampusApplication)getApplicationContext()).reLogin();
			}
			
		});
	}

	public void onRefresh() {  
        new Handler().postDelayed(new Runnable() {  
            public void run() {  
            	
                getDownloadSubject(false); 
            }  
        }, 50);  
    }
	
	private void getDownloadSubject(boolean showProg) {
		showProgress(showProg);

		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			
				jo.put("范围","新生");

			if(!showProg && imageList.size()>0)
				jo.put("curImageName", imageList.get(0).getName());
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getAlbumList(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

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
				msg.what = 3;
				msg.obj = response;
				mHandler.sendMessage(msg);
				
			}
		});
	}
	
	private void showProgress(boolean progress) {
		if (progress) {
			
			swipeLayout.setVisibility(View.GONE);
			loadingLayout.setVisibility(View.VISIBLE);
		} else {
			loadingLayout.setVisibility(View.GONE);
			swipeLayout.setVisibility(View.VISIBLE);
		}
	}
	
	
	
	
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case -1:// 璇锋眰澶辫触
				
				AppUtility.showErrorToast(AlbumFlowActivity.this,
						msg.obj.toString());
				
				break;
			case 3:// 鑾峰彇鐩稿唽
				
				swipeLayout.setRefreshing(false);  
				final String result1 = msg.obj.toString();
				
				Thread thread=new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            {  
		            	byte [] resultByte = null;
						String unZlibStr=null;
						if (AppUtility.isNotEmpty(result1)) 
						{
							try {
								resultByte = Base64.decode(result1
										.getBytes("GBK"));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							unZlibStr = ZLibUtils.decompress(resultByte);
						}
						try {
							JSONObject job=new JSONObject(unZlibStr);
							String curImageName=job.optString("curImageName");
							JSONArray jo = job.getJSONArray("相册");
							List<AlbumImageInfo> newlist = AlbumImageInfo.toList(jo);
							
								if(curImageName!=null && !curImageName.equals("null") && curImageName.length()>0)
								{
									for(int i=newlist.size()-1;i>=0;i--)
									{
										imageList.add(0,newlist.get(i));
									}
								}
								else
									imageList=(ArrayList<AlbumImageInfo>) newlist;
								
						} catch (JSONException e) {
							e.printStackTrace();
						}  
						Message msg = new Message();
						msg.what = 6;
						mHandler.sendMessage(msg);	
		            }  
		        });  
		        thread.start();  
				
				break;
			
			case 6:
				showProgress(false); 
				mAdapter=new WaterfallAdapter(imageList,AlbumFlowActivity.this);
				mclv.setAdapter(mAdapter);
			
			
				if (imageList.size()==0) {
					AppUtility.showToastMsg(AlbumFlowActivity.this, getString(R.string.albumempty));
				}
				break;
			}
		}
	};
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		 switch (checkedId) {
         case R.id.button21:
        	 getDownloadSubject(true);
             return;
         case R.id.button23:
        	 getDownloadSubject(true);
             return;
		 }
	}
	
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}
	private ArrayList<AlbumMsgInfo> getUnreadList()
	{
		unreadList.clear();
		try {
			String hostId=PrefUtility.get(Constants.PREF_CHECK_HOSTID, "");
			unreadList=(ArrayList<AlbumMsgInfo>) getHelper().getAlbumMsgDao().queryBuilder().orderBy("id", false).where().eq("ifRead",0).and().eq("toId", hostId).query();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return unreadList;
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("imageList",imageList);
		savedInstanceState.putString("picturePath", picturePath);
		
		
		
	}
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        imageList = (ArrayList<AlbumImageInfo>) savedInstanceState.getSerializable("imageList");
        picturePath=savedInstanceState.getString("picturePath");
    }
	
}
