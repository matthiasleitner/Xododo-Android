package com.apicloud.other;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;

public class Helper {
	/**
	 * 弹出对话框
	 * @param context
	 * @param handler 此参数可以为空，如果在其他线程调用，则必须传入此参数
	 * @param title
	 * @param msg
	 */
	public static void alert(final Context context,Handler handler ,final String title , final String msg) {
		if( handler != null )
		{
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					alert(context,title , msg);
				}
			});
		}
		else
		{
			alert(context,title , msg);
		}
		
	}
	
	static void alert(Context context,String title , String msg)
	{
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("确定", null);
		builder.show();
	}
}
