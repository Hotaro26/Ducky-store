package com.hotaro.duckystore.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPull() {
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = {}
    ) {}
}
