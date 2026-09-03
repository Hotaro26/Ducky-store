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
            repository.getApps()
                .onSuccess { apps ->
                    val groupedApps = apps.groupBy { asset ->
                        asset.name.split("-universal")[0]
                            .split("-arm64")[0]
                            .split("-armeabi")[0]
                            .split("-3-")[0]
                            .replace(".apk", "")
                    }

                    // Create groups with null metadata first to show UI immediately
                    var currentGroups = groupedApps.map { (id, variants) ->
                        val readableName = id.replaceFirstChar { it.uppercase() }.replace("_", " ")
                        AppGroup(id, readableName, variants)
                    }.sortedBy { it.baseName }

                    _uiState.value = MainScreenUiState.Success(currentGroups)

                    // Fetch metadata in background
                    val metadataDeferred = currentGroups.map { group ->
                        async {
                            val metadata = repository.getAppMetadata(group.originalId).getOrNull()
                            group.copy(metadata = metadata)
                        }
                    }

                    currentGroups = metadataDeferred.awaitAll()
                    _uiState.value = MainScreenUiState.Success(currentGroups)
                }
                .onFailure { error ->
                    _uiState.value = MainScreenUiState.Error(error)
                }
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<AppGroup>) : MainScreenUiState
}
