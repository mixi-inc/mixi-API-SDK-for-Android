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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

/**
 * android版mixiアプリ、mixi Graph APIの認証、APIコールを行うためのインターフェイス.
 * 
 * MixiContainerFactoryを使用してインスタンスを取得するようにしてください。
 */
public interface MixiContainer {

    /**
     * mixi Graph APIの認可処理を行う
     * 
     * この処理の後に、各APIが呼び出せるようになる。
     * 
     * @param activity 呼び出し元のactivity
     * @param permissions 認可したいスコープ
     * @param activityCode {@link Activity#onActivityResult}のrequestCode
     * @param listener 認可処理完了後に呼び出すコールバッククラス
     */
    void authorize(Activity activity, String[] permissions,
            int activityCode, final CallbackListener listener);

    /**
     * ダイアログを出す
     * 
     * バックボタンは有効です。
     * 
     * @param context ダイアログを表示するコンテキスト
     * @param action 表示対象のアクションを指定(invite等)
     * @param parameters 呼び出すアクションに渡すパラメータ
     * @param listener 処理完了時、またはエラー時に実行される処理を記述してください
     * 
     */
    void showDialog(Context context, String action, Map<String, String> parameters,
            final CallbackListener listener);

    /**
     * ダイアログを出す
     * 
     * @param context ダイアログを表示するコンテキスト
     * @param action 表示対象のアクションを指定(invite等)
     * @param parameters 呼び出すアクションに渡すパラメータ
     * @param listener 処理完了時、またはエラー時に実行される処理を記述してください
     * @param isCancelable falseの場合、バックボタンが無効になります。
     * 
     */
    void showDialog(Context context, String action, Map<String, String> parameters,
            final CallbackListener listener, boolean isCancelable);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * GETでリクエストを実行する。 パラメータは特に指定しない場合に使用する。
     * 
     * ex)send("people/@me",listener);
     * 
     * などを指定する場合に使用する
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * GETでリクエストを実行する。 parametersにパラメータを指定する
     * 
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param parameters リクエストに付加するパラメータのmap
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath,
            Map<String, String> parameters, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * httpメソッドはパラメータにて指定されたもので実行される
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param method httpメソッド
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath,
            HttpMethod method, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * httpメソッドはパラメータにて指定されたもので実行される パラメータは
     * <p>
     * name1=value1&name2=value2
     * </p>
     * のように展開される
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param method httpメソッド
     * @param parameters リクエストに付加するパラメータのmap
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath, HttpMethod method,
            Map<String, String> parameters, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * photoAPIなど、バイナリデータをリクエストに含む場合に使用する。
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param contentType コンテントタイプ
     * @param stream リクエストボディとなるデータのInputStream
     * @param length streamの長さ
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath, String contentType,
            InputStream stream, long length, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * photoAPIなど、バイナリデータをリクエストに含む場合に使用する。
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param contentType コンテントタイプ
     * @param method httpメソッド
     * @param stream リクエストボディとなるデータのInputStream
     * @param length streamの長さ
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath, String contentType, HttpMethod method,
            InputStream stream, long length, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * パラメータにjsonを指定する場合に使用する リクエストボティはjson.toString()の結果が展開される。
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param json json形式のパラメータ
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath,
            JSONObject json, CallbackListener listener);

    /**
     * endpointPathで指定されたエンドポイントへのリクエストを実行する.
     * 
     * パラメータにjsonを指定する場合に使用する リクエストボティはjson.toString()の結果が展開される。
     * 
     * @param endpointPath リクエストを実行するエンドポイント
     * @param method httpメソッド
     * @param json json形式のパラメータ
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void send(String endpointPath, HttpMethod method,
            JSONObject json, CallbackListener listener);

    /**
     * 認証のintentから戻ってきた際に実行するメソッド.
     * 
     * 認証は公式アプリ、またはmixiの提供するアカウント管理用アプリにて認証されます。
     * authorizeメソッドを実行すると認証のためのintentがstartActivityForResult()で実行されます。
     * その後、authorizeを呼び出したActivityのonActivityResultが自動的に実行されますので、 onActivityResultにこのメソッドを指定してください。
     * onActivityResultを下記のように指定してください。
     * ひとつのActivityで複数のintentを指定することがある場合は、requestCodeで判定を行うようにしてください。
     * 
     * <pre>
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     container.authorizeCallback(requestCode, resultCode, data);
     * }
     * </pre>
     * 
     * @param requestCode {@link Activity#onActivityResult}のrequestCode
     * @param resultCode intent結果のコード値
     * @param data intent先から値を返すためのオブジェクト
     */
    void authorizeCallback(int requestCode, int resultCode, Intent data);

    /**
     * 認証済みかを判定する.
     * 
     * 
     * @return 認証済みの場合true
     */
    boolean isAuthorized();

    /**
     * 認証を解除する
     * 
     * @param activity 呼び出し元のactivity
     * @param activityCode {@link Activity#onActivityResult}のrequestCode
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    void logout(Activity activity, int activityCode, CallbackListener listener);

    /**
     * MixiContainerの初期処理を行う.
     * 
     * send,authorize,showDialog,logoutを呼ぶActivityは必ずこのメソッドを呼んでください。
     * このメソッドがfalseを返した場合は、MixiContainerの各メソッドでエラーが発生する可能性があります。
     * また、サービスのbindなので、このメソッドを呼んだ直後は使えないので注意してください。
     * 実際に使える様になるタイミングはandroidのActivity#bindServiceドキュメントを参照してください。
     * 引数のActivityは、各APIを呼び出す際に必要になることがあるので、 アプリケーションが起動している間はonDestroyされないようにしてください。
     * 
     * @param contextWrapper MixiContainerを使用するcontextWrapper
     * @return 成功した場合にtrue
     * @see Activity#bindService
     */
    boolean init(ContextWrapper contextWrapper);

    /**
     * MixiContainerの終了処理を行う.
     * 
     * initを呼んだContextWrapperはonDestroyの中でこのメソッドを呼んでください。
     * 呼ばない場合はandroid.app.ServiceConnectionLeakedが発生します
     * 
     * @param contextWrapper MixiContainerを使用したcontextWrapper
     * @see Activity#unbindService
     */
    void close(ContextWrapper contextWrapper);

    /**
     * 課金APIの呼び出しを行うメソッド
     * 
     * CallbackListenerは{@link #paymentCallback(int, int, Intent)}内で実行されるので必ず
     * {@link #paymentCallback(int, int, Intent)}を
     * {@link Activity#onActivityResult(int requestCode, int resultCode, Intent data)}内で呼ぶようにしてください
     * 
     * @param activity 呼び出し元のactivity
     * @param param payment用パラメータ
     * @param activityCode {@link Activity#onActivityResult}のrequestCode
     * @param listener 完了時に実行する処理を記述したコールバッククラス
     */
    public void requestPayment(final Activity activity, PaymentParameter param,
            int activityCode, final CallbackListener listener);

    /**
     * 
     * 課金APIを呼び出し、処理が完了した際に実行するメソッド.
     * 
     * 課金処理は公式アプリにて実行されます。
     * {@link #requestPayment(Activity, PaymentParameter, int, CallbackListener)}
     * メソッドを実行すると課金のためのintentが{@link Activity#startActivityForResult(Intent, int)}で実行されます。 その後、
     * {@link Activity#onActivityResult(int, int, Intent)}が実行されます requestCodeは、
     * {@link MixiContainer#authorize(Activity, String[], int, CallbackListener)}、
     * {@link MixiContainer#logout(Activity, String[], int, CallbackListener)}と同じ値にならないようにしてください。
     * 
     * 
     * <pre>
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     container.authorizeCallback(requestCode, resultCode, data);
     *     container.paymentCallback(requestCode, resultCode, data);
     * }
     * </pre>
     * 
     * @param requestCode {@link Activity#onActivityResult}のrequestCode
     * @param resultCode intent結果のコード値
     * @param data intent先から値を返すためのオブジェクト
     */
    public void paymentCallback(int requestCode, int resultCode, Intent data);

    /**
     * アドプログラムの設定を行うメソッド.
     * 
     * このメソッドを呼び出すことにより、アドプログラム用の設定がされる
     * 
     * @param activity アドプログラム用表示を行うactivity
     * @param param アドプログラム用パラメータ
     */
    void setupAd(Activity activity, AdParameter param);
}
