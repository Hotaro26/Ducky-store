package com.hotaro.duckystore.data

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val githubRetrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val metadataRetrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/Hotaro26/Ducky-store/main/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val githubService = githubRetrofit.create(GithubService::class.java)
    private val metadataService = metadataRetrofit.create(MetadataService::class.java)

    suspend fun getApps(): Result<List<GithubAsset>> = runCatching {
        val release = githubService.getLatestRelease()
        // Filter only APK assets
        release.assets.filter { it.name.endsWith(".apk") }
    }

    suspend fun getDataFolderContents(): Result<List<String>> = runCatching {
        val contents = githubService.getDataContents()
        contents.filter { it.name.endsWith(".json") }.map { it.name.replace(".json", "") }
    }

    suspend fun getAppMetadata(appName: String): Result<AppMetadata?> = runCatching {
        val response = metadataService.getAppMetadata(appName)
        response.firstOrNull()
    }
}
