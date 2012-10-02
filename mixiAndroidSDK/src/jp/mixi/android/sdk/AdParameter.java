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
 * アドプログラム用のパラメータクラス
 */
public class AdParameter {
    /**
     * htmlの配置してあるパス.
     * 
     * コピーする際にパスを変更する場合は、それに合わせたパスを設定すること デフォルトのままでアクセスするファイルは assets/mixi/header.html
     * 
     */
    public String headerPath = "file:///android_asset/mixi/header.html";
    private int version = 1;

    public int getVerion() {
        return version;
    }
}
