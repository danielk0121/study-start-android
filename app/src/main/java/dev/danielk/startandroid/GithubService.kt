package dev.danielk.startandroid

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubService {
    // 파일/폴더 목록 조회
    @GET("repos/{owner}/{repo}/contents/{path}")
    fun getContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String = "" // 기본값은 루트 경로
    ): Call<List<RepoContent>>

    // 단일 객체 조회
    @GET("repos/{owner}/{repo}/contents/{path}")
    fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String
    ): Call<RepoContent> // 목록과 달리 단일 객체를 반환함
}