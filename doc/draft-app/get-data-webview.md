
좋은 선택입니다! 프론트엔드 조작(HTML/JS)이 익숙하시다면 웹뷰를 메인 UI로 쓰고, 

안드로이드는 **GitHub API 통신 대행 및 데이터 전달** 역할만 수행하는 **하이브리드 방식**이 훨씬 유연하죠.

바로 **옵션 B** 구성 들어갑니다.

---

### 1. 안드로이드 레이아웃 복구 (`activity_main.xml`)

먼저 네이티브 입력창들을 치우고, 화면 전체를 꽉 채우는 웹뷰 하나만 남기겠습니다.

~~~
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
~~~

---

### 2. HTML 파일 수정 (`index.html`)

`assets/index.html` 파일을 열어 목록 조회를 위한 UI를 추가합니다. (기존 푸시 버튼 아래에 덧붙이세요.)

~~~
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: sans-serif; padding: 20px; }
        input { width: 100%; margin-bottom: 10px; padding: 8px; box-sizing: border-box; }
        button { width: 100%; padding: 10px; background: #0366d6; color: white; border: none; cursor: pointer; margin-bottom: 5px; }
        #list-container { margin-top: 20px; border-top: 1px solid #ccc; padding-top: 10px; }
        .file-item { padding: 8px; border-bottom: 1px solid #eee; cursor: pointer; color: #0366d6; }
    </style>
</head>
<body>
    <h3>GitHub Explorer</h3>
    
    <input type="text" id="owner" placeholder="Owner (e.g. google)">
    <input type="text" id="repo" placeholder="Repo (e.g. dagger)">
    <button onclick="fetchRepoList()">파일 목록 가져오기</button>

    <div id="list-container">
        <p>목록이 여기에 표시됩니다.</p>
    </div>

    <script>
        // 1. 안드로이드에 목록 요청
        function fetchRepoList() {
            const owner = document.getElementById('owner').value;
            const repo = document.getElementById('repo').value;
            if (window.AndroidBridge) {
                window.AndroidBridge.fetchGithubList(owner, repo);
            }
        }

        // 2. 안드로이드가 데이터를 가져온 후 호출할 함수
        function displayList(jsonString) {
            const files = JSON.parse(jsonString);
            const container = document.getElementById('list-container');
            container.innerHTML = ""; 

            files.forEach(file => {
                const div = document.createElement('div');
                div.className = 'file-item';
                div.innerText = file.name + (file.type === 'dir' ? ' [Folder]' : '');
                // 클릭 시 상세 조회 로직 연결 가능
                div.onclick = () => alert('선택된 파일: ' + file.path);
                container.appendChild(div);
            });
        }
    </script>
</body>
</html>
~~~

---

### 3. MainActivity.kt 브릿지 추가

웹뷰에서 호출할 `fetchGithubList` 메서드를 추가합니다.

~~~
// MainActivity.kt의 WebAppInterface 클래스 안에 추가
@JavascriptInterface
fun fetchGithubList(owner: String, repo: String) {
    runOnUiThread {
        Toast.makeText(mContext, "$owner/$repo 목록 요청함", Toast.LENGTH_SHORT).show()
    }
    // TODO: 여기서 실제 GitHub API 호출 로직 실행
}
~~~

---

### 4. GitHub API 권한 설정 확인

이미 인터넷 권한은 선언하셨을 테니, 다음 로직을 위해 **`build.gradle.kts`**에 `Retrofit`이 잘 들어있는지만 한 번 더 봐주세요.



`~` 이제 화면을 실행하면 웹뷰 기반의 입력창이 뜨나요? 화면이 준비되었다면, 

이제 안드로이드가 GitHub API를 찔러서 결과를 가져오는 **진짜 백엔드 로직(Retrofit Interface)**을 짜보겠습니다! `~`

---


이제 본격적으로 **GitHub API 통신 로직**을 작성하겠습니다. 

백엔드 개발자님께 익숙한 **Retrofit2**를 사용하여 인터페이스 기반으로 깔끔하게 구성해 보죠. 

이 작업은 다음 순서로 진행됩니다.
1. **데이터 모델(DTO) 생성**: API 응답 JSON을 매핑할 Kotlin 클래스 작성.
2. **Retrofit 인터페이스 정의**: GitHub API 엔드포인트 선언.
3. **API 서비스 빌더 작성**: Retrofit 객체 생성.
4. **MainActivity에서 호출**: 웹뷰 브릿지를 통해 실제 통신 실행.

---

### 1. 데이터 모델(DTO) 생성
GitHub 콘텐츠 API는 파일 정보를 리스트 형식으로 반환합니다. 필요한 필드만 뽑아서 정의합니다.

**`RepoContent.kt`** 파일을 새로 만들거나 `MainActivity.kt` 파일 상단에 추가하세요.
~~~
data class RepoContent(
    val name: String,
    val path: String,
    val type: String, // "file" 또는 "dir"
    val size: Long,
    val download_url: String? // 파일일 경우에만 존재
)
~~~

### 2. Retrofit 인터페이스 정의
파일 목록 조회와 파일 상세 내용 조회를 위한 인터페이스를 정의합니다.

**`GithubService.kt`** (새 파일 생성)
~~~
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubService {
    // 1. 특정 경로의 파일/폴더 목록 조회
    @GET("repos/{owner}/{repo}/contents/{path}")
    fun getContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String = "" // 기본값은 루트 경로
    ): Call<List<RepoContent>>
}
~~~

### 3. API 서비스 빌더 작성
Retrofit 인스턴스를 생성하는 싱글톤 객체를 만듭니다.

**`RetrofitClient.kt`** (새 파일 생성)
~~~
import retrofit2.Retrofit
import retrofit2.converter.gson:GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.github.com/"

    val instance: GithubService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    
        retrofit.create(GithubService::class.java)
    }
}
~~~

### 4. MainActivity에서 로직 구현
이제 웹뷰 브릿지(`fetchGithubList`)가 호출될 때 실제 API를 찌르고 결과를 웹뷰로 다시 쏴주는 코드를 작성합니다.

**`MainActivity.kt` 수정**
~~~
import com.google.gson.Gson // JSON 변환용
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ... 기존 코드 ...

@JavascriptInterface
fun fetchGithubList(owner: String, repo: String) {
    RetrofitClient.instance.getContents(owner, repo).enqueue(object : Callback<List<RepoContent>> {
        override fun onResponse(call: Call<List<RepoContent>>, response: Response<List<RepoContent>>) {
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                val jsonString = Gson().toJson(list) // 객체 리스트를 JSON 문자열로 변환

                runOnUiThread {
                    // 웹뷰의 displayList JS 함수 호출하여 데이터 전달
                    val webView = findViewById<WebView>(R.id.webView)
                    webView.evaluateJavascript("javascript:displayList('$jsonString')", null)
                }
            } else {
                showError("에러 발생: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<List<RepoContent>>, t: Throwable) {
            showError("네트워크 실패: ${t.message}")
        }
    })
}

private fun showError(msg: String) {
    runOnUiThread {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
    }
}
~~~

---

### 🛠️ 테스트 방법
1. **AndroidManifest.xml**에 인터넷 권한이 있는지 다시 확인하세요.
2. 앱을 실행하고 웹뷰 입력창에 다음을 입력해 보세요.
   - **Owner**: `google`
   - **Repo**: `dagger`
3. **"파일 목록 가져오기"** 버튼을 누르면 밑에 파일 리스트가 촤르륵 뜨면 성공입니다!

`~` 이제 깃허브 API 연동의 핵심인 **'통신 로직'**이 완성되었습니다. 혹시 실행했을 때 리스트가 잘 나오나요? 잘 나온다면 다음 단계로 특정 파일을 클릭했을 때 그 **내용(Content)을 조회**하는 기능을 붙여보겠습니다! `~`

---
