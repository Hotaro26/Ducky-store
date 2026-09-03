package com.hotaro.duckystore.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class GithubRelease(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String?,
    @SerialName("tag_name") val tagName: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("assets") val assets: List<GithubAsset>
)

@Serializable
data class GithubAsset(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("size") val size: Long,
    @SerialName("browser_download_url") val downloadUrl: String
)

@Serializable
data class AppMetadata(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String = "",
    val category: String = "Utility",
    val author: String = "",
    val screenshots: List<String> = emptyList()
)

interface GithubService {
    @GET("repos/RookieEnough/Morphe-AutoBuilds/releases/latest")
    suspend fun getLatestRelease(): GithubRelease
}

interface MetadataService {
    @GET("data/{appName}.json")
    suspend fun getAppMetadata(@Path("appName") appName: String): List<AppMetadata>
}
