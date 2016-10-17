package com.apicloud.loggers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.apicloud.other.Helper;

import android.os.Environment;

/**
 * 记录text文件类型的log
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

		//写时间头部
		header = header + " " + Helper.getNowTime("HH:mm:ss") + "：";
		byte[] buf = header.getBytes("gbk");
		mFile.write(buf);

		//写内容
		buf = content.getBytes("gbk");
        mFile.write(buf);
        //吸入换行符
        mFile.write(new byte[]{13,10});
        mFile.flush();
	}
}
