package com.apicloud.moduleEcsPrint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class Comp_BluetoothDeviceManager {

	public Comp_BluetoothDeviceManager() {		
	}
	
	// 定义一个类，存储搜索到的蓝牙设备信息
	public class BTDevice {
		public String name;
		public String address;
		public int bondState;
	}
	
	// 定义一个链表，保存搜索到的蓝牙设备信息
	private List<BTDevice> mBtDevices = null;
	
	// 静态辅助函数，将蓝牙设备信息列表转换为 JSON 字符串
	public static String GetJsonOfDeviceList(List<BTDevice> deviceList) {
		JSONArray jsonArr = new JSONArray();
		for (BTDevice device : deviceList) {
			JSONObject jo = new JSONObject();
			try {
				jo.put("name", device.name);
				jo.put("address", device.address);
				jo.put("bondState", device.bondState);
				jsonArr.put(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonArr.toString();
	}
	
	// 接收到周边蓝牙设备广播消息时，通过该 Handler 向主线程发送消息
	private Handler mBroadcastHandler = null;

	// 定义向主线程发送消息的消息类型
	//public static final int SCAN_FOUND = 2;
	public static final int SCAN_FINISHED = 4;

	// 定义一个成员变量，存储错误信息，供调用者查询
	public String mErrorMsg = null;
	
	// 广播接收器，接收扫描到的周边蓝牙设备
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		// 搜索进度对话框
		ProgressDialog progressDialog = null;

		@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	// 显示搜索进度对话框
				progressDialog = ProgressDialog.show(context, "请稍等...", "搜索蓝牙设备中...", true);
				
				// 初始化设备信息列表
				if(mBtDevices == null) {
					mBtDevices = new ArrayList<BTDevice>();
				}
				mBtDevices.clear();
			}	        
	        // 当有设备被发现的时候会收到 action == BluetoothDevice.ACTION_FOUND 的广播
	        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

	            //广播的 intent 里包含了一个 BluetoothDevice 对象
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

	            //假设我们用一个 ListView 展示发现的设备，那么每收到一个广播，就添加一个设备到 设备列表 里
	            BTDevice dInfo = new BTDevice();
	            dInfo.name = device.getName();
	            dInfo.address = device.getAddress();
	            dInfo.bondState = device.getBondState();
	            mBtDevices.add(dInfo);
	        }
	        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

	        	// 关闭搜索进度对话框
	        	progressDialog.dismiss();
	        	
	        	if(mBroadcastHandler != null) {
	        		// 获取一个Message对象，设置 what为 SCAN_FINISHED 表示搜索完成
	        		Message msg = Message.obtain();
	        		msg.obj = mBtDevices;
	        		msg.what = SCAN_FINISHED;
	        		// 发送这个消息到消息队列中
	        		mBroadcastHandler.sendMessage(msg);
	        	}
	        	
	        	// 取消注册的广播监听
	        	context.unregisterReceiver(mReceiver);
	        }	        
	    }
	};
	
	public void ScanBluetoothDevices(Context context, Handler handler) {
		// 获取蓝牙适配器设备实例
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// 如果设备不支持蓝牙，或者蓝牙功能未启用，设置错误信息并返回
		if (adapter == null || !adapter.isEnabled()) {
			mErrorMsg = "该设备不支持蓝牙或者未启用蓝牙功能，请先打开蓝牙功能";
			return;
		}
		
		// 如果当前正在搜索蓝牙设备，停止当前的搜索
		if (adapter.isDiscovering()) {
			adapter.cancelDiscovery();
		}
		adapter.cancelDiscovery();
		// 注册广播监听
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);

		// 保存主线程的消息处理 Handler，搜索到蓝牙设备时，向主线程发送消息
		mBroadcastHandler = handler;

		// 开始搜索蓝牙设备
		adapter.startDiscovery();
	}
	
	public List<BTDevice> GetBondedDevices() {
		// 构造一个设备信息列表
		List<BTDevice> devices = new ArrayList<BTDevice>();

		// 获取蓝牙适配器设备实例
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// 如果设备不支持蓝牙，或者蓝牙功能未启用，设置错误信息并返回
		if (adapter == null || !adapter.isEnabled()) {
			mErrorMsg = "该设备不支持蓝牙或者未启用蓝牙功能，请先打开蓝牙功能";
			return devices;
		}

		// 获取已绑定的设备集合
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

		// 如果有绑定的设备
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {

				// 把绑定设备的信息添加到列表
				BTDevice dInfo = new BTDevice();
				dInfo.name = device.getName();
				dInfo.address = device.getAddress();
				dInfo.bondState = device.getBondState();
				devices.add(dInfo);
			}
		}
		// 返回绑定设备信息列表
		return devices;
	}
	
	// 自动配对设置Pin值
	static public boolean SetPin(Class btClass, BluetoothDevice device, String strPin) throws Exception {
		Method autoBondMethod = btClass.getMethod("setPin", new Class[] { byte[].class });
		Boolean result = (Boolean) autoBondMethod.invoke(device, new Object[] { strPin.getBytes() });
		return result;
	}

	// 开始配对
	static public boolean createBond(Class btClass, BluetoothDevice device) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	// 与设备解除配对
	static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}
}
