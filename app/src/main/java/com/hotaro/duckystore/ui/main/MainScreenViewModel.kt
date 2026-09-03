package com.hotaro.duckystore.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotaro.duckystore.data.AppRepository
import com.hotaro.duckystore.data.GithubAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppGroup(
    val baseName: String,
    val variants: List<GithubAsset>
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
                    val groups = apps.groupBy { asset ->
                        asset.name.split("-universal")[0]
                            .split("-arm64")[0]
                            .split("-armeabi")[0]
                            .split("-3-")[0] // hack for prime-video
                            .replaceFirstChar { it.uppercase() }
                            .replace("_", " ")
                            .replace(".apk", "")
                    }.map { (name, variants) ->
                        AppGroup(name, variants)
                    }.sortedBy { it.baseName }

                    _uiState.value = MainScreenUiState.Success(groups)
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
