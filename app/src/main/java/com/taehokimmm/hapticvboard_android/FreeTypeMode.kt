package com.taehokimmm.hapticvboard_android

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun FreeTypeMode(
    innerPadding: PaddingValues,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var options = listOf("yes", "no")
    var selectedOption by remember { mutableStateOf("no") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "Is Tick?")
                Column {
                    options.forEachIndexed{index, option -> (
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedOption == option,
                                    onClick = {
                                        selectedOption = option
                                    }
                                )
                                Text(text = option)
                            }
                            )}
                }
            }

            TextButton(onClick = { inputText = "" }) {
                Text("Clear", color = Color(0xFF006AFF), fontSize = 20.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp, 16.dp)
                    .height(30.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = inputText,
                    fontSize = 20.sp,
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { key ->
                        if (key == "delete") {
                            if (hapticMode == HapticMode.VOICE) {
                                val deletedChar = if (inputText.isNotEmpty())
                                    inputText.last() + "Deleted" else "nothing deleted"
                                soundManager?.speakOut(deletedChar)
                            }
                        }

                        inputText = when (key) {
                            "delete" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            else -> inputText + key
                        }
                    },
                    lastWord = if(inputText.isNotEmpty()) inputText.last() else null,
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = if (selectedOption == "yes")
                                        HapticMode.VOICEPHONEMETICK
                                    else HapticMode.VOICEPHONEME
                )
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                    MultiTouchView(context).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                })

            }
        }
    }
}

@Composable
fun FreeTypeWithGroup(
    innerPadding: PaddingValues,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    group: List<List<String>>,
    name: List<String>
) {
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var selectedTabIndex by remember { mutableStateOf(0) }

    var allKeys = ('a'..'z').map { it.toString() }
    val allowKeys = if (selectedTabIndex in 0..group.size) {
        allKeys.filter { it in group[selectedTabIndex] }
    } else {
        emptyList()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomStart)
        ) {

            Spacer(modifier = Modifier.height(440.dp))
            Box() {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { },
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = HapticMode.VOICEPHONEME,
                    allow = allowKeys,
                )
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                    MultiTouchView(context).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                })
            }
        }
    }
}

@Preview
@Composable
fun PreviewFreeTypeMode() {
    FreeTypeMode(PaddingValues(0.dp), null, null, HapticMode.NONE)
}

@Preview
@Composable
fun PreviewFreeTypewithGroup() {
    FreeTypeWithGroup(PaddingValues(0.dp), null, null, listOf(listOf()), listOf())
}