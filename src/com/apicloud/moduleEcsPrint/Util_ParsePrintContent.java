package com.apicloud.moduleEcsPrint;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Util_ParsePrintContent {

	// 定义一个字符串，保存输入的打印内容
	private String mOriginalContent = null;
	
	// 定义一个字节数组，保存要发送给打印机的字节指令
	private byte[] mPrintCmdBytes = null;
	
	// 构造函数，打印内容作为参数输入
	public Util_ParsePrintContent(String content) {
		mOriginalContent = content;
	}
	
	// 获取要发送给打印机的字节流
	public byte[] GetPrintCommandBytes() {
		
		if (mPrintCmdBytes == null || mPrintCmdBytes.length <= 0) {
			String[] lines = mOriginalContent.split("<BR>");

			List<byte[]> cmdLines = new ArrayList<byte[]>();
			for (int i = 0; i < lines.length; i++) {
				cmdLines.add(ConvertLineToPrintCommand(lines[i]));
			}
			mPrintCmdBytes = Util_EcsPrintCommand.byteMerger(cmdLines);
		}
		return mPrintCmdBytes;
	}
	
	// 将要打印的单行文本，转换成发送给打印机的字节流
	static public byte[] ConvertLineToPrintCommand(String line)
	{
		// 构造一个 1024字节的缓冲区，缓存打印指令
		ByteBuffer buf = ByteBuffer.allocate(1024);

		// 写入打印初始化指令
		buf.put(Util_EcsPrintCommand.init_printer());

		try {
			// 设置居中
			if (line.contains("<C>")) {
				buf.put(Util_EcsPrintCommand.alignCenter());
				buf.put(line.replaceAll("<C>|</C>", "").getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(3));
			}
			// 居中放大
			else if (line.contains("<CA>")) {
				buf.put(Util_EcsPrintCommand.fontSizeSetBig(2));
				buf.put(Util_EcsPrintCommand.alignCenter());
				buf.put(line.replaceAll("<CA>|</CA>", "").getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(3));
			}
			// 字体加粗
			// else if(line.contains("<B>")) {
			//
			// }
			// 字体变高
			else if (line.contains("<L>")) {
				buf.put(Util_EcsPrintCommand.fontSizeSethigh(1));
				buf.put(line.replaceAll("<L>|</L>", "").getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(1));
			}
			// 字体加宽
			// else if(line.contains("<W>")) {
			// buf.put(line.replaceAll("<W>|</W>", "").getBytes("gbk"));
			// buf.put(Util_EcsPrintCommand.nextLine(1));
			// }
			// 字体放大
			else if (line.contains("<A>")) {
				buf.put(Util_EcsPrintCommand.boldOn());
				buf.put(line.replaceAll("<A>|</A>", "").getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(1));
			}
			// 切纸
			else if (line.contains("<CUT>")) {
				buf.put(line.replaceAll("<CUT>", "").getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(1));
				buf.put(Util_EcsPrintCommand.feedPaperCutAll());
			}
			// 正常打印
			else {
				buf.put(line.getBytes("gbk"));
				buf.put(Util_EcsPrintCommand.nextLine(1));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 调用flip()使limit变为当前的position的值,position变为0,
		// 为接下来从ByteBuffer读取做准备
		buf.flip();

		// 构建一个byte数组
		byte[] result = new byte[buf.limit()];
		// 从 ByteBuffer 中读取数据到 byte 数组中
		buf.get(result);

		return result;
	}
}
