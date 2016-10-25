package com.apicloud.EscPos.Format;

import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import com.apicloud.other.ByteArrayOutput;

public class EscStringParser {

	ArrayList<EscStringElement> Children = new ArrayList<EscStringElement>();
	public void Parse(String text) {
		StringBuffer content = new StringBuffer(text);
		
		EscStringElement currentEle = new EscStringElement();
		while(content.length() > 0)
		{
			char _char = content.charAt(0);
			if(_char == '<' || _char == '>')
			{
				if(currentEle.Children.size() > 0)
				{
					Children.add(currentEle);
					currentEle = new EscStringElement();
				}
			}
			currentEle.Parse(content);
			Children.add(currentEle);
			currentEle = new EscStringElement();
			
		}

	}
	
	/**
	 * 获取打印命令
	 * @return
	 */
	public byte[] getBytes()
	{
		ByteArrayOutput buffer = new ByteArrayOutput();
		for( EscStringElement element : Children )
		{
			element.getBytes(buffer);
		}
		if(buffer.LastIsLF == false)
		{
			//最后一个字符应该是换行符
			buffer.write(10);
		}
		return buffer.toByteArray();
	}
	
}
