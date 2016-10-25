package com.apicloud.EscPos;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;
/**
 * 蓝牙扫描枪，蓝牙打印机要设为串口模式，如果是键盘模式，可能会有问题
 * @author Administrator
 *
 */
public class BluetoothScanner implements IScanner {

	// 蓝牙串口服务 UUID，
	private UUID SerialPortServiceClass_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	OnReceiveListener m_listener;

	String m_BluetoothAddr;
	static String TAG = "BluetoothScanner";
	BluetoothSocket m_Socket;
	BluetoothDevice m_Device;
	String m_Pin;
	/**
	 * 
	 * @param addr 蓝牙地址
	 * @param pin 配对码
	 */
	public BluetoothScanner(String addr,String pin) {
		m_BluetoothAddr = addr;
		m_Pin = pin;
	}
	
	/**
	 * 检查设备是否配对
	 * @throws Exception 
	 */
	@SuppressLint("NewApi")
	void checkDeviceState() throws Exception{
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		m_Device = adapter.getRemoteDevice(m_BluetoothAddr);
		
		if(m_Device == null)
		{
			throw new Exception("无法连接蓝牙扫描枪");
		}
		
		
		if (m_Device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				boolean hr = m_Device.setPin(m_Pin.getBytes());
				hr = m_Device.createBond();
			} catch (Exception e) {
				try {
					boolean hr = m_Device.setPin("1234".getBytes());
					hr = m_Device.createBond();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
//		boolean hr = m_Device.fetchUuidsWithSdp();
//		ParcelUuid[] uuids = m_Device.getUuids();
//
//		for(int i = 0 ; i < uuids.length ; i ++)
//		{
//			ParcelUuid uuid = uuids[i];
//			SerialPortServiceClass_UUID = uuid.getUuid();
//			break;
//		}
	}
	
	/**
	 * 开始监听蓝牙扫描
	 * @throws Exception 
	 */
	public void startListen() throws Exception
	{
		checkDeviceState();
		connectDevice(m_Device);
	}

    @SuppressLint("NewApi")
	private void connectDevice(BluetoothDevice device) throws Exception {          
        Log.v(TAG, "start connect device " + device.getName());
        m_Socket = device.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);     
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
