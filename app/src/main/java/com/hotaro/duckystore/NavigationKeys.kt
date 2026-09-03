package com.hotaro.duckystore

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object Settings : NavKey
@Serializable data class AppDetail(
    val name: String,
    val size: String,
    val downloadUrl: String
) : NavKey
