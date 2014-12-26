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
 * {@link MixiContainer#requestPayment}に渡すパラメータのクラス
 */
public class PaymentParameter {

    /** clientIdのbundle用名称 */
    static final String CLIENT_ID_NAME = "client_id";
    /** inventoryCodeのbundle用名称 */
    static final String INVENTORY_CODE_NAME = "inventory_code";
    /** signatureのbundle用名称 */
    static final String SIGNATURE_NAME = "signature";
    /** callbackUrlのbundle用名称 */
    static final String CALLBACK_URL_NAME = "callback_url";
    /** itemIdのbundle用名称 */
    static final String ITEM_ID_NAME = "item_id";
    /** itemNameのbundle用名称 */
    static final String ITEM_NAME_NAME = "item_name";
    /** itemPriceのbundle用名称 */
    static final String ITEM_PRICE_NAME = "item_price";
    /** isTestのbundle用名称 */
    static final String IS_TEST_NAME = "is_test";
    static final String VERSION_NAME = "version";
    /** mixiサーバから呼び出されるバックエンドサーバのURL */
    public String callbackUrl;
    /** 決済処理のテストの場合は”true”, 実際の決済の場合は”false” */
    public boolean isTest;
    /** アイテムID */
    public String itemId;
    /** アイテム名 */
    public String itemName;
    /** アイテムのポイント数 */
    public int itemPrice;
    /** バックエンドサーバ側でこの決済を特定するための任意の文字列 */
    public String inventoryCode;
    /** 署名 */
    public String signature;
    private int version = 1;

    /**  */
    public int getVertion() {
        return version;
    }
}
