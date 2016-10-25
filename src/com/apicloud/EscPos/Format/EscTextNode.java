package com.apicloud.EscPos.Format;

import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;

import com.apicloud.EscPos.CommandBuilder;
import com.apicloud.other.ByteArrayOutput;
import com.apicloud.other.QRCodeUtil;

public class EscTextNode extends EscStringElement {
	public String Text = "";
	@Override
	public void getBytes(ByteArrayOutput buffer) {
		if( this.Parent.TagName.equalsIgnoreCase("QR") )
		{
			CommandBuilder builder = new CommandBuilder();
			Bitmap bitmap = QRCodeUtil.createQRImage(this.Text, 300, 300, null);
			byte[] data = builder.getImageBytes(bitmap);
			buffer.write( data );
      		bitmap.recycle();
		}
		else
		{
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

	@Override
	public void Parse(StringBuffer content) {
		while(content.length() > 0)
		{
			char _char = content.charAt(0);
			if(_char == '<' )
			{
				return;
			}
			else
			{
				Text += _char;
				content.deleteCharAt(0);
			}
		}
	}
	
}
