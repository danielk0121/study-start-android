package dev.danielk.startandroid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
            runOnUiThread {
                Toast.makeText(mContext, "$owner/$repo 목록 요청함", Toast.LENGTH_SHORT).show()
            }
            // TODO: 여기서 실제 GitHub API 호출 로직 실행
        }
    }
}
