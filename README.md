# CreatorBoard
　※コードはClaude AIに書かせてます。
> 楽曲制作のすべてを、一つの場所で。

Ableton Live ユーザー向けの楽曲制作管理 Web アプリケーションです。  
プロジェクトのカンバン管理から `.als` ファイルの自動解析まで、制作ワークフローを一元化します。

---

## スクリーンショット

### ログイン画面
<!-- スクリーンショットを撮影後、以下のパスに画像を配置してください -->
<!-- ![ログイン画面](docs/screenshots/login.png) -->

### ダッシュボード
<!-- ![ダッシュボード](docs/screenshots/dashboard.png) -->

### Ableton Analyzer
<!-- ![Analyzer](docs/screenshots/analyzer.png) -->

---

## 機能一覧

- **ユーザー認証** — 新規登録・ログイン・ログアウト（Spring Security + BCrypt）
- **プロジェクト管理** — カンバン形式（未着手・進行中・完了）でプロジェクトを管理
- **フェーズ管理** — 作曲中・編曲中・ミキシング中など制作フェーズを記録
- **メモ機能** — プロジェクトごとに自由記述のメモを保存
- **Ableton Analyzer** — `.als` ファイルをアップロードして BPM・トラック・使用デバイスを自動解析
- **ダッシュボード** — プロジェクト進捗サマリー・フェーズ別グラフ・解析履歴を一覧表示

---

## 技術スタック

### バックエンド
| 技術 | 用途 |
|---|---|
| Java 21 | メイン言語 |
| Spring Boot 3 | Web フレームワーク |
| Spring Security | 認証・認可 |
| Spring Data JPA | DB アクセス |
| Thymeleaf | テンプレートエンジン |
| Python 3 / Flask | .als ファイル解析 API |

### フロントエンド
| 技術 | 用途 |
|---|---|
| HTML / CSS / JavaScript | UI 実装 |
| Chart.js | グラフ描画 |

### データベース
| 環境 | 使用 DB |
|---|---|
| 開発（学校） | H2（インメモリ） |
| 本番（自宅） | MySQL |

---

## システム構成

```
ブラウザ
  └── Spring Boot（ポート 8080）
        ├── 認証・プロジェクト管理・ダッシュボード
        └── Flask（ポート 5000）へ HTTP 通信
              └── .als ファイル解析（parser_engine.py）
```

---

## セットアップ手順

### 必要な環境

- Java 21 以上
- Maven
- Python 3.x / Anaconda
- MySQL（自宅環境のみ）

### 1. リポジトリのクローン

```bash
git clone https://github.com/kazuki-fk/creator-board.git
cd creator-board
```

### 2. データベースの作成（自宅・MySQL 環境のみ）

```sql
CREATE DATABASE creatorboard;
```

### 3. 環境変数の設定（自宅・Mac のみ）

```bash
echo 'export SPRING_PROFILE=home' >> ~/.zshrc
source ~/.zshrc
```

### 4. Flask サーバーの起動

```bash
cd python
pip install flask
python app.py
```

### 5. Spring Boot の起動

```bash
# Mac
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

### 6. ブラウザでアクセス

```
http://localhost:8080
```

---

## DB テーブル設計

```
users          — ユーザー情報（id, username, email, password_hash, role）
projects       — プロジェクト（id, user_id, title, genre, bpm, status, phase, memo）
als_analyses   — 解析結果（id, user_id, file_name, bpm, track_count, devices_json）
```

---

## ディレクトリ構成

```
creator-board/
├── python/
│   ├── app.py               # Flask エントリーポイント
│   └── parser_engine.py     # .als 解析エンジン
├── src/main/
│   ├── java/com/creatorboard/
│   │   ├── config/          # Spring Security 設定
│   │   ├── controller/      # コントローラー
│   │   ├── entity/          # エンティティ
│   │   ├── repository/      # リポジトリ
│   │   └── service/         # サービス
│   └── resources/
│       ├── templates/       # Thymeleaf テンプレート
│       └── static/          # CSS / JS
└── pom.xml
```

---

## 開発環境の切り替え

`application.properties` でプロファイルを切り替えています。

| 環境 | プロファイル | DB |
|---|---|---|
| 学校（Windows） | school | H2（起動時にリセット） |
| 自宅（Mac） | home | MySQL |

自宅 Mac では環境変数 `SPRING_PROFILE=home` を設定することで自動的に MySQL 接続に切り替わります。

---

## 使用した主なライブラリ

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Flask](https://flask.palletsprojects.com/)
- [Chart.js](https://www.chartjs.org/)
- [Lombok](https://projectlombok.org/)

---

## 作者

- GitHub: [kazuki-fk](https://github.com/kazuki-fk)
