package com.apicloud.EscPos;

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

	static BluetoothDevice Device;
	String mAddr;
	public BluetoothPrinter(String bluetoothAddr)
	{
		mAddr = bluetoothAddr;
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
		QRCodeUtil qrutil = new QRCodeUtil();
		Bitmap bitmap = qrutil.createQRImage(code, width, height, null);
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
	 */
	void checkDeviceState(){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (Device == null) {
			Device = adapter.getRemoteDevice(mAddr);
		}
		
		if (Device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				Comp_BluetoothDeviceManager.SetPin(Device.getClass(), Device, "0000");
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
	 * 往设备写入数据
	 * @param data1 第一个数据包
	 * @param data2 第二个数据包（可能为null）
	 * @throws IOException 
	 */
	void write2Device(byte[] data1 , byte[] data2) throws Exception
	{
		//检查设备是否配对
		checkDeviceState();
		int errorCount = 0;
		while (true) {
			try {
				BluetoothSocket socket = Device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
				socket.connect();
				OutputStream printStream = socket.getOutputStream();

				printStream.write(data1);

				if(data2 != null)
				printStream.write(data2);
				
				// 关闭 socket
				printStream.close();
				socket.close();
				break;
			} catch (Exception e) {
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
