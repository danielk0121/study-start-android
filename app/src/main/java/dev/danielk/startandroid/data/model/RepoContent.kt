package dev.danielk.startandroid.data.model

data class RepoContent(
    val name: String,
    val path: String,
    val type: String, // "file" 또는 "dir"
    val size: Long,
    val download_url: String?, // 파일일 경우에만 존재
    val content: String?
)