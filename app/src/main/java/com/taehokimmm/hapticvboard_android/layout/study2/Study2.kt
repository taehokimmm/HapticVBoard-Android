package com.taehokimmm.hapticvboard_android.layout.study2

import android.content.Context
import android.view.MotionEvent
import android.widget.EditText
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.calculateIKI
import com.taehokimmm.hapticvboard_android.calculateUER
import com.taehokimmm.hapticvboard_android.calculateWPM
import com.taehokimmm.hapticvboard_android.database.Study2Metric
import com.taehokimmm.hapticvboard_android.database.addStudy2Metric
import com.taehokimmm.hapticvboard_android.keyboardEfficiency
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun Study2Test(
    innerPadding: PaddingValues,
    subject: String,
    testNumber: Int,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current
    var testIter by remember { mutableIntStateOf(0) }
    val testList = readTxtFile(context, R.raw.phrases)

    // WPM
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var wordCount by remember { mutableIntStateOf(0) }

    // IKI
    val keystrokeTimestamps = remember { mutableStateListOf<Long>() }
    // Keyboard Efficiency
    var keyStrokeNum by remember{mutableStateOf(0)}

    LaunchedEffect(testIter) {
        if (testIter == 0) {
            startTime = System.currentTimeMillis()
        } else if (testIter < testNumber) {
            wordCount = inputText.split("\\s+".toRegex()).size
            val targetText = testList[testIter-1]
            val wpm = calculateWPM(startTime, endTime, wordCount)
            val iki = calculateIKI(keystrokeTimestamps)
            val uer = calculateUER(targetText, inputText)
            var ke = keyboardEfficiency(inputText, keyStrokeNum)

            val data = Study2Metric(
                testIter,  wpm, iki, uer, ke, targetText, inputText
            )
            addStudy2Metric(context, subject, hapticMode, data)
            startTime = System.currentTimeMillis()
            keystrokeTimestamps.clear()
            keyStrokeNum = 0
            inputText = ""
        } else {
            navController!!.navigate("study2/end/$subject")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        TextDisplay(testIter, testNumber, testList[testIter])
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp, 16.dp)
                    .heightIn(min = 30.dp, max = 200.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        EditText(ctx).apply {
                            hint = "Enter text here"
                            textSize = 20f
                            showSoftInputOnFocus = false
                            setText(inputText)
                            setSelection(inputText.length)
                            isFocusable = true
                            isCursorVisible = true
                            isPressed=true
                        }
                    },
                    update = { editText ->
                        if (editText.text.toString() != inputText) {
                            editText.setText(inputText)
                            editText.setSelection(inputText.length)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { key ->
                        inputText = when (key) {
                            "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            "Enter" -> {
                                endTime = System.currentTimeMillis()
                                testIter++
                                inputText
                            }
                            else -> {
                                inputText + key
                            }
                        }
                        keystrokeTimestamps += System.currentTimeMillis()
                        keyStrokeNum += 1
                    },
                    enterKeyVisibility = true,
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = hapticMode
                )
                AndroidView(modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                    factory = { context ->
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

fun readTxtFile(context: Context, resId: Int): List<String> {
    val inputStream = context.resources.openRawResource(resId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val lines = reader.readLines()
    reader.close()
    return lines
}

@Composable
fun TextDisplay(testIter: Int, testNumber: Int, testString: String) {
    Column(
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = testString, fontSize = 20.sp
            )
        }
    }
}