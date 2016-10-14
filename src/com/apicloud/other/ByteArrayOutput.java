package com.apicloud.other;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 * ByteArrayOutputStream��һ���̳��࣬��Ҫ�������ж����һ��д����ַ��Ƿ��ǻ��з�
 * @author Administrator
 *
 */
public class ByteArrayOutput extends ByteArrayOutputStream {
	@Override
	public synchronized void write(int oneByte) {
		LastIsLF = (oneByte == 10);
		super.write(oneByte);
	}
	/**
	 * ���һ���ֽ��Ƿ��ǻ��з�
	 */
	public boolean LastIsLF = false;
	@Override
	public void write(byte[] buffer) {
		if(buffer.length > 0)
		{
			LastIsLF = (buffer[buffer.length - 1] == 10);
		}
		try {
			super.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
