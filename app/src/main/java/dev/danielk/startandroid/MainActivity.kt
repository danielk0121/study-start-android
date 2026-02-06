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
        setContentView(R.layout.activity_main)

        val myWebView: WebView = findViewById(R.id.webView)

        myWebView.settings.apply {
            javaScriptEnabled = true  // JS 허용
            domStorageEnabled = true
        }

        // 브릿지 연결: HTML 내 window.AndroidBridge와 매핑됨
        myWebView.addJavascriptInterface(WebAppInterface(this), "AndroidBridge")

        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl("file:///android_asset/index.html")
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
    }
}
