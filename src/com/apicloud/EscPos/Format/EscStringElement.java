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
	public String TagName = "";
	
	public ArrayList<EscStringElement> Children = new ArrayList<EscStringElement>();
	static Hashtable<String, byte[]> Commands = null;
	public EscStringElement Parent;
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
	 * ��ȡ��ǩ������
	 * @param buffer
	 */
	void getTagBytes(ByteArrayOutput buffer)
	{
		if( TagName.equalsIgnoreCase("br") )
		{
		}
		else if( TagName.equalsIgnoreCase("CUT") )
		{
		}
		else if( TagName.equalsIgnoreCase("QR") )
		{
		}
		else
		{
			if(this.Parent == null)
			{
				//�������û�б�ǩ������������Ϊ��׼��С
				CommandBuilder builder = new CommandBuilder();
				buffer.write( builder.textAlign(0) );
	             //��׼��С
				buffer.write( builder.fontSize(1) );
				buffer.write( builder.bold(false) );
			}
			for(int i = 0 ; i < TagName.length() ; i ++)
			{
				String name = TagName.substring(i, i + 1);
				byte[] cmd = Commands.get(name);
				buffer.write( cmd );
			}
		}
	}
	
	/**
	 * ������д�뻺����
	 * @return
	 */
	public void getBytes(ByteArrayOutput buffer)
	{
		if( TagName.equalsIgnoreCase("br") )
		{
			buffer.write(10);
		}
		else if( TagName.equalsIgnoreCase("CUT") )
		{
			if( buffer.LastIsLF == false )
			{
				//���һ�����ǻ��з����Ӹ����з�������ֽ
				buffer.write(10);
			}
			CommandBuilder builder = new CommandBuilder();
			byte[] data = builder.feedPaperCutAll();
			buffer.write( data);
		}
		else if( TagName.equalsIgnoreCase("QR") )
		{
			if(this.Children.size() > 0)
			{
				if( buffer.LastIsLF == false )
				{
					//���һ�����ǻ��з����Ӹ����з����ٴ�ӡͼƬ
					buffer.write(10);
				}
				this.Children.get(0).getBytes(buffer);
			}
		}
		else
		{
			
			for(EscStringElement child : this.Children )
			{
				if( child instanceof EscTextNode )
				{
					//�����������֣��Ѹ��ڵ�ĸ�ʽ�����������һ�Σ���<A><B>hello</B></A>
					//��ӡhello��ʱ�򣬻����ΰ�A��B���������������һ��
					EscStringElement parent = this;
					ArrayList<EscStringElement> links = new ArrayList<EscStringElement>();
					while(parent != null)
					{
						links.add(0, parent);
						parent = parent.Parent;
					}
					for( EscStringElement p : links )
					{
						p.getTagBytes(buffer);
					}
				}
				child.getBytes(buffer);
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
					//������ǩ
					content.delete(0, 2);
					while(_char != '>' && content.length() > 0)
					{
						_char = content.charAt(0);
						content.deleteCharAt(0);
					}
					return;
				}
				else if(TagName.length() == 0 && this.Children.size() == 0 )
				{
					content.deleteCharAt(0);
					_char = content.charAt(0);
					//��ʼ��ǩ����ȡ��ǩ��
					while(_char != '>' && content.length() > 1)
					{
						TagName += _char;
						content.deleteCharAt(0);
						_char = content.charAt(0);
					}
					content.deleteCharAt(0);
					if(TagName.equalsIgnoreCase("BR") || TagName.equalsIgnoreCase("CUT") )
					{
						//���з���ֱ�ӷ���
						return;
					}
					continue;
				}
				else
				{
					EscStringElement strEle = new EscStringElement();
					this.Children.add(strEle);
					
					strEle.Parent = this;
					strEle.Parse(content);
				}
			}
			else
			{
				EscTextNode textEle = new EscTextNode();
				this.Children.add(textEle);
				
				textEle.Parent = this;
				textEle.Parse(content);
				continue;
			}
			
			
		}
	}
	
	
}
