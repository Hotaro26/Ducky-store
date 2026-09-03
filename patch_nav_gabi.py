import re

with open("app/src/main/java/com/hotaro/duckystore/Navigation.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
"""

content = content.replace("import androidx.compose.animation.fadeOut", imports + "import androidx.compose.animation.fadeOut")

navdisplay_old = """            transitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)) togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            },
            popTransitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)) togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            },"""

navdisplay_new = """            transitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut()
            },
            popTransitionSpec = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut()
            },"""

content = content.replace(navdisplay_old, navdisplay_new)

with open("app/src/main/java/com/hotaro/duckystore/Navigation.kt", "w") as f:
    f.write(content)
