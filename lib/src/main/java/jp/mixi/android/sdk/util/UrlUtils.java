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
package jp.mixi.android.sdk.util;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 
 * 
 */
public class UrlUtils {

    private static final String PARAM_SEPARATOR = "&";
    private static final String EQUAL = "=";
    private static final String TAG = "UrlUtils";
    private UrlUtils() {

    }

    /**
     * mapからクエリパラメータを組み立てる
     * 
     * @param params パラメータのMap
     * @return 組み立てられたString
     */
    public static String encodeUrl(Map<String, String> params) {

        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String key : params.keySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(PARAM_SEPARATOR);
            }
            String value = params.get(key);
            if (key != null && value != null) {

                try {
                    builder.append(URLEncoder.encode(key, HTTP.UTF_8) + EQUAL
                            + URLEncoder.encode(params.get(key), HTTP.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        }
        return builder.toString();
    }

    /**
     * Bundleからクエリパラメータを組み立てる
     * 
     * @param params パラメータのBundle
     * @return 組み立てられたString
     */
    public static String encodeUrlForBundle(Bundle params) {
        if (params == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String key : params.keySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(PARAM_SEPARATOR);
            }
            try {
                builder.append(URLEncoder.encode(key, HTTP.UTF_8) + EQUAL
                        + URLEncoder.encode(params.getString(key), HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
        return builder.toString();
    }

    /**
     * クエリのパラメータをBundleに変換する
     * 
     * @param url 対象となるurl文字列
     * @return Bundle
     */
    public static Bundle decodeUrlToBundle(String url) {
        return decodeUrl(Uri.parse(url).getQuery());
    }

    /**
     * クエリのパラメータをBundleに変換する
     * 
     * @deprecated decodeUrlToBundleに変更
     * @param url 元の文字列
     * @return Bundle
     */
    @Deprecated
    public static Bundle dencodeUrlToBundle(String url) {
        return decodeUrl(url);
    }

    /**
     * クエリのパラメータをBundleに変換する
     * 
     * @param data 元の文字列
     * @return Bundle
     */
    private static Bundle decodeUrl(String data) {
        Bundle bundle = new Bundle();
        if (data != null) {
            String[] array = data.split(PARAM_SEPARATOR);
            for (String parameter : array) {
                String[] vals = parameter.split(EQUAL);
                if (vals.length == 2) {
                    try {
                        bundle.putString(URLDecoder.decode(vals[0], HTTP.UTF_8),
                                URLDecoder.decode(vals[1], HTTP.UTF_8));
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        return bundle;
    }
}
