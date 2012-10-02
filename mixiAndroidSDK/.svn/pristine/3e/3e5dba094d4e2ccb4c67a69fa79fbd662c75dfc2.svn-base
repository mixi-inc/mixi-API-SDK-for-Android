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

/**
 * 
 */
public class ErrorInfo {

    /** 通信エラー */
    public static final int NETWORK_ERROR = 1;
    /** サーバーエラー */
    public static final int SERVER_ERROR = 2;
    /** 公式アプリが入っていない */
    public static final int OFFICIAL_APP_NOT_FOUND = 3;
    /** 公式アプリの有効なログイン情報が端末上にない */
    public static final int OFFICIAL_APP_NOT_AUTHORIZE = 4;
    /** 公式アプリの持つログイン情報が既に無効 */
    public static final int OFFICIAL_APP_EXPIRED = 5;
    /** アプリケーションの認可が取り消されている */
    public static final int REVOKE_AUTHORIZE = 6;
    /** 公式アプリで発生した、その他のエラー */
    public static final int OTHER_ERROR = 99;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String mMessage;
    private int mErrorCode = 0;
    private String mUrl;
    private Exception mException;

    /**
     * メッセージだけを受け取るのコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public ErrorInfo(String message) {
        this.mMessage = message;
    }

    /**
     * メッセージとエラーコードだけを受け取るのコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param errorCode エラーコード
     */
    public ErrorInfo(String message, int errorCode) {
        this.mMessage = message;
        this.mErrorCode = errorCode;
    }

    /**
     * Exceptionを受け取るコンストラクタ Exceptionが発生している場合のエラー
     * 
     * @param e Exception
     */
    public ErrorInfo(Exception e) {
        this.mMessage = e.getMessage();
        this.mException = e;
    }

    /**
     * メッセージ、エラーコード、URLを受け取るコンストラクタ
     * 
     * @param message エラー内容
     * @param errorCode エラーコード(httpエラーのコード値)
     * @param failingUrl エラーの発生したURL
     */
    public ErrorInfo(String message, int errorCode, String failingUrl) {
        this.mErrorCode = errorCode;
        this.mMessage = message;
        this.mUrl = failingUrl;
    }

    /**
     * エラーメッセージを取得する
     * 
     * @return エラーメッセージ
     */
    public String getMessage() {
        if (mUrl == null) {
            return mMessage;
        }
        return mMessage + ":" + mUrl;
    }

    /**
     * エラーコードを取得するメソッド
     * 
     * @return HTTPコード
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * Exceptionを持っているか
     * 
     * @return Exceptionの情報を持っている場合はtrue
     */
    public boolean hasException() {
        if (mException == null) {
            return false;
        }
        return true;
    }

    /**
     * 発生したエラーのExceptionを返す
     * 
     * @return 発生したException
     */
    public Exception getException() {
        return mException;
    }
}
