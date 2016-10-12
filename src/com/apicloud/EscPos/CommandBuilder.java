package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.apicloud.moduleEcsPrint.API_PosPrinter;

import android.graphics.Bitmap;
import android.graphics.Color;

public class CommandBuilder {
	/**
	 * ��ӡָ���ı�
	 * @param content ��Ҫ��ӡ������
	 * @param copyNum ��ӡ����
	 * @return
	 * @throws IOException 
	 */
	public byte[] getPrintCmd(String content,int copyNum) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		//��ʼ����ӡ��
		buffer.write(initPriner());
		//��������
		byte[] bs = getContentByFormat(content);
		//���뻺����
		for(int i = 0 ; i < copyNum ; i ++)
		{
			if( i > 0 )
			{
				//������ǵ�һ�ݣ����м����һ���հ��У��γ�һ�м��
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
	 * ��Ǯ��
	 * @return
	 */
	public byte[] openCashBox()
	{
		return new byte[]{(byte)27, (byte)112,(byte)0,(byte)60,(byte)255};
	}
	
	/**
	 * ����������ı�ǩ��ʽ
	 * @param content
	 * @return
	 * @throws IOException 
	 */
	byte[] getContentByFormat(String content) throws IOException
	{
		boolean lastIsLF = false;//��ʾ���һ���ַ��Ƿ��ǻ��з�
		 ByteArrayOutputStream data = new ByteArrayOutputStream();
         String[] lines = content.split("<BR>");
         for(int i = 0; i < lines.length; i++)
         {
             String line = lines[i];
             //�Ƿ���ֽ
             boolean cutPage = false;
             if (line.contains("<C>"))
             {
                 line = line.replaceAll("<C>|</C>", "");
                 //����
                 data.write( textAlign(1) );
                 //��׼��С
                 data.write( fontSize(1) );
             }
             else if (line.contains("<CA>"))
             {
            	 line = line.replaceAll("<CA>|</CA>", "");
            	//����
                 data.write( textAlign(1) );
                 //2����С
                 data.write( fontSize(2) );
             }
             else if (line.contains("<B>"))
             {
            	 line = line.replaceAll("<B>|</B>", "");
            	//�����
                 data.write( textAlign(0) );
                 //��׼��С
                 data.write( fontSize(1) );
                 data.write( bold(true) );
             }
             else if (line.contains("<CUT>"))
             {
            	 cutPage = true;
            	 line = line.replaceAll("<CUT>", "");
             }
             else
             {
            	//�����
                 data.write( textAlign(0) );
                 //��׼��С
                 data.write( fontSize(1) );
             }
             
             //��ӡ����
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
            	 //������һ���ַ����ǻ��з�����һ�����з������и�
            	 if( lastIsLF == false )
            	 {
            		 data.write((byte)10);
            	 }
            	 data.write(feedPaperCutAll());
             }
             else
             {
            	 //��ӡ������
                 data.write((byte)10);
                 lastIsLF = true;
             }
         }

      return data.toByteArray();
	}
	
	/**
	 * ��ʼ����ӡ��
	 * @return
	 */
	byte[] initPriner(){
		return new byte[]{ 27 , 64 };
	}
	
	/**
	 * ���ֶ���
	 * @param align 0:����� 1:�ж��� 2:�Ҷ���
	 * @return
	 */
	byte[] textAlign(int align)
	{
		return new byte[]{ 27 , 97 , (byte)align };
	}
	
	/**
	 * �����»��ߣ�1�����
	 * @param lineWidth �߿�
	 * @return
	 */
	byte[] underline(int lineWidth) {
		return new byte[]{ 27 , 45 ,(byte)lineWidth };
	}
	
	/**
	 * ����Ӵ�
	 * @param onOff �������ǹر� true:on 
	 * @return
	 */
	byte[] bold(boolean onOff) {
		return new byte[]{ 27 , 69 , onOff ? (byte)0xF : (byte)0 };
	}
	
	/**
	 * ������Ϊ��׼��n��
	 * @param num �Ŵ���
	 * @return
	 */
	byte[] fontSize(int num){
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
	
	/**
	 * ����ȡ����������
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
	 * ��ֽ��ȫ���и�
	 * 
	 * @return
	 */
	byte[] feedPaperCutAll() {
		byte[] result = new byte[4];
		result[0] = 29;
		result[1] = 86;
		result[2] = 65;
		result[3] = 0;
		return result;
	}
	
	/**
	 * ��ֽ���и�����һ�㲻�У�
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
	 * ��ȡ��ӡͼƬ���ֽ�����
	 * @param bmp
	 * @return
	 */
	public byte[] getImageBytes(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			// ��ʼ����ӡ��
			buffer.write(initPriner());

			// ESC * m nL nH d1��dk ѡ��λͼģʽ
			// ESC * m nL nH
			byte[] escBmp = new byte[] { 0x1B, 0x2A, 0x00, 0x00, 0x00 };

			escBmp[2] = (byte) 0x21;// 8��˫�ܶ�

			// nL, nH
			escBmp[3] = (byte) (width % 256);
			escBmp[4] = (byte) (width / 256);
			// ��С�м��
			buffer.write(new byte[] { 27, 51, 0 });

			int[] pixels = new int[width * height]; // ͨ��λͼ�Ĵ�С�������ص�����

			bmp.getPixels(pixels, 0, width, 0, 0, width, height);

			byte[] data = new byte[24 / 8];
			// ѭ��ͼƬ���ش�ӡͼƬ
			// ѭ����
			for (int i = 0; i < height; i+=24) {
				// ����ģʽΪλͼģʽ
				buffer.write(escBmp);
				// ѭ����
				for (int j = 0; j < width; j++) {
					for (int k = 0; k < 24; k++) {
						if ((i + k) < height) // if within the BMP size
						{
							int h = i + k;

							int pixel = pixels[width * h + j];
							/** ��ȡ�ңǣ�ֵ **/
							int r = Color.red(pixel);
							int g = Color.green(pixel);
							int b = Color.blue(pixel);

							//data��3���ֽڣ���ʵ����3���ֽ�=24bit�����Ǵ���24�����أ�ÿһλ0����ʾ����ӡ(��ɫ)  1����ʾ��ӡ����ɫ��
							if (r < 100 || g < 100 || b < 100) {
								data[k / 8] = (byte)(((int)data[k / 8]) | 1<<(7 - k%8));
							}
						}
					}
					// һ��д��һ��data��24������
					buffer.write(data);

					data[0] = (byte) 0;
					data[1] = (byte) 0;
					data[2] = (byte) 0; // Clear to Zero.
				}
				// ���У���ӡ�ڶ���
				byte[] data2 = new byte[] { 0xA };
				buffer.write(data2);
			} // data

			// ��ʼ��һ�´�ӡ���������ָ��м��
			buffer.write(initPriner());
		} catch (Exception e) {

		}

		return buffer.toByteArray();
	}
	
}