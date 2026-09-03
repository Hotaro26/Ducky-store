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
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = MainScreenUiState.Loading
            
            // Fetch list of JSONs in data folder and the Github release concurrently
            val jsonListDeferred = async { repository.getDataFolderContents().getOrDefault(emptyList()) }
            val releaseAppsDeferred = async { repository.getApps().getOrDefault(emptyList()) }
            val boxesListDeferred = async { repository.getBoxesFolderContents().getOrDefault(emptyList()) }
            
            val jsonList = jsonListDeferred.await()
            val releaseApps = releaseAppsDeferred.await()
            val boxesList = boxesListDeferred.await()
            
            if (jsonList.isEmpty()) {
                _uiState.value = MainScreenUiState.Error(Exception("No apps found in data folder"))
                return@launch
            }

            // Group release variants by inferred baseName
            val releaseGroups = releaseApps.groupBy { asset ->
                asset.name.split("-universal")[0]
                    .split("-arm64")[0]
                    .split("-armeabi")[0]
                    .split("-3-")[0]
                    .replace(".apk", "")
            }

            // Create initial groups based on the JSON files
            val currentGroups = jsonList.map { jsonId ->
                val readableName = jsonId.replaceFirstChar { it.uppercase() }.replace("_", " ")
                
                // Try to find matching variants from Morphe-AutoBuilds
                val variants = releaseGroups[jsonId] ?: releaseGroups[readableName.lowercase()] ?: emptyList()
                
                AppGroup(jsonId, readableName, variants)
            }.sortedBy { it.baseName }
            
            val prefs = getApplication<Application>().getSharedPreferences("ducky_metadata_cache", Context.MODE_PRIVATE)
            val jsonParser = Json { ignoreUnknownKeys = true }
            
            // First pass: Load from Cache instantly
            var cachedGroups = currentGroups.map { group ->
                val cachedJson = prefs.getString(group.originalId, null)
                val metadata = if (cachedJson != null) {
                    try { jsonParser.decodeFromString<AppMetadata>(cachedJson) } catch (e: Exception) { null }
                } else null
                
                val finalVariants = if (group.variants.isEmpty() && metadata != null && metadata.downloadUrl.isNotEmpty()) {
                    val sizeValue = metadata.size.replace(" MB", "").toDoubleOrNull() ?: 0.0
                    val sizeBytes = (sizeValue * 1024 * 1024).toLong()
                    listOf(GithubAsset(name = "${metadata.name}.apk", size = sizeBytes, downloadUrl = metadata.downloadUrl))
                } else group.variants
                
                group.copy(metadata = metadata, variants = finalVariants)
            }
            
            // Load cached Box Data
            val cachedBoxesJson = prefs.getString("boxes_cache", null)
            val cachedBoxes = if (cachedBoxesJson != null) {
                try { jsonParser.decodeFromString<List<BoxData>>(cachedBoxesJson) } catch (e: Exception) { emptyList() }
            } else emptyList()
            
            _uiState.value = MainScreenUiState.Success(cachedGroups, cachedBoxes)

            // Fetch metadata in background for fresh updates
            val metadataDeferred = currentGroups.map { group ->
                async {
                    val metadata = repository.getAppMetadata(group.originalId).getOrNull()
                    if (metadata != null) {
                        prefs.edit().putString(group.originalId, jsonParser.encodeToString(metadata)).apply()
                    }
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
            prefs.edit().putString("boxes_cache", jsonParser.encodeToString(loadedBoxes)).apply()
            
            _uiState.value = MainScreenUiState.Success(fullyLoadedGroups, loadedBoxes)
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<AppGroup>, val boxes: List<BoxData> = emptyList()) : MainScreenUiState
}
