package com.apicloud.EscPos;

public interface IScanner {
	 /**
     * ����ɨ��ǹ���������¼�����
     *
     * @param listener 
     */
	public void setOnReceiveListener(OnReceiveListener listener);
	void close();
	/**
	 * ��ʼ����ɨ��
	 * @throws Exception 
	 */
	void startListen() throws Exception;
}
