package com.apicloud.appLifecycleEvent;

import com.apicloud.EscPos.IPrinter;
import com.apicloud.EscPos.NetPrinter;
import com.apicloud.moduleEcsPrint.Comp_BluetoothPrint;
import com.uzmap.pkg.uzcore.uzmodule.AppInfo;
import com.uzmap.pkg.uzcore.uzmodule.ApplicationDelegate;

import android.app.Activity;
import android.content.Context;

public class AppEventDelegate extends ApplicationDelegate {

	/**
	 * �̳���ApplicationDelegate���࣬APICloud������Ӧ�ó�ʼ��֮���ͻὫ�����ʼ��һ�Σ���newһ�������������������ã�
	 * ��Ӧ�����������ڼ䣬�Ὣ���������¼�ͨ���ö���֪ͨ������<br>
	 * ������Ҫ��module.json�����ã�����name�ֶο����������ã���Ϊ���ֶν������ԣ���ο�module.json��EasDelegate������
	 */
	public AppEventDelegate() {
		//Ӧ�������ڼ䣬�ö���ֻ���ʼ��һ������
	}

	@Override
	public void onApplicationCreate(Context context, AppInfo info) {
		//TODO ������������г�ʼ����Ҫ��Application��onCreate�г�ʼ���Ķ���
	}

	@Override
	public void onActivityResume(Activity activity, AppInfo info) {
		//APP�Ӻ�̨�ص�ǰ̨ʱ��APICloud���潫ͨ���ú����ص��¼�
		//TODO �������������ʵ������Ҫ���߼����������
	}

	@Override
	public void onActivityPause(Activity activity, AppInfo info) {
		//APP��ǰ̨�˵���̨ʱ��APICloud���潫ͨ���ú����ص��¼�
		//TODO �������������ʵ������Ҫ���߼����������
	}

	@Override
	public void onActivityFinish(Activity activity, AppInfo info) {
		//APP������������ʱ��APICloud���潫ͨ���ú����ص��¼�
		
		// �ͷ�����������ӡ����Դ
		Comp_BluetoothPrint.DisposeAllInstance();
	}
}
