package com.taehokimmm.hapticvboard_android.layout.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.MotionEvent
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.addLog
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun KeyboardLayout(
    touchEvents: List<MotionEvent>,
    onKeyPress: ((String) -> Unit)? = null,
    onKeyRelease: (String) -> Unit,
    enterKeyVisibility: Boolean = false,
    soundManager: SoundManager? = null,
    hapticManager: HapticManager?,
    hapticMode: HapticMode = HapticMode.NONE,
    allow: List<String> = ('a'..'z').map { it.toString() } + listOf(
        "Space", "Backspace", "Replay"
    ),
    logData: Any? = null,
    name: String? = ""
) {
    val context = LocalContext.current
    // Coordinates for each key
    val keyPositions = remember { mutableStateOf(mapOf<String, LayoutCoordinates>()) }

    // Active touch pointers
    val activeTouches = remember { mutableStateMapOf<Int, String>() }

    // Root coordinates for global positioning
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(
            Color(
                255, 235, 205
            )
        )
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
            val lastRow = listOf("Space")

            Spacer(modifier = Modifier.height(20.dp))
            keys.forEach { rowKeys ->
                Row {
                    rowKeys.forEach { key ->
                        DrawKey(key = key,
                            isPressed = activeTouches.values.contains(key) || (!(allow.contains(key))),
                            onPositioned = { coordinates ->
                                handlePositioned(key, coordinates, keyPositions)
                            })
                    }
                }
                Spacer(modifier = Modifier.height(0.dp))
            }

            Row {
                if (enterKeyVisibility) {
                    Spacer(modifier = Modifier.width(57.dp))
                }
                lastRow.forEach { key ->
                    DrawKey(key = key,
                        isPressed = activeTouches.values.contains(key) || (!(allow.contains(key))),
                        onPositioned = { coordinates ->
                            handlePositioned(key, coordinates, keyPositions)
                        })
                }
                if (enterKeyVisibility) {
                    DrawKey(key = "Replay",
                        isPressed = activeTouches.values.contains("Replay"),
                        onPositioned = { coordinates ->
                            handlePositioned("Replay", coordinates, keyPositions)
                        })
                }
            }


            Spacer(modifier = Modifier.height(15.dp))
        }
    }

    // Assuming touchEvents is a parameter of type List<MotionEvent>
    // Create a mutable copy for local modification
    val mutableTouchEvents = touchEvents.toMutableList()

    if (mutableTouchEvents.isNotEmpty()) {
        processTouchEvent(
            mutableTouchEvents,
            keyPositions.value,
            activeTouches,
            onKeyPress,
            onKeyRelease,
            soundManager!!,
            hapticManager!!,
            hapticMode,
            allow,
            logData,
            context,
            name
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
    val width = 41.dp
    val height = 63.dp
    val textSize = 28.sp
    val spacing = 0.dp

    val backgroundColor = when {
        isPressed -> Color.Gray
        key == "Replay" -> Color(0xFF006AFF)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .padding(spacing)
            .border(1.dp, Color.Black)
            .size(
                when (key) {
                    "Space" -> width * 6 + spacing * 8
                    "Replay" -> width * 2.5f
                    "Shift", "Backspace", -> width * 1.5f
                    else -> width
                },
                when (key) {
                    "Space", "Replay" -> height * 2f
                    else -> height
                })
            .background(backgroundColor)
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates)
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (key) {
                "Backspace" -> "⌫"
                "Replay" -> "Replay"
                "Shift" -> "⇧"
                "Space" -> " "
                else -> key
            },
            fontSize = if (key == "Replay") 18.sp else textSize,
            color = if (key == "Replay") Color.White else Color.Black,

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

fun replaySound(
    event: MotionEvent,
    pointerId: Int,
    activeTouches: MutableMap<Int, String>,
    allow: List<String>,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    if (activeTouches.containsKey(pointerId)) return
    activeTouches[pointerId] = "true"
    // Play sound for additional touches
    val key = activeTouches[event.getPointerId(0)]!!
    if (allow.contains(key)) hapticManager?.generateHaptic(key, hapticMode)
    else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
        key,
        HapticMode.VOICE
    )
    Log.d("TouchEvent", "additional touch: $key")
}

fun processTouchEvent(
    events: MutableList<MotionEvent>,
    keyPositions: Map<String, LayoutCoordinates>,
    activeTouches: MutableMap<Int, String>,
    onKeyPressed: ((String) -> Unit)?,
    onKeyReleased: (String) -> Unit,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode,
    allow: List<String>,
    logData: Any?,
    context: Context,
    name: String?
) {
    for(event in events) {
        if (event.pointerCount == 1) {
            val pointerId = event.getPointerId(event.actionIndex)
            activeTouches.keys.forEach { id -> if (pointerId != id) activeTouches.remove(id) }
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_1_DOWN -> {
                val pointerId = event.getPointerId(event.actionIndex)
                replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode)
            }
            MotionEvent.ACTION_POINTER_1_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)
                activeTouches.remove(pointerId)
            }
            MotionEvent.ACTION_DOWN-> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)

                    if (i >= 1) {
                        replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode)
                    }

                    val pointerPosition = Offset(event.getX(i), event.getY(i))
                    val key = keyPositions.entries.find { (_, coordinates) ->
                        isPointerOverKey(coordinates, pointerPosition)
                    }?.key
                    if (key != null && activeTouches[pointerId] != key) {
                        activeTouches[pointerId] = key
                        if (allow.contains(key)) hapticManager?.generateHaptic(key, hapticMode)
                        else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
                            key,
                            HapticMode.VOICE
                        )
                        Log.d("TouchEvent", "Initial key pressed: $key for pointer $pointerId")
                        if (onKeyPressed != null)
                            onKeyPressed(key)
                        // Add Log
                        if (name != null && logData != null) {
                            addLog(
                                context,
                                name,
                                logData,
                                "DOWN",
                                key,
                                pointerPosition.x,
                                pointerPosition.y
                            )
                        }
                    }
                    else if (pointerPosition.y < 1533) {
                        // Key pressed out of bounds
                        Log.d(
                            "TouchEvent",
                            "Key pressed out of bounds for pointer $pointerId, Coordinates: $pointerPosition"
                        )
                        activeTouches[pointerId] = "Out of Bounds"
                        // Add Log
                        if (name != null && logData != null) {
                            addLog(
                                context,
                                name,
                                logData,
                                "DOWN",
                                "Out of Bounds",
                                pointerPosition.x,
                                pointerPosition.y
                            )
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)

                val key = activeTouches.remove(pointerId)
                if (key != null && key != "true") {
                    if (key != "Out of Bounds") {
                        if (allow.contains(key))
                            hapticManager?.generateHaptic(key, hapticMode)
                        else {
                            if (hapticMode == HapticMode.VOICEPHONEME)
                                hapticManager?.generateHaptic(key, HapticMode.VOICETICK)
                            else
                                hapticManager?.generateHaptic(key, HapticMode.TICK)
                        }
                        onKeyReleased(key)
                    }

                    // Add Log
                    if (name != null && logData != null) {
                        val pointerPosition =
                            Offset(event.getX(event.actionIndex), event.getY(event.actionIndex))
                        addLog(
                            context, name, logData, "UP", key, pointerPosition.x, pointerPosition.y
                        )
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    if (i >= 1) {
                        replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode)
                        // ignore the move event for additional touches
                        continue
                    }

                    val pointerPosition = Offset(event.getX(i), event.getY(i))
                    val key = keyPositions.entries.find { (_, coordinates) ->
                        isPointerOverKey(coordinates, pointerPosition)
                    }?.key
                    if (key != null && activeTouches[pointerId] != key) {

                        Log.d(
                            "TouchEvent",
                            "Key moved from ${activeTouches[pointerId]} to $key for pointer $pointerId"
                        )
                        if (allow.contains(key))
                            hapticManager?.generateHaptic(key, hapticMode)
                        else {
                            if (hapticMode == HapticMode.VOICEPHONEME)
                                hapticManager?.generateHaptic(key, HapticMode.VOICETICK)
                            else
                                hapticManager?.generateHaptic(key, HapticMode.TICK)
                        }

                        if (activeTouches[pointerId] == "Out of Bounds")
                            hapticManager!!.generateVibration("Replay")
                        activeTouches[pointerId] = key

                        // Add Log
                        if (name != null && logData != null) {
                            addLog(
                                context, name, logData,
                                if (activeTouches[pointerId] == null) "DOWN" else "MOVE",
                                key,
                                pointerPosition.x, pointerPosition.y
                            )
                        }

                        if (activeTouches[pointerId] == null) {
                            if (onKeyPressed != null)
                                onKeyPressed(key)
                        }

                        activeTouches[pointerId] = key
                    } else if (key == null && pointerPosition.y < 1533) {
                        // Key moved out of bounds, need to fix random number 1533
                        Log.d(
                            "TouchEvent",
                            "Key moved out of bounds from ${activeTouches[pointerId]} for pointer $pointerId, Coordinates: $pointerPosition"
                        )
                        if (activeTouches[pointerId] != "Out of Bounds" && activeTouches[pointerId] != null)
                            hapticManager!!.generateVibration("Replay")
                        activeTouches[pointerId] = "Out of Bounds"
                        // Add Log
                        if (name != null && logData != null) {
                            addLog(
                                context, name, logData, "MOVE", "Out of Bounds",
                                pointerPosition.x, pointerPosition.y
                            )
                        }
                    }
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
        soundManager = null,
        hapticManager = null,
        hapticMode = HapticMode.NONE
    )
}