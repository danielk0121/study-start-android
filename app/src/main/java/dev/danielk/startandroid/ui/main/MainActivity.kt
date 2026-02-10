package dev.danielk.startandroid.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import dev.danielk.startandroid.R
import dev.danielk.startandroid.webbridge.WebAppInterface

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 웹뷰가 포함된 레이아웃으로 설정, activity_main.xml에 WebView가 있어야 함
        setContentView(R.layout.activity_main)

        // 웹뷰 바인딩
        myWebView = findViewById(R.id.webView)

        // 웹뷰 설정
        myWebView.settings.apply {
            javaScriptEnabled = true  // 자바스크립트 허용 필수
            domStorageEnabled = true  // 로컬 저장소 허용
        }

        // 브릿지 연결, 분리된 클래스 생성 및 인스턴스 전달
        myWebView.addJavascriptInterface(WebAppInterface(this, myWebView), "AndroidBridge")

        // index.html 로드
        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl("file:///android_asset/index.html")
    }
}
