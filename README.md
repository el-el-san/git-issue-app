# GitHub Issue Manager - Flutter

GitHubのデザインにインスパイアされた美しいダークテーマを持つ、GitHubイシュー管理用Flutterモバイルアプリケーションです。

## 機能

- 🔐 **セキュアな認証**
  - Personal Access Token（推奨）
  - Device Flow OAuth (作成中）
  - セキュアなトークン保存

- 📋 **イシュー管理**
  - イシューの表示（全て、オープン、クローズ）
  - ラベル付きでの新規イシュー作成
  - イシューの詳細とコメントの表示
  - イシューへのコメント追加
  - ページネーション付き無限スクロール

- ⚙️ **イシュー作成オプション**
  - 現在日時をタイトルに自動追加
  - 任意のカスタムテキストを説明文に自動追加
  - 設定の永続化

- 🎨 **美しいUI**
  - GitHubインスパイアのダークテーマ
  - Material Design 3コンポーネント
  - スムーズなアニメーションとトランジション
  - レスポンシブレイアウト

- 🔒 **セキュリティ**
  - トークンのセキュア保存
  - 環境ベースの設定
  - ハードコードされた秘密情報なし

## はじめに

### 前提条件

- GitHubアカウントとPersonal Access Token
- Android (iOS 未対応）

### App インストール

Releases Latest の app-release.apk をダウンロードしインストール 

## 認証

### Personal Access Token（推奨）

1. GitHub設定 > Developer settings > Personal access tokensに移動
2. `repo`スコープで新しいトークンを生成
3. アプリでトークンを使用してログイン

### Device Flow　（対応中）

1. アプリで「Device Flowを開始」をクリック
2. 提供されたコードをコピー
3. github.com/login/deviceにアクセス
4. コードを入力
5. アプリを認証

## アーキテクチャ

- **状態管理**: Provider
- **ナビゲーション**: go_router
- **HTTP クライアント**: Dio
- **ストレージ**: flutter_secure_storage + shared_preferences
- **UI**: Material Design 3

## セキュリティ

- GitHub Client IDはハードコードされていません
- トークンはセキュアに保存されます
- Apk ビルドはGithub Actionsによって実施されます。 GH_CLIENT_ID にてSecretsにClientID設定が必要です

## ライセンス

このプロジェクトはMITライセンスの下でライセンスされています - 詳細はLICENSEファイルを参照してください。
