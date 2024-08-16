package com.taehokimmm.hapticvboard_android.layout.study1.intro

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainGroup(
    innerPadding: PaddingValues,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    group: List<List<String>>,
    name: List<String>
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(0) }
    var isExplaining by remember {mutableStateOf(false)}

    val handler = Handler(Looper.getMainLooper())
    val runnables = remember { mutableStateListOf<Runnable>() }

    fun explainKey(key: String) {
        isExplaining = true
        soundManager?.speakOutChar(key)

        // Clear any previous runnables before adding new ones
        runnables.clear()

        runnables.add(
            delay({soundManager?.playPhoneme(key)},1500, handler)
        )
        runnables.add(
            delay({hapticManager?.generateHaptic( key,HapticMode.PHONEME) },2200, handler)
        )
        runnables.add(
            delay({isExplaining = false}, 2200, handler)
        )
    }

    fun onSelect(index: Int) {
        if (isExplaining) {
            runnables.forEach { handler.removeCallbacks(it) }
            isExplaining = false
        }
        val key = group[selectedTabIndex][index]
        explainKey(key)
        selectedIndex = index
    }

    // Swipe Gesture
    var horizontalSwipeStart by remember{mutableStateOf(0f)}
    var horizontalSwipeEnd by remember{mutableStateOf(0f)}
    var verticalSwipeStart by remember{mutableStateOf(0f)}
    var verticalSwipeEnd by remember{mutableStateOf(0f)}

    val swipeThreshold = 50

    LaunchedEffect(selectedTabIndex) {
        if (isExplaining) {
            runnables.forEach { handler.removeCallbacks(it) }
            isExplaining = false
        }
        selectedIndex = 0
        soundManager?.speakOutKor(name[selectedTabIndex])
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect(selectedIndex) },
                    onDoubleTap = {
                        if (isExplaining) return@detectTapGestures
                        soundManager?.speakOutKor(name[selectedTabIndex])
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        horizontalSwipeStart = offset.x
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        horizontalSwipeEnd = change.position.x
                    },
                    onDragEnd = {
                        val horizontalSwipeAmount = horizontalSwipeEnd - horizontalSwipeStart
                        if (horizontalSwipeAmount > swipeThreshold) {
                            if (selectedIndex < group[selectedTabIndex].size - 1) {
                                selectedIndex++
                            } else {
                                selectedIndex = 0
                            }
                            onSelect(selectedIndex)
                        } else if (horizontalSwipeAmount < -swipeThreshold) {
                            if (selectedIndex > 0) {
                                selectedIndex--
                            } else {
                                selectedIndex = group[selectedTabIndex].size - 1
                            }
                            onSelect(selectedIndex)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        verticalSwipeStart = offset.y
                    },
                    onVerticalDrag = { change, dragAmount ->
                        verticalSwipeEnd = change.position.y
                    },
                    onDragEnd = {
                        val verticalSwipeAmount = verticalSwipeEnd - verticalSwipeStart
                        if (verticalSwipeAmount < -swipeThreshold) {
                            if (selectedTabIndex > 0) {
                                selectedTabIndex--
                            } else {
                                selectedTabIndex = name.size - 1
                            }

                        } else if (verticalSwipeAmount > swipeThreshold) {
                            if (selectedTabIndex < name.size - 1) {
                                selectedTabIndex++
                            } else {
                                selectedTabIndex = 0
                            }
                        }
                    }
                )
            }
    ) {
        TabRow(selectedTabIndex = 0, indicator = { tabPositions ->
            SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
            )
        }, tabs = {
            name.forEachIndexed { i, n ->
                TextButton(onClick = { selectedTabIndex = i }) {
                    Text(n, fontSize = 20.sp)
                }
            }
        })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .align(Alignment.BottomStart)
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,

                ) {
                group[selectedTabIndex].forEachIndexed { index, alphabet ->
                    Box (
                        modifier = Modifier
                            .size(120.dp, 120.dp)
                            .padding(0.dp)
                            .align(Alignment.CenterVertically)
                            .background(
                                if (index == selectedIndex) Color.Blue else Color.White
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alphabet.toUpperCase(),
                            color =  (
                                if (index == selectedIndex) Color.White else Color.Blue),
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}