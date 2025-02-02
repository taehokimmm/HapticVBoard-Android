package com.taehokimmm.hapticvboard_android.layout.view

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.lang.Math.round

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
    lastWord: Char? = null,
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

    val handler = Handler(Looper.getMainLooper())
    var runnable by remember { mutableStateOf<Runnable?>(null)}
    var isRepeat by remember {mutableStateOf(false)}
    val outOfBound = 1566
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
    var arrayX: Array<Float?> = remember{arrayOfNulls<Float>(MASK_LENGTH)}
    var arrayY: Array<Float?> = remember{arrayOfNulls<Float>(MASK_LENGTH)}
    var array_index by remember{mutableStateOf(0)};
    fun movingAverageFilter() : Offset? {
        val sortedX = arrayX.filterNotNull().sorted()
        val sortedY = arrayY.filterNotNull().sorted()

        if (sortedX.isEmpty() || sortedY.isEmpty()) return null
        var mediumX = sortedX[sortedX.size/2]
        var mediumY = sortedY[sortedY.size/2]

        if (mediumX == null || mediumY == null) return null
        return Offset(mediumX, mediumY)
    }

    fun insertIntoArray(offset: Offset) {
        arrayX[array_index] = offset.x;
        arrayY[array_index] = offset.y;
        array_index++;
        if (array_index >= MASK_LENGTH) {
            array_index = 0;
        }
    }
    fun initializeArray() {
        for(i in 0 .. MASK_LENGTH -1 ) {
            arrayX[i] = null
            arrayY[i] = null
        }
        array_index = 0;
    }

    // Assuming touchEvents is a parameter of type List<MotionEvent>
    // Create a mutable copy for local modification
    val mutableTouchEvents = touchEvents.toMutableList()

    if (mutableTouchEvents.isNotEmpty()) {
        var touchPos: Offset? = null
        val outOfBound = 1566
        for(event in mutableTouchEvents) {
            if (event.pointerCount == 1) {
                val pointerId = event.getPointerId(event.actionIndex)
                activeTouches.keys.forEach { id -> if (pointerId != id) activeTouches.remove(id) }
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_1_DOWN -> {
                    Log.d("hapticManager", "action down 1")
                    val pointerId = event.getPointerId(event.actionIndex)
                    replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData, lastWord)
                }
                MotionEvent.ACTION_POINTER_1_UP -> {
                    val pointerId = event.getPointerId(event.actionIndex)
                    activeTouches.remove(pointerId)
                }
                MotionEvent.ACTION_DOWN-> {
                    for (i in 0 until event.pointerCount) {
                        val pointerId = event.getPointerId(i)

                        val pointerPosition = Offset(event.getX(i), event.getY(i))
                        val key = keyPositions.value.entries.find { (_, coordinates) ->
                            isPointerOverKey(coordinates, pointerPosition)
                        }?.key

                        if (i >= 1) {
                            replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData, lastWord)
                            continue
                        }
                        if (key != null && activeTouches[pointerId] != key) {
                            if (onKeyPress != null)
                                onKeyPress(key)

                            activeTouches[pointerId] = key
                            if (allow.contains(key)) {
                                if (key == "delete") onDelete(lastWord, hapticMode, hapticManager)
                                else hapticManager?.generateHaptic(key, hapticMode, isPress = true)
                            }
                            else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
                                key,
                                HapticMode.VOICE, isPress = true
                            )

                        }
                        else if (pointerPosition.y < outOfBound) {
                            // Key pressed out of bounds
//                        Log.d(
//                            "TouchEvent",
//                            "Key pressed out of bounds for pointer $pointerId, Coordinates: $pointerPosition"
//                        )
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
                        touchPos = pointerPosition
                    }
                }

                MotionEvent.ACTION_UP -> {

                    val movingWindowAverage = movingAverageFilter()

                    val pointerId = event.getPointerId(event.actionIndex)

                    var key: String? = null
                    if (movingWindowAverage != null) {
                        key = keyPositions.value.entries.find { (_, coordinates) ->
                            isPointerOverKey(coordinates, movingWindowAverage)
                        }?.key
                    }

                    val originkey = activeTouches.remove(pointerId)
                    if (originkey == null) {
                        key = null
                        touchPos = null
                    }

                    Log.d(
                        "TouchEvent",
                        "Key Up Moving Window: $movingWindowAverage, key: $key originKey: $originkey"
                    )
                    if (key != null && key != "true") {
                        if (key != "Out of Bounds") {
                            if (allow.contains(key)) {
                                if(key == "delete") {
                                    onDelete(lastWord, hapticMode, hapticManager, MotionEvent.ACTION_UP)
                                }
                                else hapticManager?.generateHaptic(key, hapticMode)
                            }
                            else {
                                if (hapticMode == HapticMode.VOICEPHONEME)
                                    hapticManager?.generateHaptic(key, HapticMode.VOICE)
                                else
                                    hapticManager?.generateHaptic(key, HapticMode.TICK)
                            }
                            onKeyRelease(key)
                        }

                        // Add Log
                        if (name != null && logData != null) {
                            val pointerPosition =
                                Offset(event.getX(event.actionIndex), event.getY(event.actionIndex))
                            addLog(
                                context, name, logData, "UP", key, pointerPosition.x, pointerPosition.y
                            )
                        }
                        runnable?.let { handler.removeCallbacks(it) }
                        isRepeat = false
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        val pointerId = event.getPointerId(i)

                        val pointerPosition = Offset(event.getX(i), event.getY(i))
                        val key = keyPositions.value.entries.find { (_, coordinates) ->
                            isPointerOverKey(coordinates, pointerPosition)
                        }?.key

                        if (i >= 1) {
                            replaySound(event, pointerId, activeTouches, allow, hapticManager, hapticMode, context, name, logData, lastWord)
                            // ignore the move event for additional touches
                            continue
                        }

                        if (pointerPosition.y > outOfBound && activeTouches[pointerId] == "Out of Bounds") {
                            activeTouches[pointerId] = ""
                        }

                        if (key != null && activeTouches[pointerId] != key) {

                        Log.d(
                            "TouchEvent",
                            "Key moved from ${activeTouches[pointerId]} to $key for pointer $pointerId"
                        )

                            if (allow.contains(key)) {
                                if (key == "delete") {
                                    onDelete(lastWord, hapticMode, hapticManager)
                                } else hapticManager?.generateHaptic(key, hapticMode, isPress = true)

                            }
                            else {
                                if (hapticMode == HapticMode.VOICEPHONEME)
                                    hapticManager?.generateHaptic(key, HapticMode.VOICE, isPress = true)
                            }

                            if (activeTouches[pointerId] == null) {
                                if (onKeyPress != null)
                                    onKeyPress(key)
                            }
                        } else if (key == null && pointerPosition.y < outOfBound && activeTouches[pointerId] != "Out of Bounds") {
                            // Key moved out of bounds, need to fix random number 1566
                        Log.d(
                            "TouchEvent",
                            "Key moved out of bounds from ${activeTouches[pointerId]} for pointer $pointerId, Coordinates: $pointerPosition"
                        )
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
                        touchPos = pointerPosition
                    }
                }
            }

        }
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

fun onDelete(
    lastWord: Char?,
    hapticMode:HapticMode,
    hapticManager: HapticManager?,
    motionEvent: Int? = MotionEvent.ACTION_DOWN) {

    val delayMillis: Long = 250L
    if (lastWord == null) {
        hapticManager?.generateHaptic("delete", hapticMode)
        return
    }

    var deletedWord = lastWord.toString()
    if (lastWord == ' ') deletedWord = "Space"
    Log.d("hapticManager", "on deleted $hapticMode")
    when(hapticMode) {
        HapticMode.PHONEME -> {
            hapticManager?.generateHaptic("delete",hapticMode)
            delay({
                hapticManager?.generateHaptic(deletedWord, HapticMode.PHONEME)
            }, delayMillis)
        }
        HapticMode.VOICE -> {
            if (motionEvent == MotionEvent.ACTION_UP)
                hapticManager?.generateHaptic(deletedWord + " deleted", hapticMode)
            else
                hapticManager?.generateHaptic("delete " + deletedWord, hapticMode)
        }
        HapticMode.VOICEPHONEME -> {
            if (motionEvent == MotionEvent.ACTION_UP)
                hapticManager?.generateHaptic(deletedWord + " deleted", HapticMode.VOICE)
            else
                hapticManager?.generateHaptic("delete " + deletedWord, HapticMode.VOICE)

            hapticManager?.generateHaptic("delete", HapticMode.PHONEME)
            delay({
                hapticManager?.generateHaptic(deletedWord, HapticMode.PHONEME)
            }, delayMillis)
        }
        else -> {}
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
    logData: Any?,
    lastWord: Char?,
    delayMillis: Long = 250L,
) {
    if (activeTouches.containsKey(pointerId)) return
    activeTouches[pointerId] = "true"
    // Play sound for additional touches
    val key = activeTouches[event.getPointerId(0)]!!
    val pointerPosition = Offset(event.getX(pointerId), event.getY(pointerId))
    if (allow.contains(key)) {
        if (key == "delete") {
            onDelete(lastWord, hapticMode, hapticManager)
        } else {
            hapticManager?.generateHaptic(key, hapticMode, isPress = true)
        }
    }
    else if (hapticMode == HapticMode.VOICEPHONEME) hapticManager?.generateHaptic(
        key,
        HapticMode.VOICE
    )

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