package com.apicloud.EscPos;

public interface IScanner {
	 /**
     * 设置扫描枪接受数据事件监听
     *
     * @param listener 
     */
	public void setOnReceiveListener(OnReceiveListener listener);
	void close();
	/**
	 * 开始监听扫描
	 * @throws Exception 
	 */
	void startListen() throws Exception;
}
