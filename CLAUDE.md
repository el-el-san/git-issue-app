# Kotlin実装計画 - GitHub Issue Manager

## 現在のFlutter実装分析

### アプリケーション概要
- GitHub Issue管理アプリ（Flutter版）
- 機能: GitHub認証、Issue一覧・詳細表示、Issue作成、コメント機能、ラベル管理
- パッケージ名: com.gitissueapp.app

### 主要機能
1. **認証機能**
   - GitHub OAuth Device Flow認証
   - トークン管理（Secure Storage）

2. **Issue管理**
   - Issue一覧表示（フィルタリング）
   - Issue詳細表示
   - Issue作成
   - コメント表示・作成
   - ラベル管理・更新

3. **UI/UX**
   - ダークテーマ
   - ナビゲーション（go_router）
   - Markdown表示

## Kotlin実装計画

### 1. アーキテクチャ設計

#### 全体アーキテクチャ: MVVM + Repository パターン
```
Presentation Layer (UI)
├── Activities/Fragments
├── ViewModels
└── Data Binding

Domain Layer
├── Use Cases
├── Models
└── Repository Interfaces

Data Layer
├── Repository Implementations
├── Data Sources (API, Local Storage)
└── DTOs
```

#### 技術スタック
- **UI**: Material Design 3, View Binding
- **非同期処理**: Coroutines + Flow
- **HTTP通信**: Retrofit + OkHttp
- **JSON**: Kotlinx Serialization
- **DI**: Hilt
- **ローカルストレージ**: DataStore + EncryptedSharedPreferences
- **Navigation**: Navigation Component
- **Markdown**: Markwon

### 2. プロジェクト構成

```
app/
├── src/main/kotlin/com/gitissueapp/
│   ├── data/
│   │   ├── api/
│   │   │   ├── GitHubApiService.kt
│   │   │   ├── dto/
│   │   │   └── interceptor/
│   │   ├── repository/
│   │   ├── storage/
│   │   └── util/
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   ├── presentation/
│   │   ├── ui/
│   │   │   ├── auth/
│   │   │   ├── issue/
│   │   │   ├── home/
│   │   │   └── settings/
│   │   ├── viewmodel/
│   │   └── util/
│   ├── di/
│   └── GitIssueApplication.kt
├── res/
│   ├── layout/
│   ├── values/
│   └── navigation/
└── AndroidManifest.xml
```

### 3. 実装詳細計画

#### Phase 1: 基盤構築
1. プロジェクト構造作成
2. 依存関係設定 (Hilt, Retrofit, etc.)
3. 基本Models/DTOs作成
4. GitHub API Service実装

#### Phase 2: 認証機能
1. OAuth Device Flow実装
2. 認証状態管理
3. Secure Storage実装
4. 認証UI作成

#### Phase 3: 核心機能
1. Issue Repository実装
2. Issue一覧画面
3. Issue詳細画面
4. Issue作成機能

#### Phase 4: 追加機能
1. コメント機能
2. ラベル管理
3. 設定画面
4. テーマ管理

#### Phase 5: 最適化・テスト
1. エラーハンドリング強化
2. キャッシュ機能
3. Unit/Integration テスト
4. APK最適化

### 4. 依存関係設定

#### build.gradle.kts (Module: app)
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    
    // Storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Markdown
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### 5. 主要クラス設計

#### GitHubApiService
```kotlin
@Singleton
class GitHubApiService @Inject constructor(
    private val api: GitHubApi,
    private val authRepository: AuthRepository
) {
    suspend fun getIssues(owner: String, repo: String, state: String = "all"): List<IssueDto>
    suspend fun getIssue(owner: String, repo: String, number: Int): IssueDto
    suspend fun createIssue(owner: String, repo: String, request: CreateIssueRequest): IssueDto
    // ... other methods
}
```

#### IssueRepository
```kotlin
@Singleton
class IssueRepositoryImpl @Inject constructor(
    private val apiService: GitHubApiService,
    private val issueDao: IssueDao
) : IssueRepository {
    override fun getIssues(owner: String, repo: String): Flow<List<Issue>>
    override suspend fun refreshIssues(owner: String, repo: String)
    override suspend fun createIssue(owner: String, repo: String, title: String, body: String?): Issue
}
```

#### IssueListViewModel
```kotlin
@HiltViewModel
class IssueListViewModel @Inject constructor(
    private val issueRepository: IssueRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IssueListUiState())
    val uiState: StateFlow<IssueListUiState> = _uiState.asStateFlow()
    
    fun loadIssues(owner: String, repo: String)
    fun refreshIssues()
    fun filterIssues(state: String)
}
```

### 6. GitHub Actions設定

#### .github/workflows/build.yml
```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Build APK
      run: ./gradlew assembleRelease
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-release.apk
```

### 7. 実装順序

1. **基盤**: プロジェクト構造、DI設定、基本Models
2. **API**: GitHub API Service、Repository層
3. **認証**: OAuth実装、Secure Storage
4. **UI基盤**: Theme、Navigation設定
5. **Issue機能**: 一覧→詳細→作成の順
6. **追加機能**: コメント、ラベル、設定
7. **最適化**: キャッシュ、エラーハンドリング、テスト

### 8. 移行時の注意点

- Flutter固有の機能（Provider等）をKotlinのアーキテクチャに適切に変換
- GitHub API認証フローの互換性確保
- 既存データ（設定、キャッシュ）の移行方法検討
- UI/UXの一貫性維持

### 9. テスト戦略

- Unit Test: Repository、ViewModel、UseCase
- Integration Test: API Service、Database操作
- UI Test: 主要画面の動作確認
- E2E Test: 認証〜Issue作成までの基本フロー

### 10. パフォーマンス最適化

- LazyLoading for Issue lists
- Image caching for avatars
- Background sync for issue updates
- Proguard/R8 optimization for APK size

この計画に基づいて段階的にKotlinでの実装を進めていきます。

## 実装状況・ビルド履歴

### 完了済み機能
- ✅ **Phase 1**: 基盤構築 (プロジェクト構造、依存関係設定)
- ✅ **Phase 2**: OAuth認証機能 (GitHub Device Flow、Secure Storage)
- ✅ **Phase 3**: 核心機能 (Issue一覧、詳細、作成)
- ✅ **GitHub Actions**: 自動APKビルド設定

### 最新ビルド: v9.0.0 (2025-07-13)
**場所**: `/storage/emulated/0/Download/`
- **GitIssueApp-v9-Fixed-release.apk** (6.1MB) - 本番用
- **GitIssueApp-v9-Fixed-debug.apk** (7.6MB) - デバッグ用

**修正内容:**
1. OAuth認証404エラー修正 (form-encoded形式対応)
2. Issue作成404エラー修正 (GitHub API v4互換)
3. バージョン競合問題解決 (v9.0.0)
4. 詳細エラーログ追加

### 残課題・改善点
- [ ] **Phase 4**: コメント機能実装
- [ ] **Phase 4**: ラベル管理機能
- [ ] **Phase 4**: 設定画面・テーマ管理
- [ ] **Phase 5**: キャッシュ機能・パフォーマンス最適化

### 技術的解決済み問題
1. **Gradle設定**: Flutter → Kotlin移行完了
2. **OAuth認証**: GitHub Device Flow正常動作
3. **API通信**: OkHttp + Gson構成で安定動作
4. **APKビルド**: GitHub Actions自動化完了
5. **権限問題**: プライベートリポジトリアクセス対応