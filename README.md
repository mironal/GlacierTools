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
2. Vaultの目録取得(Archiveの一覧取得)

##Jobの開始とは？
Amazon GlaierにJobの開始を指示することです。Amazon Glaierに指示を出すとすぐにJobのID等を含むレスポンスが来ます。この後4時間程度Jobの完了を待つ必要があります。
Jobの完了を検出するためにはAmazon SNS、Amazon SQSを使用する方法が推奨されていますが、ポーリングによる方法でも可能です。

このコマンドラインユーティリティーでは処理を簡潔にするためにポーリングを使用しています。SNS、SQSによる方法は別途提供予定です。

##Job出力の取得とは?
Job出力とはArchiveのデータそのものや、Archiveの一覧を含むテキストデータのことです。開始したJobが完了するとJob出力が取得可能になります。



#コマンドの使用方法
##コマンド構成
コマンドは以下の4つから構成されています。

1. vault_controller.jar
2. archive_controller.jar
3. job_operator.jar
4. restore_job.jar


## vault_controller.jar
Vaultに対する操作を行うコマンドです.以下の機能を提供します.

1. Vaultの作成
2. Vaultの詳細取得
3. Vaultの一覧取得
4. Vaultの削除

###使用方法
`java -jar vault_controller.jar cmd [--vault vaultname] [--region region] [--properties prop_filename]`

    cmd          : create | desc | list | delete | help
    --vault      : The name of the Vault.
    --region     : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1
    --properties : If you want to specify explicitly AwsCredentials.properties
    
###example
####Vaultの作成(Create Vault)
#####デフォルトのリージョン(us-east-1)にvaultnameというVaultを作成
    java -jar vault_controller.jar create --vault vaultname
    
##### 指定したリージョン(ap-northeast-1)にvaultnameというVaultを作成
    java -jar vault_controller.jar create --vault vaultname --region ap-northeast-1
#####指定したAwsCredentials.propertiesを使用してvaultnameというVaultを作成
    java -jar vault_controller.jar create --vault vaultname --properties myAwsPropFile.properties
#####リージョンとAwsCredentials.propertiesを指定してvaultnameというVaultを作成
    java -jar vault_controller.jar create --vault vaultname --region us-west-2 --properties myAwsPropFile.properties

####Vaultの詳細取得
#####デフォルトのリージョンのvaultnameというVaultの詳細を取得.
    java -jar vault_controller.jar desc --vault vaultname
#####リージョン指定
    java -jar vault_controller.jar desc --vault vaultname --region us_west-2

####Vault一覧を取得
#####デフォルトのリージョンのVault一覧を取得
    java -jar vault_controller.jar list
#####リージョン指定
    java -jar vault_controller.jar list --region ue-west-2
    
####Vault削除
#####デフォルトのリージョン(us-east-1)にvaultnameというVaultを削除
    java -jar vault_controller.jar delete --vault vaultname
#####リージョン指定
    java -jar vault_controller.jar delete --vault vaultname --region ap-northeast-1

## archive_controller.jar
Archiveに対する操作をするコマンドです.以下の機能を提供します.

1. Archiveのアップロード
2. Archiveのダウンロード
3. Archiveの削除

###使用方法
`java -jar archive_controller.jar cmd [--vault vaultname] [--archive archiveId] [--file filename] [--force] [--region region] [--properties prop_filename]`

    cmd          : upload | donwload | delete
    --vault      : The name of the Vault.
    --archive    : The ID of the archive.
    --file       : Specifies the name of a file that is uploaded when the upload. When the download is the name of the saved file.
    --force      : If there is a file with the same name at the time of download, Force overwrite.
    --region     : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1
    --properties : If you want to specify explicitly AwsCredentials.properties
    
###exsample
####Archiveのアップロード
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultに filenameのファイルをアップロード
    java -jar archive_controller.jar upload --vault vaultname --file filename
#####リージョン指定
    java -jar rchive_controller.jar upload --vault vaultname --file filename --region ap-northeast-1
#####AwsCredentials.properties指定
     java -jar rchive_controller.jar upload --vault vaultname --file filename --region ap-northeast-1 --properties myAwsPropFile.properties
#####リージョンとAwsCredentials.properties指定
     java -jar rchive_controller.jar upload --vault vaultname --file filename --region ap-northeast-1 --region us-west-2 --properties myAwsPropFile.properties
     
####Archiveのダウンロード
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultからarchiveIdのArchiveをfilenameというファイル名でダウンロード
    java -jar archive_controller.jar donwload --vault vaultname --file filename --archive archiveId
#####リージョン指定
    java -jar archive_controller.jar donwload --vault vaultname --file filename --archive archiveId --region ap-northeast-1    
#####ダウンロード先に同名のファイルが在った場合、上書きする
    java -jar archive_controller.jar donwload --vault vaultname --file filename -force --archive archiveId
    
※ --forceオプションを指定するとダウンロード開始前に該当ファイルを削除します.
※ ダウンロード完了まで4時間程度掛かります.

####Archiveの削除
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultのarchiveIdのArchiveを削除
    java -jar archive_controller.jar delete --vault vaultname --archive archiveId
#####リージョン指定
    java -jar rchive_controller.jar delete --vault vaultname --archive archiveId --region ap-northeast-1

## job_operator.jar
Jobに対する操作を行うコマンドです.以下の機能を提供します.

1. Jobの開始
  1. Vaultの目録取得
  2. Archiveのダウンロード
2. Job出力の取得
3. Jobの詳細取得
4. Jobの一覧取得
5. Jobの待機

Jobの開始の際に--asyncオプションをつけることによって、Jobの完了を待機せずにプログラムを終了できます.終了時にJobの復元に必要なパラメータが出力されるので、restore_job.jarと組み合わせることで非同期的にJobの制御を行うことができます.

###使用方法
`java -jar job_operator.jar cmd [--vault vaultname]　[--archive archiveId] [--file filename] [--job jobId] [--region region] [--properties prop_filename] [--async]`

    cmd          : inventory | archive | list | desc | help
    --vault      : The name of the Vault.
    --archive    : The ID of the Archive.
    --job        : The ID of the Job.
    --file       : Save file name.
    --region     : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1
    --properties : If you want to specify explicitly AwsCredentials.properties.
    
###example
####Vaultの目録取得(Get Vault inventory)
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultの目録を取得
    java -jar job_operator.jar inventory --vault vaultname
##### リージョン指定
    java -jar job_operator.jar inventory --vault vaultname --region ap-northeast-1

####Archiveのダウンロード(Download Archive)
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultにあるarchiveIdのArchiveをfilenameという名前でダウンロード
    java -jar job_operator.jar archive --vault vaultname --archive archiveId --file filename
##### リージョン指定
    java -jar job_operator.jar archive --vault vaultname --archive archiveId --file filename --region ap-northeast-1
    
####Jobの一覧を取得(Get Job list)
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultのJob一覧を取得
    java -jar job_operator.jar list --vault vaultname
##### リージョン指定
    java -jar job_operator.jar list --vault vaultname --region ap-northeast-1
    
※取得できるのは進行中、または最近完了したJobの一覧です.

####Jobの詳細を取得(Get Job Describe )
#####デフォルトのリージョン(us-east-1)のvaultnameというVaultのjobIdのJobの詳細を取得
    java -jar job_operator.jar desc --vault vaultname --job jobId
##### リージョン指定
    java -jar job_operator.jar desc --vault vaultname --job jobId --region ap-northeast-1


## restore_job.jar
job_operator.jarに--asyncオプションを指定して実行したJobの復元を行います.

1. Job出力の取得(donload)
2. Jobが完了しているかチェック.


###使用方法
`java -jar restore_job.jar cmd [--restore restore_prop_filename] [--properties prop_filename]`

    cmd          : download | check | desc
    --restore    : 
    --properties : If you want to specify explicitly AwsCredentials.properties
    