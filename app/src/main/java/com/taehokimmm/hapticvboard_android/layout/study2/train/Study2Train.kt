package com.taehokimmm.hapticvboard_android.layout.study2.train

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.database.Study2TrainAnswer
import com.taehokimmm.hapticvboard_android.database.Study2TrainLog
import com.taehokimmm.hapticvboard_android.database.addStudy2TrainAnswer
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import org.intellij.lang.annotations.Language
import java.util.Locale

@Composable
fun Study2Train(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val totalBlock = 3
    val modeCnt = 3
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableIntStateOf(-1) }
    var modeIter by remember { mutableIntStateOf(0) }
    var modeNames = listOf("음성 모드 학습", "진동 모드 학습", "테스트")

    var testAlphabets = ('a'..'z').map { it.toString() }
    if (subject == "practice") {
        testAlphabets = ('a'..'b').map { it.toString() }
    }
    val testNumber = testAlphabets.size

    var testList by remember { mutableStateOf(listOf("")) }

    var isTypingMode by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }

    val databaseName = subject+ "_study2"

    var timer by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }

    var isExplaining by remember {mutableStateOf(false)}
    val handler = Handler(Looper.getMainLooper())
    var runnables = remember { mutableStateListOf<Runnable>() }

    var inputKey by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var correctAnswer by remember {mutableStateOf(0)}
    var wrongAnswer by remember {mutableStateOf(listOf(listOf("")))}


    fun initMetric() {
        startTime = System.currentTimeMillis()
    }

    fun speak(word: String) {
        soundManager.stop()
        soundManager.speakOutChar(word)
        startTime = -1L
    }

    fun addLogging(inputText: String) {
        endTime = System.currentTimeMillis()
        val data = Study2TrainAnswer(
            answer = testList[testIter],
            perceived = inputText,
            iteration = testIter,
            mode = modeIter,
            block = testBlock,
            duration = endTime - startTime
        )
        addStudy2TrainAnswer(context, databaseName, data)
    }

    fun explainKey(key: String, delay: Long = 0) {
        // Clear any previous runnables before adding new ones
        runnables.clear()
        isExplaining = true

        runnables.add(
            delay({
                soundManager.stop()}, delay, handler)
        )

        runnables.add(
            delay({soundManager?.speakOut(key)}, delay, handler)
        )
        // Phoneme
        runnables.add(
            delay({ soundManager.playPhoneme(key) }, 700+delay, handler)
        )

        runnables.add(
            delay({
                hapticManager?.generateHaptic(
                    key,
                    HapticMode.PHONEME
                )
            }, 1400+delay, handler)
        )

        runnables.add(
            delay({isExplaining = false}, 1400, handler)
        )
    }


    fun onConfirm(key: String): Boolean {
        isTypingMode = false
        inputKey = key
        isCorrect = (key == testList[testIter])
        if (isCorrect) correctAnswer++
        else wrongAnswer += listOf(listOf(testList[testIter], inputKey))

        if (modeIter <= 1) {
            delay(
                {// Correction Feedback
                    soundManager.playSound(isCorrect)
                }, 500
            )
        }

        addLogging(key)

        if (modeIter == 2) {
            soundManager.playEarcon("beep")
            delay({
                testIter++
                initMetric()
            }, 1000)
        }
        return true
    }

    var isSpeakingDone by remember { mutableStateOf(true) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        // Initiate TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
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

    fun speakOutEng(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setLanguage(Locale.US)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun speakOutKor(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setLanguage(Locale.KOREA)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun explainResult() {
        if (isSpeakingDone == false) return
        // Explain Result
        speakOutKor(testList.size.toString() + "개 중 " + correctAnswer.toString() + "개 정답")
        speakOutKor("오답")

        if (wrongAnswer.size == 1) {
            speakOutKor("없음")
        } else {
            wrongAnswer.forEach { wrong ->
                if (wrong.size == 1) return@forEach
                speakOutEng(wrong[0])
            }
        }
    }

    LaunchedEffect(isTypingMode) {
        if (isTypingMode == false && modeIter < 2) {
            if (testIter != -1 && testIter < testList.size
                ) {
                explainKey(testList[testIter], 1500)
            }
        }
    }

    LaunchedEffect(timer) {
        kotlinx.coroutines.delay(1000L)
        timer++

        var temp: Int
        if (modeIter == 2) {
            temp = 60 - timer
        } else {
            temp = 0
        }
        if (temp < 0) temp = 0
        if (temp != countdown) countdown = temp
    }



    LaunchedEffect(modeIter) {

        if (modeIter >= modeCnt) {
            testBlock++
            if (testBlock >= totalBlock) {
                closeStudy2Database()
                navController!!.navigate("study2/train/end/$subject")
            } else {
                modeIter = 0
            }
            return@LaunchedEffect
        }


    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            testList = testAlphabets.shuffled()
            val modeName = modeNames[modeIter]
            soundManager.stop()
            soundManager.speakOut(modeName + " : 시작하려면 이중탭하세요.")

        } else if (testIter >= testList.size) {
            explainResult()
            countdown = 1
            timer = 0
        } else {
            isTypingMode = true
            speak(testList[testIter])
        }
    }

    if (testIter == -1) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show countdown (MM:SS)
            Text(
                text = "%02d:%02d".format(countdown / 60, countdown % 60),
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
            )
            if (modeIter < modeNames.size) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    testIter = 0
                                },
                                onTap = {
                                    val modeName = modeNames[modeIter]
                                    soundManager.stop()
                                    soundManager.speakOut(modeName + " : 시작하려면 이중탭하세요.")
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start \n Block : " + (testBlock + 1) + "\n Mode : " + modeNames[modeIter],
                        fontSize = 20.sp
                    )
                }
            }
        }
    } else if (testIter < testList.size) {
       Box(
                modifier = when(isTypingMode) {
                    true ->  Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    false ->  Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (isExplaining) return@detectTapGestures
                                    soundManager.stop()
                                    runnables.apply {
                                        forEach { handler.removeCallbacks(it) }
                                        clear()
                                    }
                                    soundManager.playEarcon("beep")
                                    isExplaining = false
                                    delay({
                                        testIter++
                                        isTypingMode = true
                                    }, 800)
                                },
                                onTap = {
                                    if (isExplaining) return@detectTapGestures
                                    explainKey(testList[testIter])
                                }
                            )
                        }
                }
            ) {
           TrainTextDisplay(
               testBlock,
               totalBlock,
               testIter,
               testNumber,
               modeIter,
               testList[testIter],
               soundManager
           )
           Box(
               modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
           ) {
               if (isTypingMode) {
                   KeyboardLayout(
                       touchEvents = keyboardTouchEvents,
                       onKeyPress = { key ->
                           if (modeIter == 0)
                               soundManager.stop()
                           startTime = System.currentTimeMillis()
                       },
                       onKeyRelease = { key ->
                           if (key == "Shift") return@KeyboardLayout
                           if (modeIter == 1) soundManager.speakOut(key)
                           onConfirm(key)
                       },
                       enterKeyVisibility = false,
                       soundManager = soundManager,
                       hapticManager = hapticManager,
                       hapticMode = if (modeIter == 0) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                       logData = Study2TrainLog(
                           block = testBlock,
                           iteration = testIter,
                           mode = modeIter,
                           answer = testList[testIter]
                       ),
                       name = databaseName
                   )
               } else {
                   Box(
                       modifier = Modifier.fillMaxSize().padding(innerPadding),
                       contentAlignment = Alignment.Center
                   ) {
                       Text(
                           text = inputKey.toUpperCase(),
                           color = if (isCorrect) Color.Green else Color.Red,
                           fontSize = 120.sp,
                           fontWeight = FontWeight.Bold
                       )
                   }
               }
           }
       }
        if (isTypingMode) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                factory = { context ->
                    MultiTouchView(
                        context,
                        onTap = {speak(testList[testIter])}
                    ).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                }
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (countdown > 0) return@detectTapGestures
                            modeIter++
                            testIter = -1
                            correctAnswer = 0
                            wrongAnswer = listOf(listOf(""))
                            soundManager.playEarcon("beep")
                        },
                        onTap = {
                            explainResult()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                // Show countdown (MM:SS)
                Text(
                    text = "%02d:%02d".format(countdown / 60, countdown % 60),
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "정답 : " + correctAnswer +" / " + testList.size,
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text (text = "눌러야하는 키 / 실제 누른 키")
                wrongAnswer.forEach { wrong ->
                    if (wrong.size == 2) {
                        Text(text = wrong[0] + " : " + wrong[1], fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}


@Composable
fun TrainTextDisplay(testBlock: Int, blockNumber: Int, testIter: Int, testNumber: Int, modeIter: Int, testString: String, soundManager: SoundManager) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Block : ${testBlock + 1} / $blockNumber", fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (modeIter < 3)
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mode : ${listOf("Train-Voice", "Train-Phoneme", "Test")[modeIter]}",
                    fontSize = 15.sp
                )
            }

        Spacer(modifier = Modifier.height(20.dp))

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
                text = testString.uppercase(), fontSize = 120.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}