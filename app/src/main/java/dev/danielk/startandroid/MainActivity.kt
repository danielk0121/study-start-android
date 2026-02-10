package dev.danielk.startandroid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. 웹뷰가 포함된 레이아웃으로 설정 (activity_main.xml에 WebView가 있어야 함)
        setContentView(R.layout.activity_main)

        // 2. 레이아웃 내의 WebView 찾아오기
        val myWebView: WebView = findViewById(R.id.webView)

        myWebView.settings.apply {
            javaScriptEnabled = true  // 자바스크립트 허용 필수
            domStorageEnabled = true  // 로컬 저장소 허용
        }

        // 3. 브릿지 연결 (HTML의 window.AndroidBridge와 연결)
        myWebView.addJavascriptInterface(WebAppInterface(this), "AndroidBridge")

        // 4. index.html 로드
        myWebView.loadUrl("file:///android_asset/index.html")
        myWebView.webViewClient = WebViewClient()
    }

    // JS 인터페이스 클래스
    inner class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun pushToGithub(title: String, content: String) {
            // 별도 스레드에서 실행될 수 있으므로 UI 작업은 runOnUiThread 사용
            runOnUiThread {
                // 잘 전달받았는지 토스트 메시지로 먼저 확인!
                Toast.makeText(mContext, "제목: $title\n내용이 전달되었습니다.",
                    Toast.LENGTH_LONG).show()
            }
            // TODO: 여기서 GitHub API 연동 로직(Retrofit 등)이 들어갈 예정입니다.
        }

        @JavascriptInterface
        fun fetchGithubList(owner: String, repo: String) {
            return fetchGithubList(owner, repo, "")
        }

        @JavascriptInterface
        fun fetchGithubList(owner: String, repo: String, path: String) {
            Log.d("GithubAPI", "목록 요청 - Owner: $owner, Repo: $repo, Path: $path")

            RetrofitClient.instance.getContents(owner, repo, path).enqueue(object : Callback<List<RepoContent>> {
                override fun onResponse(call: Call<List<RepoContent>>, response: Response<List<RepoContent>>) {
                    onResponseHandler(response)
                }

                override fun onFailure(call: Call<List<RepoContent>>, t: Throwable) {
                    showError("onFailure: ${t.message}", t)
                }

                private fun onResponseHandler(response: Response<List<RepoContent>>) {
                    if (response.isSuccessful) {
                        val list = response.body() ?: emptyList()

                        // 폴더 우선 -> 이름순 정렬 로직
                        val sortedList = list.sortedWith(compareBy<RepoContent> {
                            if (it.type == "dir") 0 else 1
                        }.thenBy { it.name.lowercase() })

                        val jsonString = Gson().toJson(sortedList)

                        runOnUiThread {
                            val webView = findViewById<WebView>(R.id.webView)
                            webView.evaluateJavascript("javascript:displayList('$jsonString')", null)
                        }
                    } else {
                        // errorBody()는 스트림이므로 string()으로 변환해서 읽습니다.
                        val errorJson = response.errorBody()?.string()
                        showError("${response.code()}, $errorJson")
                    }
                }
            })
        }

        @JavascriptInterface
        fun fetchFileContent(owner: String, repo: String, path: String) {
            Log.i("DraftApp", "owner: $owner, repo: $repo, path: $path")

            RetrofitClient.instance.getFileContent(owner, repo, path).enqueue(object : Callback<RepoContent> {
                override fun onResponse(call: Call<RepoContent>, response: Response<RepoContent>) {
                    onResponseHandler(response)
                }

                override fun onFailure(call: Call<RepoContent>, t: Throwable) {
                    showError("onFailure: ${t.message}", t)
                }

                private fun onResponseHandler(response: Response<RepoContent>) {
                    if (response.isSuccessful) {
                        val fileData = response.body()

                        // GitHub은 파일 내용을 Base64로 인코딩해서 줌. 이를 디코딩함.
                        val content = fileData?.content?.replace("\n", "") ?: ""
                        val decodedBytes = Base64.decode(content, Base64.DEFAULT)
                        val decodedString = String(decodedBytes)

                        runOnUiThread {
                            val webView = findViewById<WebView>(R.id.webView)

                            // JS의 displayFileContent 함수로 디코딩된 문자열 전달
                            // 줄바꿈 처리를 위해 역슬래시 등을 이스케이프 처리하여 전달
                            val escapedString = decodedString.replace("'", "\\'").replace("\n", "\\n")
                            webView.evaluateJavascript("javascript:displayFileContent('$escapedString')", null)
                        }
                    } else {
                        // errorBody()는 스트림이므로 string()으로 변환해서 읽습니다.
                        val errorJson = response.errorBody()?.string()
                        showError("${response.code()}, $errorJson")
                    }
                }
            })
        }

        private fun showError(msg: String) {
            Log.e("DraftApp", "에러 발생: $msg")
            runOnUiThread {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
            }
        }

        private fun showError(msg: String, t: Throwable) {
            Log.e("DraftApp", "에러 발생: $msg", t)
            runOnUiThread {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
