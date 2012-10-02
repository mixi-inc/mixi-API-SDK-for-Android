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
 * SDK内で使用する定数等
 */
class Constants {
    /** APIの成功時のリダイレクトURI */
    static final String REDIRECT_URI = "mixi-connect://success";
    /** APIのキャンセル時のリダイレクトURI */
    static final String CANCEL_URI = "mixi-connect://cancel";
    /** APIのエラー時のリダイレクトURI */
    static final String ERROR_URI = "mixi-connect://error";

    static final String GRAPH_BASE_URL = "https://api.mixi-platform.com/2";

    static final String COUNTER_URL = GRAPH_BASE_URL + "/apps/user/count";
    static final String APP_COUNTER_URL = GRAPH_BASE_URL + "/apps/user/count/all";
    /** android2.2のSDKバージョン番号 */
    static final int SUPPORTED_SDK_VERSION = 8;
    /** mixiAndroidSDKのバージョン番号 */
    static final String VERSION = "1.8";
    /** SDKからアクセスする際のユーザーエージェント */
    static final String USER_AGENT =
            System.getProperties().getProperty("http.agent") + " mixi-Android-SDK/" + VERSION;

    /** instanceは作らない */
    private Constants() {
    }
}
