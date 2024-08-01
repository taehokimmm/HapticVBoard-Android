package com.taehokimmm.hapticvboard_android.layout.study1

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
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

    fun onSelect(index: Int) {
        hapticManager?.generateHaptic(
            group[selectedTabIndex][index],
            HapticMode.PHONEME
        )
        Log.e("Speak Out", " : "+ group[selectedTabIndex][index].toUpperCase())
        soundManager?.speakOut(group[selectedTabIndex][index].toUpperCase())
        selectedIndex = index
    }

    // Swipe Gesture
    var swipeAmount by remember{mutableStateOf(0f)}
    var swipeStartTime by remember{mutableStateOf(0L)}

    LaunchedEffect(selectedTabIndex) {
        selectedIndex = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {onSelect(selectedIndex)}
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {offset ->
                        swipeStartTime = System.currentTimeMillis() },
                    onHorizontalDrag = {change, dragAmount ->
                        swipeAmount = dragAmount},
                    onDragEnd = {
                        val time = System.currentTimeMillis() - swipeStartTime
                        val speed = swipeAmount / time * 1000
                        if (swipeAmount > 0 && speed > 0) {
                            if (selectedIndex < group[selectedTabIndex].size - 1) {
                                selectedIndex ++
                            } else {
                                selectedIndex = 0
                            }
                            onSelect(selectedIndex)
                        } else if (swipeAmount < 0 && speed < 0) {
                            if (selectedIndex > 0) {
                                selectedIndex --
                            } else {
                                selectedIndex = group[selectedTabIndex].size - 1
                            }
                            onSelect(selectedIndex)
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