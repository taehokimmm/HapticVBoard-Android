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
import com.taehokimmm.hapticvboard_android.MovingWindowFilter
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
    allow: List<String> = ('a'..'z').map { it.toString() } + listOf("Space", "delete","Shift"),
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

    val outOfBound = 1566

    val movingWindowFilter = MovingWindowFilter();

    val midX = MovingWindowFilter()
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
                listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "delete"),
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
                lastRow.forEach { key ->
                    DrawKey(key = key,
                        isPressed = activeTouches.values.contains(key) || (!(allow.contains(key))),
                        onPositioned = { coordinates ->
                            handlePositioned(key, coordinates, keyPositions)
                        })
                }
            }


            Spacer(modifier = Modifier.height(15.dp))
        }
    }


    // Moving Filter
    val MASK_LENGTH = 10;
    var array: Array<Offset?> = remember{arrayOfNulls<Offset>(MASK_LENGTH)}
    var time: Array<Long?> = remember{arrayOfNulls<Long>(MASK_LENGTH)}
    var array_index by remember{mutableStateOf(0)};
    fun movingAverageFilter() : Offset? {
        val i:Int = 0
        var sumX:Float = 0.0F
        var sumY:Float = 0.0F
        var nullNum: Int = 0

        for (i in 0 ..  MASK_LENGTH - 1) {
            if(array[i] != null) {
                sumX += array[i]?.x!!
                sumY += array[i]?.y!!
            } else {
                nullNum ++
            }
        }

        if (nullNum == MASK_LENGTH) {
            return null
        }
        return Offset(sumX/MASK_LENGTH, sumY/MASK_LENGTH);
    }

    fun insertIntoArray(offset: Offset) {
        array[array_index] = offset;
        if (time[array_index] != null )
            Log.d("touchevent", "$array_index : ${time[array_index]?.minus(System.currentTimeMillis())}")
        time[array_index] = System.currentTimeMillis()
        array_index++;
        if (array_index >= MASK_LENGTH) {
            array_index = 0;
        }
    }
    // 1731389676418 - 1731389676307
    fun initializeArray() {
        array = arrayOfNulls<Offset>(MASK_LENGTH)
    }

    // Assuming touchEvents is a parameter of type List<MotionEvent>
    // Create a mutable copy for local modification
    val mutableTouchEvents = touchEvents.toMutableList()

    if (mutableTouchEvents.isNotEmpty()) {
        val touchPos: Offset? = processTouchEvent(
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
            name,
            movingAverageFilter()
        )
        if (touchPos == null) {
            initializeArray()
        } else {
            insertIntoArray(touchPos)
        }
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
                    "a" -> width * 1.5f
                    "l" -> width * 1.5f
                    "Space" -> width * 7
                    "Replay" -> width * 2.5f
                    "Shift", "delete", -> width * 1.5f
                    else -> width
                },
                when (key) {
                    "Space" -> height * 1.8f
                    else -> height
                })
            .background(backgroundColor)
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates)
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (key) {
                "delete" -> "⌫"
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
    hapticMode: HapticMode,
    context: Context,
    name: String?,
    logData: Any?
) {
    if (activeTouches.containsKey(pointerId)) return
    activeTouches[pointerId] = "true"
    // Play sound for additional touches
    val key = activeTouches[event.getPointerId(0)]!!
    val pointerPosition = Offset(event.getX(pointerId), event.getY(pointerId))
    if (allow.contains(key)) hapticManager?.generateHaptic(key, hapticMode)
    else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
        key,
        HapticMode.VOICE
    )
//    Log.d("TouchEvent", "additional touch: $key")

    if (name != null && logData != null) {
        addLog(
            context,
            name,
            logData,
            "Replay",
            key,
            pointerPosition.x,
            pointerPosition.y
        )
    }
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
    name: String?,
    movingWindowAverage: Offset?,
) : Offset? {
    var returnValue: Offset? = null
    val outOfBound = 1566
    for(event in events) {
        if (event.pointerCount == 1) {
            val pointerId = event.getPointerId(event.actionIndex)
            activeTouches.keys.forEach { id -> if (pointerId != id) activeTouches.remove(id) }
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_1_DOWN -> {
                val pointerId = event.getPointerId(event.actionIndex)
                replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData)
            }
            MotionEvent.ACTION_POINTER_1_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)
                activeTouches.remove(pointerId)
            }
            MotionEvent.ACTION_DOWN-> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)

                    val pointerPosition = Offset(event.getX(i), event.getY(i))
                    val key = keyPositions.entries.find { (_, coordinates) ->
                        isPointerOverKey(coordinates, pointerPosition)
                    }?.key

                    if (i >= 1) {
                        replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData)
                    }

                    if (key != null && activeTouches[pointerId] != key) {
                        if (onKeyPressed != null)
                            onKeyPressed(key)
                        
                        activeTouches[pointerId] = key
                        if (allow.contains(key)) hapticManager?.generateHaptic(key, hapticMode)
                        else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
                            key,
                            HapticMode.VOICE
                        )
                        Log.d("TouchEvent", "Initial key pressed: $key for pointer $pointerId")
                    }
                    else if (pointerPosition.y < outOfBound) {
                        // Key pressed out of bounds
                        Log.d(
                            "TouchEvent",
                            "Key pressed out of bounds for pointer $pointerId, Coordinates: $pointerPosition"
                        )
                        activeTouches[pointerId] = "Out of Bounds"
                    }


                    // Add Log
                    if (name != null && logData != null) {
                        addLog(
                            context,
                            name,
                            logData,
                            "DOWN",
                            key ?: "Out of Bounds",
                            pointerPosition.x,
                            pointerPosition.y
                        )
                    }
                    returnValue = pointerPosition
                }
            }

            MotionEvent.ACTION_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)

                var key: String? = null
                if (movingWindowAverage != null) {
                    key = keyPositions.entries.find { (_, coordinates) ->
                        isPointerOverKey(coordinates, movingWindowAverage)
                    }?.key
                }

                val originkey = activeTouches.remove(pointerId)
                if (originkey == null) {
                    key = null
                    returnValue = null
                }

                if (key != null && key != "true") {
                    Log.d(
                        "TouchEvent",
                        "Key Up Moving Window: $movingWindowAverage, key: $key originKey: $originkey"
                    )
                    if (key != "Out of Bounds") {
                        if (allow.contains(key)) {
                            if(key == "delete" || (key == "Space" && hapticMode != HapticMode.PHONEME)) {

                            } else {
                                hapticManager?.generateHaptic(key, hapticMode)
                            }
                        }
                        else {
                            if (hapticMode == HapticMode.VOICEPHONEME)
                                hapticManager?.generateHaptic(key, HapticMode.VOICETICK)
                            else
                                hapticManager?.generateHaptic(key, HapticMode.TICK)
                        }
                        onKeyReleased(key)
                    } else {
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

                    val pointerPosition = Offset(event.getX(i), event.getY(i))
                    val key = keyPositions.entries.find { (_, coordinates) ->
                        isPointerOverKey(coordinates, pointerPosition)
                    }?.key

                    if (i >= 1) {
                        replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData)
                        // ignore the move event for additional touches
                        continue
                    }

                    if (pointerPosition.y > outOfBound && activeTouches[pointerId] == "Out of Bounds") {
                        activeTouches[pointerId] = ""
                    }

                    if (key != null && activeTouches[pointerId] != key) {

//                        Log.d(
//                            "TouchEvent",
//                            "Key moved from ${activeTouches[pointerId]} to $key for pointer $pointerId"
//                        )

                        if (allow.contains(key)) {
                            hapticManager?.generateHaptic(key, hapticMode)
                        }
                        else {
                            if (hapticMode == HapticMode.VOICEPHONEME)
                                hapticManager?.generateHaptic(key, HapticMode.VOICE)
                        }

                        if (activeTouches[pointerId] == null) {
                            if (onKeyPressed != null)
                                onKeyPressed(key)
                        }
                    } else if (key == null && pointerPosition.y < outOfBound && activeTouches[pointerId] != "Out of Bounds") {
                        // Key moved out of bounds, need to fix random number 1566
//                        Log.d(
//                            "TouchEvent",
//                            "Key moved out of bounds from ${activeTouches[pointerId]} for pointer $pointerId, Coordinates: $pointerPosition"
//                        )
                        // Add Log
                        if (name != null && logData != null) {
                            addLog(
                                context, name, logData,
                                if (activeTouches[pointerId] == null) "DOWN" else "MOVE",
                                "Out of Bounds",
                                pointerPosition.x, pointerPosition.y
                            )
                        }
                    }


                    // Add Log
                    if (name != null && logData != null) {
                        addLog(
                            context, name, logData,
                            if (activeTouches[pointerId] == null) "DOWN" else "MOVE",
                            key ?: "Out of Bounds",
                            pointerPosition.x, pointerPosition.y
                        )
                    }
                    activeTouches[pointerId] = key?: "Out of Bounds"
                    returnValue = pointerPosition
                }
            }
        }

    }
    return returnValue
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