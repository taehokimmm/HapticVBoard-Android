package com.taehokimmm.hapticvboard_android

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
    soundManager: SoundManager? = null
) {
    // Coordinates for each key
    val keyPositions = remember { mutableStateOf(mapOf<String, LayoutCoordinates>()) }

    // Active touch pointers
    val activeTouches = remember { mutableStateMapOf<Int, String>() }

    // Root coordinates for global positioning
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .onGloballyPositioned { coordinates ->
                rootCoordinates = coordinates
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val keys = listOf(
                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "Backspace"),
                listOf(",", "Space", ".")
            )

            Spacer(modifier = Modifier.height(16.dp))
            keys.forEach { rowKeys ->
                Row {
                    rowKeys.forEach { key ->
                        DrawKey(
                            key = key,
                            isPressed = activeTouches.values.contains(key),
                            onPositioned = { coordinates ->
                                handlePositioned(key, coordinates, keyPositions)
                            }
                        )
                    }
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
        processTouchEvent(event, keyPositions.value, activeTouches, onKeyRelease, soundManager!!)
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
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .padding(spacing)
            .clip(RoundedCornerShape(5.dp))
            .size(
                when (key) {
                    "Space" -> width * 5 + spacing * 8
                    "Shift", "Backspace" -> width * 1.5f
                    else -> width
                }, height
            )
            .background(backgroundColor)
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (key) {
                "Backspace" -> "⌫"
                "Enter" -> "↵"
                "Shift" -> "⇧"
                "Space" -> " "
                else -> key
            },
            fontSize = textSize,
            color = Color.Black
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
    soundManager: SoundManager
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
                    soundManager.playSoundForKey(key)
                    println("Initial key pressed: $key for pointer $pointerId")
                }
            }
        }

        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
            val pointerId = event.getPointerId(event.actionIndex)
            val key = activeTouches.remove(pointerId)
            if (key != null) {
                println("Key released: $key for pointer $pointerId")
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
                    println("Key moved from ${activeTouches[pointerId]} to $key for pointer $pointerId")
                    soundManager.playSoundForKey(key)
                    activeTouches[pointerId] = key
                }
            }
        }
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
        touchEvents = emptyList(),
        onKeyRelease = {},
        soundManager = null
    )
}