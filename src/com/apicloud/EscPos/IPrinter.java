package com.apicloud.EscPos;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONObject;

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
	 * ��ӡͼƬ
	 * @param base64  base64����
	 */
	void printImage(String base64) throws Exception;
	/**
	 * ��Ǯ��
	 * @throws IOException 
	 */
	void openCashBox() throws Exception;
	
	/**
	 * ���ش�ӡ��״̬
	 * @return
	 * @throws Exception
	 */
	JSONObject getStatus() throws Exception;
}
