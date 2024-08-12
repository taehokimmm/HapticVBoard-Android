package com.taehokimmm.hapticvboard_android.layout.study2.train

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import com.taehokimmm.hapticvboard_android.calculatePressDuration
import com.taehokimmm.hapticvboard_android.calculateUER
import com.taehokimmm.hapticvboard_android.calculateWPM
import com.taehokimmm.hapticvboard_android.database.Study2Metric
import com.taehokimmm.hapticvboard_android.database.Study2TestLog
import com.taehokimmm.hapticvboard_android.database.addStudy2Metric
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.keyboardEfficiency
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.layout.study2.TextDisplay
import com.taehokimmm.hapticvboard_android.layout.study2.readTxtFile
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Locale

@Composable
fun Study2Train(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current

    val totalBlock = 3
    val testNumber = 5
    val modeCnt = 2
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableIntStateOf(-1) }
    var modeIter by remember { mutableIntStateOf(0) }
    var testWords by remember { mutableStateOf(readTxtFile(context, R.raw.five_letter_words)) }

    var testList by remember { mutableStateOf(listOf("")) }

    // WPM
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var wordCount by remember { mutableIntStateOf(0) }

    // IKI
    val keystrokeTimestamps = remember { mutableStateListOf<Long>() }

    // Press Duration
    var pressDurations by remember{mutableStateOf(listOf(0L))}
    var pressStartTime by remember{mutableLongStateOf(0)}

    // Keyboard Efficiency
    var keyStrokeNum by remember { mutableStateOf(0) }

    var isSpeakingDone by remember { mutableStateOf(false) }
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
            }
        }
    }

    LaunchedEffect(testBlock) {
        testList = testWords.slice(testBlock * testNumber until (testBlock + 1) * testNumber)
    }

    fun speak(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")

        tts?.setSpeechRate(1f)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun speakWord(word: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        isSpeakingDone = false


        delay({
            tts?.setSpeechRate(2f)
            for (index in 0 until word.length) {
                tts?.speak(
                    word[index].toString(), TextToSpeech.QUEUE_ADD, params, "utteranceId"
                )
            }
            tts?.setSpeechRate(1f)
        }, 1000)
    }


    fun speakWordLetter(word:String) {
        speak(word)
        speakWord(word)
    }

    fun addLogging() {
        endTime = System.currentTimeMillis()
        wordCount = inputText.split("\\s+".toRegex()).size
        val targetText = testList[testIter]
        val wpm = calculateWPM(startTime, endTime, targetText)
        //val iki = calculateIKI(keystrokeTimestamps)
        val pd = calculatePressDuration(pressDurations)
        val uer = calculateUER(targetText, inputText)
        var ke = keyboardEfficiency(inputText, keyStrokeNum)
        val data = Study2Metric(
            testBlock, testIter*modeCnt + modeIter, wpm, pd, uer, ke, targetText, inputText
        )
        addStudy2Metric(context, subject+ "_train2", data)
    }

    fun initMetric() {
        startTime = System.currentTimeMillis()
        keystrokeTimestamps.clear()
        keyStrokeNum = 0
    }

    fun onConfirm(): Boolean {
        val isCorrect = (inputText == testList[testIter])
        soundManager.playSound(isCorrect)

        var delay = 0L
        if (!isCorrect) {
            delay(
                {
                    speakWord(inputText)
                }, 500
            )
            delay = 2000L
        }

        delay({
            initMetric()
            addLogging()
            modeIter++
            inputText = ""
        }, 1000 + delay)
        return true
    }

    LaunchedEffect(modeIter) {
        if (testIter == -1) return@LaunchedEffect
        if (modeIter == modeCnt) {
            modeIter = 0
            testIter++
            return@LaunchedEffect
        }

        if (modeIter == 0) {
            speakWordLetter(testList[testIter])
        } else {
            tts?.speak(testList[testIter], TextToSpeech.QUEUE_ADD, null)
        }
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            soundManager.speakOutKor("시작하려면 탭하세요")
        } else if (testIter < testNumber) {

        } else {
            testBlock++

            if (testBlock > totalBlock) {
                closeStudy2Database()
                navController!!.navigate("study2/train/end/$subject")
            } else {
                testIter = 0
            }
        }
    }

    if (testIter == -1) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    modeIter = 0
                    testIter = 0
                    speakWordLetter(testList[testIter])
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(
                    text = "Tap to Start \n Block : " + testBlock, fontSize = 20.sp
                )
            }
        }
    } else if (testIter < testList.size) {
        Box(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
        ) {
            TrainTextDisplay(testBlock, totalBlock, testIter, testNumber, modeIter, modeCnt, testList[testIter])
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
                    AndroidView(modifier = Modifier.fillMaxWidth(), factory = { ctx ->
                        EditText(ctx).apply {
                            hint = "Enter text here"
                            textSize = 20f
                            showSoftInputOnFocus = false
                            setText(inputText)
                            setSelection(inputText.length)
                            isFocusable = true
                            isCursorVisible = true
                            isPressed = true
                        }
                    }, update = { editText ->
                        if (editText.text.toString() != inputText) {
                            editText.setText(inputText)
                            editText.setSelection(inputText.length)
                        }
                    })
                }

                Spacer(modifier = Modifier.height(20.dp))
                    Box {
                        KeyboardLayout(
                            touchEvents = keyboardTouchEvents,
                            onKeyPress = { key ->
                                pressStartTime = System.currentTimeMillis()
                            },
                            onKeyRelease = { key ->
                                if (key == "Space") {
                                    onConfirm()
                                    return@KeyboardLayout
                                } else if (key == "Replay") {
                                    // Replay word
                                    speak(testList[testIter])
                                }

                                inputText = when (key) {
                                    "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
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

                                    if (modeIter == 0) {
                                        hapticManager?.generateHaptic(key, HapticMode.VOICE)
                                    }
                                }
                            },
                            enterKeyVisibility = true,
                            soundManager = soundManager,
                            hapticManager = hapticManager,
                            hapticMode = if (modeIter == 0) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                            logData = Study2TestLog(
                                iteration = testIter * modeCnt + modeIter,
                                block = testBlock,
                                targetText = testList[testIter],
                                inputText = inputText
                            ),
                            name = subject+ "_train2"
                        )
                    }

            }
        }
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(),
            factory = { context ->
                MultiTouchView(context).apply {
                    onMultiTouchEvent = { event ->
                        keyboardTouchEvents.clear()
                        keyboardTouchEvents.add(event)
                    }
                }
            }
        )

    }
}

@Composable
fun TrainTextDisplay(testBlock: Int, blockNumber: Int, testIter: Int, testNumber: Int, modeIter:Int, modeCnt: Int, testString: String) {
    Column(
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Block : ${testBlock + 1} / $blockNumber", fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Iteraction : ${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mode : ${modeIter + 1} / $modeCnt", fontSize = 20.sp
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