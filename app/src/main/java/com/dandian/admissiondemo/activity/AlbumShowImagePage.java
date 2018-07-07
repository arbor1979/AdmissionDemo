package com.dandian.admissiondemo.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dandian.admissiondemo.api.CampusAPI;
import com.dandian.admissiondemo.api.CampusException;
import com.dandian.admissiondemo.api.CampusParameters;
import com.dandian.admissiondemo.api.RequestListener;
import com.dandian.admissiondemo.base.Constants;
import com.dandian.admissiondemo.entity.AlbumImageInfo;
import com.dandian.admissiondemo.entity.AlbumMsgInfo;
import com.dandian.admissiondemo.fragment.AlbumImageFragment;
import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.Base64;
import com.dandian.admissiondemo.util.PrefUtility;
import com.dandian.admissiondemo.util.ZLibUtils;
import com.dandian.admissiondemo.R;



public class AlbumShowImagePage extends FragmentActivity {

	private ViewPager mViewpager;
	private StuInfoPagerAdapter mStuInfoPagerAdapter;
	private boolean misScrolled;
	public ArrayList<AlbumImageInfo> browsedList,praisedList,deletedList,commentedList;
	Button bn_back, menu,btnRight,btnShare;
	String hostid;
	LinearLayout bottomLayout;
	EditText edit;
	ImageView faceImage;
	View viewpager_layout;
	
	private RelativeLayout express_spot_layout;
	
	private ProgressBar proBar;
	//private Intent intent;
	//private Student studentInfo;
	
	
	private ArrayList<AlbumImageInfo> imageList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_image_page);
		proBar=(ProgressBar)findViewById(R.id.progressBar2);
		hostid = PrefUtility.get(Constants.PREF_CHECK_HOSTID, "");
		imageList=(ArrayList<AlbumImageInfo>)getIntent().getSerializableExtra("imageList");
		
		browsedList=new ArrayList<AlbumImageInfo>();
		praisedList=new ArrayList<AlbumImageInfo>();
		deletedList=new ArrayList<AlbumImageInfo>();
		commentedList=new ArrayList<AlbumImageInfo>();
		initTitle();
		initViewPager();
		if(!browsedList.contains(imageList.get(0)))
			browsedList.add(imageList.get(0));

		getImageDetailInfo();
	}
	private void getImageDetailInfo()
	{
		//proBar.setVisibility(View.VISIBLE);
		String imageIds="";
		for(int i=0;i<imageList.size();i++)
		{
			if(imageIds.length()!=0)
				imageIds=imageIds+";";
			imageIds=imageIds+imageList.get(i).getName();
		}
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("imageIds",imageIds);
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getAlbumDetailList(params,new RequestListener() {

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
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
				
			}
		});
		
		
	}
	private void initTitle() {
		
		bn_back = (Button) findViewById(R.id.back);
		bn_back.setVisibility(View.VISIBLE);
		
		bn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();
			}
		});

	

	}
	
	
	public void hideOrShowSoftinput(View view) {
		InputMethodManager manager = (InputMethodManager) this
				.getSystemService(Service.INPUT_METHOD_SERVICE);
		if (manager.isActive()) {
			manager.hideSoftInputFromWindow(edit.getWindowToken(),
					0);
			viewpager_layout.setVisibility(View.GONE);
		} else {
			manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	
	@SuppressWarnings("deprecation")
	private void initViewPager() {
		mViewpager = (ViewPager) findViewById(R.id.stuinfo_pager);
		mStuInfoPagerAdapter = new StuInfoPagerAdapter(
				getSupportFragmentManager(), imageList);
		mViewpager.setAdapter(mStuInfoPagerAdapter);
		mViewpager.setPageMargin(30);
		mViewpager.setOnPageChangeListener(new OnPageChangeListener(){
		
			@Override
			public void onPageScrollStateChanged(int state) 
			{		
				switch (state) {		
					case ViewPager.SCROLL_STATE_DRAGGING:			
						misScrolled = false;			
						break;		
					case ViewPager.SCROLL_STATE_SETTLING:			
						misScrolled = true;			
						break;		
					case ViewPager.SCROLL_STATE_IDLE:			
						if (mViewpager.getCurrentItem() == mViewpager.getAdapter().getCount() - 1 && !misScrolled) 
						{				
							AppUtility.showToastMsg(AlbumShowImagePage.this, getString(R.string.lastone));
						}
						else if (mViewpager.getCurrentItem() == 0 && !misScrolled) 
						{
							AppUtility.showToastMsg(AlbumShowImagePage.this, getString(R.string.firstone));
						}
						misScrolled = true;			
						break;		
					}
			}
			 

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				
				AlbumImageInfo image=imageList.get(arg0);
				if(!browsedList.contains(image))
					browsedList.add(image);
				
			}
			
			
		});
	}

	

	public class StuInfoPagerAdapter extends FragmentStatePagerAdapter {
		List<AlbumImageInfo> list = new ArrayList<AlbumImageInfo>();
		FragmentManager fm;
		public StuInfoPagerAdapter(FragmentManager fm, List<AlbumImageInfo> list) {
			super(fm);
			this.list = list;
			this.fm=fm;
		}
		
		@Override
		public Fragment getItem(int position) {
			Bundle bundle = null;
			AlbumImageInfo image=list.get(position);
			
			Log.d("ImageFragment",position+"getItem:"+image.getName());
			AlbumImageFragment mStuInfoFragment = new AlbumImageFragment();
			bundle = new Bundle();
			bundle.putSerializable("imageInfo",  image);
			mStuInfoFragment.setArguments(bundle);
			return mStuInfoFragment;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) 
		{
			AlbumImageFragment f = (AlbumImageFragment) super.instantiateItem(container, position);
			AlbumImageInfo image=list.get(position);
			Log.d("ImageFragment",position+"instantiateItem:"+image.getName());
		    f.setImage(image);
		    return f;
		}
		
		@Override
		public int getItemPosition(Object object) {
		     return PagerAdapter.POSITION_NONE;
		}
		
		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}
		@Override  
	    public void destroyItem(ViewGroup container, int position, Object object) 
		{
			super.destroyItem(container, position, object);
			/*
			AlbumImageFragment fragment = (AlbumImageFragment)object; 
			//fragment.main_image.setImageResource(R.drawable.empty_photo);
			Log.d("ImageFragment",position+"destroyItem:"+fragment.getImage().getName());
			if(fm.getFragments().contains(fragment))
				fm.getFragments().remove(fragment);
			*/
		}
		
	}
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void handleMessage(Message msg) {
			
			
			switch (msg.what) {
			case -1:// 璇锋眰澶辫触
				proBar.setVisibility(View.INVISIBLE);
				AppUtility.showErrorToast(AlbumShowImagePage.this,
						msg.obj.toString());
				
				break;
			case 1:// 获取赞和评论
				
				final String result1 = msg.obj.toString();
				Thread thread=new Thread(new Runnable()  
		        {  
		            @Override  
		            public void run()  
		            {  
		            	byte[] resultByte=null;
						String	resultStr="";
						if (AppUtility.isNotEmpty(result1)) 
						{
							try {
								resultByte =Base64.decode(result1
										.getBytes("GBK"));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							resultStr = ZLibUtils.decompress(resultByte);
						}
						try {
							JSONObject jo=new JSONObject(resultStr);
							JSONObject praiseOb=jo.getJSONObject("点赞");
							JSONObject commentOb=jo.getJSONObject("评论");
							for(int i=0;i<imageList.size();i++)
							{
								AlbumImageInfo image=imageList.get(i);
								if(praiseOb!=null)
								{
									JSONArray ja=praiseOb.getJSONArray(image.getName());
									if(ja!=null)
									{
										
										for(int j=0;j<ja.length();j++)
										{
											JSONObject item=ja.getJSONObject(j);
											AlbumMsgInfo u=new AlbumMsgInfo(item);
											image.getPraiseList().add(u);
										}
										
									}
										
										
								}
								if(commentOb!=null)
								{
									JSONArray ja=commentOb.getJSONArray(image.getName());
									if(ja!=null)
									{
										
										for(int j=0;j<ja.length();j++)
										{
											JSONObject item=ja.getJSONObject(j);
											AlbumMsgInfo u=new AlbumMsgInfo(item);
											image.getCommentsList().add(u);
										}
										
									}
								}
								
							}
							
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Message msg = new Message();
						msg.what = 3;
						mHandler.sendMessage(msg);
		            }  
		        });  
		        thread.start();  
				
				
				break;
			case 3:
				proBar.setVisibility(View.INVISIBLE);
				mStuInfoPagerAdapter.notifyDataSetChanged();
				//updateRightButton(mViewpager.getCurrentItem());
				break;
			}
		}
	};

}
