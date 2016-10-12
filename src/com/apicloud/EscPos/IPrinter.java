package com.apicloud.EscPos;

import java.io.IOException;
import java.net.UnknownHostException;

public interface IPrinter {
	/**
	 * ��ӡ
	 * @param content ����
	 * @param copyNum ��ӡ����
	 */
	void print(String content,int copyNum) throws Exception;
	
	
	/**
	 * ��ӡ��ά��
	 * @param code ����
	 * @param width ��ά��Ŀ��
	 * @param height ��ά��ĸ߶�
	 */
	void printQRCode(String code,int width,int height) throws Exception;
	
	/**
	 * ��Ǯ��
	 * @throws IOException 
	 */
	void openCashBox() throws Exception;
}
