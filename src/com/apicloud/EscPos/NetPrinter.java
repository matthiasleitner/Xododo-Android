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
 * ip打印机
 * @author Administrator
 *
 */
public class NetPrinter implements IPrinter {
	
	/**
	 *  打印机 IP 地址
	 */
	public String IPAddr = null;

	/**
	 * 打印机 Socket 端口号（Port)，默认值为 9100
	 */
	public int SocketPort = 9100;

	/**
	 * 
	 * @param ipAddr ip地址
	 */
	public NetPrinter(String ipAddr)
	{
		IPAddr = ipAddr;
	}
	
	public void openCashBox() throws Exception {
		CommandBuilder cmdBuilder = new CommandBuilder();
		//获取打开钱箱的命令
		byte[] data1 = cmdBuilder.openCashBox();
		write2Device(data1, null);
	}

	@Override
	public void print(String content,int copyNum) throws Exception{
		// 获取ESC命令
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content , copyNum);
		
		//发送两个打印命令，因为缓冲区的前面有两个空行
		byte[] data2 = new byte[]{10,10};
		
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
	
	/**
	 * 往设备写入数据
	 * @param data1 第一个数据包
	 * @param data2 第二个数据包（可能为null）
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
				
				// 关闭 socket
				printStream.close();
				socket.close();
				break;
			} catch (Exception e) {
				//连接失败，可能其他设备在连接此打印机，继续重试，
				try {
					errorCount++;
					//允许重复10次
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
