package com.taehokimmm.hapticvboard_android.layout.textentry

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.taehokimmm.hapticvboard_android.database.TextEntryLog
import com.taehokimmm.hapticvboard_android.database.TextEntryMetric
import com.taehokimmm.hapticvboard_android.database.addTextEntryMetric
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.getAllowGroup
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

@Composable
fun Study3(
    innerPadding: PaddingValues,
    subject: String,
    isPractice: Boolean,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode,
    testBlock: Int
) {
    val hapticName = when(hapticMode) {
        HapticMode.TICK -> "vibration"
        HapticMode.PHONEME -> "phoneme"
        HapticMode.VOICE -> "audio"
        HapticMode.VOICEPHONEME -> "voicephoneme"
        else -> ""
    }
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current

    val phonemeBlock = 12
    val totalBlock = when (isPractice) {
        true -> 1
        false -> when(hapticMode) {
            HapticMode.VOICE -> 3
            HapticMode.PHONEME -> phonemeBlock
            else -> 1
        }
    }

    val testNumber = when (isPractice) {
        true -> 5
        false -> 5
    }
    var testBlock by remember { mutableStateOf(testBlock) }
    var testIter by remember { mutableIntStateOf(-1) }
    var testWords by remember { mutableStateOf(listOf("")) }
    var testWordCnt by remember { mutableIntStateOf(-1) }

    var testList by remember { mutableStateOf(listOf("")) }

    //---------------METRICS ----------------------------//
    // WPM
    var wpmAvg by remember { mutableDoubleStateOf(.0) }
    var wpmList = remember { mutableStateListOf<Double>() }
    var wpmBlockList = remember { mutableStateListOf<Double>() }
    var wpmBlockAvg = remember { mutableDoubleStateOf(.0) }
    var error by remember { mutableDoubleStateOf(.0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var sentenceStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var sentenceEndTime by remember { mutableLongStateOf(0L) }

    // ERROR
    // Variables related to T-seq change
    var oldVal by remember {mutableStateOf("")}
    val tsequence = remember {mutableStateListOf<String>()}

    // Variables related to results
    var IF by remember {mutableIntStateOf(0)}

    // IKI
    var keystrokeTimestamps by remember { mutableStateOf(listOf<Long>()) }

    // Press Duration
    var localPressDurations by remember{mutableStateOf(listOf<Long>())}
    var pressDurations by remember{mutableStateOf(listOf<Long>())}
    var pressStartTime by remember{mutableLongStateOf(0)}

    // Keyboard Efficiency
    var keyStrokeNum by remember { mutableStateOf(0) }

    var isSpeakingDone by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var phrases by remember { mutableStateOf(listOf("")) }

    var modeIter by remember {mutableStateOf(-1)}

    var timer by remember { mutableStateOf(0) }

    LaunchedEffect(timer) {
        kotlinx.coroutines.delay(1000L)
        timer++
    }

    LaunchedEffect(Unit) {
        var phrases1 = when (isPractice) {
            true -> readTxtFile(context, R.raw.practice_phrase)
            false -> readTxtFile(context, R.raw.phrase80)
        }

        if (isPractice) {
            phrases = phrases1
        } else {
            if (hapticMode == HapticMode.PHONEME) {
                phrases = phrases1.slice(0..phonemeBlock * testNumber - 1)
            } else if (hapticMode == HapticMode.VOICE) {
                phrases = phrases1.slice(phonemeBlock * testNumber..(phonemeBlock + 2) * testNumber - 1)
            }
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

    fun speak(word: String, speed:Float = 1f) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setSpeechRate(speed)
        tts?.setLanguage(Locale.US)
        tts?.speak(word, TextToSpeech.QUEUE_ADD,
            params, "utteranceId")
    }

    fun speakKor(word: String, speed:Float = 1f) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setSpeechRate(speed)
        tts?.setLanguage(Locale.KOREAN)
        tts?.speak(word, TextToSpeech.QUEUE_ADD,
            params, "utteranceId")
    }

    fun speakWord(word: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setLanguage(Locale.US)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
        playEarcon("silent")
        for (index in 0 until word.length) {
            tts?.setSpeechRate(1f)
            var letter = word[index].toString()
            tts?.speak(
                letter, TextToSpeech.QUEUE_ADD, params, "utteranceId"
            )
        }
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

    fun addLoggingPerWord() {
        val targetWord = testWords[testWordCnt]
        val inputWords = inputText.split(" ")
        val inputWord = inputWords[inputWords.size - 1]
        val wpm = calculateWPM(startTime, endTime, inputWord)
        wpmList.add(wpm)
        wpmAvg = wpmList.average()
        Log.d("text entry", "AVG $wpmAvg wpm : current $wpm wpm")

        if (isPractice) return
        val pd = calculatePressDuration(localPressDurations)

        val data = TextEntryMetric(
            hapticName,
            testBlock,
            testIter,
            wpm,
            0.0,
            pd,
            -1.0,
            -1.0,
            -1.0,
            -1.0,
            targetWord,
            inputWord
        )
        addTextEntryMetric(context, subject, data)

    }

    fun addLogging() {
        val targetText = if(isPractice) testList[0] else testList[testIter]
//        val wpm = calculateWPM(sentenceStartTime, sentenceEndTime, inputText.trim())
        val errors = getError(targetText, tsequence, IF)
        val cer = errors[0] * 100
        val uer = errors[1] * 100
        val ter = errors[2] * 100
        error = uer
        wpmBlockList.add(wpmAvg)
        if (isPractice) return

        val pd = calculatePressDuration(pressDurations)

        var ke = keyboardEfficiency(inputText, keyStrokeNum)
        val data = TextEntryMetric(
            hapticName,
            testBlock,
            testIter,
            wpmAvg,
            1.0,
            pd,
            uer,
            cer,
            ter,
            ke,
            targetText,
            inputText
        )
        addTextEntryMetric(context, subject, data)
    }


    // Infer the action that caused a change in text
    fun guessChangeInfo(t1: String, t2: String): List<Any> {
        if (t1.isEmpty()) {
            return listOf("insert", 0, t2.length - t1.length)
        } else if (t2.isEmpty()) {
            IF += t1.length
            return listOf("delete", 0, t1.length)
        }

        var i = 0
        while (t1[i] == t2[i]) {
            i++

            if (i == t1.length) {
                return listOf("insert", t1.length, t2.length - t1.length)
            }
            else if (i == t2.length) {
                IF += t1.length - t2.length
                return listOf("delete", t2.length, t1.length - t2.length)
            }
        }
        var j = 1
        while (t1[t1.length - j] == t2[t2.length - j]) {
            j++
            if (j == t1.length + 1) {
                return listOf("insert", 0, t2.length - t1.length)
            } else if (j == t2.length + 1) {
                IF += t1.length - t2.length
                return listOf("delete", 0, t1.length - t2.length)
            }
        }

        if (i + j - 1 >= t1.length) {
            if (t2.length > t1.length) {
                return listOf("insert", i, t2.length - t1.length)
            } else {
                IF += t1.length - t2.length
                return listOf("delete", t2.length - j + 1, t1.length - t2.length)
            }
        } else {

            if (t2.length <= i + j - 1) {
                IF += t1.length - t2.length
                return listOf("delete", i, t1.length - t2.length)
            } else {
                IF += t1.length - j + 1 - i
                return listOf("replace", i, t1.length - j + 1 - i)
            }
        }
    }

    // Detect changes in transcribed input
    fun onTranscribeChange(newText: String) {
        val currentVal = newText
        if (currentVal == oldVal) return

        val res = guessChangeInfo(oldVal, currentVal)
        oldVal = currentVal
        tsequence.add(currentVal)
    }

    fun initMetric() {
        startTime = -1
        sentenceStartTime = -1
        keystrokeTimestamps = listOf()
        pressDurations = listOf()
        localPressDurations = listOf()
        keyStrokeNum = 0
        IF = 0
        wpmList.clear()

        tsequence.clear()
        tsequence.add("")
        oldVal = ""
    }

    fun onConfirm() {
        tts?.stop()
        isSpeakingDone = true
        playEarcon("beep")
        addLoggingPerWord()
        addLogging()
        modeIter = 2
    }

    fun onSpace(): Boolean {
        if (testWordCnt < testWords.size - 1) {
            addLoggingPerWord()
            startTime = -1
            localPressDurations = listOf()
            testWordCnt ++
            speakWord(testWords[testWordCnt])
        } else if(inputText.isNotEmpty()) {
            onConfirm()
            return true
        }
        return false
    }

    fun explainResult() {
        val wpmFormatted = String.format("%.1f", wpmAvg)
        val errorFormatted = String.format("%.1f", error)
        speak(inputText)
        speakKor("속도 : " + wpmFormatted, speed = 1.2f)
//        speakKor("오류 : " + errorFormatted + "%", speed = 1.2f)
    }


    fun explainSentence() {
        isSpeakingDone = false
        tts?.stop()
        delay({
            tts?.stop()
            var sentence = if (isPractice) testList[0] else testList[testIter]
            speak(sentence)
            playEarcon("start")
            speakSentence(sentence)
//            playEarcon("start")
//            speakSpelling(sentence)
//            playEarcon("start")
//            speak(sentence)
        }, 500)
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            timer = 0
            testList = if (isPractice) phrases
                else phrases.slice(testBlock * testNumber..(testBlock + 1) * testNumber - 1)
        } else if (testIter < testNumber) {
            var targetText = if (isPractice) testList[0] else testList[testIter]
            testWords = targetText.split(" ")
            testWordCnt = 0
            modeIter = 0
            explainSentence()
        } else {
            testBlock++
            if (testBlock >= totalBlock) {
                closeStudyDatabase()
                navController!!.navigate("textEntry/end/$subject")
            } else {
                testIter = -1
            }
        }
    }

    LaunchedEffect(modeIter) {
        if (testIter == -1) return@LaunchedEffect
        if (modeIter == 2) {
            explainResult()
        }
    }


    if (testIter == -1) {
        // Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show Time (MM:SS)
            Text(
                text = "%02d:%02d".format(timer / 60, timer % 60),
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(onClick = {
                wpmBlockList.clear()
                inputText = ""
                testIter = 0
            }) {
                Text("Skip")
            }

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Block : ${testBlock + 1} / $totalBlock", fontSize = 20.sp
                )
            }

            wpmBlockList.forEachIndexed({ index, wpm ->
                Text(text = "Block${index+1} : ${String.format("%.1f",wpm)} wpm")
            })
            Text(text = "Avg : ${String.format("%.1f",wpmBlockList.average())} wpm")

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inputText,
                    fontSize = 30.sp
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { key ->
                        inputText = when (key) {
                            "delete" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            else -> inputText + key
                        }
                    },
                    lastWord = if(inputText.isNotEmpty()) inputText.last() else null,
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = hapticMode,
                    allow = getAllowGroup("123")
                )

                AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                    MultiTouchView(context).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                })
            }
        }
    } else if (isPractice || testIter < testList.size) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        tts?.stop()
                        if (testIter > 0) testIter--
                    },
                    colors = ButtonColors(Color.White, Color.Blue, Color.Gray, Color.Black),
                    enabled = testIter > 0
                ) {
                    Text("Prev")
                }
                Button(
                    onClick = {
                        tts?.stop()
                        testIter++
                    },
                    colors = ButtonColors(Color.White, Color.Blue, Color.Gray, Color.Black),
                ) {
                    Text("Next")
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = when (modeIter) {
                        1 -> Modifier
                            .fillMaxSize()
                            .padding(innerPadding)

                        else -> Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (modeIter == 0) {
                                            tts?.stop()
                                            isSpeakingDone = true
                                            playEarcon("beep")
                                            modeIter = 1
                                            inputText = ""
                                            initMetric()
                                            delay({
                                                speakWord(testWords[testWordCnt])
                                            }, 500)
                                        } else {
                                            if (isSpeakingDone) {
                                                tts?.stop()
                                                isSpeakingDone = true
                                                playEarcon("beep")
                                                testIter++
                                                inputText = ""
                                            }
                                        }
                                    },
                                    onTap = {
                                        if (isSpeakingDone == false) return@detectTapGestures
                                        if (modeIter == 0) {
                                            explainSentence()
                                        } else {
                                            //explainResult()
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
                                text = "Block : ${testBlock + 1} / $totalBlock", fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Trial : ${testIter + 1} / $testNumber", fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if(isPractice) testList[0] else testList[testIter], fontSize = 30.sp, color = Color.Blue
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inputText,
                                fontSize = 30.sp
                            )
                        }

                        if (modeIter == 2) {
                            Spacer(modifier = Modifier.height(20.dp))
                            val wpmFormatted = String.format("%.1f", wpmAvg)
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Speed : $wpmFormatted WPM",
                                    fontSize = 40.sp
                                )
                            }
                        }
                    }

                    if (modeIter == 1) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            KeyboardLayout(
                                touchEvents = keyboardTouchEvents,
                                onKeyPress = { key ->
                                    if (startTime == -1L) {
                                        startTime = System.currentTimeMillis()
                                    }
                                    if (sentenceStartTime == -1L) {
                                        sentenceStartTime = System.currentTimeMillis()
                                    }
                                    tts?.stop()
                                    isSpeakingDone = true
                                    pressStartTime = System.currentTimeMillis()
                                },
                                onKeyRelease = { key ->
                                    if (key == "delete") {
                                        if (inputText.isNotEmpty() && inputText.last() == ' ') {
                                            if (testWordCnt > 0)  testWordCnt --
                                        }
                                    } else if (key == "Space") {
                                        endTime = System.currentTimeMillis()
                                        sentenceEndTime = System.currentTimeMillis()

                                        val isEnd = onSpace()
                                        if (isEnd) return@KeyboardLayout
                                    }
                                    inputText = when (key) {
                                        "delete" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                                        "Space" -> if (inputText.isNotEmpty() && inputText.last() != ' ') "$inputText " else inputText
                                        "Shift" -> inputText
                                        else -> {
                                            inputText + key
                                        }
                                    }
                                    onTranscribeChange(inputText)
                                    val curTime = System.currentTimeMillis()
                                    if (pressStartTime != -1L) {
                                        val pressDur = curTime - pressStartTime
                                        pressDurations += pressDur
                                        if (key != "delete" && key != "Space" && key != "Shift")
                                            localPressDurations+= pressDur
                                    }
                                    keyStrokeNum += 1
                                    pressStartTime = -1L
                                },
                                allow = getAllowGroup("123"),
                                enterKeyVisibility = false,
                                soundManager = soundManager,
                                hapticManager = hapticManager,
                                hapticMode = hapticMode,
                                 logData = TextEntryLog(
                                    mode = hapticName,
                                    iteration = testIter,
                                    block = testBlock,
                                    targetText = if (isPractice) testList[0] else testList[testIter],
                                    inputText = inputText
                                ),
                                lastWord = if(inputText.isNotEmpty()) inputText.last() else null,
                                name = subject
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
                                    speakWord(testWords[testWordCnt])
                                },
                                onDoubleTap = {
                                    if (inputText.isEmpty()) return@MultiTouchView
                                    onConfirm()
                                },
                                onLeftSwipe = {

                                },
                                onRightSwipe = {

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
    }
}

fun readTxtFile(context: Context, resId: Int): List<String> {
    val inputStream = context.resources.openRawResource(resId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val lines = reader.readLines()
    reader.close()
    return lines
}