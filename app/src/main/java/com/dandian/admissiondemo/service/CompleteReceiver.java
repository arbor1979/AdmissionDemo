package com.dandian.admissiondemo.service;


import com.dandian.admissiondemo.util.AppUtility;
import com.dandian.admissiondemo.util.IntentUtility;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.app.DownloadManager.Query; 
public class CompleteReceiver extends BroadcastReceiver {

	private DownloadManager downloadManager; 
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();  
        if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {  
        	AppUtility.showToastMsg(context, "下载完成");
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);                                                                                      //TODO 鍒ゆ柇杩欎釜id涓庝箣鍓嶇殑id鏄惁鐩哥瓑锛屽鏋滅浉绛夎鏄庢槸涔嬪墠鐨勯偅涓涓嬭浇鐨勬枃浠?  
            Query query = new Query();  
            query.setFilterById(id);  
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);  
            Cursor cursor = downloadManager.query(query);  
              
            int columnCount = cursor.getColumnCount();  
            String path = null;                                                                                                                                       //TODO 杩欓噷鎶婃墍鏈夌殑鍒楅兘鎵撳嵃涓?涓嬶紝鏈変粈涔堥渶姹傦紝灏辨?庝箞澶勭悊,鏂囦欢鐨勬湰鍦拌矾寰勫氨鏄痯ath  
            while(cursor.moveToNext()) {  
                for (int j = 0; j < columnCount; j++) {  
                    String columnName = cursor.getColumnName(j);  
                    String string = cursor.getString(j);  
                    if(columnName.equals("local_uri")) {  
                        path =string;  
                    }  
                    if(string != null) {  
                    	
                        System.out.println(columnName+": "+ string);  
                    }else {  
                        System.out.println(columnName+": null");  
                    }  
                }  
            }  
            cursor.close();  
            if(path!=null)
            {
        
	            if(path.startsWith("content:")) 
	            {  
	            	cursor = context.getContentResolver().query(Uri.parse(path), null, null, null, null);  
	                columnCount = cursor.getColumnCount();  
	                while(cursor.moveToNext())
	                {  
	                                    for (int j = 0; j < columnCount; j++) {  
	                                                String columnName = cursor.getColumnName(j);  
	                                                String string = cursor.getString(j);  
	                                                if(string != null) {  
	                                                     System.out.println(columnName+": "+ string);  
	                        }else {  
	                            System.out.println(columnName+": null");  
	                        }  
	                    }  
	                }  
	                cursor.close();  
	            }  
	            Intent aintent=IntentUtility.openUrl(Uri.decode(path).replace("file://", ""));
	            if(aintent!=null)
	            	IntentUtility.openIntent(context, aintent,true);
            }
              
        }else if(action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {  
              
        }  
	}

}
