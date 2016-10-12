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
	
	// ����һ���࣬�洢�������������豸��Ϣ
	public class BTDevice {
		public String name;
		public String address;
		public int bondState;
	}
	
	// ����һ�����������������������豸��Ϣ
	private List<BTDevice> mBtDevices = null;
	
	// ��̬�����������������豸��Ϣ�б�ת��Ϊ JSON �ַ���
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
	
	// ���յ��ܱ������豸�㲥��Ϣʱ��ͨ���� Handler �����̷߳�����Ϣ
	private Handler mBroadcastHandler = null;

	// ���������̷߳�����Ϣ����Ϣ����
	//public static final int SCAN_FOUND = 2;
	public static final int SCAN_FINISHED = 4;

	// ����һ����Ա�������洢������Ϣ���������߲�ѯ
	public String mErrorMsg = null;
	
	// �㲥������������ɨ�赽���ܱ������豸
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		// �������ȶԻ���
		ProgressDialog progressDialog = null;

		@Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
	        	// ��ʾ�������ȶԻ���
				progressDialog = ProgressDialog.show(context, "���Ե�...", "���������豸��...", true);
				
				// ��ʼ���豸��Ϣ�б�
				if(mBtDevices == null) {
					mBtDevices = new ArrayList<BTDevice>();
				}
				mBtDevices.clear();
			}	        
	        // �����豸�����ֵ�ʱ����յ� action == BluetoothDevice.ACTION_FOUND �Ĺ㲥
	        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

	            //�㲥�� intent �������һ�� BluetoothDevice ����
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

	            //����������һ�� ListView չʾ���ֵ��豸����ôÿ�յ�һ���㲥�������һ���豸�� �豸�б� ��
	            BTDevice dInfo = new BTDevice();
	            dInfo.name = device.getName();
	            dInfo.address = device.getAddress();
	            dInfo.bondState = device.getBondState();
	            mBtDevices.add(dInfo);
	        }
	        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

	        	// �ر��������ȶԻ���
	        	progressDialog.dismiss();
	        	
	        	if(mBroadcastHandler != null) {
	        		// ��ȡһ��Message�������� whatΪ SCAN_FINISHED ��ʾ�������
	        		Message msg = Message.obtain();
	        		msg.obj = mBtDevices;
	        		msg.what = SCAN_FINISHED;
	        		// ���������Ϣ����Ϣ������
	        		mBroadcastHandler.sendMessage(msg);
	        	}
	        	
	        	// ȡ��ע��Ĺ㲥����
	        	context.unregisterReceiver(mReceiver);
	        }	        
	    }
	};
	
	public void ScanBluetoothDevices(Context context, Handler handler) {
		// ��ȡ�����������豸ʵ��
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// ����豸��֧��������������������δ���ã����ô�����Ϣ������
		if (adapter == null || !adapter.isEnabled()) {
			mErrorMsg = "���豸��֧����������δ�����������ܣ����ȴ���������";
			return;
		}
		
		// �����ǰ�������������豸��ֹͣ��ǰ������
		if (adapter.isDiscovering()) {
			adapter.cancelDiscovery();
		}
		adapter.cancelDiscovery();
		// ע��㲥����
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);

		// �������̵߳���Ϣ���� Handler�������������豸ʱ�������̷߳�����Ϣ
		mBroadcastHandler = handler;

		// ��ʼ���������豸
		adapter.startDiscovery();
	}
	
	public List<BTDevice> GetBondedDevices() {
		// ����һ���豸��Ϣ�б�
		List<BTDevice> devices = new ArrayList<BTDevice>();

		// ��ȡ�����������豸ʵ��
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		// ����豸��֧��������������������δ���ã����ô�����Ϣ������
		if (adapter == null || !adapter.isEnabled()) {
			mErrorMsg = "���豸��֧����������δ�����������ܣ����ȴ���������";
			return devices;
		}

		// ��ȡ�Ѱ󶨵��豸����
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

		// ����а󶨵��豸
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {

				// �Ѱ��豸����Ϣ��ӵ��б�
				BTDevice dInfo = new BTDevice();
				dInfo.name = device.getName();
				dInfo.address = device.getAddress();
				dInfo.bondState = device.getBondState();
				devices.add(dInfo);
			}
		}
		// ���ذ��豸��Ϣ�б�
		return devices;
	}
	
	// �Զ��������Pinֵ
	static public boolean SetPin(Class btClass, BluetoothDevice device, String strPin) throws Exception {
		Method autoBondMethod = btClass.getMethod("setPin", new Class[] { byte[].class });
		Boolean result = (Boolean) autoBondMethod.invoke(device, new Object[] { strPin.getBytes() });
		return result;
	}

	// ��ʼ���
	static public boolean createBond(Class btClass, BluetoothDevice device) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	// ���豸������
	static public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}
}
