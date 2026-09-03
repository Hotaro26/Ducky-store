package com.hotaro.duckystore

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object Settings : NavKey

@Serializable
data class Variant(
    val name: String,
    val size: String,
    val downloadUrl: String
)

@Serializable data class BoxDetail(
    val title: String,
    val appIds: List<String>
) : NavKey

@Serializable data class AppDetail(
    val originalId: String,
    val name: String,
    val variants: List<Variant>
) : NavKey
