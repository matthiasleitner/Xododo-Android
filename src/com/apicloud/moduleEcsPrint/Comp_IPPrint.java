package com.apicloud.moduleEcsPrint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comp_IPPrint {

	// 打印机 IP 地址
	public String mIPAddr = null;
		
	// 打印机 Socket 端口号（Port)，默认值为 9100
	public int mSocketPort = 9100;
	
	// 构造函数，保存打印机 IP 地址
	public Comp_IPPrint(String ipAddr) {
		mIPAddr = ipAddr;
	}
	 
	// 判断是否为合法 IP 地址
	static public boolean IsIPAddress(String ipAddr) {
		String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";;
		Pattern pattern = Pattern.compile(ip);   
		Matcher matcher = pattern.matcher(ipAddr);   
		return matcher.matches();   

	}
	
	// 连接打印机，并发送打印数据
	public void ConnectAndSendData(final byte[] data, final int copyNum) {
		
		Thread printThread = new Thread() {
			
			@Override
			public void run() {
				
				// 加上同步锁，使打印任务排队执行
				synchronized(Comp_IPPrint.class) {  
					try {
						Socket socket = new Socket(mIPAddr, mSocketPort);
						OutputStream printStream = socket.getOutputStream();
	
						int repeat = copyNum < 1 ? 1 : copyNum;
						do { printStream.write(data, 0, data.length); } while (--repeat > 0);
						
						printStream.close();
						socket.close();
					} catch (UnknownHostException e) {
						// ToDo: 向主线程发送 IP 地址错误信息
					} catch (IOException e) {
						// ToDo: 向主线程发送 打印失败信息
					}
				}
			}
		};
		printThread.start();
	}
}
