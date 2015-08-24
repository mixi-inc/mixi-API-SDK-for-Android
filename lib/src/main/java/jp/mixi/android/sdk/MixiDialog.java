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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.mixi.android.sdk.util.UrlUtils;

/**
 * 
 * WebVewダイアログ表示クラス
 */
@TargetApi(8)
class MixiDialog extends Dialog {
    private static final String TAG = "MixiDialog";

    /** 読み込みURL */
    private String mUrl;

    /** 結果をハンドリングするリスナー */
    private CallbackListener mListener;

    private Map<String, String> mParams;

    private Handler mHandler = new Handler();

    /**
     * ダイアログのオブジェクトを生成する
     * 
     * コンテキストは呼び元のthisを指定してください。
     * 
     * @param context 特に意図がない場合は、呼び元のActivityを指定してください。
     * @param url ダイアログにWebViewで表示したいページのURLを指定してください。
     * @param params パラメータ
     * @param listener ダイアログの処理が完了した際の動作を記述したDialogListenerを指定してください。
     * @param isCancelable falseの場合、バックボタンが無効になります。
     */
    public MixiDialog(Context context, String url,
            Map<String, String> params, CallbackListener listener, boolean isCancelable) {
        super(context, R.style.Theme_MixiDialogTheme);
        this.mUrl = url;
        this.mListener = listener;
        this.mParams = params;
        this.setCancelable(isCancelable);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.client_authentication_dialog);
        setUpWebView((WebView) findViewById(R.id.webview));

    }

    @SuppressLint("JavascriptInterface")
    private WebView setUpWebView(WebView webView) {
        Log.v(TAG, "setwebview");
        webView.getSettings().setUserAgentString(Constants.USER_AGENT);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new MixiWebviewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsHandler(), "mixi");

        String token = mParams.get("oauth_token");
        int sdkInt = Integer.valueOf(Build.VERSION.SDK);
        // android2.1対応
        if (sdkInt < Constants.SUPPORTED_SDK_VERSION) {
            Uri uri = Uri.parse(mUrl);
            if (token != null) {
                Builder builder = uri.buildUpon();
                builder.appendQueryParameter("oauth_token", token);
                uri = builder.build();
            }
            webView.loadUrl(uri.toString());
        } else {
            HashMap<String, String> extraHeaders = new HashMap<String, String>();
            if (token != null) {
                extraHeaders.put("Authorization", "OAuth " + token);
            }
            webView.loadUrl(mUrl, extraHeaders);
        }

        return webView;
    }

    private void showLoading(int resId) {
        showLoading(getContext().getText(resId).toString());
    }

    private void showLoading(String message) {
        ((TextView) findViewById(R.id.progressLabel)).setText(message);

        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        progress.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));

        View webView = findViewById(R.id.webview);
        webView.setVisibility(View.GONE);
        webView.startAnimation(AnimationUtils.loadAnimation(getContext(),
                android.R.anim.fade_out));
    }

    private void hideLoading() {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        progress.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));

        View webView = findViewById(R.id.webview);
        webView.setVisibility(View.VISIBLE);
        webView.requestFocus(View.FOCUS_DOWN);

        webView.startAnimation(AnimationUtils.loadAnimation(getContext(),
                android.R.anim.fade_in));
    }

    /**
     * 最初にロードしたスレッドからのみ操作が可能なので用意
     * 
     * @param url url
     * @param data リクエストボディ
     */
    private void postUrl(final String url, final byte[] data) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                WebView webView = (WebView) findViewById(R.id.webview);
                webView.postUrl(url, data);

            }
        });
    }

    /**
     * ダイアログ内に表示されるWebViewのClient
     */
    private final class MixiWebviewClient extends WebViewClient {
        private static final String TAG = "MixiWebviewClient";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // リダイレクト処理を一部独自に実行する
            Log.d(TAG, "redirect:" + url);
            // 正常取得用URLなら処理をする
            if (url.startsWith(Constants.REDIRECT_URI)) {
                Bundle values = UrlUtils.decodeUrlToBundle(url);
                Log.v(TAG, "onComplete");
                MixiDialog.this.dismiss();
                mListener.onComplete(values);
                return true;
            } else if (url.startsWith(Constants.ERROR_URI)) {
                Bundle values = UrlUtils.decodeUrlToBundle(url);
                String error = values.getString("error");
                String errorCd = values.getString("code");
                MixiDialog.this.dismiss();
                if (errorCd == null) {
                    mListener.onError(new ErrorInfo(error));
                } else {
                    mListener.onError(new ErrorInfo(error, Integer.parseInt(errorCd), url));
                }
                return true;
            } else if (url.startsWith(Constants.CANCEL_URI)) {
                Log.v(TAG, "onCancel");
                MixiDialog.this.dismiss();
                mListener.onCancel();
                return true;
            } else if (url.startsWith(Constants.GRAPH_BASE_URL)) {
                // 内部遷移はそのまま
                Log.v(TAG, "inner redirect");
                MixiDialog.this.dismiss();
                return false;
            }
            Log.v(TAG, "nomal redirect");
            // 関係ないURLはIntent.ACTION_VIEWで処理する
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            MixiDialog.this.dismiss();
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            hideLoading();
            MixiDialog.this.dismiss();
            mListener.onFatal(new ErrorInfo(description, errorCode, failingUrl));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // 読込中はプログレスバーを表示しておく
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "page start");
            showLoading(R.string.loading_message);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "load success");
            // 読み込みが終わったらプログレスバーを消去
            hideLoading();
            super.onPageFinished(view, url);
        }
    }

    /**
     * 
     */
    public class JsHandler {

        /**
         * webView上に表示されてるボタンに
         * 
         * onClick(mixi.post(パラメータ))
         * 
         * と書かれていて、そのjsがキックされたときに、このメソッドが実行される
         * 
         * @param endpoint 実際に叩くAPIのエンドポイント(固定だからパラメータとしてはいらないのかも)
         * @param params パラメータをJSON形式でもらう
         */
        public void post(String endpoint, String params) {
            try {
                // POST用のパラメータを組み立てる
                JSONObject root = new JSONObject(params);
                Builder builder = new Builder();
                builder.appendQueryParameter("oauth_token", mParams.get("oauth_token"));
                for (@SuppressWarnings("unchecked")
                Iterator<String> iterator = root.keys(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    builder.appendQueryParameter(key,
                            URLEncoder.encode(root.getString(key), HTTP.UTF_8));
                }

                // webViewで表示
                postUrl(Constants.GRAPH_BASE_URL + endpoint,
                        builder.build().getQuery().getBytes());
            } catch (JSONException e) {
                MixiDialog.this.dismiss();
                mListener.onFatal(new ErrorInfo(e));
            } catch (UnsupportedEncodingException e) {
                MixiDialog.this.dismiss();
                mListener.onFatal(new ErrorInfo(e));
            }
        }
    }
}
