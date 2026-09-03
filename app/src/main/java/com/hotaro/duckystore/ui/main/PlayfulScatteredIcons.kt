package com.hotaro.duckystore.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun PlayfulScatteredIcons(seed: String) {
    val icons = listOf(
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.Face,
        Icons.Default.PlayArrow,
        Icons.Default.Send,
        Icons.Default.Build,
        Icons.Default.ShoppingCart,
        Icons.Default.Done
    )
    
    val random = remember(seed) { Random(seed.hashCode()) }
    
    val cells = listOf(
        Pair(0, 0), Pair(1, 0), Pair(2, 0),
        Pair(0, 1), Pair(1, 1), Pair(2, 1)
    ).shuffled(random).take(5)
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val cellW = w / 3
        val cellH = h / 2
        
        cells.forEach { (col, row) ->
            val icon = icons.random(random)
            val size = random.nextInt(24, 56).dp
            val rotation = random.nextInt(-45, 45).toFloat()
            val offsetX = (cellW * col.toFloat()) + (cellW / 2) - (size / 2) + random.nextInt(-10, 10).dp
            val offsetY = (cellH * row.toFloat()) + (cellH / 2) - (size / 2) + random.nextInt(-10, 10).dp
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier
                    .absoluteOffset(x = offsetX, y = offsetY)
                    .size(size)
                    .rotate(rotation)
            )
        }
    }
}
