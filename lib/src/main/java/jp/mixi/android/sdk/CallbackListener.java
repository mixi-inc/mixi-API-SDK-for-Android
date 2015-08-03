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

import android.os.Bundle;



/**
 * mixi android SDKで表示されるダイアログの結果によって分ける処理
 * 
 * 
 * 
 * 
 */
public interface CallbackListener {
    /**
     * 成功時に行われる処理
     * 
     * 処理成功時に行う処理を記述する。
     * 
     * @param values ログイン成功時のパラメータ
     */
    void onComplete(Bundle values);


    /**
     * キャンセルされた場合の処理
     * 
     * キャンセルされた場合に行う処理を記述する。
     * 
     */
    public void onCancel();

    /**
     * エラー発生時の処理
     * 
     * エラー発生時の処理を記述する。
     * 
     * @param e 発生元のExceptionを含むErrorInfo
     */
    void onFatal(ErrorInfo e);

    /**
     * mixi Graph APIのアクセスでエラーが発生した場合
     * 
     * @param e エラーコードとメッセージが含まれるErrorInfo
     */
    void onError(ErrorInfo e);

}
