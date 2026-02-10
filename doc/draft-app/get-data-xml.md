
드디어 조작 설정을 마치고 본 게임으로 들어가는군요!   

백엔드 개발자 스타일대로 **권한(Permission) -> 화면(UI) -> 기능(Logic)** 순서로 아주 깔끔하게 끊어서 진행해 보겠습니다.  

---

### 1. 안드로이드 권한 설정 (Internet)

안드로이드 앱이 외부(GitHub API)와 통신하려면

가장 먼저 `AndroidManifest.xml`에 인터넷 권한을 선언해야 합니다. 

이건 선택이 아니라 필수입니다.

1.  **`app/src/main/AndroidManifest.xml`** 파일을 엽니다.
2.  `<manifest>` 태그 바로 아래, `<application>` 태그가 시작되기 전 위치에 아래 한 줄을 추가하세요.

~~~
<uses-permission android:name="android.permission.INTERNET" />
~~~

### 2. 안드로이드 화면(UI) 개발

목록과 상세 조회를 위해 간단한 레이아웃을 먼저 잡겠습니다. 

지금은 복잡한 리사이클러뷰(RecyclerView) 대신, 빠르게 결과를 확인할 수 있도록 

**스크롤 가능한 텍스트 뷰**와 **입력창** 위주로 구성하겠습니다.

**`app/src/main/res/layout/activity_main.xml`** 

(없으면 생성)을 열고 아래 코드를 붙여넣으세요. 

(이때 아까 설정한 **마우스 우클릭 -> Paste**나 **adb**를 활용해 보세요!)

~~~
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="GitHub Repository Explorer"
        android:textSize="20sp"
        android:textStyle="bold" />

    <EditText
        android:id="@+id/etRepoOwner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Owner (e.g. google)" />

    <EditText
        android:id="@+id/etRepoName"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Repo Name (e.g. dagger)" />

    <Button
        android:id="@+id/btnFetchList"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="파일 목록 가져오기" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="10dp"
        android:background="#F0F0F0">

        <TextView
            android:id="@+id/tvResult"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dp"
            android:text="결과가 여기에 표시됩니다."
            android:textColor="#333333" />
    </ScrollView>

</LinearLayout>
~~~

---

### 3. 기능 구현을 위한 사전 준비 (의존성 확인)

GitHub API와 통신할 때 백엔드에서 즐겨 쓰는 `Retrofit`과 `Gson`을 사용할 예정입니다. 

아까 제가 슬쩍 언급했던 `build.gradle.kts` 설정이 되어 있는지 확인해 주세요.

* **파일 위치:** `app/build.gradle.kts` (Module :app)
* **확인할 내용:**
    ~~~
    dependencies {
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    }
    ~~~

---

### 🛠️ 다음 단계

1.  **권한 설정**과 **XML 레이아웃** 수정이 완료되셨나요?
2.  에뮬레이터에서 앱을 실행(Run)했을 때 입력창 2개와 버튼이 예쁘게 뜨는지 확인해 주세요.

`~` 화면이 잘 뜨는지 확인되면, 바로 **GitHub API용 Interface**를 정의하고 **목록 불러오기 로직**을 작성해 보겠습니다! `~`

---

