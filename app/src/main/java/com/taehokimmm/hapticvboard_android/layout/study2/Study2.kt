package com.taehokimmm.hapticvboard_android.layout.study2

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.layout.study1.test.CheckboxWithLabel
import com.taehokimmm.hapticvboard_android.layout.study1.test.Spinner
import com.taehokimmm.hapticvboard_android.layout.study1.train.TestDisplay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun Study2Test(
    innerPadding: PaddingValues,
    testName: String,
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

//    val database = AppDatabase.getDatabase(context)
//    val testMetricDao = database.testMetricDao()

    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var wordCount by remember { mutableIntStateOf(0) }
    var accuracy by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(testIter) {
        if (testIter == 0) {
            startTime = System.currentTimeMillis()
        } else if (testIter < testNumber) {
            endTime = System.currentTimeMillis()
            wordCount = inputText.split("\\s+".toRegex()).size
           // accuracy = calculateAccuracy(inputText, testList[testIter - 1])
           // val wpm = calculateWPM(startTime, endTime, wordCount)
//            testMetricDao.insert(
//                TestMetric(
//                    testName = testName,
//                    iteration = testIter,
//                    wpm = wpm,
//                    accuracy = accuracy,
//                    touchMetrics = emptyList(),
//                    timestamp = System.currentTimeMillis()
//                )
//            )
            startTime = System.currentTimeMillis()
        } else {
            navController!!.navigate("study2/end/$testName")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        TestDisplay(testIter, testNumber, testList[testIter][0], soundManager)
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
                Text(
                    text = inputText,
                    fontSize = 20.sp,
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
                                testIter++
                                ""
                            }

                            else -> inputText + key
                        }
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