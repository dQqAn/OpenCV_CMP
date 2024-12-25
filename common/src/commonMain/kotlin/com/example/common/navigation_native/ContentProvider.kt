package com.example.common.navigation_native

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.common.CameraPageContent
import com.example.common.CameraViewModel
import com.example.common.MainPageContent
import com.example.common.StitcherViewModel

@Composable
fun BoxWithConstraintsScope.contentProvider(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel,
) {
    val navigationStack = rememberSaveable(
        saver = listSaver<NavigationStack<Page>, Page>(
            restore = { NavigationStack(*it.toTypedArray()) },
            save = { it.stack },
        )
    ) {
        NavigationStack(MainPager())
    }

//    val navigationStackSize: Int = navigationStack.stack.size

    AnimatedContent(
        targetState = navigationStack.lastWithIndex(),
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(400),
                initialOffsetX = { fullWidth -> fullWidth }
            ) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { fullWidth -> -fullWidth }
                    )
        })
    { (_, page) ->
        when (page) {
            is MainPager -> {
                MainPageContent(
                    stitcherViewModel
                ) {
                    navigationStack.push(CameraPager())
                }
            }

            is CameraPager -> {
                CameraPageContent(
                    stitcherViewModel, cameraViewModel
                ) {
                    navigationStack.back()
                }
            }
        }
    }
}

@Composable
fun localContent(
    stitcherViewModel: StitcherViewModel,
    cameraViewModel: CameraViewModel,
) {
    BoxWithConstraints {
        contentProvider(stitcherViewModel, cameraViewModel)
    }
}