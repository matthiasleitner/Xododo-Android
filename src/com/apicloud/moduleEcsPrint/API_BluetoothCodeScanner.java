package com.apicloud.moduleEcsPrint;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.apicloud.EscPos.BluetoothScanner;
import com.apicloud.EscPos.OnReceiveListener;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class API_BluetoothCodeScanner extends UZModule {
	static Context m_Context;
	public static Handler handler;
	public API_BluetoothCodeScanner(UZWebView webView) {
		super(webView);
		handler = new Handler();
		m_Context = webView.getContext();
		
	}
	
	/**
	 * 设置蓝牙扫描枪的回调事件
	 * @param moduleContext
	 */
	public void jsmethod_setScannerCallback(final UZModuleContext moduleContext){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String addr = moduleContext.optString("addr");
				String pin = moduleContext.optString("pin" , "0000");
				BluetoothScanner scanner = new BluetoothScanner(addr,pin);
				//注册扫描枪回调事件
				scanner.setOnReceiveListener(new OnReceiveListener() {
					
					@Override
					public void OnReceiveCode(Object sender, String code) {
						//接收到条码信息
						moduleContext.success(code, false, false);
					}
					
					@Override
					public void OnError(Object sender, String errMessage) {
						//发生错误
						JSONObject errObj = new JSONObject();
						try {
							errObj.put("error", errMessage);
						} catch (JSONException e1) {
						}
						moduleContext.error(null, errObj, true);
					}
				});
				try {
					//开启蓝牙扫描枪
					scanner.startListen();
				} catch (Exception e) {
					JSONObject errObj = new JSONObject();
					try {
						errObj.put("error", e.getMessage());
					} catch (JSONException e1) {
					}
					moduleContext.error(null, errObj, true);
				}
			}
		}).start();
		
	}

}
