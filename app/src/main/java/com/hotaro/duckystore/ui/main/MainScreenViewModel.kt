package com.hotaro.duckystore.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import androidx.lifecycle.viewModelScope
import com.hotaro.duckystore.data.AppMetadata
import com.hotaro.duckystore.data.AppRepository
import com.hotaro.duckystore.data.GithubAsset
import com.hotaro.duckystore.data.BoxData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@kotlinx.serialization.Serializable
data class AppGroup(
    val originalId: String,
    val baseName: String,
    val variants: List<GithubAsset>,
    val metadata: AppMetadata? = null
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository()

    private val _uiState = MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)
    val uiState: StateFlow<MainScreenUiState> = _uiState

    private val _isRateLimited = MutableStateFlow(false)
    val isRateLimited: StateFlow<Boolean> = _isRateLimited

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val prefs = getApplication<Application>().getSharedPreferences("ducky_metadata_cache", Context.MODE_PRIVATE)
            val jsonParser = Json { ignoreUnknownKeys = true }
            
            // 1. INSTANT CACHE RENDER (Super fast startup, skips network completely)
            val fullCacheJson = prefs.getString("full_ui_cache", null)
            val cachedBoxesJson = prefs.getString("boxes_cache", null)
            
            if (fullCacheJson != null) {
                try {
                    val cachedGroups = jsonParser.decodeFromString<List<AppGroup>>(fullCacheJson)
                    val cachedBoxes = if (cachedBoxesJson != null) {
                        try { jsonParser.decodeFromString<List<BoxData>>(cachedBoxesJson) } catch (e: Exception) { emptyList() }
                    } else emptyList()
                    
                    _uiState.value = MainScreenUiState.Success(cachedGroups, cachedBoxes)
                } catch (e: Exception) {
                    _uiState.value = MainScreenUiState.Loading
                }
            } else {
                _uiState.value = MainScreenUiState.Loading
            }

            // 2. BACKGROUND NETWORK SYNC
            val jsonListDeferred = async { repository.getDataFolderContents().getOrDefault(emptyList()) }
            val releaseAppsDeferred = async { repository.getApps().getOrDefault(emptyList()) }
            val boxesListDeferred = async { repository.getBoxesFolderContents().getOrDefault(emptyList()) }
            
            val jsonList = jsonListDeferred.await()
            val releaseApps = releaseAppsDeferred.await()
            val boxesList = boxesListDeferred.await()
            
            // If network failed (e.g., rate limit) and we have cache, just gracefully stop and let them use the cache
            if (jsonList.isEmpty()) {
                _isRateLimited.value = true
                if (fullCacheJson == null) {
                    _uiState.value = MainScreenUiState.Error(Exception("No apps found or GitHub rate limit exceeded. Please try again later."))
                }
                _isRefreshing.value = false
                return@launch
            } else {
                _isRateLimited.value = false
            }

            // Group release variants by inferred baseName
            val releaseGroups = releaseApps.groupBy { asset ->
                asset.name.split("-universal")[0]
                    .split("-arm64")[0]
                    .split("-armeabi")[0]
                    .split("-3-")[0]
                    .replace(".apk", "")
            }

            val currentGroups = jsonList.map { jsonId ->
                val baseName = jsonId.replace(".json", "")
                val readableName = baseName.replaceFirstChar { it.uppercase() }.replace("_", " ")
                val variants = releaseGroups[jsonId] ?: releaseGroups[readableName.lowercase()] ?: emptyList()
                AppGroup(jsonId, readableName, variants)
            }.sortedBy { it.baseName }

            // Fetch metadata in background for fresh updates
            val metadataDeferred = currentGroups.map { group ->
                async {
                    val metadata = repository.getAppMetadata(group.originalId).getOrNull()
                    val finalVariants = if (group.variants.isEmpty() && metadata != null && metadata.downloadUrl.isNotEmpty()) {
                        val sizeValue = metadata.size.replace(" MB", "").toDoubleOrNull() ?: 0.0
                        val sizeBytes = (sizeValue * 1024 * 1024).toLong()
                        listOf(GithubAsset(name = "${metadata.name}.apk", size = sizeBytes, downloadUrl = metadata.downloadUrl))
                    } else {
                        group.variants
                    }
                    group.copy(metadata = metadata, variants = finalVariants)
                }
            }

            val fullyLoadedGroups = metadataDeferred.awaitAll()
            
            // Load Box Data
            val boxDataDeferred = boxesList.map { boxId ->
                async {
                    val appIds = repository.getBoxMetadata(boxId).getOrNull() ?: emptyList()
                    val title = boxId.replace("_", " ").replaceFirstChar { it.uppercase() }
                    BoxData(boxId, title, appIds)
                }
            }
            val loadedBoxes = boxDataDeferred.awaitAll().filter { it.appIds.isNotEmpty() }
            
            // SAVE FULL STATE TO CACHE
            prefs.edit()
                .putString("full_ui_cache", jsonParser.encodeToString(fullyLoadedGroups))
                .putString("boxes_cache", jsonParser.encodeToString(loadedBoxes))
                .apply()
            
            _uiState.value = MainScreenUiState.Success(fullyLoadedGroups, loadedBoxes)
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<AppGroup>, val boxes: List<BoxData> = emptyList()) : MainScreenUiState
}
