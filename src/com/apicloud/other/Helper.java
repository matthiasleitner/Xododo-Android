package com.apicloud.other;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.apicloud.loggers.ILog;
import com.apicloud.loggers.TextFileLog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;

public class Helper {
	static ILog logger;
	
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
	
	/**
	 * 获取系统当前时间
	 * @return
	 */
	public static String getNowTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}
	public static String getNowTime(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}
	static void alert(Context context,String title , String msg)
	{
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("确定", null);
		builder.show();
	}

	/**
	 * 启动log机制
	 * @throws Exception 
	 */
	public static void startLog() throws Exception
	{
		if(logger == null)
		{
			logger = new TextFileLog("main");
		}
	}
	
	
	public static void Log(String header , String content) 
	{
		if(logger != null)
		{
			try {
				logger.writeLine(header, content);
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		
	}
}
