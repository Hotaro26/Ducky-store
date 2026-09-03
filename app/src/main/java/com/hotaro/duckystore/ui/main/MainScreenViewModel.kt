package com.hotaro.duckystore.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotaro.duckystore.data.AppMetadata
import com.hotaro.duckystore.data.AppRepository
import com.hotaro.duckystore.data.GithubAsset
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppGroup(
    val originalId: String,
    val baseName: String,
    val variants: List<GithubAsset>,
    val metadata: AppMetadata? = null
)

class MainScreenViewModel : ViewModel() {
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
            
            val jsonList = jsonListDeferred.await()
            val releaseApps = releaseAppsDeferred.await()
            
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
            
            // Fetch metadata in background BEFORE emitting success
            val metadataDeferred = currentGroups.map { group ->
                async {
                    val metadata = repository.getAppMetadata(group.originalId).getOrNull()
                    // If no variants were found in Morphe, use the JSON's fallback downloadUrl
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
            _uiState.value = MainScreenUiState.Success(fullyLoadedGroups)
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<AppGroup>) : MainScreenUiState
}
