package com.apicloud.loggers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.apicloud.other.Helper;

import android.os.Environment;

/**
 * ��¼text�ļ����͵�log
 * @author Administrator
 *
 */
public class TextFileLog implements ILog {
	FileOutputStream mFile;

	public TextFileLog(String fileName) throws Exception
	{
		fileName += Helper.getNowTime("dd_hhmmss") + ".txt";
		String pathName= Environment.getExternalStorageDirectory().getPath() + "/XododoPos/";  

        File path = new File(pathName);  
        File file = new File(pathName + fileName);  
        if( !path.exists()) {   
            path.mkdir();  
        }  
        if( !file.exists()) {  
            file.createNewFile();  
        }  
        mFile = new FileOutputStream(file);  
        
	}

	@Override
	public void writeLine(String header , String content) throws Exception {

		//дʱ��ͷ��
		header = header + " " + Helper.getNowTime("HH:mm:ss") + "��";
		byte[] buf = header.getBytes("gbk");
		mFile.write(buf);

		//д����
		buf = content.getBytes("gbk");
        mFile.write(buf);
        //���뻻�з�
        mFile.write(new byte[]{13,10});
        mFile.flush();
	}
}
