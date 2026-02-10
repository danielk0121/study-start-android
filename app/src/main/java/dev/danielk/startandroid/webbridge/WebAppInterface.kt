package dev.danielk.startandroid.webbridge

import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import dev.danielk.startandroid.data.model.RepoContent
import dev.danielk.startandroid.data.remote.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WebAppInterface(
    private val mContext: Context, // MainActivity : AppCompatActivity()
    private val webView: WebView
) {
    @JavascriptInterface
    fun pushToGithub(title: String, content: String) {
        // 별도 스레드에서 실행될 수 있으므로 UI 작업은 runOnUiThread 사용
        (mContext as? AppCompatActivity)?.runOnUiThread {
            // 잘 전달받았는지 토스트 메시지로 먼저 확인!
            Toast.makeText(mContext, "제목: $title\n내용이 전달되었습니다.",
                Toast.LENGTH_LONG).show()
        }
        // TODO: 여기서 GitHub API 연동 로직(Retrofit 등)이 들어갈 예정
    }

    @JavascriptInterface
    fun fetchGithubList(owner: String, repo: String, path: String) {
        Log.i("fetchGithubList", "목록 요청 - Owner: $owner, Repo: $repo, Path: $path")
        RetrofitClient.instance.getContents(owner, repo, path).enqueue(object : Callback<List<RepoContent>> {
            override fun onFailure(call: Call<List<RepoContent>>, t: Throwable) {
                showError("onFailure: ${t.message}", t)
            }
            override fun onResponse(call: Call<List<RepoContent>>, response: Response<List<RepoContent>>) {
                onResponseHandler(response)
            }
            private fun onResponseHandler(response: Response<List<RepoContent>>) {
                if(!response.isSuccessful) {
                    // errorBody()는 스트림이므로 string()으로 변환해서 읽습니다.
                    val errorJson = response.errorBody()?.string()
                    showError("${response.code()}, $errorJson")
                    return
                }

                // 수신 데이터: 목록
                val list = response.body() ?: emptyList()

                // 폴더 우선 -> 이름순 정렬 로직
                val sortedList = list.sortedWith(compareBy<RepoContent> {
                    if (it.type == "dir") 0 else 1
                }.thenBy { it.name.lowercase() })

                val jsonString = Gson().toJson(sortedList)
                sendToWeb("javascript:displayList('$jsonString')")
            }
        })
    }

    @JavascriptInterface
    fun fetchFileContent(owner: String, repo: String, path: String) {
        Log.i("fetchFileContent", "owner: $owner, repo: $repo, path: $path")
        RetrofitClient.instance.getFileContent(owner, repo, path).enqueue(object : Callback<RepoContent> {
            override fun onFailure(call: Call<RepoContent>, t: Throwable) {
                showError("onFailure: ${t.message}", t)
            }
            override fun onResponse(call: Call<RepoContent>, response: Response<RepoContent>) {
                onResponseHandler(response)
            }
            private fun onResponseHandler(response: Response<RepoContent>) {
                if(!response.isSuccessful) {
                    // errorBody()는 스트림이므로 string()으로 변환해서 읽습니다.
                    val errorJson = response.errorBody()?.string()
                    showError("${response.code()}, $errorJson")
                    return
                }

                // 수시 데이터: 단일 파일
                val fileData = response.body()
                val escapedString = decodePayload(fileData)
                sendToWeb("javascript:displayFileContent('$escapedString')")
            }

            private fun decodePayload(fileData: RepoContent?): String {
                // GitHub은 파일 내용을 Base64로 인코딩해서 줌. 이를 디코딩함.
                val content = fileData?.content?.replace("\n", "") ?: ""
                val decodedBytes = Base64.decode(content, Base64.DEFAULT)
                val decodedString = String(decodedBytes)

                // JS의 displayFileContent 함수로 디코딩된 문자열 전달
                // 줄바꿈 처리를 위해 역슬래시 등을 이스케이프 처리하여 전달
                return decodedString.replace("'", "\\'").replace("\n", "\\n")
            }
        })
    }

    private fun showError(msg: String) {
        Log.e("DraftApp", "에러 발생: $msg")
        (mContext as? AppCompatActivity)?.runOnUiThread {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(msg: String, t: Throwable) {
        Log.e("DraftApp", "에러 발생: $msg", t)
        (mContext as? AppCompatActivity)?.runOnUiThread {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendToWeb(script: String) {
        (mContext as? AppCompatActivity)?.runOnUiThread {
            webView.evaluateJavascript(script, null)
        }
    }
}