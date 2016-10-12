package com.apicloud.EscPos;

public interface OnReceiveListener {
	void OnReceiveCode(Object sender, String code);
	void OnError(Object sender, String errMessage);
}
