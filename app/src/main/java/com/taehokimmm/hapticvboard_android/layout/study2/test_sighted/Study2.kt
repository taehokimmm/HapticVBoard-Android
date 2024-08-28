package com.taehokimmm.hapticvboard_android.layout.study2.test_sighted

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.database.Study2Metric
import com.taehokimmm.hapticvboard_android.database.Study2TestLog
import com.taehokimmm.hapticvboard_android.database.addStudy2Metric
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun Study2Test(
    innerPadding: PaddingValues,
    subject: String,
    isPractice: Boolean,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode,
    testBlock: Int
) {
    val databaseName = subject + "_" + when(hapticMode) {
        HapticMode.TICK -> "vibration"
        HapticMode.PHONEME -> "phoneme"
        HapticMode.VOICE -> "audio"
        HapticMode.VOICEPHONEME -> "voicephoneme"
        else -> ""
    }
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current

    val totalBlock = when (isPractice) {
        true -> 1
        false -> 2
    }
    val testNumber = when (isPractice) {
        true -> 3
        false -> 5
    }
    var testBlock by remember { mutableStateOf(testBlock) }
    var testIter by remember { mutableIntStateOf(-1) }
    var testWords by remember { mutableStateOf(listOf("")) }
    var testWordCnt by remember { mutableIntStateOf(-1) }

    var testList by remember { mutableStateOf(listOf("")) }

    // WPM
    var wpm by remember { mutableDoubleStateOf(.0) }
    var uer by remember { mutableDoubleStateOf(.0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }

    // IKI
    val keystrokeTimestamps = remember { mutableStateListOf<Long>() }

    // Press Duration
    var pressDurations by remember{mutableStateOf(listOf(0L))}
    var pressStartTime by remember{mutableLongStateOf(0)}

    // Keyboard Efficiency
    var keyStrokeNum by remember { mutableStateOf(0) }

    var isSpeakingDone by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var phrases by remember { mutableStateOf(listOf("")) }

    var modeIter by remember {mutableStateOf(-1)}
    var horizontalDragStart by remember {mutableStateOf(0f)}
    var horizontalDragEnd by remember {mutableStateOf(0f)}
    var verticalDragStart by remember {mutableStateOf(0f)}
    var verticalDragEnd by remember {mutableStateOf(0f)}
    val swipeThreshold = 100

    var timer by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(30) }

    LaunchedEffect(timer) {
        kotlinx.coroutines.delay(1000L)
        timer++

        var temp: Int
        if (testBlock == 0) {
            temp = 0
        } else {
            temp = 120 - timer
        }
        if (temp < 0) temp = 0
        if (temp != countdown) countdown = temp
    }

    LaunchedEffect(Unit) {
        var phrases1 = when (isPractice) {
            true -> readTxtFile(context, R.raw.practice_phrase)
            false -> readTxtFile(context, R.raw.phrases30)
        }
        if (hapticMode == HapticMode.PHONEME) {
            phrases = phrases1.slice(0..totalBlock * testNumber - 1)
        } else {
            phrases = phrases1.slice(totalBlock * testNumber..totalBlock * testNumber * 2 - 1)
        }

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
                tts?.addEarcon("swish", "com.taehokimmm.hapticvboard_android", R.raw.swish)
                tts?.addEarcon("silent", "com.taehokimmm.hapticvboard_android", R.raw.silent_quarter)
                tts?.addEarcon("silent_short", "com.taehokimmm.hapticvboard_android", R.raw.silent_1)
                tts?.addEarcon("beep", "com.taehokimmm.hapticvboard_android", R.raw.beep)
                tts?.addEarcon("start", "com.taehokimmm.hapticvboard_android", R.raw.correct)
            }
        }
    }

    fun playEarcon(earcon: String) {
        isSpeakingDone = false
        tts?.playEarcon(earcon, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun speak(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setSpeechRate(1f)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun speakSentence(sentence: String) {
        val words = sentence.split(' ')
        for (index in 0 until words.size) {
            speak(words[index])
        }
    }

    fun speakSpelling(sentence: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        val words = sentence.split(" ")
        for (i in 0 until words.size) {
            val word = words[i]
            tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
            playEarcon("silent")
            for (index in 0 until word.length) {
                tts?.setSpeechRate(1f)
                var letter = word[index].toString()
                tts?.speak(
                    letter, TextToSpeech.QUEUE_ADD, params, "utteranceId"
                )
            }
            if (i != words.size - 1)
                playEarcon("swish")
        }
    }

    fun addLogging() {
        endTime = System.currentTimeMillis()
        val targetText = testList[testIter]
        wpm = calculateWPM(startTime, endTime, targetText)
        uer = calculateUER(targetText, inputText)

        if (isPractice) return
        val iki = calculateIKI(keystrokeTimestamps)
        val pd = calculatePressDuration(pressDurations)
        var ke = keyboardEfficiency(inputText, keyStrokeNum)
        val data = Study2Metric(
            testBlock, testIter, wpm, pd, uer, ke, targetText, inputText
        )
        addStudy2Metric(context, databaseName, data)
    }

    fun initMetric() {
        startTime = System.currentTimeMillis()
        keystrokeTimestamps.clear()
        keyStrokeNum = 0
    }

    fun onConfirm() {
        tts?.stop()
        isSpeakingDone = true
        playEarcon("beep")
        addLogging()
        modeIter = 2
    }

    fun onSpace() {
        if (testWordCnt < testWords.size - 1) {
            testWordCnt ++
            speak(testWords[testWordCnt])
        }
    }

    fun explainResult() {
        val wpmFormatted = String.format("%.1f", wpm)
        val uerFormatted = String.format("%.1f", uer)
        speak(inputText)
        speak("Speed : " + wpmFormatted + "Word Per Minute")
        speak("Error : " + uerFormatted + "%")
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            countdown = 120
            timer = 0
            testList = phrases.slice(testBlock * testNumber..(testBlock + 1) * testNumber - 1)
        } else if (testIter < testNumber) {
            val targetText = testList[testIter]
            testWords = targetText.split(" ")
            testWordCnt = 0
            modeIter = 0
        } else {
            testBlock++
            if (testBlock >= totalBlock) {
                closeStudy2Database()
                navController!!.navigate("study2/test/end/$subject")
            } else {
                testIter = -1
            }
        }
    }

    fun explainSentence() {
        isSpeakingDone = false
        delay({
            tts?.stop()
            val sentence = testList[testIter]
            speak(sentence)
            playEarcon("start")
            speakSentence(sentence)
            playEarcon("start")
            speakSpelling(sentence)
            playEarcon("start")
            speak(sentence)
        }, 500)
    }

    LaunchedEffect(modeIter) {
        if (testIter == -1) return@LaunchedEffect
        if (modeIter == 2) {
            explainResult()
        } else if (modeIter == 0) {
            explainSentence()
        }
    }


    if (testIter == -1) {
        // Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show countdown (MM:SS)
            Text(
                text = "%02d:%02d".format(countdown / 60, countdown % 60),
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(onClick = {
                testIter = 0
            }) {
                Text("Skip")
            }

            if (countdown == 0)
                Button(
                    onClick = {
                        testIter = 0
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(corner = CornerSize(0)),
                    colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
                ) {
                    Text(
                        text = "Tap to Start \n Block : " + (testBlock + 1).toString(), fontSize = 20.sp
                    )
                }
        }
    } else if (testIter < testList.size) {
        Box(
            modifier = when(modeIter) {
                1 -> Modifier
                    .fillMaxSize()
                    .padding(innerPadding)

                else -> Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                tts?.stop()
                                isSpeakingDone = true
                                playEarcon("beep")
                                if (modeIter == 0) {
                                    modeIter = 1
                                    initMetric()
                                    delay({
                                        speak(testWords[testWordCnt])
                                    }, 500)
                                } else {
                                    testIter++
                                    inputText = ""
                                }
                            },
                            onTap = {
                                if (isSpeakingDone == false) return@detectTapGestures
                                if (modeIter == 0) {
                                    explainSentence()
                                } else {
                                    explainResult()
                                }
                            }
                        )
                    }
            }
        ) {
            // Text Box
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
                        text = testList[testIter], fontSize = 20.sp, color = Color.Blue
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = inputText,
                        fontSize = 20.sp)
                }

                if (modeIter == 2) {
                    Spacer(modifier = Modifier.height(20.dp))
                    val wpmFormatted = String.format("%.1f", wpm)
                    val uerFormatted = String.format("%.1f", uer)
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WPM : $wpmFormatted \n UER : $uerFormatted", fontSize = 20.sp
                        )
                    }
                }
            }

            if (modeIter == 1) {
                Box (
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = { key ->
                            tts?.stop()
                            isSpeakingDone = true
                            pressStartTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            if (key == "Space") {
                                onSpace()
                            } else if (key == "Backspace") {
                                if (inputText.isNotEmpty() && inputText.last() == ' ') {
                                    if (testWordCnt > 0)  testWordCnt --
                                }
                            }
                            inputText = when (key) {
                                "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                                "Space" -> "$inputText "
                                "Shift" -> inputText
                                "Replay" -> {
                                    inputText
                                }
                                else -> {
                                    inputText + key
                                }
                            }
                            if (key != "Replay") {
                                val curTime = System.currentTimeMillis()
                                val pressDur = curTime - pressStartTime
                                pressDurations += pressDur
                                keystrokeTimestamps += curTime
                                keyStrokeNum += 1
                            }
                        },
                        enterKeyVisibility = false,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = hapticMode,
                        logData = Study2TestLog(
                            iteration = testIter,
                            block = testBlock,
                            targetText = testList[testIter],
                            inputText = inputText
                        ),
                        name = databaseName
                    )
                }
            }
        }

        if (modeIter == 1) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                factory = { context ->
                    MultiTouchView(
                        context,
                        onTap = {
                            tts?.stop()
                            speak(testList[testIter])
                        },
                        onDoubleTap = {
                            onConfirm()
                        }
                    ).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                }
            )
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