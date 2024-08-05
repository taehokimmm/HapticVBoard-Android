package com.taehokimmm.hapticvboard_android.layout.study2

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.keyboardEfficiency
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

@Composable
fun Study2Test(
    innerPadding: PaddingValues,
    subject: String,
    isPractice: Boolean,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current

    val totalBlock = when(isPractice) {
        true -> 1
        false -> 4
    }
    val testNumber = when (isPractice) {
        true -> 4
        false -> 5
    }
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableIntStateOf(-1) }
    var testWords by remember { mutableStateOf(listOf("")) }
    var testWordCnt by remember { mutableIntStateOf(-1) }


    var phrases = when (isPractice) {
        true -> readTxtFile(context, R.raw.practice_phrase)
        false -> readTxtFile(context, R.raw.phrase40)
    }

    if (hapticMode == HapticMode.VOICE) {
        phrases = phrases.slice(0..totalBlock * testNumber - 1)
    } else {
        phrases = phrases.slice(totalBlock * testNumber .. phrases.size)
    }

    var testList by remember { mutableStateOf(listOf("")) }

    // WPM
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var wordCount by remember { mutableIntStateOf(0) }

    // IKI
    val keystrokeTimestamps = remember { mutableStateListOf<Long>() }

    // Keyboard Efficiency
    var keyStrokeNum by remember{mutableStateOf(0)}

    var isSpeakingDone by remember {mutableStateOf(false)}
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        // Initiate TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeakingDone = false
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeakingDone = true
                    }

                    override fun onError(utteranceId: String?) {
                    }
                })
                tts?.setSpeechRate(0.75f)
            }
        }
    }

    fun speak(word: String){
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")

        tts?.setSpeechRate(0.5f)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")

        tts?.setSpeechRate(1f)
//        delay(
//            {
//                for (index in 0 until word.length) {
//                    tts?.speak(word[index].toString(), TextToSpeech.QUEUE_ADD, params, "utteranceId")
//                }
//            },
//            500
//        )
    }

    fun onConfirm(): Boolean {
        if (testWordCnt < testWords.size - 1) {
            testWordCnt ++
            speak(testWords[testWordCnt])
            return false
        } else {
//                wordCount = inputText.split("\\s+".toRegex()).size
//                val targetText = testList[testIter]
//                val wpm = calculateWPM(startTime, endTime, wordCount)
//                val iki = calculateIKI(keystrokeTimestamps)
//                val uer = calculateUER(targetText, inputText)
//                var ke = keyboardEfficiency(inputText, keyStrokeNum)
//                val data = Study2Metric(
//                    testIter,  wpm, iki, uer, ke, targetText, inputText
//                )
//                addStudy2Metric(context, subject, hapticMode, data)
//                startTime = System.currentTimeMillis()
//                keystrokeTimestamps.clear()
//                keyStrokeNum = 0
//
            testIter++
            inputText = ""
            return true
        }
    }


    LaunchedEffect(testIter) {
        if (testIter == -1) {
            soundManager.speakOut("Tap to start Block " + (testBlock + 1).toString())
            testList = phrases.slice(testBlock * testNumber .. (testBlock + 1) * testNumber - 1)
        } else if (testIter < testNumber) {
            val targetText = testList[testIter]
            speak(targetText)
            testWords = targetText.split(" ")
            testWordCnt = -1
            onConfirm()
        } else {
            testBlock++
            if (testBlock >= totalBlock) {
                //closeStudy2Database()
                navController!!.navigate("study2/end/$subject")
            } else {
                testIter = -1
            }
        }
    }


    if (testIter == -1) {
        // Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    testIter = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(text="Tap to Start \n Block : " + (testBlock + 1).toString(),
                    fontSize = 20.sp)
            }
        }
    } else if (testIter < testList.size) {
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
                if (isSpeakingDone) {
                    Box {
                        KeyboardLayout(
                            touchEvents = keyboardTouchEvents,
                            onKeyRelease = { key ->
                                var isEnd = false
                                if (key == "Space") {
                                    if (inputText.last() != ' ') {
                                        isEnd = onConfirm()
                                    } else {
                                        speak(testWords[testWordCnt])
                                    }
                                } else if (key == "Replay") {
                                    // Replay word
                                    speak(testWords[testWordCnt])
                                }

                                if (isEnd) return@KeyboardLayout
                                inputText = when (key) {
                                    "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                                    "Space" -> "$inputText "
                                    "Shift" -> inputText
                                    "Replay" -> {
                                        endTime = System.currentTimeMillis()
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