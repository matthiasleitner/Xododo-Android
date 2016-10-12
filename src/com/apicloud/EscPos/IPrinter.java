package com.apicloud.EscPos;

import java.io.IOException;
import java.net.UnknownHostException;

public interface IPrinter {
	/**
	 * 打印
	 * @param content 内容
	 * @param copyNum 打印几份
	 */
	void print(String content,int copyNum) throws Exception;
	
	
	/**
	 * 打印二维码
	 * @param code 内容
	 * @param width 二维码的宽度
	 * @param height 二维码的高度
	 */
	void printQRCode(String code,int width,int height) throws Exception;
	
	/**
	 * 打开钱箱
	 * @throws IOException 
	 */
	void openCashBox() throws Exception;
}
