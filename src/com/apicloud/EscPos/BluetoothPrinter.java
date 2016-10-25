package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.apicloud.other.Helper;
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
	public JSONObject getStatus() throws Exception {
		// 获取ESC命令
		CommandBuilder cmdBuilder = new CommandBuilder();
		JSONObject result = new JSONObject();
		// 检查设备是否配对
		checkDevicePinState();
		// 打开socket，获取OutputStream
		OutputStream printStream = getSocketOutputStream();
		InputStream readStream = getSocketInputStream();

		// 返回打印机状态
		printStream.write(cmdBuilder.getStatus(1));
		int value = readStream.read();

		// 钱箱状态
		if ((value & (1 << 2)) == 0) {
			result.put("CashBoxStatus", "opened");
		} else {
			result.put("CashBoxStatus", "closed");
		}
		// 联机状态
		if ((value & (1 << 3)) == 0) {
			result.put("ConnectStatus", "connected");
		} else {
			result.put("ConnectStatus", "disconnected");
		}

		// 返回脱机状态如下
		printStream.write(cmdBuilder.getStatus(2));
		value = readStream.read();

		if ((value & (1 << 5)) == 0) {
			result.put("PaperStatus", "has paper");
		} else {
			result.put("PaperStatus", "no paper");
		}

		printStream.close();
		printStream.close();
		closeSocket();
		return result;
	}
	
	@Override
	public void print(String content, int copyNum) throws Exception {
		boolean hasCut = content.contains("<CUT>");
		// 获取ESC命令
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content, copyNum);
		
		byte[] data2 = null;
		if(hasCut == false)//判断是否有切纸命令
		{
			//如果有切纸的命令，不需要外加两个10的命令，也可以打印全
			//如果没有切纸的命令， 发送两个打印命令，因为缓冲区的前面有两个空行
			data2 = new byte[] { 10, 10 };
		}
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
	public void printImage(String base64) throws Exception {
		Bitmap bitmap = Helper.string2Bitmap(base64);
		CommandBuilder cmdBuilder = new CommandBuilder();
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
	 * 检查设备是否配对(继承者需要重载)
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

		Helper.Log("BluetoothPrinter bond state", state + "");
		if (state != BluetoothDevice.BOND_BONDED) {
			try {
				Helper.Log("BluetoothPrinter", "尝试绑定");
				Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, mPin);
				Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
			} catch (Exception e) {
				Helper.Log("BluetoothPrinter bond error", e.getMessage());
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
	 * 打开socket，获取OutputStream(继承者需要重载)
	 * @return
	 * @throws Exception
	 */
	OutputStream getSocketOutputStream() throws Exception
	{
		Helper.Log("BluetoothPrinter getSocketStream", "尝试连接蓝牙设备");
		mSocket = m_Device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		mSocket.connect();
		OutputStream os = mSocket.getOutputStream();
		Helper.Log("BluetoothPrinter getSocketStream", "成功连接");
		return os;
	}
	
	/**
	 * 获取InputStream(继承者需要重载)
	 * @return
	 * @throws Exception
	 */
	InputStream getSocketInputStream() throws Exception
	{
		return mSocket.getInputStream();
	}
	
	/**
	 * 关闭socket(继承者需要重载)
	 * @throws IOException
	 */
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
				OutputStream printStream = getSocketOutputStream();

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
					printStream.flush();
					//不能输出数据太快，打印机可能打印慢，太快会在打印机丢积数据，打印机的内存有限，丢积数据过多，会有数据丢失
					Thread.sleep(50);
				}

				if(data2 != null)
				printStream.write(data2); 
				
				printStream.flush();
				Helper.Log("Printer write2device", "写完数据");
				// 关闭 socket
				printStream.close();
				closeSocket();
				break;
			} catch (Exception e) {
				Helper.Log("Printer write error", "重新尝试连接打印机，并发送数据");
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
