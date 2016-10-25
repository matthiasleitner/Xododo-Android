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
	 * 获取标签的命令
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
				//如果上面没有标签，把字体设置为标准大小
				CommandBuilder builder = new CommandBuilder();
				buffer.write( builder.textAlign(0) );
	             //标准大小
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
	 * 把命令写入缓冲流
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
				//最后一个不是换行符，加个换行符，再切纸
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
					//最后一个不是换行符，加个换行符，再打印图片
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
					//如果是输出文字，把父节点的格式定义重新输出一次，如<A><B>hello</B></A>
					//打印hello的时候，会依次把A、B这两条命令先输出一次
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
					//结束标签
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
					//开始标签，获取标签名
					while(_char != '>' && content.length() > 1)
					{
						TagName += _char;
						content.deleteCharAt(0);
						_char = content.charAt(0);
					}
					content.deleteCharAt(0);
					if(TagName.equalsIgnoreCase("BR") || TagName.equalsIgnoreCase("CUT") )
					{
						//换行符，直接返回
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
