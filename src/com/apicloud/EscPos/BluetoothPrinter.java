package com.apicloud.EscPos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;

import com.apicloud.moduleEcsPrint.API_PosPrinter;
import com.apicloud.moduleEcsPrint.Comp_BluetoothDeviceManager;
import com.apicloud.other.Helper;
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
	public JSONObject getStatus() throws Exception {
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		JSONObject result = new JSONObject();
		// ����豸�Ƿ����
		checkDevicePinState();
		// ��socket����ȡOutputStream
		OutputStream printStream = getSocketOutputStream();
		InputStream readStream = getSocketInputStream();

		// ���ش�ӡ��״̬
		printStream.write(cmdBuilder.getStatus(1));
		int value = readStream.read();

		// Ǯ��״̬
		if ((value & (1 << 2)) == 0) {
			result.put("CashBoxStatus", "opened");
		} else {
			result.put("CashBoxStatus", "closed");
		}
		// ����״̬
		if ((value & (1 << 3)) == 0) {
			result.put("ConnectStatus", "connected");
		} else {
			result.put("ConnectStatus", "disconnected");
		}

		// �����ѻ�״̬����
		printStream.write(cmdBuilder.getStatus(2));
		value = readStream.read();

		if ((value & (1 << 5)) == 0) {
			result.put("PaperStatus", "has paper");
		} else {
			result.put("PaperStatus", "no paper");
		}

		printStream.close();
		printStream.close();
		closeSocket();
		return result;
	}
	
	@Override
	public void print(String content, int copyNum) throws Exception {
		boolean hasCut = content.contains("<CUT>");
		// ��ȡESC����
		CommandBuilder cmdBuilder = new CommandBuilder();
		byte[] data1 = cmdBuilder.getPrintCmd(content, copyNum);
		
		byte[] data2 = null;
		if(hasCut == false)//�ж��Ƿ�����ֽ����
		{
			//�������ֽ���������Ҫ�������10�����Ҳ���Դ�ӡȫ
			//���û����ֽ����� ����������ӡ�����Ϊ��������ǰ������������
			data2 = new byte[] { 10, 10 };
		}
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
	public void printImage(String base64) throws Exception {
		Bitmap bitmap = Helper.string2Bitmap(base64);
		CommandBuilder cmdBuilder = new CommandBuilder();
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
	 * ����豸�Ƿ����(�̳�����Ҫ����)
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

		Helper.Log("BluetoothPrinter bond state", state + "");
		if (state != BluetoothDevice.BOND_BONDED) {
			try {
				Helper.Log("BluetoothPrinter", "���԰�");
				Comp_BluetoothDeviceManager.SetPin(m_Device.getClass(), m_Device, mPin);
				Comp_BluetoothDeviceManager.createBond(m_Device.getClass(), m_Device);
			} catch (Exception e) {
				Helper.Log("BluetoothPrinter bond error", e.getMessage());
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
	 * ��socket����ȡOutputStream(�̳�����Ҫ����)
	 * @return
	 * @throws Exception
	 */
	OutputStream getSocketOutputStream() throws Exception
	{
		Helper.Log("BluetoothPrinter getSocketStream", "�������������豸");
		mSocket = m_Device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		mSocket.connect();
		OutputStream os = mSocket.getOutputStream();
		Helper.Log("BluetoothPrinter getSocketStream", "�ɹ�����");
		return os;
	}
	
	/**
	 * ��ȡInputStream(�̳�����Ҫ����)
	 * @return
	 * @throws Exception
	 */
	InputStream getSocketInputStream() throws Exception
	{
		return mSocket.getInputStream();
	}
	
	/**
	 * �ر�socket(�̳�����Ҫ����)
	 * @throws IOException
	 */
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
				OutputStream printStream = getSocketOutputStream();

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
					printStream.flush();
					//�����������̫�죬��ӡ�����ܴ�ӡ����̫����ڴ�ӡ���������ݣ���ӡ�����ڴ����ޣ��������ݹ��࣬�������ݶ�ʧ
					Thread.sleep(50);
				}

				if(data2 != null)
				printStream.write(data2); 
				
				printStream.flush();
				Helper.Log("Printer write2device", "д������");
				// �ر� socket
				printStream.close();
				closeSocket();
				break;
			} catch (Exception e) {
				Helper.Log("Printer write error", "���³������Ӵ�ӡ��������������");
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
