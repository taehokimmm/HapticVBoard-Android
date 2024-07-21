package com.taehokimmm.hapticvboard_android

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.MotionEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun KeyboardLayout(
    touchEvents: List<MotionEvent>,
    onKeyRelease: (String) -> Unit,
    enterKeyVisibility: Boolean = false,
    soundManager: SoundManager? = null,
    serialManager: SerialManager?,
    hapticMode: HapticMode = HapticMode.NONE
) {
    // Coordinates for each key
    val keyPositions = remember { mutableStateOf(mapOf<String, LayoutCoordinates>()) }

    // Active touch pointers
    val activeTouches = remember { mutableStateMapOf<Int, String>() }

    // Root coordinates for global positioning
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Color.LightGray)
        .onGloballyPositioned { coordinates ->
            rootCoordinates = coordinates
        }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val keys = listOf(
                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "Backspace"),
            )
            val lastRow = listOf(",", "Space", ".")

            Spacer(modifier = Modifier.height(16.dp))
            keys.forEach { rowKeys ->
                Row {
                    rowKeys.forEach { key ->
                        DrawKey(key = key,
                            isPressed = activeTouches.values.contains(key),
                            onPositioned = { coordinates ->
                                handlePositioned(key, coordinates, keyPositions)
                            })
                    }
                }
            }

            Row {
                if (enterKeyVisibility) {
                    Spacer(modifier = Modifier.width(57.dp))
                }
                lastRow.forEach { key ->
                    DrawKey(key = key,
                        isPressed = activeTouches.values.contains(key),
                        onPositioned = { coordinates ->
                            handlePositioned(key, coordinates, keyPositions)
                        })
                }
                if (enterKeyVisibility) {
                    DrawKey(key = "Enter",
                        isPressed = activeTouches.values.contains("Enter"),
                        onPositioned = { coordinates ->
                            handlePositioned("Enter", coordinates, keyPositions)
                        })
                }
            }


            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Assuming touchEvents is a parameter of type List<MotionEvent>
    // Create a mutable copy for local modification
    val mutableTouchEvents = touchEvents.toMutableList()

    if (mutableTouchEvents.isNotEmpty()) {
        val event = mutableTouchEvents[0]
        processTouchEvent(
            event, keyPositions.value, activeTouches, onKeyRelease, soundManager!!, serialManager!!, hapticMode
        )
        mutableTouchEvents.clear()
    }
}

@Composable
fun DrawKey(
    key: String,
    isPressed: Boolean,
    onPositioned: (LayoutCoordinates) -> Unit,
) {
    val width = 36.dp
    val height = width * 1.5f
    val textSize = 20.sp
    val spacing = 2.dp

    val backgroundColor = when {
        isPressed -> Color.Gray
        key == "Enter" -> Color(0xFF006AFF)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .padding(spacing)
            .clip(RoundedCornerShape(5.dp))
            .size(
                when (key) {
                    "Space" -> width * 5 + spacing * 8
                    "Shift", "Backspace", "Enter" -> width * 1.5f
                    else -> width
                }, height
            )
            .background(backgroundColor)
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates)
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (key) {
                "Backspace" -> "⌫"
                "Enter" -> "next"
                "Shift" -> "⇧"
                "Space" -> " "
                else -> key
            },
            fontSize = if (key == "Enter") 18.sp else textSize,
            color = if (key == "Enter") Color.White else Color.Black
        )
    }
}

fun handlePositioned(
    key: String,
    coordinates: LayoutCoordinates,
    keyPositions: MutableState<Map<String, LayoutCoordinates>>
) {
    keyPositions.value = keyPositions.value.toMutableMap().apply {
        this[key] = coordinates
    }
}

fun processTouchEvent(
    event: MotionEvent,
    keyPositions: Map<String, LayoutCoordinates>,
    activeTouches: MutableMap<Int, String>,
    onKeyReleased: (String) -> Unit,
    soundManager: SoundManager,
    serialManager: SerialManager?,
    hapticMode: HapticMode
) {
    when (event.actionMasked) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
            for (i in 0 until event.pointerCount) {
                val pointerId = event.getPointerId(i)
                val pointerPosition = Offset(event.getX(i), event.getY(i))
                val key = keyPositions.entries.find { (_, coordinates) ->
                    isPointerOverKey(coordinates, pointerPosition)
                }?.key
                if (key != null) {
                    activeTouches[pointerId] = key
                    hapticFeedback(soundManager, serialManager!!, hapticMode, key)
                    Log.d("TouchEvent", "Initial key pressed: $key for pointer $pointerId")
                }
            }
        }

        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
            val pointerId = event.getPointerId(event.actionIndex)
            val key = activeTouches.remove(pointerId)
            if (key != null) {
                Log.d("TouchEvent", "Key released: $key for pointer $pointerId")
                onKeyReleased(key)
            }
        }

        MotionEvent.ACTION_MOVE -> {
            for (i in 0 until event.pointerCount) {
                val pointerId = event.getPointerId(i)
                val pointerPosition = Offset(event.getX(i), event.getY(i))
                val key = keyPositions.entries.find { (_, coordinates) ->
                    isPointerOverKey(coordinates, pointerPosition)
                }?.key
                if (key != null && activeTouches[pointerId] != key) {
                    Log.d(
                        "TouchEvent",
                        "Key moved from ${activeTouches[pointerId]} to $key for pointer $pointerId"
                    )
                    hapticFeedback(soundManager, serialManager!!, hapticMode, key)
                    activeTouches[pointerId] = key
                }
            }
        }
    }
}

fun hapticFeedback(
    soundManager: SoundManager,
    serialManager: SerialManager,
    hapticMode: HapticMode,
    key: String,
) {
    when (hapticMode) {
        HapticMode.VOICE -> soundManager.playSoundForKey(key)
        HapticMode.SERIAL -> serialManager.write("P${key.uppercase()}WAV".toByteArray())
        else -> return
    }
}

fun isPointerOverKey(coordinates: LayoutCoordinates, pointerPosition: Offset): Boolean {
    val topLeft = coordinates.positionInRoot()
    val bottomRight =
        Offset(topLeft.x + coordinates.size.width, topLeft.y + coordinates.size.height)
    return pointerPosition.x in topLeft.x..bottomRight.x && pointerPosition.y in topLeft.y..bottomRight.y
}

@Preview
@Composable
fun KeyboardLayoutPreview() {
    KeyboardLayout(
        touchEvents = emptyList(), onKeyRelease = {}, soundManager = null, serialManager = null, hapticMode = HapticMode.NONE
    )
}