package jp.mixi.android;

import android.os.Bundle;

interface IRemoteAuthenticator{

	/**
	 * トークンをリフレッシュする
	 */
	Bundle tokenRefresh(in Bundle bundle);
}