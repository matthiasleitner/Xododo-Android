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
	 * �����Ի���
	 * @param context
	 * @param handler �˲�������Ϊ�գ�����������̵߳��ã�����봫��˲���
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
	 * ��ȡϵͳ��ǰʱ��
	 * @return
	 */
	public static String getNowTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		return str;
	}
	public static String getNowTime(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String str = formatter.format(curDate);
		return str;
	}
	static void alert(Context context,String title , String msg)
	{
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("ȷ��", null);
		builder.show();
	}

	/**
	 * ����log����
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
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		
	}
}
