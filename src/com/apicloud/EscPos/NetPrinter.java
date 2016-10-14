package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.other.QRCodeUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * ip打印机
 * @author Administrator
 *
 */
public class NetPrinter extends BluetoothPrinter {
	

	/**
	 * 打印机 Socket 端口号（Port)，默认值为 9100
	 */
	public int SocketPort = 9100;

	Socket mSocket;
	/**
	 * 
	 * @param ipAddr ip地址
	 */
	public NetPrinter(String ipAddr)
	{
		super(ipAddr);
	}
	
	
	@Override
	void checkDevicePinState() throws Exception {
		//不需要检查配对状态
	}


	@Override
	OutputStream getSocketStream() throws Exception {
		mSocket = new Socket();
		SocketAddress socAddress = new InetSocketAddress(mAddr, SocketPort); 
		//设置连接超时时间，单位毫秒
		mSocket.connect(socAddress, 1500);
		return mSocket.getOutputStream();
	}


	@Override
	void closeSocket() throws IOException {
		mSocket.close();
	}


}
