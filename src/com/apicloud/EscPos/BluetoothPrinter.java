package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.apicloud.other.QRCodeUtil;
/**
 * 蓝牙打印机
 * @author Administrator
 *
 */
public class BluetoothPrinter implements IPrinter {
	// 蓝牙串口服务 UUID，
	private static final UUID SerialPortServiceClass_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	BluetoothDevice m_Device;
	protected String mAddr;
	String mPin;
	BluetoothSocket mSocket;
	/**
	 * 
	 * @param bluetoothAddr 蓝牙地址
	 * @param pin 配对密码
	 */
	public BluetoothPrinter(String bluetoothAddr,String pin)
	{
		mAddr = bluetoothAddr;
		mPin = pin;
	}
	public BluetoothPrinter(String bluetoothAddr)
	{
		mAddr = bluetoothAddr;
		mPin = "0000";
	}
	
	@Override
	public void print(String content, int copyNum) throws Exception {
		// 获取ESC命令
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content, copyNum);
		// 发送两个打印命令，因为缓冲区的前面有两个空行
		byte[] data2 = new byte[] { 10, 10 };

		write2Device(data1, data2);
	}
	
	@Override
	public void printQRCode(String code,int width,int height) throws Exception {
		// 获取ESC命令
		CommandBuilder cmdBuilder = new CommandBuilder();
		Bitmap bitmap = QRCodeUtil.createQRImage(code, width, height, null);
		byte[] data1 = cmdBuilder.getImageBytes(bitmap);
		bitmap.recycle();

		// 发送两个打印命令，因为缓冲区的前面有两个空行
		byte[] data2 = new byte[] { 10, 10 };

		write2Device(data1, data2);
	}

	@Override
	public void openCashBox() throws Exception {
		CommandBuilder cmdBuilder = new CommandBuilder();
		//获取打开钱箱的命令
		byte[] data1 = cmdBuilder.openCashBox();
		write2Device(data1, null);
	}

	/**
	 * 检查设备是否配对
	 * @throws Exception 
	 */
	void checkDevicePinState() throws Exception{
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		m_Device = adapter.getRemoteDevice(mAddr);
		
		if(m_Device == null)
		{
			throw new Exception("无法连接蓝牙打印机");
		}

		int state = m_Device.getBondState();
		if (state != BluetoothDevice.BOND_BONDED) {
			try {
				Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, mPin);
				Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
			} catch (Exception e) {
				try {
					Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, "1234");
					Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 打开socket，获取OutputStream
	 * @return
	 * @throws Exception
	 */
	OutputStream getSocketStream() throws Exception
	{
		mSocket = m_Device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		mSocket.connect();
		return mSocket.getOutputStream();
	}
	
	void closeSocket() throws IOException
	{
		mSocket.close();
	}
	
	/**
	 * 往设备写入数据
	 * @param data1 第一个数据包
	 * @param data2 第二个数据包（可能为null）
	 * @throws IOException 
	 */
	void write2Device(byte[] data1 , byte[] data2) throws Exception
	{
		//检查设备是否配对
		checkDevicePinState();
		int errorCount = 0;
		while (true) {
			try {
				//打开socket，获取OutputStream
				OutputStream printStream = getSocketStream();

				//为了防止数据过长，分行打印
				int position = 0;
				while(position < data1.length)
				{
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					//先获取一行数据
					for(int i = position ; i < data1.length ; i ++)
					{
						byte b = data1[i];
						buffer.write(b);
						if(b == 10)
						{
							break;
						}
					}
					position += buffer.size();
					//把这行数据发送到打印机
					printStream.write(buffer.toByteArray());
				}

				if(data2 != null)
				printStream.write(data2);
				
				// 关闭 socket
				printStream.close();
				closeSocket();
				break;
			} catch (Exception e) {
				checkDevicePinState();
				
				//连接失败，可能其他设备在连接此打印机，继续重试，
				try {
					errorCount++;
					//允许尝试10次
					if(errorCount > 10)
					{
						throw e;
					}
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
		}
	}
}
