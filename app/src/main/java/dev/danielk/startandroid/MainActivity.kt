package dev.danielk.startandroid

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myWebView: WebView = findViewById(R.id.webView)

        // 1. 웹뷰 설정: 자바스크립트 허용 (GitHub API 연동 등에 필요할 수 있음)
        myWebView.settings.javaScriptEnabled = true

        // 2. 중요: 새 창이 뜨지 않고 웹뷰 내에서 페이지가 이동하도록 설정
        myWebView.webViewClient = WebViewClient()

        // 3. Hello World 출력 (직접 HTML을 로드하거나 URL을 로드할 수 있음)
//        val htmlData = "<html><body><h1>Hello World!</h1><p>이것은 안드로이드 웹뷰입니다.</p></body></html>"
//        myWebView.loadData(htmlData, "text/html", "UTF-8")
        // 3.  `MainActivity.kt`에서 `webView.loadUrl("file:///android_asset/index.html")`로 불러오면 됩니다.
        // 만약 특정 사이트를 띄우고 싶다면:
        // myWebView.loadUrl("https://www.google.com")
        myWebView.loadUrl("file:///android_asset/index.html")





    }
}

