
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

