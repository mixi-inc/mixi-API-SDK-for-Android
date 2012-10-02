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
 * MixiContainerを生成するときのパラメータのクラス.
 * 
 * mixiアプリの場合はクライアントIDにアプリIDを指定してください。
 * GRAPH_APIの場合はクライアントIDにConsumer Keyを指定してください。
 */
public class Config {

    /** selectorのmixiアプリ用の値 */
    public static final int APPLICATION = 1;

    /** selectorのmixi Graph API用の値 */
    public static final int GRAPH_API = 2;

    /** クライアントID */
    public String clientId;

    /** アプリかGraph APIかのセレクタ */
    public int selector = APPLICATION;

    /**
     * http.socket.timeoutの設定.
     * Mixi GraphAPIにリクエストを送った際のレスポンスのtimeoutの設定(ms)
     * 初期値は20000msです。
     */
    public int connectionTimeout = 20000;

    /**
     * Mixi GraphAPIにリクエストを送る際のtimeoutの設定(ms)
     *  初期値は20000msです。
     */
    public int socketTimeout = 20000;

    public String version = Constants.VERSION;

}
