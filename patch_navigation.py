import re

with open("app/src/main/java/com/hotaro/duckystore/Navigation.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.layout.padding", "import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.consumeWindowInsets")
content = content.replace("modifier = Modifier.padding(innerPadding),", "modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),")

with open("app/src/main/java/com/hotaro/duckystore/Navigation.kt", "w") as f:
    f.write(content)
