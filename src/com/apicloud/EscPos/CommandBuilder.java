package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.apicloud.EscPos.Format.EscStringParser;
import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.other.QRCodeUtil;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CommandBuilder {
	/**
	 * 打印指定文本
	 * @param content 需要打印的内容
	 * @param copyNum 打印几份
	 * @return
	 * @throws IOException 
	 */
	public byte[] getPrintCmd(String content,int copyNum) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		//初始化打印机
		buffer.write(initPriner());
		//解析内容
		byte[] bs = getContentByFormat(content);
		//放入缓冲区
		for(int i = 0 ; i < copyNum ; i ++)
		{
			if( i > 0 )
			{
				//如果不是第一份，在中间插入一个空白行，形成一行间隔
				try {
					buffer.write("  \n".getBytes("gbk"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			buffer.write(bs);
		}
		
		return buffer.toByteArray();
	}
	
	/**
	 * 打开钱箱
	 * @return
	 */
	public byte[] openCashBox()
	{
		return new byte[]{27, 112,0,60,(byte)255};
	}
	
	/**
	 * 解析内容里的标签格式
	 * @param content
	 * @return
	 * @throws IOException 
	 */
	byte[] getContentByFormat(String content) throws IOException
	{
		boolean lastIsLF = false;//表示最后一个字符是否是换行符
		 ByteArrayOutputStream data = new ByteArrayOutputStream();
         String[] lines = content.split("<BR>");
         for(int i = 0; i < lines.length; i++)
         {
             String line = lines[i];
             //是否切纸
             boolean cutPage = false;
             if (line.contains("<C>"))
             {
                 line = line.replaceAll("<C>|</C>", "");
                 //居中
                 data.write( textAlign(1) );
                 //标准大小
                 data.write( fontSize(1) );
             }
             else if (line.contains("<A>"))
             {
                 line = line.replaceAll("<A>|</A>", "");
                 //居中
                 data.write( textAlign(1) );
                 
                 data.write( fontSize(2) );
             }
             else if (line.contains("<CA>"))
             {
            	 line = line.replaceAll("<CA>|</CA>", "");
            	//居中
                 data.write( textAlign(1) );
                 //2倍大小
                 data.write( fontSize(2) );
             }
             else if (line.contains("<B>"))
             {
            	 line = line.replaceAll("<B>|</B>", "");
            	//左对齐
                 data.write( textAlign(0) );
                 //标准大小
                 data.write( fontSize(1) );
                 data.write( bold(true) );
             }
             else if (line.contains("<L>"))
             {
            	 line = line.replaceAll("<L>|</L>", "");
            	//左对齐
                 data.write( textAlign(0) );
                 //字体高度增加
                 data.write( fontSizeSetHeight(1) );
             }
             else if (line.contains("<AB>"))
             {
            	 line = line.replaceAll("<AB>|</AB>", "");
            	//左对齐
                 data.write( textAlign(0) );

                 data.write( fontSize(2) );
                 data.write( bold(true) );
             }
             else if (line.contains("<AL>"))
             {
            	 line = line.replaceAll("<AL>|</AL>", "");
            	//左对齐
                 data.write( textAlign(0) );

                 data.write( fontSize(2) );
             }
             else if (line.contains("<BL>"))
             {
            	 line = line.replaceAll("<BL>|</BL>", "");
            	//左对齐
                 data.write( textAlign(0) );

                 data.write( fontSizeSetHeight(1) );
                 data.write( bold(true) );
             }
             else if (line.contains("<ABL>"))
             {
            	 line = line.replaceAll("<ABL>|</ABL>", "");
            	//左对齐
                 data.write( textAlign(0) );

                 data.write( fontSize(2) );
                 data.write( fontSizeSetHeight(1) );
                 data.write( bold(true) );
             }
             else if (line.contains("<AT>"))
             {
            	 line = line.replaceAll("<AT>|</AT>", "");
            	//左对齐
                 data.write( textAlign(0) );

                 data.write( fontSize(4) );
             }
             else if (line.contains("<QR>"))
             {
            	 line = line.replaceAll("<QR>|</QR>", "");
            	 Bitmap bitmap = QRCodeUtil.createQRImage(line, 300, 300, null);
         		data.write( getImageBytes(bitmap) );
         		bitmap.recycle();
         		line="";
             }
             else if (line.contains("<CUT>"))
             {
            	 cutPage = true;
            	 line = line.replaceAll("<CUT>", "");
             }
             else
             {
            	//左对齐
                 data.write( textAlign(0) );
                 //标准大小
                 data.write( fontSize(1) );
             }
             
             //打印内容
             try {
            	 if(line.length() > 0)
            	 {
            		 data.write(line.getBytes("gbk"));
            		 lastIsLF = false;
            	 }
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
             if(cutPage)
             {
            	 //如果最后一个字符不是换行符，加一个换行符，再切割
            	 if( lastIsLF == false )
            	 {
            		 data.write((byte)10);
            	 }
            	 data.write(feedPaperCutAll());
             }
             else
             {
            	 //打印并换行
                 data.write((byte)10);
                 lastIsLF = true;
             }
         }

      return data.toByteArray();
	}
	
	/**
	 * 初始化打印机
	 * @return
	 */
	byte[] initPriner(){
		return new byte[]{ 27 , 64 };
	}
	
	/**
	 * 文字对齐
	 * @param align 0:左对齐 1:中对齐 2:右对齐
	 * @return
	 */
	public byte[] textAlign(int align)
	{
		return new byte[]{ 27 , 97 , (byte)align };
	}
	
	/**
	 * 绘制下划线（1点宽）
	 * @param lineWidth 线宽
	 * @return
	 */
	byte[] underline(int lineWidth) {
		return new byte[]{ 27 , 45 ,(byte)lineWidth };
	}
	
	/**
	 * 字体加粗
	 * @param onOff 开启还是关闭 true:on 
	 * @return
	 */
	public byte[] bold(boolean onOff) {
		return new byte[]{ 27 , 69 , onOff ? (byte)0xF : (byte)0 };
	}
	
	/**
	 * 字体变大为标准的n倍
	 * @param num 放大倍数
	 * @return
	 */
	public byte[] fontSize(int num){
		byte realSize = 0;
		switch (num) {
		case 1:
			realSize = 0;
			break;
		case 2:
			realSize = 17;
			break;
		case 3:
			realSize = 34;
			break;
		case 4:
			realSize = 51;
			break;
		case 5:
			realSize = 68;
			break;
		case 6:
			realSize = 85;
			break;
		case 7:
			realSize = 102;
			break;
		case 8:
			realSize = 119;
			break;
		}
		byte[] result = new byte[3];
		result[0] = 29;
		result[1] = 33;
		result[2] = realSize;
		return result;
	}
	
	public byte[] fontSizeSetHeight(int num){
		byte realSize = 0;
		switch (num) {
		case 1:
			realSize = 1;
			break;
		case 2:
			realSize = 17;
			break;
		case 3:
			realSize = 34;
			break;
		case 4:
			realSize = 51;
			break;
		case 5:
			realSize = 68;
			break;
		case 6:
			realSize = 85;
			break;
		case 7:
			realSize = 102;
			break;
		case 8:
			realSize = 119;
			break;
		}
		byte[] result = new byte[3];
		result[0] = 29;
		result[1] = 33;
		result[2] = realSize;
		return result;
	}
	
	/**
	 * 字体取消倍宽倍高
	 * 
	 * @param num
	 * @return
	 */
	byte[] fontSizeSetSmall(int num) {
		byte[] result = new byte[3];
		result[0] = 27;
		result[1] = 33;

		return result;
	}
	
	/**
	 * 进纸并全部切割
	 * 
	 * @return
	 */
	public byte[] feedPaperCutAll() {
		byte[] result = new byte[4];
		result[0] = 29;
		result[1] = 86;
		result[2] = 65;
		result[3] = 0;
		return result;
	}
	
	/**
	 * 获取打印机状态
	 *  /// 返回打印机状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:一个或两个钱箱打开  1:两个钱箱都关闭
        /// 第四位：0:联机  1:脱机
        /// 第五位：固定为1
        /// 第六位：未定义
        /// 第七位：未定义
        /// 第八位：固定为0
        /// 
        /// 返回脱机状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:上盖关  1:上盖开
        /// 第四位：0:未按走纸键  1:按下走纸键
        /// 第五位：固定为1
        /// 第六位：0:打印机不缺纸  1: 打印机缺纸
        /// 第七位：0:没有出错情况  1:有错误情况
        /// 第八位：固定为0
        /// 
        /// 返回错误状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：未定义
        /// 第四位：0:切刀无错误  1:切刀有错误
        /// 第五位：固定为1
        /// 第六位：0:无不可恢复错误  1: 有不可恢复错误
        /// 第七位：0:打印头温度和电压正常  1:打印头温度或电压超出范围
        /// 第八位：固定为0
        /// 
        /// 返回传送纸状态如下：
        /// 第一位：固定为0
        /// 第二位：固定为1
        /// 第三位：0:有纸  1:纸将尽
        /// 第四位：0:有纸  1:纸将尽
        /// 第五位：固定为1
        /// 第六位：0:有纸  1:纸尽
        /// 第七位：0:有纸  1:纸尽
        /// 第八位：固定为0
	 * @param num 1:打印机状态 2:脱机状态 3:错误状态 4:传送纸状态
	 * @return
	 */
	public byte[] getStatus(int num)
	{
		byte[] result = new byte[3];
		result[0] = 16;
		result[1] = 4;
		result[2] = (byte)num;
		return result;
		
	}
	
	/**
	 * 进纸并切割（左边留一点不切）
	 * 
	 * @return
	 */
	byte[] feedPaperCutPartial() {
		byte[] result = new byte[4];
		result[0] = 29;
		result[1] = 86;
		result[2] = 66;
		result[3] = 0;
		return result;
	}
	
	/**
	 * 获取打印图片的字节数据
	 * @param bmp
	 * @return
	 */
	public byte[] getImageBytes(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			// 初始化打印机
			buffer.write(initPriner());

			// ESC * m nL nH d1…dk 选择位图模式
			// ESC * m nL nH
			byte[] escBmp = new byte[] { 0x1B, 0x2A, 0x00, 0x00, 0x00 };

			escBmp[2] = (byte) 0x21;// 8点双密度

			// nL, nH
			escBmp[3] = (byte) (width % 256);
			escBmp[4] = (byte) (width / 256);
			// 最小行间距
			buffer.write(new byte[] { 27, 51, 0 });

			int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

			bmp.getPixels(pixels, 0, width, 0, 0, width, height);

			byte[] data = new byte[24 / 8];
			// 循环图片像素打印图片
			// 循环高
			for (int i = 0; i < height; i+=24) {
				// 设置模式为位图模式
				buffer.write(escBmp);
				// 循环宽
				for (int j = 0; j < width; j++) {
					for (int k = 0; k < 24; k++) {
						if ((i + k) < height) // if within the BMP size
						{
							int h = i + k;

							int pixel = pixels[width * h + j];
							/** 获取ＲＧＢ值 **/
							int r = Color.red(pixel);
							int g = Color.green(pixel);
							int b = Color.blue(pixel);

							//data是3个字节，其实就是3个字节=24bit，就是代表24个像素，每一位0：表示不打印(白色)  1：表示打印（黑色）
							if (!(r > 150 && g > 150 && b > 150)) {
								data[k / 8] = (byte)(((int)data[k / 8]) | 1<<(7 - k%8));
							}
						}
					}
					// 一次写入一个data，24个像素
					buffer.write(data);

					data[0] = (byte) 0;
					data[1] = (byte) 0;
					data[2] = (byte) 0; // Clear to Zero.
				}
				// 换行，打印第二行
				byte[] data2 = new byte[] { 0xA };
				buffer.write(data2);
			} // data

			// 初始化一下打印机，让它恢复行间隔
			buffer.write(initPriner());
		} catch (Exception e) {

		}

		return buffer.toByteArray();
	}
	
}
