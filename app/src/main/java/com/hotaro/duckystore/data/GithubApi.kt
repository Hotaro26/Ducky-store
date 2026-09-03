package com.hotaro.duckystore.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

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

interface GithubService {
    @GET("repos/RookieEnough/Morphe-AutoBuilds/releases/latest")
    suspend fun getLatestRelease(): GithubRelease
}
