#これは何？
Amazon Glacierを制御する為のユーティリティーコマンドです。Javaで実装されているため実行環境を気にせず使用することができます。

Amazon Glacierとは大容量かつ、ほとんどアクセスしないデータの保存に適した低価格なストレージサービスです。保存したデータはAmazonのストレージ内で冗長化され破損する確率は極めて低くなっています。
詳細は[Amazon Glacier](http://aws.amazon.com/jp/glacier/)を参照して下さい。


#何が出来るの？
Amazon Glaierが提供する機能の殆どをコマンドライン形式で使用することができます。

以下に現在対応している機能の一覧を紹介します。Amazon Glaier独自の用語が沢山出てきますが、後ほど説明します。

##現在提供している機能
###Vaultに対する操作
* 作成
* 削除
* 詳細取得
* 一覧取得

###Archiveに対する操作 
* アップロード
* ダウンロード
* 削除

##Jobに対する操作
* Jobの開始
* Job出力の取得
* Jobの詳細取得
* Jobの一覧取得


#用語の説明
##Archive
ArchiveはAmazon Glaierに保存するデータの単位です。一つのテキストファイルをArchiveに保存することもできますし、大量の画像ファイルを一つにまとめた後(zip等)Archiveに保存することもできます。
##Vault
VaultはArchiveを保存しておく箱です。
1つのVaultには0個以上のArchiveが保存されています。

##Job
JobはAmazon Glaierに保存したArchiveやArchiveの一覧を取得するための作業の単位です。Jobは次の非同期的な２段階プロセスで構成され、開始してから出力の取得が可能になるまで（1～2の間)には通常4時間程度待つ必要があります。

1. Jobの開始
2. Job出力の取得

また、Jobには現在以下の種類があります。
1. Archiveのダウンロード
2. Archiveの一覧取得

##Jobの開始とは？
Amazon GlaierにJobの開始を指示することです。Amazon Glaierに指示を出すとすぐにJobのID等を含むレスポンスが来ます。この後4時間程度Jobの完了を待つ必要があります。
Jobの完了を検出するためにはAmazon SNS、Amazon SQSを使用する方法が推奨されていますが、ポーリングによる方法でも可能です。

このコマンドラインユーティリティーでは処理を簡潔にするためにポーリングを使用しています。SNS、SQSによる方法は別途提供予定です。

##Job出力の取得とは?
Job出力とはArchiveのデータそのものや、Archiveの一覧を含むテキストデータのことです。開始したJobが完了するとJob出力が取得可能になります。



#コマンドの使用方法
##コマンド構成
コマンドは以下の3つから構成されています。

1. vault_controller.jar
2. archive_controller.jar
3. job_controller.jar

### vault_controller.jar
Vaultに対する操作を行うコマンドです.
####使用方法
java -jar vault_controller.jar cmd [--vault vaultname]
