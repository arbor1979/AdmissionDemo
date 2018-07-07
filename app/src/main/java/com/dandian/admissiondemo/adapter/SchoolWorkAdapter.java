package com.dandian.admissiondemo.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.admissiondemo.activity.CourseClassActivity;
import com.dandian.admissiondemo.activity.SchoolActivity;
import com.dandian.admissiondemo.activity.WebSiteActivity;
import com.dandian.admissiondemo.entity.SchoolWorkItem;
import com.dandian.admissiondemo.util.BadgeView;
import com.dandian.admissiondemo.R;

public class SchoolWorkAdapter extends BaseAdapter {
	private String TAG = "SchoolWorkAdapter";
	private Context mContext;
	private LayoutInflater inflater;
	List<SchoolWorkItem> schoolWorkItems;

	public SchoolWorkAdapter(Context c, List<SchoolWorkItem> schoolWorkItems) {
		mContext = c;
		this.schoolWorkItems = schoolWorkItems;
		inflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return schoolWorkItems.size();
	}

	public void setSchoolWorkItems(List<SchoolWorkItem> schoolWorkItems){
		this.schoolWorkItems = schoolWorkItems;
	}
	@Override
	public Object getItem(int position) {
		return schoolWorkItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (null == convertView) {
			convertView = inflater.inflate(R.layout.school_work_item, null);
			holder = new ViewHolder();

			holder.ibWrokPic = (ImageView) convertView.findViewById(R.id.ib_school_work_pic);
			holder.tvWorkName = (TextView) convertView.findViewById(R.id.tv_school_work_name);
			holder.badge = new BadgeView(mContext, holder.ibWrokPic);
			holder.badge.setBadgeMargin(0,0);
			//holder.badge.setBackgroundResource(R.drawable.badge_ifaux);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		AQuery aq = new AQuery(convertView);
		final SchoolWorkItem schoolWorkItem = (SchoolWorkItem) getItem(position);
		if(schoolWorkItem.getUnread()>0)
		{
			holder.badge.setText(String.valueOf(schoolWorkItem.getUnread()));
			
			holder.badge.show();
		}
		else
			holder.badge.hide();
			

		String imgUrl = schoolWorkItem.getWorkPicPath();
		Log.d(TAG, "----------imgUrl:" + imgUrl);
		
		Bitmap bitmap = aq.getCachedImage(imgUrl);
		if (bitmap != null) {
			aq.id(holder.ibWrokPic).image(bitmap);
		}
		else
			aq.id(holder.ibWrokPic).image(imgUrl,true,true);
		
		
		holder.tvWorkName.setText(schoolWorkItem.getDisplayText());
		convertView.setPadding(6, 10, 6, 0);
		holder.ibWrokPic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(schoolWorkItem.getTemplateName().equals("浏览器"))
				{
					Intent contractIntent = new Intent(mContext,WebSiteActivity.class);
					contractIntent.putExtra("htmlText","<script>location.href='"+schoolWorkItem.getInterfaceName()+"';</script>");
					schoolWorkItem.setInterfaceName(schoolWorkItem.getInterfaceName().replace("\\", "/"));
					String[] loginUrl=schoolWorkItem.getInterfaceName().split("mobiles");
					contractIntent.putExtra("loginUrl", loginUrl[0]+"mobiles/processcheck.php");
					contractIntent.putExtra("title", schoolWorkItem.getDisplayText());
					mContext.startActivity(contractIntent);
				}
				else if(schoolWorkItem.getTemplateName().equals("文件下载"))
				{
					Intent contractIntent = new Intent(mContext,CourseClassActivity.class);
					contractIntent.putExtra("title", schoolWorkItem.getWorkText());
					contractIntent.putExtra("display", schoolWorkItem.getDisplayText());
					mContext.startActivity(contractIntent);
				}
				else
				{
					Intent intent = new Intent(mContext, SchoolActivity.class);
					intent.putExtra("title", schoolWorkItem.getWorkText());
					intent.putExtra("display", schoolWorkItem.getDisplayText());
					intent.putExtra("interfaceName",schoolWorkItem.getInterfaceName());
					intent.putExtra("templateName",schoolWorkItem.getTemplateName());
					mContext.startActivity(intent);
				}
			}
		});
		return convertView;
	}

	class ViewHolder {
		ImageView ibWrokPic;
		TextView tvWorkName;
		BadgeView badge;
	}
}
