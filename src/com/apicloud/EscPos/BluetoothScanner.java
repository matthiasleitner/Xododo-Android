package com.apicloud.EscPos;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
/**
 * 蓝牙扫描枪，蓝牙打印机要设为串口模式，如果是键盘模式，可能会有问题
 * @author Administrator
 *
 */
public class BluetoothScanner implements IScanner {

	// 蓝牙串口服务 UUID，
	private static final UUID SerialPortServiceClass_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	OnReceiveListener m_listener;

	String m_BluetoothAddr;
	static String TAG = "BluetoothScanner";
	BluetoothSocket m_Socket;
	BluetoothAdapter m_adapter;
	static BluetoothDevice Device;
	String m_Pin;
	public BluetoothScanner(String addr,String pin) {
		m_BluetoothAddr = addr;
		m_Pin = pin;
	}
	
	/**
	 * 检查设备是否配对
	 */
	void checkDeviceState(){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (Device == null) {
			Device = adapter.getRemoteDevice(m_BluetoothAddr);
		}
		
		if (Device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				Comp_BluetoothDeviceManager.SetPin(Device.getClass(), Device, m_Pin);
				Comp_BluetoothDeviceManager.createBond(Device.getClass(), Device);
			} catch (Exception e) {
				try {
					Comp_BluetoothDeviceManager.SetPin(Device.getClass(), Device, "1234");
					Comp_BluetoothDeviceManager.createBond(Device.getClass(), Device);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 开始监听蓝牙扫描
	 * @throws Exception 
	 */
	public void startListen() throws Exception
	{
		checkDeviceState();
		connectDevice(Device);
	}

    private void connectDevice(BluetoothDevice device) throws Exception {          
        Log.v(TAG, "start connect device " + device.getName());
        m_Socket = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);     
        m_Socket.connect();  
        //开启线程接收扫描枪数据
        new Thread(runForReceive).start();
    }    
    

    Runnable runForReceive = new Runnable() {
		
		@Override
		public void run() {
			InputStream stream = null;
			byte[] buffer = new byte[1024];
			while(m_Socket != null)
			{
				try
				{
					if(stream == null)
					{
						stream = m_Socket.getInputStream();
					}
					int readed = stream.read(buffer);
					if(readed > 0)
					{
						byte[] result = new byte[readed];
						System.arraycopy(buffer, 0, result, 0, readed);
						String code = new String(result,"UTF-8").trim();
						
						if(m_listener != null)
			        	{
			        		m_listener.OnReceiveCode(BluetoothScanner.this, code);
			        	}
					}
					
					Thread.sleep(100);
				}
				catch(Exception ex)
				{
					if(m_Socket != null)//如果是null，那应该是socket close的时候引发的异常，可以忽略
					{
						close();
						ex.printStackTrace();
			        	if(m_listener != null)
			        	{
			        		m_listener.OnError(BluetoothScanner.this, ex.getMessage());
			        	}
					}
		        	return;
				}
			}
		}
	};
    
	@Override
	public void setOnReceiveListener(OnReceiveListener listener) {
		m_listener = listener;
	}

	@Override
	public void close() {
		try
		{
			if (m_Socket != null) {
				BluetoothSocket socket = m_Socket;
				m_Socket = null;
				socket.close();
				
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
        	
		}
	}

}
