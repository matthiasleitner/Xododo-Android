package com.apicloud.moduleEcsPrint;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class Comp_BluetoothPrint {

	// 定义一个静态 Map，保存以蓝牙地址为 Key 的打印机对象实例
	private static Map<String, Comp_BluetoothPrint> mInstanceMap = null;

	// 静态方法，通过蓝牙地址获取打印机对象
	public static Comp_BluetoothPrint GetInstance(String bluetoothAddr) {
		if (mInstanceMap == null) {
			mInstanceMap = new HashMap<String, Comp_BluetoothPrint>();
		}

		if (mInstanceMap.containsKey(bluetoothAddr)) {
			return mInstanceMap.get(bluetoothAddr);
		} else {
			Comp_BluetoothPrint instance = new Comp_BluetoothPrint(bluetoothAddr);
			mInstanceMap.put(bluetoothAddr, instance);
			return instance;
		}
	}
	
	// 关闭打印机连接，并释放所有打印机对象
	public static void DisposeAllInstance() {
		
		if (mInstanceMap != null) {
			
			// 关闭蓝牙连接，释放打印机所占用资源
			for(Comp_BluetoothPrint printer: mInstanceMap.values()) {
				printer.CancelAndClear(0);
			}
			
			// 清空打印机实例字典，释放对象
			mInstanceMap.clear();
		}
	}

	// 定义一个字符串，保存蓝牙打印机地址
	private String mBluetoothAddr = null;

	// 私有构造函数（用户不能直接创建对象），构造指定蓝牙地址的打印机对象
	private Comp_BluetoothPrint(String bluetoothAddr) {
		mBluetoothAddr = bluetoothAddr;
	}

	// 获取打印机对象的蓝牙地址
	public String GetBuletoothAddr() {
		return mBluetoothAddr;
	}
	
	// 判断是否为合法 MAC 地址
	static public boolean IsMacAddress(String macAddr) {
		Pattern pattern = Pattern.compile("/b(?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}/b");
		Matcher matcher = pattern.matcher(macAddr);   
		return matcher.matches();  
	}

	// 蓝牙串口服务 UUID
	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	// 定义一个变量，保存蓝牙设备
	private BluetoothDevice mDevice = null;

	public void ConnectAndSendData(final byte[] data, final int copyNum) {

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (mDevice == null) {
			mDevice = adapter.getRemoteDevice(mBluetoothAddr);

			if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
				try {
					Comp_BluetoothDeviceManager.SetPin(mDevice.getClass(), mDevice, "0000");
					Comp_BluetoothDeviceManager.createBond(mDevice.getClass(), mDevice);
				} catch (Exception e) {
					try {
						Comp_BluetoothDeviceManager.SetPin(mDevice.getClass(), mDevice, "1234");
						Comp_BluetoothDeviceManager.createBond(mDevice.getClass(), mDevice);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		Thread printThread = new Thread() {
			@Override
			public void run() {
				
				// 加上同步锁，使打印任务排队执行
				synchronized(Comp_BluetoothPrint.class) {
					
					// 连续连接不同的蓝牙打印机会出现问题，需要在首次连接打印机前暂停3秒钟
					if(mSocket == null) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}  
					}
					
					// 如果打印时出现异常，最多有三次重试的机会
					while (mRetryTimes++ < 3) {
						try {
							// 尝试发送数据到打印机
							TryPrint(data, copyNum);
							break;
						} catch (Exception e) {
							// 如果出现异常，放弃打印，并还原打印状态
							CancelAndClear(mRetryTimes + 1);
						}
					}
					
					if(mRetryTimes > 3) {
						// ToDo: 发送打印失败通知
					}
					
					// 将失败统计次数清零
					mRetryTimes = 0;
				}
			}
		};
		printThread.start();
		
	}
	
	// 打印过程中保存打印状态
	private BluetoothSocket mSocket = null;
	private boolean mIsConnected = false;
	private int mRetryTimes = 0;
	private OutputStream mOutputStream = null;
	
	// 尝试连接蓝牙打印机，并发送数据
	private void TryPrint(byte[] data, int copyNum) throws Exception {
		
		if (mSocket == null) {
			mSocket = mDevice.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		}

		if (mSocket != null && !mIsConnected) {
			mSocket.connect();
			mIsConnected = true;
		}
		
		if (mIsConnected) {
			mOutputStream = mSocket.getOutputStream();
			
			do { 
				mOutputStream.write(data, 0, data.length);
				mOutputStream.flush();
			} while (--copyNum > 0);
		}
	}

	// 放弃打印，并还原打印状态
	private void CancelAndClear(int sleepSecond) {
		
		mIsConnected = false;
		
		if (mOutputStream != null) {
			try {
				mOutputStream.close();
			} catch (IOException e) {
			}
			mOutputStream = null;
		}
		
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
			}
			mSocket = null;
		}		
		
		if(sleepSecond > 0) {
			try {
				Thread.sleep(1000 * sleepSecond);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}  
		}
	}
}
