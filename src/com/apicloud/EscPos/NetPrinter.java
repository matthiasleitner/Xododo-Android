package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.other.Helper;
import com.apicloud.other.QRCodeUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * ip��ӡ��
 * @author Administrator
 *
 */
public class NetPrinter extends BluetoothPrinter {
	

	/**
	 * ��ӡ�� Socket �˿ںţ�Port)��Ĭ��ֵΪ 9100
	 */
	public int SocketPort = 9100;

	Socket mSocket;
	/**
	 * 
	 * @param ipAddr ip��ַ
	 */
	public NetPrinter(String ipAddr)
	{
		super(ipAddr);
	}
	
	
	@Override
	void checkDevicePinState() throws Exception {
		//����Ҫ������״̬
	}


	@Override
	OutputStream getSocketOutputStream() throws Exception {
		Helper.Log("NetPrinter getSocketStream", "��������");
		mSocket = new Socket();
		SocketAddress socAddress = new InetSocketAddress(mAddr, SocketPort); 
		//�������ӳ�ʱʱ�䣬��λ����
		mSocket.connect(socAddress, 1500);
		OutputStream os = mSocket.getOutputStream();
		Helper.Log("NetPrinter getSocketStream", "���ӳɹ�");
		return os;
	}
	
	@Override
	InputStream getSocketInputStream() throws Exception
	{
		return mSocket.getInputStream();
	}
	@Override
	void closeSocket() throws IOException {
		mSocket.close();
	}


}
