package com.apicloud.moduleEcsPrint;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import com.apicloud.EscPos.BluetoothPrinter;
import com.apicloud.EscPos.BluetoothScanner;
import com.apicloud.EscPos.IPrinter;
import com.apicloud.EscPos.NetPrinter;
import com.apicloud.EscPos.OnReceiveListener;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager.BTDevice;
import com.apicloud.other.Helper;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class API_PosPrinter extends UZModule {

	static Context m_Context;
	public static Handler handler;
	public API_PosPrinter(UZWebView webView) {
		super(webView);
		handler = new Handler();
		m_Context = webView.getContext();
		
	}

	// 传入 JSON 格式
	// { status: 'bonded: 只获取绑定的蓝牙设备，all(默认): 获取所有搜索到的蓝牙设备'  }
	public void jsmethod_getBluetoothPrinters(final UZModuleContext moduleContext) {

		String status = moduleContext.optString("status");
		Comp_BluetoothDeviceManager deviceManager = new Comp_BluetoothDeviceManager();

		if ("bonded".equals(status)) {
			List<BTDevice> bondedbtDevices = deviceManager.GetBondedDevices();

			if (bondedbtDevices.size() > 0) {
				String strJson = deviceManager.GetJsonOfDeviceList(bondedbtDevices);
				moduleContext.success(strJson, true, true);
				return;
			}			
		} else {
			Handler scanHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case Comp_BluetoothDeviceManager.SCAN_FINISHED:
						List<BTDevice> btDevices = (List<BTDevice>) msg.obj;

						String strJson = Comp_BluetoothDeviceManager.GetJsonOfDeviceList(btDevices);
						moduleContext.success(strJson, true, true);
						break;
					}
					super.handleMessage(msg);
				}
			};
			deviceManager.ScanBluetoothDevices(mContext, scanHandler);
		}
		
		if (deviceManager.mErrorMsg != null && deviceManager.mErrorMsg.length() > 0) {
			try {
				JSONObject errMsg = new JSONObject();
				errMsg.put("errMsg", deviceManager.mErrorMsg);
				moduleContext.error(null, errMsg, true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 传入 JSON 格式
	// { address: '要绑定的蓝牙打印机 MAC 地址', pin: '可选，绑定时的Pin码，默认为 0000' }
	public void jsmethod_createBondToPrinter(UZModuleContext moduleContext) {
		
		String deviceAddr = moduleContext.optString("address", "null");
		String strPin = moduleContext.optString("pin", "0000");
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if(adapter != null && adapter.isEnabled())
		{
			// 如果当前正在搜索蓝牙设备，停止当前的搜索
			if (adapter.isDiscovering()) {
				adapter.cancelDiscovery();
			}
			
			BluetoothDevice device = adapter.getRemoteDevice(deviceAddr);

			try {
				// 判断给定地址下的device是否已经配对
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					Comp_BluetoothDeviceManager.SetPin(device.getClass(), device, strPin);
					Comp_BluetoothDeviceManager.createBond(device.getClass(), device);
				}

				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					JSONObject ret = new JSONObject();
					ret.put("result", "ok");
					moduleContext.success(ret, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// 传入 JSON 格式
	// { address: '要解绑的蓝牙打印机 MAC 地址' }
	public void jsmethod_removeBondToPrinter(UZModuleContext moduleContext){
		
		String deviceAddr = moduleContext.optString("address", "null");

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null && adapter.isEnabled()) {
			// 如果当前正在搜索蓝牙设备，停止当前的搜索
			if (adapter.isDiscovering()) {
				adapter.cancelDiscovery();
			}

			BluetoothDevice device = adapter.getRemoteDevice(deviceAddr);

			try {
				// 判断给定地址下的device是否已经配对
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					Comp_BluetoothDeviceManager.removeBond(device.getClass(), device);
				}

				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					JSONObject ret = new JSONObject();
					ret.put("result", "ok");
					moduleContext.success(ret, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * 
	 * @param moduleContext {printerAddr:'打印机地址' , pin:'蓝牙配对码'}
	 */
	public void jsmethod_openCashBox(final UZModuleContext moduleContext)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try
				{
					String printerAddr = moduleContext.optString("printerAddr");
					String pin = moduleContext.optString("pin" , "0000");
					//创建打印机对象实例
					IPrinter ipPrinter = createPrinter(printerAddr,pin);
					ipPrinter.openCashBox();
				}
				catch(Exception e)
				{
					Helper.alert(m_Context,handler, "Error", e.getMessage());
				}
			}
		}).start();
		
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

	/**
	 * 打印二维码
	 * @param moduleContext {printerAddr: '打印机Mac地址或IP地址',pin:'蓝牙配对码' , code:'二维码内容' ,width:二维码宽,height:二维码高}
	 */
	public void jsmethod_printQRCode(final UZModuleContext moduleContext){
		// 必须在另一个线程执行，因为socket在当前线程创建不了
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String code = moduleContext.optString("code");
					String printerAddr = moduleContext.optString("printerAddr");
					String pin = moduleContext.optString("pin" , "0000");
					int width = moduleContext.optInt("width");
					int height = moduleContext.optInt("height");

					IPrinter ipPrinter = null;
					if (printerAddr.contains(":")) {
						ipPrinter = new BluetoothPrinter(printerAddr,pin);
					} else if (printerAddr.contains(".")) {
						ipPrinter = new NetPrinter(printerAddr);
					} else {
						throw new Exception("打印机地址错误");
					}
					ipPrinter.printQRCode(code, width, height);
				} catch (Exception e) {
					Helper.alert(m_Context, handler, "Error", e.getMessage());
				}
			}
		}).start();
	}
	
	/**
	 * 创建打印机实例
	 * @param address 打印机地址
	 * @param pin 配对编码，如蓝牙的配对码，如果为null，则默认0000
	 * @return
	 * @throws Exception 
	 */
	IPrinter createPrinter(String address , String pin) throws Exception
	{
		if(pin == null || pin.length() == 0)
			pin = "0000";
		
		IPrinter ipPrinter = null;
		if(address.contains(":")) {
			ipPrinter = new BluetoothPrinter(address,pin);
		}
		else if(address.contains(".")) {
			ipPrinter = new NetPrinter(address);
		}
		else
		{
			throw new Exception("打印机地址错误:" + address);
		}
		
		return ipPrinter;
	}
	
	public void jsmethod_startLog(final UZModuleContext moduleContext){
		try
		{
			Helper.startLog();
		}
		catch(Exception e)
		{
			Helper.alert(m_Context,handler, "startLog Error", e.getMessage());
		}
	}
	// 传入 JSON 格式
		// { taskList: [ printerAddr: '打印机Mac地址或IP地址',pin:'蓝牙配对号，可以为null' content: '打印内容', copyNum '可选，打印次数' ] }
	public void jsmethod_printOnSpecifiedPrinters(final UZModuleContext moduleContext){
		Helper.Log("printOnSpecifiedPrinters", "enter");
		//必须在另一个线程执行，因为socket在当前线程创建不了
		new Thread(new Runnable() {

			@Override
			public void run() {
				JSONArray taskList = moduleContext.optJSONArray("taskList");
				
				for(int i=0; i< taskList.length(); i++) {
					try {
						JSONObject jo = taskList.getJSONObject(i);
						String printerAddr = jo.getString("printerAddr");
						String content = jo.getString("content");
						String pin = jo.optString("pin" , "0000");
						int copyNum = jo.optInt("copyNum");

						Helper.Log("printOnSpecifiedPrinters printerAddr", printerAddr);
						Helper.Log("printOnSpecifiedPrinters pin", pin);
						Helper.Log("printOnSpecifiedPrinters content", content);
						
						//创建打印机对象实例
						IPrinter ipPrinter = createPrinter(printerAddr,pin);
						ipPrinter.print(content, copyNum);
						
						//如果不是最后一个打印任务，稍微停一停，让打印机自己缓一缓，否则打印机可能会出现2-3秒的反应迟钝
						if( i < taskList.length() - 1 )
						{
							Thread.sleep(50);
						}
					} catch (Exception e) {
						String err = e.getMessage();
						Helper.Log("printOnSpecifiedPrinters Error", err);
						Helper.alert(m_Context,handler, "Error", err);
						return;
					}

				}
			}
		}).start();
		
		

	}
	
//	public void jsmethod_testPrinter(UZModuleContext moduleContext){
//		
//	}
//	
//	public void jsmethod_getPrinterStatus(UZModuleContext moduleContext){
//		
//	}
//	
//	public void jsmethod_printImage(UZModuleContext moduleContext){
//		
//	}
}
