import re

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.hotaro.duckystore.R
"""
content = content.replace("import com.hotaro.duckystore.data.GithubAsset", imports + "import com.hotaro.duckystore.data.GithubAsset")

# Replace TopAppBar
old_top_bar = """                TopAppBar(
                    title = { Text("Ducky Store") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )"""

new_top_bar = """                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                                contentDescription = "Ducky Store Icon",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ducky Store", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )"""
content = content.replace(old_top_bar, new_top_bar)

# Modify PullToRefreshBox content to include the custom search bar
old_pull_content = """                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadApps() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val s = state) {"""

new_pull_content = """                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadApps() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Custom Search Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search apps...") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (val s = state) {"""
content = content.replace(old_pull_content, new_pull_content)

# Remove M3 SearchBar overlay
start_search_bar = content.find("        // Search Bar Overlay")
end_search_bar = content.find("}\n\n@Composable\nfun AppItem")
if start_search_bar != -1 and end_search_bar != -1:
    # also add closing braces for Column and Box inside PullToRefreshBox
    content = content[:start_search_bar] + """                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem""" + content[end_search_bar + len("}\n\n@Composable\nfun AppItem"):]

with open("app/src/main/java/com/hotaro/duckystore/ui/main/MainScreen.kt", "w") as f:
    f.write(content)
