package com.dandian.admissiondemo;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

import org.apache.http.client.HttpClient;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.dandian.admissiondemo.activity.LoginActivity;
import com.dandian.admissiondemo.activity.WebSiteActivity;
import com.dandian.admissiondemo.base.Constants;
import com.dandian.admissiondemo.db.DatabaseHelper;
import com.dandian.admissiondemo.entity.AllInfo;
import com.dandian.admissiondemo.entity.ContactsFriends;
import com.dandian.admissiondemo.entity.ContactsInfo;
import com.dandian.admissiondemo.entity.ContactsMember;
import com.dandian.admissiondemo.entity.Student;
import com.dandian.admissiondemo.entity.User;
import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.Base64;
import com.dandian.admissiondemo.util.FileUtility;
import com.dandian.admissiondemo.util.PrefUtility;
import com.dandian.admissiondemo.util.ZLibUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class CampusApplication extends Application {
	private HttpClient httpClient;
	private Map<String,ContactsMember> linkManDic;//所有联系人
	private List<ContactsFriends>  linkGroupList;//联系人组
	private Map<String,List<Student>>  studentDic;//所带学生
	private User loginUserObj; 
	private DatabaseHelper database;
	
	public void reLogin()
	{
		Intent intent = new Intent(this,
				LoginActivity.class);
		
		loginUserObj=null;
		PrefUtility.put(Constants.PREF_LOGIN_PASS, "");
		PrefUtility.put(Constants.PREF_CHECK_CODE, "");
		WebSiteActivity.loginDate=null;
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		System.exit(0);
	}
	public Map<String, ContactsMember> getLinkManDic() {
		if(linkManDic==null)
		{
			getLinkManFromPref();
		}
		if(linkManDic==null)
			reLogin();
		
		return linkManDic;
	}

	public void setLinkManDic(Map<String, ContactsMember> linkManDic) {
		this.linkManDic = linkManDic;
	}

	public List<ContactsFriends> getLinkGroupList() {
		/*
		if(linkGroupList==null)
		{
			getLinkManFromPref();
			
		}
		*/
		if(linkGroupList==null)
			reLogin();
		return linkGroupList;
	}

	public void setLinkGroupList(List<ContactsFriends> linkGroupList) {
		this.linkGroupList = linkGroupList;
	}

	public Map<String, List<Student>> getStudentDic() {
		if(studentDic==null)
		{
			getStudentFromPref();			
		}
		return studentDic;
	}

	public void setStudentDic(Map<String, List<Student>> studentDic) {
		this.studentDic = studentDic;
	}
	private User getUserByDao()
	{
		Dao<User, Integer> userDao;
		User user=null;
		 try {
				userDao = getHelper().getUserDao();
				user=userDao.queryBuilder().queryForFirst();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return user;
	}
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		}
		return database;
	}

	public void setLoginUserObj(User loginUserObj) {
		this.loginUserObj = loginUserObj;
		
	}
	public User getLoginUserObjNull() {
		/*
		if(loginUserObj==null)
		{
			loginUserObj=getUserByDao();
		}
		*/
		return loginUserObj;
	}
	public User getLoginUserObj() {
		
		if(loginUserObj==null)
		{
			loginUserObj=getUserByDao();
		}
		
		return loginUserObj;
	}
	private void getLinkManFromPref()
	{
		String str=PrefUtility.get(Constants.PREF_INIT_CONTACT_STR,"");
		byte[] contact64byte = null;
		String resultContact = "";
		try {
			if (AppUtility.isNotEmpty(str)) {
				contact64byte = Base64.decode(str.getBytes("GBK"));
				resultContact = ZLibUtils.decompress(contact64byte);
				Object obj=JSONValue.parseStrict(resultContact);
				ContactsInfo contacts = new ContactsInfo(obj);
				if (contacts != null) {
					
					setLinkManDic(contacts.getLinkManDic());
					setLinkGroupList(contacts.getContactsFriendsList());
					
				}
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void getStudentFromPref()
	{
		String str=PrefUtility.get(Constants.PREF_INIT_DATA_STR,"");
		byte[] contact64byte = null;
		String resultContact = "";
		try {
			if (AppUtility.isNotEmpty(str)) {
				contact64byte = Base64.decode(str.getBytes("GBK"));
				resultContact = ZLibUtils.decompress(contact64byte);
				net.minidev.json.JSONObject obj=(net.minidev.json.JSONObject) JSONValue.parseStrict(resultContact);
				AllInfo allInfo = new AllInfo(obj);
				setStudentDic(allInfo.getStudentList());
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		AppUtility.setContext(this);
		FileUtility.cacheDir=this.getExternalFilesDir(null).getAbsolutePath();
		FileUtility.creatSDDir(FileUtility.SDPATH);
		updateTable();
	}

	public static Context getContext() {
		return AppUtility.getContext();
		
	}

	private void updateTable()
	{
		/*
		updateColumn(getHelper().getWritableDatabase(), "ChatFriend", "hostid", "varchar", "''");
		updateColumn(getHelper().getWritableDatabase(), "ChatMsg", "hostid", "varchar", "''");
		updateColumn(getHelper().getWritableDatabase(), "ChatMsg", "remoteimage", "varchar", "''");
		updateColumn(getHelper().getWritableDatabase(), "ChatMsg", "sendstate", "varchar", "''");
		updateColumn(getHelper().getWritableDatabase(), "ChatMsg", "msg_id", "varchar", "''");
		updateColumn(getHelper().getWritableDatabase(), "AlbumMsgInfo", "toName", "varchar", "''");
		
		try {
			TableUtils.createTable(getHelper().getConnectionSource(), ChatMsgDetail.class);
		} catch (SQLException e) {
			
		}*/
	}
	private void updateColumn(SQLiteDatabase db, String tableName,
            String columnName, String columnType, Object defaultField) {
    try {
            if (db != null) {
                    Cursor c = db.rawQuery("SELECT * from " + tableName
                                    + " limit 1 ", null);
                    boolean flag = false;

                    if (c != null) {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                    if (columnName.equalsIgnoreCase(c.getColumnName(i))) {
                                            flag = true;
                                            break;
                                    }
                            }
                            if (flag == false) {
                                    String sql = "alter table " + tableName + " add "
                                                    + columnName + " " + columnType + " default "
                                                    + defaultField;
                                    db.execSQL(sql);
                            }
                            c.close();
                    }
            }
    } catch (Exception e) {
            e.printStackTrace();
    }
}
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		AQUtility.cleanCache(AQUtility.getCacheDir(this, AQuery.CACHE_DEFAULT), 0, 0);
		BitmapAjaxCallback.clearCache();
		FileUtility.deleteFileFolder(FileUtility.getCacheDir());
		FileUtility.deleteFileFolder(FileUtility.creatSDDir("download"));
		//FileUtility.deleteFileFolder(FileUtility.creatSDDir("相册"));
		//FileUtility.deleteFileFolder(FileUtility.creatSDDir("课件"));
	
		Runtime.getRuntime().gc();
		this.shutdownHttpClient();
		
	}
	

	private void shutdownHttpClient() {
		if (httpClient != null && httpClient.getConnectionManager() != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}


	public HttpClient getHttpClient() {
		return httpClient;
	}

	public static String getVersion() {
	    try {
	        PackageManager manager = getContext().getPackageManager();
	        PackageInfo info = manager.getPackageInfo(getContext().getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

}
