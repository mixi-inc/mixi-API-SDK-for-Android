/*
 * Copyright (C) 2011 mixi, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.mixi.android.sdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jp.mixi.android.IRemoteAuthenticator;
import jp.mixi.android.sdk.util.UrlUtils;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 * MixiContainerの実装クラス
 * 
 */
@TargetApi(3)
class MixiContainerImpl implements MixiContainer {
    /** アクセストークンのパラメータ名 */
    static final String ACCESS_TOKEN = "access_token";
    /** リフレッシュトークンのパラメータ名 */
    static final String REFRESH_TOKEN = "refresh_token";
    /** 有効期限のパラメータ名 */
    static final String EXPIRES = "expires_in";

    /** クライアントIDのパラメータ名 */
    private static final String CLIENT_ID = "client_id";
    /** セッションキー名 */
    private static final String MIXI_SESSION_KEY = "mixi_session";
    /** access_tokenのヘッダ名 */
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String STATE = "state";

    private static final String CONTENT_TYPE = "Content-Type";
    /** 結果取得用のパラメータ名 */
    private static final String RESPONSE = "response";
    /** 公式アプリがインストールされていない場合に表示されるページ */
    private static final String OFFICIAL_APP_INTRODUCTION_PAGE =
            "http://mixi.jp/official_app_introduction.pl";

    /** http.connection.timeout */
    private static final String HTTP_PARAM_CONNECTION_TIMEOUT = "http.connection.timeout";
    /** http.socket.timeout */
    private static final String HTTP_PARAM_SOCKET_TIMEOUT = "http.socket.timeout";
    private static final String ZIP_ENCODING = "gzip";

    private static final String MIXI_APPS_SCOPE = "mixi_apps2";
    private static final String DEPRECATED_SCOPE = "mixi_apps";
    private static final String ERROR_STR = "error";
    private static final String ERROR_CODE_STR = "errorCode";
    private static final String ERROR_MESSAGE_STR = "errorMessage";
    private static final String ACCOUNT_EXCEPTION = "mixiAccountException";

    /** 公式アプリの対応バージョン */
    private static final int SUPPORTED_VERSION = 19;
    /** 公式アプリの課金対応バージョン */
    private static final int PAYMENT_SUPPORTED_VERSION = 19;
    /** ログ用 */
    private static final String TAG = "MixiContainerImpl";
    /** 公式アプリのパッケージ名 */
    private static final String OFFICIAL_PACKAGE = "jp.mixi";
    private static final int VALIDATE_OFFICIAL_FOR_ACTIVITY = 1;
    private static final int VALIDATE_OFFICIAL_FOR_SERVICE = 2;
    /** 公式アプリの認証用Activity */
    private static final String AUTH_ACTIVITY =
            "jp.mixi.android.clientauthenticator.ClientAuthenticatorActivity";
    /** 課金用Activity */
    private static final String PAYMENT_ACTIVITY =
            "jp.mixi.android.sdk.payment.PaymentActivity";
    /** 公式アプリの署名 */
    private static final String MIXI_OFFICIAL_SIGNATURE =
            "30820265308201cea00302010202044d0b3f7b300d06092a864886f70d01010505003077310b3009"
            + "060355040613024a50310e300c06035504081305546f6b796f3110300e0603550407130753686962"
            + "75796131123010060355040a13096d69786920496e632e311a3018060355040b131153797374656d"
            + "204465706172746d656e74311630140603550403130d59756b692046756a6973616b69301e170d31"
            + "30313231373130343631395a170d3430313230393130343631395a3077310b300906035504061302"
            + "4a50310e300c06035504081305546f6b796f3110300e060355040713075368696275796131123010"
            + "060355040a13096d69786920496e632e311a3018060355040b131153797374656d20446570617274"
            + "6d656e74311630140603550403130d59756b692046756a6973616b6930819f300d06092a864886f7"
            + "0d010101050003818d0030818902818100bbdfdb3dbb8aaaee39a05cf6543359fe5bab780ae5362f"
            + "34e3777d0a1a8e8dc1b4fdf0c9e1046c54bef4f367ce59ab87ea04e7d81a3fc10a173c20f2250cf2"
            + "77b844447eef14c893d581f189db8b43ce78798665edde516bb483c45d9bafead9530a89257d3b3d"
            + "ca42d56f40d468ed1f6bb95ceb605eb215d328727521bbdd5b0203010001300d06092a864886f70d"
            + "0101050500038181000e0180edcf89f27790b87f34f890ec71f15c8c7340836b5079b7319062a5e3"
            + "1c1a77fe9e75f190732094e5466ad10cf4df06a9f8d5917c27bb2b9502885e877c8e239100c50bf0"
            + "5b5db268f9901090d5cf294d5e887853b1271a86590e831b85ccd858321bbbbd70601ad8656aaad2"
            + "3ac1587afd318b5dd4cc052b43809a51ca";

    // field変数

    /** mixiサーバーアクセス用のHttpClient */
    private HttpClient mHttpClient;

    /** http.connection.timeoutの値 */
    private int mConnectionTimeout;
    /** http.socket.timeoutの値 */
    private int mSocketTimeout;
    private int mSelector;

    /** 認証認可時のCallbackListener */
    private CallbackListener mAuthCallbackListener;
    /** ログアウト(revoke)時のCallbackListener */
    private CallbackListener mRevokeCallbackListener;
    /** 課金API呼び出し時のCallbackListener */
    private CallbackListener mPaymentCallbackListener;
    /** アプリID */
    private final String mClientId;
    /**  */
    private ContextWrapper mContextWrapper;
    /** authorizeのintent先判定用のコード値 */
    private int mActivityCode;
    /** logoutのintent先判定用のコード値 */
    private int mRevokeCode;
    /** requestPaymentのintent先判定用のコード値 */
    private int mPaymentRequestCode;
    /** access token */
    private String mAccessToken;
    /** refresh token */
    private String mRefreshToken;
    /** アクセストークンが失効する時間 */
    private long mExpiresIn = 0;
    /** カウント用タスク */
    private AppsCounter mAppsCounter;
    private AppsCounter mAppsStartCounter;


    /** スコープ指定なしのauthorizeで使用するスコープ */
    private String[] mDefaultScope;

    /** リフレッシュトークン取得用サービス */
    private IRemoteAuthenticator mRemoteAuth;
    /** サービス接続用のコネクタ */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteAuth = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteAuth = IRemoteAuthenticator.Stub.asInterface(service);
        }
    };

    /**
     * Mixi Graph APIへアクセスするためのクラスのインスタンスをアプリケーションIDを指定して生成.
     * 
     * @param config 起動する対象のアプリIDを指定
     */
    private MixiContainerImpl(Config config) {
        if (config == null || config.clientId == null) {
            throw new IllegalArgumentException(
                    "You must specify your client ID.");
        }
        switch (config.selector) {
        case Config.APPLICATION:
            mDefaultScope = new String[] {MIXI_APPS_SCOPE};
            break;
        case Config.GRAPH_API:
            break;
        default:
            throw new IllegalArgumentException("Config.selector is unknown value:"
                    + config.selector);
        }
        mClientId = config.clientId;
        mConnectionTimeout = config.connectionTimeout;
        mSocketTimeout = config.socketTimeout;
        mSelector = config.selector;
    }

    /**
     * インスタンス取得
     * 
     * @param config 起動する対象の設定を指定
     * @return MixiContainerのインスタンス
     */
    public static MixiContainerImpl getInstance(Config config) {
        Log.v(TAG, "get new instance");
        return new MixiContainerImpl(config);

    }

    @Override
    public void authorize(final Activity activity, String[] permissions,
                            int activityCode, final CallbackListener listener) {
        mAuthCallbackListener = new CallbackListener() {

            @Override
            public void onFatal(ErrorInfo e) {
                listener.onFatal(e);
            }

            @Override
            public void onError(ErrorInfo e) {
                listener.onError(e);
            }

            @Override
            public void onComplete(Bundle bundle) {
                // tokenの保存
                setAccessToken(bundle.getString(ACCESS_TOKEN));
                setRefreshToken(bundle.getString(REFRESH_TOKEN));
                setAccessExpiresIn(bundle.getString(EXPIRES));
                saveSession(activity);
                listener.onComplete(new Bundle());
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        };
        for (String permission : permissions) {
            if (DEPRECATED_SCOPE.equals(permission)) {
                throw new IllegalArgumentException("use permission mixi_apps2");
            }
        }
        mActivityCode = activityCode;
        // 公式アプリで認証する
        if (startSingleSignOn(activity, activityCode, permissions)) {
            saveSession(activity);
            Log.d(TAG, "SingleSignOn done");
            return;
        }
        Log.e(TAG, "official application not found");
        listener.onFatal(new ErrorInfo("official application not found"));
    }

    public void authorize(Activity activity,
            int activityCode, final CallbackListener listener) {
        authorize(activity, mDefaultScope, activityCode, listener);
    }

    @Override
    public boolean isAuthorized() {
        return (getAccessToken() != null);
    }

    @Override
    public boolean init(ContextWrapper context) {
        this.mContextWrapper = context;

        if (!validatePermission(context)) {
            return false;
        }
        Intent intent = new Intent(IRemoteAuthenticator.class.getName());
        if (!validateOfficialAppsForIntent(mContextWrapper,
                intent, VALIDATE_OFFICIAL_FOR_SERVICE, SUPPORTED_VERSION)) {
            // 公式アプリが入っていない場合に公式アプリのインストールを促す画面を出す
            new MixiDialog(context, OFFICIAL_APP_INTRODUCTION_PAGE, new HashMap<String, String>(),
                    new CallbackListener() {

                        @Override
                        public void onFatal(ErrorInfo e) {
                            Log.v(TAG, "OFFICIAL_APP_INTRODUCTION_PAGE onFatal");
                        }

                        @Override
                        public void onError(ErrorInfo e) {
                            Log.v(TAG, "OFFICIAL_APP_INTRODUCTION_PAGE onError");
                        }

                        @Override
                        public void onComplete(Bundle values) {
                            Log.v(TAG, "OFFICIAL_APP_INTRODUCTION_PAGE onComplete");
                        }

                        @Override
                        public void onCancel() {
                            Log.v(TAG, "OFFICIAL_APP_INTRODUCTION_PAGE onCancel");
                        }
                    }, true).show();
            return false;
        }

        setUpCounter();
        if (mRemoteAuth == null) {
            // セッションの復元する
            restoreSession(mContextWrapper);
            return bindRemoteService(mContextWrapper,
                    new Intent(IRemoteAuthenticator.class.getName()),
                        mServiceConnection);
        }
        return true;
    }

    private boolean validatePermission(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,
                    "No permission Manifest.permission.INTERNET");
            Builder alertBuilder = new Builder(context);
            alertBuilder
                    .setMessage(context.getString(R.string.no_permission_error));
            alertBuilder.create().show();
            return false;
        }
        return true;
    }

    private boolean bindRemoteService(ContextWrapper context,
                Intent intent, ServiceConnection connection) {
        return context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void close(ContextWrapper context) {
        if (mRemoteAuth != null) {
            context.unbindService(mServiceConnection);
            mRemoteAuth = null;
        }
    }

    @Override
    public void showDialog(Context context, String action,
            Map<String, String> parameters, final CallbackListener listener) {
        showDialog(context, action, parameters, listener, true);
    }

    @Override
    public void showDialog(final Context context, String action, Map<String, String> parameters,
            final CallbackListener listener, final boolean isCancelable) {

        Uri uri = Uri.parse(Constants.GRAPH_BASE_URL);
        android.net.Uri.Builder builder = uri.buildUpon();
        builder.appendEncodedPath("dialog" + action);
        for (Entry<String, String> param : parameters.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }
        final String url = builder.build().toString();
        AsyncTask<String, Void, Void> tasc = new AsyncTask<String, Void, Void>() {
            private Exception e;
            @Override
            protected Void doInBackground(String... params) {
                try {
                    refreshToken();
                } catch (RemoteException e) {
                    this.e = e;
                } catch (ApiException e) {
                    this.e = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void res) {
                if (e == null) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("oauth_token", getAccessToken());
                    new MixiDialog(context, url, map, listener, isCancelable).show();
                } else {
                    Log.v(TAG, "refresh token error");
                    listener.onFatal(new ErrorInfo(e));
                }
            }
        };
        tasc.execute(url);
    }

    @Override
    public void send(String endpointPath, CallbackListener listener) {
        send(endpointPath, new HashMap<String, String>(), listener);
    }


    @Override
    public void send(String endpointPath,
            Map<String, String> parameters, CallbackListener listener) {
        send(endpointPath, HttpMethod.GET, parameters, listener);
    }

    @Override
    public void send(final String endpointPath, final JSONObject json,
            final CallbackListener listener) {
        send(endpointPath, HttpMethod.POST, json, listener);
    }

    @Override
    public void send(final String endpointPath, HttpMethod method, final JSONObject json,
            final CallbackListener listener) {
        try {
            HttpUriRequest httpMethod = getHttpMethod(endpointPath, json, method);
            requestAsync(httpMethod, listener, true);
        } catch (IOException e) {
            listener.onFatal(new ErrorInfo(e));
        }

    }

    @Override
    public void send(String endpointPath, HttpMethod method,
            CallbackListener listener) {
        send(endpointPath, method, new HashMap<String, String>(), listener);
    }

    @Override
    public void send(String endpointPath, HttpMethod method,
            Map<String, String> parameters, CallbackListener listener) {
        try {
            HttpUriRequest httpMethod = getHttpMethod(endpointPath, parameters, method);
            requestAsync(httpMethod, listener, true);
        } catch (IOException e) {
            listener.onFatal(new ErrorInfo(e));
        }
    
    }

    @Override
    public void send(final String endpointPath, final String contentType,
            final InputStream stream, final long length, final CallbackListener listener) {

        AsyncTask<Void, Void, Void> tasc = new AsyncTask<Void, Void, Void>() {
            private Exception e;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    refreshToken();
                } catch (RemoteException e) {
                    this.e = e;
                } catch (ApiException e) {
                    this.e = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void res) {
                if (e == null) {
                    send(endpointPath, contentType, HttpMethod.POST, stream, length, listener);
                } else {
                    Log.v(TAG, "refresh token error");
                    listener.onFatal(new ErrorInfo(e));
                }
            }
        };
        tasc.execute(new Void[0]);
    }

    @Override
    public void send(String endpointPath, String contentType, HttpMethod method,
            InputStream stream, long length, CallbackListener listener) {
        try {
            HttpUriRequest httpMethod = getHttpMethod(endpointPath, contentType, method, stream,
                    length);
            requestAsync(httpMethod, listener, true);
        } catch (IOException e) {
            listener.onFatal(new ErrorInfo(e));
        }
    }

    @Override
    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
        // 予約したコードだったら処理する
        if (requestCode == mActivityCode) {
            callbackExecute(resultCode, data, mAuthCallbackListener);
            return;
        } else if (requestCode == mRevokeCode) {
            callbackExecute(resultCode, data, mRevokeCallbackListener);
            return;
        }
    }

    private void callbackExecute(int resultCode, Intent data, CallbackListener listener) {
        if (resultCode == Activity.RESULT_OK) {
            listener.onComplete(data.getExtras());
            return;
        } else {
            if (data != null && data.hasExtra(ERROR_STR)) {
                Log.d(TAG, data.getStringExtra(ERROR_STR));
                int code = data.getIntExtra(ERROR_CODE_STR, 0);
                if (code == ErrorInfo.SERVER_ERROR) {
                    listener.onError(new ErrorInfo(
                            data.getStringExtra(ERROR_MESSAGE_STR), code));
                    return;
                } else if (code == ErrorInfo.OTHER_ERROR
                        && ACCOUNT_EXCEPTION.equals(data
                                .getStringExtra(ERROR_STR))) {
                    listener.onError(new ErrorInfo(data
                            .getStringExtra(ERROR_MESSAGE_STR),
                            ErrorInfo.OFFICIAL_APP_ACCOUNT_ERROR));
                    return;
                }
                listener.onFatal(new ErrorInfo(
                        data.getStringExtra(ERROR_MESSAGE_STR), code));
                return;
            }
            Log.d(TAG, "Login canceled by user.");
            listener.onCancel();
            return;
        }
    }

    @Override
    public void logout(final Activity activity, int activityCode, final CallbackListener listener) {
        mRevokeCode = activityCode;
        // ログアウトに成功したらローカルに保存されているセッション情報を消した上で、ユーザーの指定したハンドリングをする
        mRevokeCallbackListener = new CallbackListener() {
            @Override
            public void onFatal(ErrorInfo e) {
                listener.onFatal(e);
            }

            @Override
            public void onError(ErrorInfo e) {
                listener.onError(e);
            }

            @Override
            public void onComplete(Bundle values) {
                deleteSession(activity);
                listener.onComplete(values);
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        };
        Map<String, String> param = new HashMap<String, String>();
        param.put(CLIENT_ID, mClientId);
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            mRevokeCallbackListener.onFatal(new ErrorInfo("not authorized",
                    ErrorInfo.REVOKE_AUTHORIZE));
            return;
        }
        param.put("token", refreshToken);

        Intent intent = new Intent();
        intent.putExtra("mode", "revoke");
        intent.setClassName(OFFICIAL_PACKAGE, AUTH_ACTIVITY);
        intent.putExtra(CLIENT_ID, mClientId);
        intent.putExtra(REFRESH_TOKEN, refreshToken);

        if (!validateOfficialAppsForIntent(activity, intent, VALIDATE_OFFICIAL_FOR_ACTIVITY,
                SUPPORTED_VERSION)) {
            mRevokeCallbackListener.onFatal(new ErrorInfo("official application not found",
                    ErrorInfo.OFFICIAL_APP_NOT_FOUND));
            return;
        }
        try {
            activity.startActivityForResult(intent, activityCode);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            mRevokeCallbackListener.onFatal(new ErrorInfo(e));
        }

    }

    /**
     * トークン削除
     * 
     * @param context
     */
    private void deleteSession(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                SharedPreferences sp = context.getSharedPreferences(
                        MIXI_SESSION_KEY, Context.MODE_PRIVATE);
                if (sp != null) {
                    sp.edit().clear().commit();
                }
                // 初期化
                setAccessToken(null);
                setRefreshToken(null);
                setAccessExpiresIn(0L);
            }
        }).start();
    }

    @Override
    public void requestPayment(final Activity activity, final PaymentParameter param,
            final int activityCode, final CallbackListener listener) {
        mPaymentCallbackListener = listener;
        mPaymentRequestCode = activityCode;
        if (!validatePaymentParam(param)) {
            listener.onError(new ErrorInfo("parameter invalid", ErrorInfo.OTHER_ERROR));
            return;
        }

        Intent intent = new Intent();
        intent.setClassName(OFFICIAL_PACKAGE, PAYMENT_ACTIVITY);
        intent.putExtras(convertPaymentBundle(param));
        if (!validateOfficialAppsForIntent(activity, intent,
                VALIDATE_OFFICIAL_FOR_ACTIVITY, PAYMENT_SUPPORTED_VERSION)) {
            listener.onFatal(new ErrorInfo(
                    activity.getString(R.string.err_unsupported_versions),
                    ErrorInfo.OFFICIAL_APP_NOT_FOUND));

            return;
        }
        try {
            activity.startActivityForResult(intent, activityCode);
        } catch (ActivityNotFoundException e) {
            Log.v(TAG, e.getMessage());
            listener.onFatal(new ErrorInfo(e));
        }

    }

    private boolean validatePaymentParam(PaymentParameter param) {
        boolean flg = true;
        if (param.callbackUrl == null) {
            flg = false;
        }
        if (param.inventoryCode == null) {
            flg = false;
        }
        if (param.itemId == null) {
            flg = false;
        }
        if (param.itemName == null) {
            flg = false;
        }
        if (param.itemPrice <= 0) {
            flg = false;
        }
        if (param.signature == null) {
            flg = false;
        }
        return flg;
    }

    /**
     * PaymentParameterをintent用のbundleに変換
     * 
     * @param param
     * @return
     */
    private Bundle convertPaymentBundle(PaymentParameter param) {
        Bundle bundle = new Bundle();
        bundle.putString(PaymentParameter.CLIENT_ID_NAME, mClientId);
        bundle.putString(PaymentParameter.CALLBACK_URL_NAME, param.callbackUrl);
        bundle.putString(PaymentParameter.INVENTORY_CODE_NAME, param.inventoryCode);
        bundle.putBoolean(PaymentParameter.IS_TEST_NAME, param.isTest);
        bundle.putString(PaymentParameter.ITEM_ID_NAME, param.itemId);
        bundle.putString(PaymentParameter.ITEM_NAME_NAME, param.itemName);
        bundle.putInt(PaymentParameter.ITEM_PRICE_NAME, param.itemPrice);
        bundle.putString(PaymentParameter.SIGNATURE_NAME, param.signature);
        bundle.putInt(PaymentParameter.VERSION_NAME, param.getVertion());
        return bundle;
    }

    @Override
    public void paymentCallback(int requestCode, int resultCode, Intent data) {
        if (requestCode == mPaymentRequestCode) {
            callbackExecute(resultCode, data, mPaymentCallbackListener);
        }
    }

    /**
     * トークンをローカルに保存.
     * 
     * @param context
     * @return
     */
    private void saveSession(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Editor editor = context.getSharedPreferences(
                        MIXI_SESSION_KEY, Context.MODE_PRIVATE)
                        .edit();
                editor.putString(ACCESS_TOKEN, getAccessToken());
                editor.putLong(EXPIRES, getAccessExpiresIn());
                editor.putString(REFRESH_TOKEN, getRefreshToken());
                editor.putString(CLIENT_ID, mClientId);
                editor.commit();
            }
        }).start();
    }

    /**
     * セッションの復元
     * 
     * @param contextWrapper アプリのactivity
     * @param listener 処理完了時、またはエラー時に実行される処理を記述してください(ここではエラーのみのはず)
     * @return セッションの復元に成功した場合はtrue
     * @throws RemoteException
     */
    boolean restoreSession(final ContextWrapper contextWrapper) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs =
                        contextWrapper.getSharedPreferences(MIXI_SESSION_KEY, Context.MODE_PRIVATE);
                String appId = prefs.getString(CLIENT_ID, null);
                if (appId == null) {
                    return;
                }
                setAccessToken(prefs.getString(ACCESS_TOKEN, null));
                setRefreshToken(prefs.getString(REFRESH_TOKEN, null));
                setAccessExpiresIn(prefs.getLong(EXPIRES, 0));
                if (isAuthorized()) {
                    runAppCounter();
                }
            }
        }).start();
        if (getAccessToken() == null) {
            return false;
        }
        return true;
    }

    /**
     * 公式アプリ判定
     * 
     * @param activity アプリのActivity
     * @param intent Intent対象(公式アプリ)
     * @return
     */
    private boolean validateOfficialAppsForIntent(
                        Context activity, Intent intent, int target, int supportedVersion) {
        String packageName = null;
        if (target == VALIDATE_OFFICIAL_FOR_ACTIVITY) {
            ResolveInfo resolveInfo = activity.getPackageManager()
                    .resolveActivity(intent, 0);
            if (resolveInfo == null) {
                Log.d(TAG, "official application not found");
                return false;
            }
            packageName = resolveInfo.activityInfo.packageName;
        } else if (target == VALIDATE_OFFICIAL_FOR_SERVICE) {
            ResolveInfo resolveInfo = activity.getPackageManager()
                    .resolveService(intent, 0);
            if (resolveInfo == null) {
                Log.d(TAG, "official application not found");
                return false;
            }
            packageName = resolveInfo.serviceInfo.packageName;
        } else {
            Log.d(TAG, "do not support option");
            return false;
        }
        try {
            PackageInfo packageInfo = activity.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo.versionCode < supportedVersion) {
                Log.d(TAG, "Unsupported version" + packageInfo.versionCode);
                return false;
            }
            // シグネチャのチェックをする
            return validateSignature(packageInfo.signatures);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "NameNotFoundException");
            return false;
        }

    }

    /**
     * 署名判定
     * 
     * @param signatures 署名の配列
     * @return 署名が正しい場合true
     */
    private boolean validateSignature(Signature[] signatures) {
        if (signatures != null && signatures.length == 1) {
            if (MIXI_OFFICIAL_SIGNATURE.equals(signatures[0].toCharsString())) {
                Log.v(TAG, "signature check OK");
                return true;
            }
        }
        Log.d(TAG, "signature check NG");
        return false;
    }


    /**
     * HTTPメソッドを取得する
     * 
     * @param endpointPath エンドポイント
     * @param json パラメータのjsonオブジェクト
     * @param httpMethod httpメソッド
     * @return HttpUriRequest
     * @throws IOException jsonパラメータが異常だった場合
     */
    private HttpUriRequest getHttpMethod(String endpointPath,
            JSONObject json, HttpMethod httpMethod) throws IOException {

        HttpEntity entity = createHttpEntity(json);
        switch (httpMethod) {
        case POST:
            HttpPost postMethod = new HttpPost(Constants.GRAPH_BASE_URL + endpointPath);
            postMethod.setEntity(entity);
            return postMethod;
        case PUT:
            HttpPut putMethod = new HttpPut(Constants.GRAPH_BASE_URL + endpointPath);
            putMethod.setEntity(entity);
            return putMethod;
        default:
            Log.e(TAG, "Unsupported http method");
            throw new IllegalArgumentException("Unsupported HttpMethod parameter:" + httpMethod);
        }
    }

    private HttpEntity createHttpEntity(JSONObject param)
            throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(param.toString(), HTTP.UTF_8);
        entity.setContentType("application/json");
        return entity;
    }

    /**
     * HTTPメソッドを取得する
     * 
     * @param endpointPath エンドポイント
     * @param parameters リクエストパラメータ
     * @param httpMethod httpメソッド
     * @return HttpUriRequest
     * @throws IOException parametersが異常だった場合
     */
    private HttpUriRequest getHttpMethod(String endpointPath,
            Map<String, String> parameters, HttpMethod httpMethod) throws IOException {
        HttpUriRequest method = null;
        String url = null;
        android.net.Uri.Builder builder = null;
        switch (httpMethod) {
        case GET:
            builder = Uri.parse(Constants.GRAPH_BASE_URL + endpointPath).buildUpon();
            builder.encodedQuery(UrlUtils.encodeUrl(parameters));
            url = builder.build().toString();
            method = new HttpGet(url);
            break;
        case POST:
            url = Constants.GRAPH_BASE_URL + endpointPath;
            HttpPost post = new HttpPost(url);
            ArrayList<NameValuePair> getParams = new ArrayList<NameValuePair>();

            for (String key : parameters.keySet()) {
                if (!key.equals(CONTENT_TYPE)) {
                    getParams.add(new BasicNameValuePair(key, parameters.get(key)));
                }
            }
            post.setEntity(new UrlEncodedFormEntity(getParams, HTTP.UTF_8));
            method = post;
            break;
        case PUT:
            ArrayList<NameValuePair> putParams = new ArrayList<NameValuePair>();
            HttpPut put = new HttpPut(Constants.GRAPH_BASE_URL + endpointPath);
            for (String key : parameters.keySet()) {
                if (!key.equals(CONTENT_TYPE)) {
                    putParams.add(new BasicNameValuePair(key, parameters.get(key)));
                }
            }
            put.setEntity(new UrlEncodedFormEntity(putParams, HTTP.UTF_8));
            method = put;
            break;
        case DELETE:
            builder = Uri.parse(Constants.GRAPH_BASE_URL + endpointPath).buildUpon();
            builder.encodedQuery(UrlUtils.encodeUrl(parameters));
            url = builder.build().toString();
            method = new HttpDelete(url);
            break;

        default:
            Log.e(TAG, "Unsupported http method");
            throw new UnsupportedOperationException("Unsupported http method:" + method);
        }
        return method;
    }

    /**
     * HTTPメソッドを取得する
     * 
     * @param endpointPath エンドポイント
     * @param stream リクエストボディのInputStream
     * @param length InputStreamの長さ
     * @return HttpUriRequest
     */
    private HttpUriRequest getHttpMethod(String endpointPath, String contentType,
            HttpMethod method, InputStream stream, long length)
            throws IOException {

        InputStreamEntity entity = new InputStreamEntity(stream, length);
        entity.setContentType(contentType);
        switch (method) {
        case POST:
            HttpPost postMethod = new HttpPost(Constants.GRAPH_BASE_URL + endpointPath);
            postMethod.setEntity(entity);
            return postMethod;
        case PUT:
            HttpPut putMethod = new HttpPut(Constants.GRAPH_BASE_URL + endpointPath);
            putMethod.setEntity(entity);
            return putMethod;

        default:
            Log.e(TAG, "Unsupported http method");
            throw new IllegalArgumentException("Unsupported HttpMethod parameter:" + method);
        }

    }

    /**
     * 共通で設定するヘッダーを設定
     * 
     * @param method
     */
    private Header[] getCommonHeaders() {
        Header[] headers = new Header[3];
        headers[0] = new BasicHeader(AUTH_HEADER_NAME, "OAuth " + mAccessToken);
        headers[1] = new BasicHeader("User-Agent", Constants.USER_AGENT);
        headers[2] = new BasicHeader("Accept-Encoding", ZIP_ENCODING);
        return headers;
    }

    /**
     * サーバーへのリクエストを実行する
     * 
     * @param method 実行するhttpリクエスト
     * @param listener 完了時に実行される処理
     * @param doRetry リフレッシュトークンを取得するかの判定
     * @return サーバからのレスポンス
     * @throws FileNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     * @throws RemoteException
     * @throws ApiException
     */
    private String request(HttpUriRequest method,
            final CallbackListener listener, boolean doRetry)
                    throws FileNotFoundException, MalformedURLException,
                    IOException, JSONException, RemoteException, ApiException {
        HttpClient client = getHttpClient();
        HttpEntity entity = null;
        try {
            if (getAccessExpiresIn() < System.currentTimeMillis()
                        + mSocketTimeout + mConnectionTimeout) {
                refreshToken();
            }
            method.setHeaders(getCommonHeaders());
            HttpResponse res = client.execute(method);
            StatusLine status = res.getStatusLine();
            entity = res.getEntity();

            // gzipだった場合は戻す
            if (isGZipEntity(entity)) {
                entity = decompressesGZipEntity(entity);
            }
            String responseBody = null;
            switch (status.getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
                // OKならstringにして返して終了
                Log.v(TAG, "HTTP OK");
                return EntityUtils.toString(entity, HTTP.UTF_8);
            case HttpStatus.SC_UNAUTHORIZED:
                if (isExpiredToken(res, status) && doRetry) {
                    refreshToken();
                    entity.consumeContent();
                    entity = null;
                    return request(method, listener, false);
                }
                responseBody = EntityUtils.toString(entity, HTTP.UTF_8);
                if (responseBody != null && responseBody.length() > 0) {
                    throw new ApiException(status, responseBody);
                } else {
                    throw new ApiException(status, status.getReasonPhrase());
                }
            default:
                responseBody = EntityUtils.toString(entity, HTTP.UTF_8);
                if (responseBody != null && responseBody.length() > 0) {
                    throw new ApiException(status, responseBody);
                } else {
                    throw new ApiException(status, status.getReasonPhrase());
                }
            }
        } finally {
            // 後始末
            if (entity != null) {
                entity.consumeContent();
            }
            if (method != null) {
                method.abort();
            }
        }
    }

    /**
     * レスポンスのgzip判定
     * 
     * @param entity レスポンスのHttpEntity
     * @return gzipエンコーディングされているか
     */
    private boolean isGZipEntity(HttpEntity entity) {
        Header header = entity.getContentEncoding();
        if (header == null) {
            return false;
        }
        String value = header.getValue();
        return (value != null && value.contains(ZIP_ENCODING));
    }

    /**
     * gzip展開したHttpEntityを返す
     * 
     * @param entity 展開前のHttpEntity
     * @return 展開後のHttpEntity
     * @throws IllegalStateException
     * @throws IOException
     */
    private HttpEntity decompressesGZipEntity(HttpEntity entity)
            throws IllegalStateException, IOException {
        return new InputStreamEntity(new GZIPInputStream(entity.getContent()), 0);
    }

    private synchronized HttpClient getHttpClient() {
        if (mHttpClient != null) {
            return mHttpClient;
        }
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = defaultHttpClient.getConnectionManager();
        HttpParams params = defaultHttpClient.getParams();
        mHttpClient = new DefaultHttpClient(
                new ThreadSafeClientConnManager(params,
                        mgr.getSchemeRegistry()), params);
        mHttpClient.getParams().setParameter(HTTP_PARAM_CONNECTION_TIMEOUT, mConnectionTimeout);
        mHttpClient.getParams().setParameter(HTTP_PARAM_SOCKET_TIMEOUT, mSocketTimeout);
        return mHttpClient;
    }

    /**
     * レスポンスがトークン期限切れかをチェック
     * 
     * @param res
     * @param status
     * @return
     */
    private boolean isExpiredToken(HttpResponse res, StatusLine status) {
        // check WWW-Authenticate header and obtain error-reason if it exists
        Header[] headers = res.getHeaders("WWW-Authenticate");
        if (headers == null || headers.length == 0) {
            return false;
        }
        Header authHeader = headers[0];

        Log.v(TAG, "AUTH HEADER : " + authHeader.getName());
        for (HeaderElement elem : authHeader.getElements()) {
            Log.v(TAG, "AUTH HEADER ELEMENT : " + elem.getName());
            if ("OAuth error".equals(elem.getName())) {
                String reason = elem.getValue();
                Log.v(TAG, "reason: " + reason);
                if (reason.startsWith("'expired")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * リクエストをHandlerにpostする
     * 
     * @param method 実行するhttpリクエスト
     * @param listener 完了時に実行される処理
     * @param doRetry エラー時に再実行するか
     */
    private void requestAsync(final HttpUriRequest method,
            final CallbackListener listener, final boolean doRetry) {

        new AsyncRequester(method, listener, doRetry).execute();

    }

    private boolean startSingleSignOn(Activity activity, int activityCode, String[] permissions) {
        // ここで公式アプリにintentして認証チェック
        Intent intent = new Intent();
        intent.putExtra("mode", "authorize");
        intent.setClassName(OFFICIAL_PACKAGE, AUTH_ACTIVITY);
        intent.putExtra(CLIENT_ID, mClientId);
        // スコープの文字列を生成
        if (permissions != null && permissions.length > 0) {
            intent.putExtra("scope", TextUtils.join(" ", permissions));
        }
        // state生成
        intent.putExtra(STATE, String.valueOf(new Random().nextLong() >>> 1));

        if (!validateOfficialAppsForIntent(activity, intent, VALIDATE_OFFICIAL_FOR_ACTIVITY,
                SUPPORTED_VERSION)) {
            return false;
        }

        try {
            activity.startActivityForResult(intent, activityCode);
        } catch (ActivityNotFoundException e) {
            Log.v(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    private void setAccessToken(String token) {
        this.mAccessToken = token;
    }

    private String getAccessToken() {
        return this.mAccessToken;
    }

    private void setRefreshToken(String token) {
        this.mRefreshToken = token;
    }

    private String getRefreshToken() {
        return this.mRefreshToken;
    }

    /**
     * アクセストークンの有効期限を設定する
     * 
     * @param expiresIn mixiから取得した有効期限の文字列
     */
    private void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null && !expiresIn.equals("0")) {
            setAccessExpiresIn(System.currentTimeMillis()
                    + (Long.parseLong(expiresIn) * 1000));
       } else {
           setAccessExpiresIn(0L);
        }
    }

    /**
     * アクセストークンの有効期限を設定する
     * 
     * @param expiresIn 実際の有効期限のlong値
     */
    private void setAccessExpiresIn(Long expiresIn) {
        this.mExpiresIn = expiresIn;
    }

    /**
     * アクセストークンの有効期限を取得
     * 
     * @return 実際の有効期限msのlong値
     */
    private long getAccessExpiresIn() {
        return this.mExpiresIn;
    }

    /**
     * トークンをリフレッシュする
     * 
     * @throws RemoteException
     * @throws ApiException
     */
    private synchronized void refreshToken() throws RemoteException, ApiException {
        Log.v(TAG, "refreshToken start");
        Bundle bundle = new Bundle();
        bundle.putString(REFRESH_TOKEN, getRefreshToken());
        bundle.putString(CLIENT_ID, mClientId);

        // 公式アプリの処理を呼び出す
        if (mRemoteAuth == null) {
            Log.v(TAG, "RemoteAuthenticator is not bind");
            throw new ApiException(ErrorInfo.OFFICIAL_APP_NOT_FOUND,
                    "RemoteAuthenticator is not bind");
        }
        Bundle response = mRemoteAuth.tokenRefresh(bundle);

        if (response.containsKey(ERROR_STR)) {
            Log.w(TAG, response.getString(ERROR_STR));
            throw new ApiException(response.getInt(ERROR_CODE_STR),
                    response.getString(ERROR_MESSAGE_STR));
        }
        String accessToken = response.getString(ACCESS_TOKEN);
        String refreshToken = response.getString(REFRESH_TOKEN);
        String expire = response.getString(EXPIRES);
        setAccessToken(accessToken);
        setRefreshToken(refreshToken);
        setAccessExpiresIn(expire);
        if (mContextWrapper != null) {
            saveSession(mContextWrapper);
        }
    }

    private synchronized void setupAppCounter() {
        if (mAppsCounter == null) {
            mAppsCounter = new AppsCounter(new HttpPost(Constants.COUNTER_URL));
        }
        if (mRemoteAuth != null) {
            runAppCounter();
        }

    }

    private synchronized void setUpCounter() {
        if (mSelector == Config.APPLICATION) {
            if (mAppsStartCounter == null) {
                mAppsStartCounter = new AppsCounter(new HttpPost(Constants.APP_COUNTER_URL),
                        new CallbackListener() {
                            @Override
                            public void onFatal(ErrorInfo e) {
                            }

                            @Override
                            public void onError(ErrorInfo e) {
                            }

                            @Override
                            public void onComplete(Bundle values) {
                                mAppsStartCounter = null;
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
            }
            if (mRemoteAuth != null) {
                runAppCounter();
            }
        }
    }

    private synchronized void runAppCounter() {
        if (mAppsCounter != null) {
            mAppsCounter.execute();
        }
        if (mAppsStartCounter != null) {
            mAppsStartCounter.execute();
        }
    }

    /**
     * 非同期通信用のhttpリクエストを投げるためのクラス
     */
    private class AsyncRequester extends AsyncTask<Void, Void, Bundle> {
        private HttpUriRequest mMethod;
        private CallbackListener mListener;
        private boolean mDoRetry;
        private boolean mIsFatal;

        private ErrorInfo mException;

        public AsyncRequester(final HttpUriRequest method,
                final CallbackListener listener, final boolean doRetry) {
            mMethod = method;
            mListener = listener;
            mDoRetry = doRetry;
        }

        @Override
        protected Bundle doInBackground(Void... params) {
            try {
                Log.v(TAG, mMethod.getURI().toString());
                String response = request(mMethod, mListener, mDoRetry);
                Bundle bundle = new Bundle();
                bundle.putString(RESPONSE, response);
                return bundle;
            } catch (IOException e) {
                mIsFatal = true;
                mException = new ErrorInfo(e);
            } catch (JSONException e) {
                mIsFatal = true;
                mException = new ErrorInfo(e);
            } catch (RemoteException e) {
                mIsFatal = true;
                mException = new ErrorInfo(e);
            } catch (ApiException e) {
                mException = new ErrorInfo(e.getMessage(), e.getCode());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            if (result != null) {
                mListener.onComplete(result);
            } else {
                if (mIsFatal) {
                    mListener.onFatal(mException);
                } else {
                    mListener.onError(mException);
                }
            }
            runAppCounter();
        }
    }

    /**
     * APIエラー時のハンドリング用クラス
     * 
     */
    private class ApiException extends Exception {

        private static final long serialVersionUID = 5337015179934580406L;

        private int mCode;
        private String mMessage;

        public ApiException(int code, String message) {
            super();
            mCode = code;
            mMessage = message;
        }
        public ApiException(StatusLine status, String responseBody) {
            super();
            mCode = status.getStatusCode();
            if (responseBody != null && responseBody.length() > 0) {
                mMessage = responseBody;
            } else {
                mMessage = status.getReasonPhrase();
            }
        }

        @Override
        public String getMessage() {
            return mMessage;
        }

        public int getCode() {
            return mCode;
        }
    }

    @Override
    public void setupAd(Activity activity, AdParameter param) {
        WebView view = (WebView) activity.findViewById(R.id.webview);
        // view がない場合はなにもしない
        if (view == null) {
            Log.w(TAG, "webview not found.");
            return;
        }
        view.setWebViewClient(new MapWebViewClient(activity));
        if (param == null || param.headerPath == null) {
            Log.w(TAG, "path is null.");
            return;
        }
        view.loadUrl(param.headerPath);
        // アプリの場合のみ設定する
        if (mSelector == Config.APPLICATION) {
            setupAppCounter();
        }
    }

    private class AppsCounter {
        private final HttpUriRequest mMethod;
        private final CallbackListener mListener;

        public AppsCounter(final HttpUriRequest method) {
            mMethod = method;
            mListener = new CallbackListener() {
                @Override
                public void onFatal(ErrorInfo e) {
                }

                @Override
                public void onError(ErrorInfo e) {
                }

                @Override
                public void onComplete(Bundle values) {
                    mAppsCounter = null;
                }

                @Override
                public void onCancel() {
                }
            };
        }

        public AppsCounter(final HttpUriRequest method, CallbackListener listener) {
            mMethod = method;
            mListener = listener;
        }

        public void execute() {
            new Thread() {
                public void run() {
                    try {
                        request(mMethod, mListener, false);
                        mListener.onComplete(null);
                    } catch (Exception e) {
                        mListener.onError(null);
                    }
                }
            }.start();
        }
        
    }



    private class MapWebViewClient extends WebViewClient {
        private Context mContext;

        MapWebViewClient(Context context) {
            super();
            this.mContext = context;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // ブラウザに飛ばす
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }
    }

}
