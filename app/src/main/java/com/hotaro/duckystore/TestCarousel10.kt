package com.hotaro.duckystore

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCarousel10() {
    val state = rememberCarouselState(itemCount = { 10 })
    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = 200.dp,
        itemSpacing = 8.dp
    ) { index ->
        androidx.compose.material3.Card(
            modifier = Modifier.maskClip(RoundedCornerShape(32.dp))
        ) {
        }
    }
}
