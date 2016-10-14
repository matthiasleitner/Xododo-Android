package com.apicloud.EscPos.Format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;

import com.apicloud.EscPos.CommandBuilder;
import com.apicloud.other.ByteArrayOutput;
import com.apicloud.other.QRCodeUtil;

public class EscStringElement  {
	String m_tagName = "";
	public String Text = "";
		
	static Hashtable<String, byte[]> Commands = null;
	EscStringElement()
	{
		if(Commands == null)
		{
			 Commands = new Hashtable<String, byte[]>();
			CommandBuilder builder = new CommandBuilder();
			Commands.put("B", builder.bold(true));
			Commands.put("C", builder.textAlign(1));
			Commands.put("A", builder.fontSize(2));
			Commands.put("L", builder.fontSizeSetHeight(1));
			Commands.put("T", builder.fontSize(4));
		}
	}
	
	/**
	 * 把命令写入缓冲流
	 * @return
	 */
	public void getBytes(ByteArrayOutput buffer)
	{
		if( m_tagName.equalsIgnoreCase("br") )
		{
			buffer.write(10);
		}
		else if( m_tagName.equalsIgnoreCase("CUT") )
		{
			if( buffer.LastIsLF == false )
			{
				//最后一个不是换行符，加个换行符，再切纸
				buffer.write(10);
			}
			CommandBuilder builder = new CommandBuilder();
			byte[] data = builder.feedPaperCutAll();
			buffer.write( data);
		}
		else if( m_tagName.equalsIgnoreCase("QR") )
		{
			if( buffer.LastIsLF == false )
			{
				//最后一个不是换行符，加个换行符，再打印图片
				buffer.write(10);
			}
			CommandBuilder builder = new CommandBuilder();
			Bitmap bitmap = QRCodeUtil.createQRImage(this.Text, 300, 300, null);
			byte[] data = builder.getImageBytes(bitmap);
			buffer.write( data );
      		bitmap.recycle();
		}
		else
		{
			for(int i = 0 ; i < this.m_tagName.length() ; i ++)
			{
				String name = m_tagName.substring(i, i + 1);
				byte[] cmd = Commands.get(name);
				buffer.write( cmd );
			}
			
			if(this.Text.length() > 0)
			{
				try {
					byte[] data = this.Text.getBytes("gbk");
					buffer.write( data );
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void Parse(StringBuffer content)
	{
		while(content.length() > 0)
		{
			char _char = content.charAt(0);
			if(_char == '<')
			{
				if( content.charAt(1) == '/' )
				{
					//结束标签
					content.delete(0, 2);
					while(_char != '>' && content.length() > 0)
					{
						_char = content.charAt(0);
						content.deleteCharAt(0);
					}
					return;
				}
				else if(m_tagName.length() == 0)
				{
					content.deleteCharAt(0);
					_char = content.charAt(0);
					//开始标签，获取标签名
					while(_char != '>' && content.length() > 1)
					{
						m_tagName += _char;
						content.deleteCharAt(0);
						_char = content.charAt(0);
					}
					content.deleteCharAt(0);
					if(m_tagName.equalsIgnoreCase("BR") || m_tagName.equalsIgnoreCase("CUT") )
					{
						//换行符，直接返回
						return;
					}
					continue;
				}
			}
			else
			{
				Text += _char;
				content.deleteCharAt(0);
				continue;
			}
			
			
		}
	}
	
	
}
