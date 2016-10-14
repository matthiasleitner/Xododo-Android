package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.apicloud.other.QRCodeUtil;
/**
 * ������ӡ��
 * @author Administrator
 *
 */
public class BluetoothPrinter implements IPrinter {
	// �������ڷ��� UUID��
	private static final UUID SerialPortServiceClass_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	BluetoothDevice m_Device;
	protected String mAddr;
	String mPin;
	BluetoothSocket mSocket;
	/**
	 * 
	 * @param bluetoothAddr ������ַ
	 * @param pin �������
	 */
	public BluetoothPrinter(String bluetoothAddr,String pin)
	{
		mAddr = bluetoothAddr;
		mPin = pin;
	}
	public BluetoothPrinter(String bluetoothAddr)
	{
		mAddr = bluetoothAddr;
		mPin = "0000";
	}
	
	@Override
	public void print(String content, int copyNum) throws Exception {
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content, copyNum);
		// ����������ӡ�����Ϊ��������ǰ������������
		byte[] data2 = new byte[] { 10, 10 };

		write2Device(data1, data2);
	}
	
	@Override
	public void printQRCode(String code,int width,int height) throws Exception {
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		Bitmap bitmap = QRCodeUtil.createQRImage(code, width, height, null);
		byte[] data1 = cmdBuilder.getImageBytes(bitmap);
		bitmap.recycle();

		// ����������ӡ�����Ϊ��������ǰ������������
		byte[] data2 = new byte[] { 10, 10 };

		write2Device(data1, data2);
	}

	@Override
	public void openCashBox() throws Exception {
		CommandBuilder cmdBuilder = new CommandBuilder();
		//��ȡ��Ǯ�������
		byte[] data1 = cmdBuilder.openCashBox();
		write2Device(data1, null);
	}

	/**
	 * ����豸�Ƿ����
	 * @throws Exception 
	 */
	void checkDevicePinState() throws Exception{
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		m_Device = adapter.getRemoteDevice(mAddr);
		
		if(m_Device == null)
		{
			throw new Exception("�޷�����������ӡ��");
		}

		int state = m_Device.getBondState();
		if (state != BluetoothDevice.BOND_BONDED) {
			try {
				Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, mPin);
				Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
			} catch (Exception e) {
				try {
					Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, "1234");
					Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * ��socket����ȡOutputStream
	 * @return
	 * @throws Exception
	 */
	OutputStream getSocketStream() throws Exception
	{
		mSocket = m_Device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		mSocket.connect();
		return mSocket.getOutputStream();
	}
	
	void closeSocket() throws IOException
	{
		mSocket.close();
	}
	
	/**
	 * ���豸д������
	 * @param data1 ��һ�����ݰ�
	 * @param data2 �ڶ������ݰ�������Ϊnull��
	 * @throws IOException 
	 */
	void write2Device(byte[] data1 , byte[] data2) throws Exception
	{
		//����豸�Ƿ����
		checkDevicePinState();
		int errorCount = 0;
		while (true) {
			try {
				//��socket����ȡOutputStream
				OutputStream printStream = getSocketStream();

				//Ϊ�˷�ֹ���ݹ��������д�ӡ
				int position = 0;
				while(position < data1.length)
				{
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					//�Ȼ�ȡһ������
					for(int i = position ; i < data1.length ; i ++)
					{
						byte b = data1[i];
						buffer.write(b);
						if(b == 10)
						{
							break;
						}
					}
					position += buffer.size();
					//���������ݷ��͵���ӡ��
					printStream.write(buffer.toByteArray());
				}

				if(data2 != null)
				printStream.write(data2);
				
				// �ر� socket
				printStream.close();
				closeSocket();
				break;
			} catch (Exception e) {
				checkDevicePinState();
				
				//����ʧ�ܣ����������豸�����Ӵ˴�ӡ�����������ԣ�
				try {
					errorCount++;
					//������10��
					if(errorCount > 10)
					{
						throw e;
					}
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
		}
	}
}
