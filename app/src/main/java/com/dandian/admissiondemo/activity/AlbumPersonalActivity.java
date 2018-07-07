package com.dandian.admissiondemo.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.dandian.admissiondemo.CampusApplication;
import com.dandian.admissiondemo.api.CampusAPI;
import com.dandian.admissiondemo.api.CampusException;
import com.dandian.admissiondemo.api.CampusParameters;
import com.dandian.admissiondemo.api.RequestListener;
import com.dandian.admissiondemo.base.Constants;
import com.dandian.admissiondemo.db.DatabaseHelper;
import com.dandian.admissiondemo.entity.AlbumImageInfo;
import com.dandian.admissiondemo.entity.User;
import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.Base64;
import com.dandian.admissiondemo.util.FileUtility;
import com.dandian.admissiondemo.util.ImageUtility;
import com.dandian.admissiondemo.util.PrefUtility;
import com.dandian.admissiondemo.R;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class AlbumPersonalActivity extends FragmentActivity implements SwipeRefreshLayout.OnRefreshListener{

	public static LinearLayout layout_menu;
	public static final int REQUEST_CODE_TAKE_PICTURE = 2;
	public static final int REQUEST_CODE_TAKE_CAMERA = 1;
	private String picturePath,hostId,userId;
	private User user;

	private ListView msv;
	private String mInterface = "AlbumPraise.php";
	private List<AlbumImageInfo> list=new ArrayList<AlbumImageInfo>();
	private LinearLayout loadingLayout;
	private SwipeRefreshLayout swipeLayout; 
	private PersonalAdapter pAdpter;

	DatabaseHelper database;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_album_personal);
		TextView title=(TextView)findViewById(R.id.setting_tv_title);
		
		msv=(ListView)findViewById(R.id.listView1);
		Button bn_back = (Button) findViewById(R.id.back);
		bn_back.setVisibility(View.VISIBLE);
		bn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		user=((CampusApplication)getApplicationContext()).getLoginUserObj();
	
		String hostName=getIntent().getStringExtra("hostName");
		title.setText(hostName+"的相册");
		
		hostId=getIntent().getStringExtra("hostId");
		userId = PrefUtility.get(Constants.PREF_CHECK_HOSTID, "");
		if(hostId==null || hostId.length()==0)
			hostId=userId;

		loadingLayout = (LinearLayout) findViewById(R.id.data_load);
		
		swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swip);  
        swipeLayout.setOnRefreshListener(this); 
       
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light,  
                android.R.color.holo_blue_bright, android.R.color.holo_orange_light);  
        getDownloadSubject(true);
        pAdpter=new PersonalAdapter(this);
        msv.setAdapter(pAdpter);
	}
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		}
		return database;
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
			
			jo.put("action","个人相册");
			jo.put("hostId",hostId);
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getDownloadSubject(params, mInterface, new RequestListener() {

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
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		
		case 3:
			if(resultCode==200)
			{
				ArrayList<AlbumImageInfo> praisedList=(ArrayList<AlbumImageInfo>) data.getSerializableExtra("praisedList");  //data涓築涓洖浼犵殑Intent
				for(int i=0;i<praisedList.size();i++)
				{
					AlbumImageInfo image=praisedList.get(i);
					for(int j=0;j<list.size();j++)
					{
						AlbumImageInfo image1=list.get(j);
						if(image1.getName().equals(image.getName()))
						{
							image1.setPraiseCount(image.getPraiseList().size());
							break;
						}
					}
				}
				ArrayList<AlbumImageInfo> commentedList=(ArrayList<AlbumImageInfo>) data.getSerializableExtra("commentedList");  //data涓築涓洖浼犵殑Intent
				for(int i=0;i<commentedList.size();i++)
				{
					AlbumImageInfo image=commentedList.get(i);
					for(int j=0;j<list.size();j++)
					{
						AlbumImageInfo image1=list.get(j);
						if(image1.getName().equals(image.getName()))
						{
							image1.setCommentCount(image.getCommentsList().size());
							break;
						}
					}
				}
				ArrayList<AlbumImageInfo> deletedList=(ArrayList<AlbumImageInfo>) data.getSerializableExtra("deletedList");  //data涓築涓洖浼犵殑Intent
				for(int i=0;i<deletedList.size();i++)
				{
					AlbumImageInfo image=deletedList.get(i);
					for(int j=0;j<list.size();j++)
					{
						if(list.get(j).getName().equals(image.getName()))
						{
							list.remove(j);
							break;
						}
					}
				}
				pAdpter.notifyDataSetChanged();
			}
			
			break;
		default:
			break;
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void handleMessage(Message msg) {
			
			String result = "";
			String resultStr = "";
			switch (msg.what) {
			case -1:// 璇锋眰澶辫触
				
				AppUtility.showErrorToast(AlbumPersonalActivity.this,
						msg.obj.toString());
				
				break;
			case 3:// 鑾峰彇鐩稿唽
				showProgress(false); 
				swipeLayout.setRefreshing(false);  
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				try {
					JSONObject jo=new JSONObject(resultStr);
					JSONArray ja = jo.getJSONArray("相册");
					list.clear();
					if(ja!=null)
						list=AlbumImageInfo.toList(ja);
					
					if (list.size()==0) {
						String tipmsg="目前还没有照片";
						AppUtility.showToastMsg(AlbumPersonalActivity.this, tipmsg);
					}
					pAdpter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			
			}
		}
	};
	public class PersonalAdapter extends BaseAdapter{

		private Context context;
		public PersonalAdapter(Context ct)
		{
			context=ct;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder vh;
			AQuery aq = new AQuery(convertView);
			if (convertView == null) 
			{		
				convertView = LayoutInflater.from(AlbumPersonalActivity.this).inflate(R.layout.activity_album_personallist_item, null);
				
				vh=new ViewHolder();
				vh.theDay=(TextView)convertView.findViewById(R.id.theDay);
				vh.theMonth=(TextView)convertView.findViewById(R.id.theMonth);
				vh.theAddress=(TextView)convertView.findViewById(R.id.theAddress);
				vh.theImage=(ImageView)convertView.findViewById(R.id.theImage);
				vh.theDescription=(TextView)convertView.findViewById(R.id.theDescription);
				vh.thePraise=(TextView)convertView.findViewById(R.id.thePraise);
				vh.theComment=(TextView)convertView.findViewById(R.id.theComment);
				convertView.setTag(vh);
			}
			else
			{
				vh = (ViewHolder) convertView.getTag();
			}
			
			AlbumImageInfo aii=list.get(position);
			vh.theDay.setText("");
			vh.theMonth.setText("");
			vh.thePraise.setText("0");
			vh.theComment.setText("0");
			boolean flag=false;
			if(position==0)
			{
				flag=true;
			}
			else
			{
				AlbumImageInfo last=list.get(position-1);
				if(!last.getTime().substring(0, 10).equals(aii.getTime().substring(0, 10)))
				{
					flag=true;
				}
			}
			if(flag)
			{
				vh.theDay.setText(aii.getTime().substring(8,10));
				vh.theMonth.setText(aii.getTime().substring(5,7)+"月");
			}
			
			BitmapAjaxCallback cb = new BitmapAjaxCallback(){
		        @Override
		        public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status){
		            if(status.getCode()==200) {
		                super.callback(url, iv, bm,status);
		                ImageUtility.writeTofiles(bm, getImagePath(url));
		            } 
		        }            
		    };
			aq.id(vh.theImage).image(aii.getUrl(),false,true,200,R.drawable.empty_photo,cb);
			vh.theAddress.setText(aii.getAddress());
			vh.theDescription.setText(aii.getDescription());
			vh.thePraise.setText(String.valueOf(aii.getPraiseCount()));
			vh.theComment.setText(String.valueOf(aii.getCommentCount()));
			vh.theImage.setTag(position);
			convertView.setOnClickListener(new OnClickListener(){
			
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int index=(Integer) v.findViewById(R.id.theImage).getTag();
					ArrayList<AlbumImageInfo> detailList=new ArrayList<AlbumImageInfo>();
					int PAGE_SIZE=15;
					for(int i=index;i<list.size();i++)
					{
						detailList.add(list.get(i));
						if(detailList.size()>PAGE_SIZE)
							break;
					}
					Intent intent=new Intent(context,AlbumShowImagePage.class);
					intent.putExtra("imageList", detailList);
					((FragmentActivity) context).startActivityForResult(intent,3);
				}
				
			});
			
			return convertView;
		}
		public class ViewHolder {
			public TextView theDay;
			public TextView theMonth;
			public TextView theAddress;
			public ImageView theImage;
			public TextView theDescription;
			public TextView thePraise;
			public TextView theComment;
			
		}
		
	}
	private String getImagePath(String imageUrl) {
		String imageName=FileUtility.getFileRealName(imageUrl);
		String imageDir = FileUtility.creatSDDir("相册");
		String imagePath = imageDir + imageName;
		return imagePath;
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("user",user);
		savedInstanceState.putString("picturePath", picturePath);
		
		
	}
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        user = (User) savedInstanceState.getSerializable("user");
        picturePath=savedInstanceState.getString("picturePath");
    }
}
