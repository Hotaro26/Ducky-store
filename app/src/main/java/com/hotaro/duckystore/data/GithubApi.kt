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
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String,
    @SerialName("size") val size: Long,
    @SerialName("browser_download_url") val downloadUrl: String
)

@Serializable
data class GithubContent(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("download_url") val downloadUrl: String?
)

@Serializable
data class AppMetadata(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String = "",
    val category: String = "Utility",
    val author: String = "",
    val downloadUrl: String = "",
    val size: String = "0 MB",
    val packageName: String = "",
    val version: String = "",
    val latestVersion: String = "",
    val screenshots: List<String> = emptyList(),
    val modInfo: List<String> = emptyList()
)

@Serializable
data class BoxData(
    val id: String,
    val title: String,
    val appIds: List<String>
)

interface GithubService {
    @GET("repos/RookieEnough/Morphe-AutoBuilds/releases/latest")
    suspend fun getLatestRelease(): GithubRelease

    @GET("repos/Hotaro26/Ducky-store/contents/data")
    suspend fun getDataContents(): List<GithubContent>

    @GET("repos/Hotaro26/Ducky-store/contents/boxes")
    suspend fun getBoxesContents(): List<GithubContent>
}

interface MetadataService {
    @GET("data/{appName}.json")
    suspend fun getAppMetadata(
        @Path("appName") appName: String,
        @retrofit2.http.Query("t") timestamp: Long = System.currentTimeMillis()
    ): List<AppMetadata>

    @GET("boxes/{boxName}.json")
    suspend fun getBoxMetadata(
        @Path("boxName") boxName: String,
        @retrofit2.http.Query("t") timestamp: Long = System.currentTimeMillis()
    ): List<String>
}
