package com.apicloud.EscPos;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.other.QRCodeUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * ip��ӡ��
 * @author Administrator
 *
 */
public class NetPrinter implements IPrinter {
	
	/**
	 *  ��ӡ�� IP ��ַ
	 */
	public String IPAddr = null;

	/**
	 * ��ӡ�� Socket �˿ںţ�Port)��Ĭ��ֵΪ 9100
	 */
	public int SocketPort = 9100;

	/**
	 * 
	 * @param ipAddr ip��ַ
	 */
	public NetPrinter(String ipAddr)
	{
		IPAddr = ipAddr;
	}
	
	public void openCashBox() throws Exception {
		CommandBuilder cmdBuilder = new CommandBuilder();
		//��ȡ��Ǯ�������
		byte[] data1 = cmdBuilder.openCashBox();
		write2Device(data1, null);
	}

	@Override
	public void print(String content,int copyNum) throws Exception{
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content , copyNum);
		
		//����������ӡ�����Ϊ��������ǰ������������
		byte[] data2 = new byte[]{10,10};
		
		write2Device(data1, data2);
	}
	
	@Override
	public void printQRCode(String code,int width,int height) throws Exception {
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		Bitmap bitmap = QRCodeUtil.createQRImage(code, width, height, null);
		byte[] data1 = cmdBuilder.getImageBytes(bitmap);
		bitmap.recycle();

		// ����������ӡ�����Ϊ��������ǰ������������
		byte[] data2 = new byte[] { 10, 10 };

		write2Device(data1, data2);
	}
	
	/**
	 * ���豸д������
	 * @param data1 ��һ�����ݰ�
	 * @param data2 �ڶ������ݰ�������Ϊnull��
	 * @throws IOException 
	 */
	void write2Device(byte[] data1 , byte[] data2) throws Exception
	{
		int errorCount = 0;
		while (true) {
			try {
				Socket socket = new Socket(IPAddr, SocketPort);
				OutputStream printStream = socket.getOutputStream();

				printStream.write(data1);

				if(data2 != null)
				printStream.write(data2);
				
				// �ر� socket
				printStream.close();
				socket.close();
				break;
			} catch (Exception e) {
				//����ʧ�ܣ����������豸�����Ӵ˴�ӡ�����������ԣ�
				try {
					errorCount++;
					//�����ظ�10��
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
