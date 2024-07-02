package com.taehokimmm.hapticvboard_android

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardLayout(onKeyRelease: (String) -> Unit) {
    val keyState = remember { mutableStateOf(false) }
    val keyPositions = remember { mutableStateOf(mapOf<String, LayoutCoordinates>()) }
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        rootCoordinates?.let {
                            pointerPosition = it.localToRoot(offset)
                            keyState.value = true
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        rootCoordinates?.let {
                            pointerPosition = it.localToRoot(change.position)
                        }
                    },
                    onDragEnd = {
                        handleKeyRelease(
                            keyState,
                            keyPositions,
                            pointerPosition,
                            onKeyRelease
                        )
                    },
                    onDragCancel = {
                        handleKeyRelease(
                            keyState,
                            keyPositions,
                            pointerPosition,
                            onKeyRelease
                        )
                    }
                )
            }
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
            keys.forEachIndexed { rowIndex, rowKeys ->
//                if (rowIndex != 0) Spacer(modifier = Modifier.height(4.dp))
                Row {
                    rowKeys.forEach { key ->
                        DrawKey(
                            key = key,
                            isPressed = keyState.value && isKeyUnderPointer(
                                key,
                                keyPositions.value,
                                pointerPosition
                            ),
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

fun isKeyUnderPointer(
    key: String,
    keyPositions: Map<String, LayoutCoordinates>,
    pointerPosition: Offset,
): Boolean {
    val coordinates = keyPositions[key] ?: return false
    val topLeft = coordinates.positionInRoot()
    val bottomRight =
        Offset(topLeft.x + coordinates.size.width, topLeft.y + coordinates.size.height)
    val isUnderPointer =
        pointerPosition.x in topLeft.x..bottomRight.x && pointerPosition.y in topLeft.y..bottomRight.y
    if (isUnderPointer) {
        println("Pointer on key: $key at $pointerPosition")
    }
    return isUnderPointer
}

fun handleKeyRelease(
    keyState: MutableState<Boolean>,
    keyPositions: MutableState<Map<String, LayoutCoordinates>>,
    pointerPosition: Offset,
    onKeyReleased: (String) -> Unit
) {
    keyPositions.value.forEach { (key, coordinates) ->
        val topLeft = coordinates.positionInRoot()
        val bottomRight =
            Offset(topLeft.x + coordinates.size.width, topLeft.y + coordinates.size.height)
        if (pointerPosition.x in topLeft.x..bottomRight.x && pointerPosition.y in topLeft.y..bottomRight.y) {
            println("Released on key: $key")
            keyState.value = false
            onKeyReleased(key)
        }
    }
}

@Preview
@Composable
fun KeyboardLayoutPreview() {
    KeyboardLayout({})
}
