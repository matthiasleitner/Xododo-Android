package com.apicloud.other;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 * ByteArrayOutputStream的一个继承类，主要作用是判断最后一个写入的字符是否是换行符
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
	 * 最后一个字节是否是换行符
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
