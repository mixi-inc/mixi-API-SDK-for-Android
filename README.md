mixi API SDK for Android
========================

Graph API を利用した Android アプリケーションの開発用に SDK を提供しています。  
このSDKを利用することで、OAuth 2.0の認証/認可の手順を実装する必要がなくなり、みなさんが開発するアプリケーションにソーシャル機能を簡単に組み込むことができるようになります。

特徴
----
mixi API SDK for Android™ の特徴は、以下の通りです。
 - 個人・法人に関わらず、パートナー登録すればどなたでも開発可能
 - OAuth 2.0の認証/認可手順の実装が不要
 - Tokenの取得/更新を自動化
 - APIコールを統一的な手順で実行可能
 - シングルサインオンが可能でユーザ認可時のパスワード入力不要
 - アプリケーション内にsecretを埋め込む必要がないため、セキュリティ向上

ダウンロード
-------

 - Gradle

```groovy
compile 'jp.mixi:mixi-api-sdk-android:1.8.0'
```

サポート端末
------------
本SDKにてサポートするAndroid端末は以下の通りです。
 - Android 2.1以降
 - Google Playが利用可能な端末

利用可能なAPI
-------------
 - [People API][1]
 - [Groups API][2]
 - [People lookup API][3]
 - [Voice API][4]
 - [Updates API][5]
 - [Check API][6]
 - [Photo API][7]
 - [Message API][8]
 - [Diary API][9]
 - [Check-in API][10]

※基本的に提供されているすべての Graph API が利用可能です。

License
-------
Apache License, Version 2.0  
http://www.apache.org/licenses/


[1]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/people-api/
[2]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/groups-api/
[3]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/people-lookup-api/
[4]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/voice-api/
[5]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/updates-api/
[6]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/check-api/
[7]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/photo-api/
[8]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/message-api/
[9]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/diary-api/
[10]:http://developer.mixi.co.jp/connect/mixi_graph_api/mixi_io_spec_top/check-in-api/
