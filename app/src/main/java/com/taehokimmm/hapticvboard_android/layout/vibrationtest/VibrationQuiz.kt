package com.taehokimmm.hapticvboard_android.layout.vibrationtest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.VibrationTestAnswer
import com.taehokimmm.hapticvboard_android.database.addVibrationTestAnswer
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase
import com.taehokimmm.hapticvboard_android.layout.intro.Location
import com.taehokimmm.hapticvboard_android.layout.intro.getLocation
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Locale


// PHASE 2 : Vibration Test
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Study1VibrationQuiz(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val context = LocalContext.current


    val totalBlock = 3
    var testIter by remember { mutableStateOf(-1) }
    var testBlock by remember { mutableStateOf(1) }

    var allowlist = getAllowGroup(group)
    if (group == "123") {
        if (testBlock == 1) allowlist = getAllowGroup("1")
        else if (testBlock == 2) allowlist = getAllowGroup("2")
        else if (testBlock == 3) allowlist = getAllowGroup("3")
    }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }
    var testNumber = testList.size

    var selectedIndex by remember {mutableStateOf(-1)}
    var selectedAnswer by remember {mutableStateOf(-1)}
    var options by remember { mutableStateOf(listOf("")) }
    var isShowAnswer by remember {mutableStateOf(false)}
    var isExplaining by remember {mutableStateOf(false)}
    var isTypingMode by remember {mutableStateOf(false)}


    // Swipe Gesture
    var horizontalDragStart by remember {mutableStateOf(0f)}
    var horizontalDragEnd by remember {mutableStateOf(0f)}
    val swipeThreshold = 100

    val screenWidth = DisplayMetrics().widthPixels;
    val boxWidth = 82.dp//132.dp
    val boxHeight = 339.dp//113.dp
    val topMargin = 395.dp

    var correctAnswer by remember {mutableStateOf(0)}
    var wrongAnswer by remember {mutableStateOf(listOf(listOf("")))}

    val handler = Handler(Looper.getMainLooper())
    val runnables = remember { mutableStateListOf<Runnable>() }

    fun explainKey(key: String, delay: Long = 0, isPhoneme: Boolean = false) {
        soundManager.stop()
        runnables.forEach { handler.removeCallbacks(it) }
        runnables.clear()

        isExplaining = true

        // Clear any previous runnables before adding new ones

        // Haptic
        if (isPhoneme) {
            // Word
            runnables.add(
                delay({ soundManager.speakOut(key) },delay+0, handler)
            )
            // Phoneme
            runnables.add(
                delay({ soundManager.playPhoneme(key) }, delay+700, handler)
            )
            runnables.add(
                delay({ hapticManager.generateHaptic(key, HapticMode.PHONEME)}, delay + 1500)
            )
            runnables.add(
                delay({isExplaining = false}, delay+1500, handler)
            )
        } else {
            // Word
            runnables.add(
                delay({ soundManager.speakOutChar(key, true) },delay+0, handler)
            )

            // Phoneme
            runnables.add(
                delay({ soundManager.playPhoneme(key) }, delay+1500, handler)
            )

            // PhonemeGroup > Location
            runnables.add(
                delay({ soundManager.speakOutPhonemeInfo(key) }, delay+2000, handler)
            )

            runnables.add(
                delay({isExplaining = false}, delay+2000, handler)
            )
        }

    }

    fun onConfirm() {
        if (isExplaining) {
            soundManager.stop()
            runnables.forEach { handler.removeCallbacks(it) }
            isExplaining = false
        }
        if (isShowAnswer) {
            if (!isTypingMode) return
            soundManager.playEarcon("beep")
            testIter++
            isTypingMode = false
        } else {
            if (selectedIndex == -1) return

            isTypingMode = false
            delay({isTypingMode = true}, 2000)
            // Correct Feedback
            val selectedOption = options[selectedIndex]
            val targetOption = testList[testIter]
            val isCorrect = selectedOption == targetOption

            if (isCorrect) correctAnswer++
            else wrongAnswer += listOf(listOf(targetOption, selectedOption))

            soundManager.playSound(isCorrect)
            selectedAnswer = selectedIndex
            // Deliver the answer feedback
            isShowAnswer = true

            //--- Append Data to Database ---//
            val data = VibrationTestAnswer(
                row = group,
                answer = targetOption,
                perceived = selectedOption,
                iter = testIter+1,
                block = testBlock
            )
            addVibrationTestAnswer(context, subject, group, data)
            // ------------------------------//
            explainKey(targetOption, 500, isPhoneme = true)
        }
    }

    fun onSelect(index: Int) {
        if (isExplaining) {
            runnables.forEach { runnable ->
                handler.removeCallbacks(runnable) }
            isExplaining = false
        }
        selectedIndex = index
        runnables.clear()

        if (isShowAnswer) {
            hapticManager.generateHaptic(
                options[selectedIndex],
                HapticMode.VOICEPHONEME
            )
        } else {
            hapticManager.generateHaptic(
                options[selectedIndex],
                HapticMode.PHONEME
            )
        }
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

    fun onNextBlock() {
        correctAnswer = 0
        wrongAnswer = listOf(listOf(""))
        testBlock++
        if (testBlock > totalBlock) {
            closeStudyDatabase()
            navController.navigate("vibrationTest/end/${subject}")
        } else {

            if (group == "123") {
                if (testBlock == 1) allowlist = getAllowGroup("1")
                else if (testBlock == 2) allowlist = getAllowGroup("2")
                else if (testBlock == 3) allowlist = getAllowGroup("3")
            }

            testList = allowlist.shuffled()
            testNumber = testList.size
            testIter = -1
        }
    }

    fun getOptions(key : String): List<String> {
        var idx = allowlist.indexOf(key);
        var options: List<String> = emptyList()
        for (i in idx - 2 .. idx+2) {
            if (i >= 0 && i < allowlist.size) {
                options += listOf(allowlist[i])
            }
        }
        return options.shuffled()
    }

    LaunchedEffect (testIter) {
        if (testIter == -1) {
            soundManager.speakOut("시작하려면 이중탭하세요.")
        } else if (testIter < testList.size) {
            selectedAnswer = -1
            selectedIndex = -1
            options = getOptions(testList[testIter])
            delay({
                explainKey(testList[testIter])
            }, 500)
            delay({
                isTypingMode = true
            }, 1000)
            isShowAnswer = false
        } else {
            explainResult()
        }
    }

    // Identification Test
    if (testIter == -1) {
        isShowAnswer = false
        options = listOf("")
        selectedAnswer = -1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            testIter = 0
                        },
                        onTap = {
                            soundManager.stop()
                            soundManager.speakOut("시작하려면 이중탭하세요.")
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(){
                Text(
                    text = "Start Block : " + (testBlock)
                    , fontSize = 20.sp
                )
                Text(
                    text = "시작하려면 이중탭하세요"
                    , fontSize = 20.sp
                )
            }
        }
    } else if (testIter == testNumber) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onNextBlock()
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

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onNextBlock()
                    },
                    colors = ButtonColors(Color.White, Color.Blue, Color.Gray, Color.Black),
                ) {
                    Text("Click or Double Tap")
                }
            }
        }
    } else if (testIter < testNumber) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onConfirm() },
                        onTap = {
                            if (!isExplaining) explainKey(
                                testList[testIter],
                                isPhoneme = isShowAnswer
                            )
                        }
                    )
                }
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (testIter > 0) testIter--
                    },
                    colors = ButtonColors(Color.White, Color.Blue, Color.Gray, Color.Black),
                    enabled =  testIter > 0
                ) {
                    Text("Prev")
                }
                Button(
                    onClick = {
                        testIter++
                    },
                    colors = ButtonColors(Color.White, Color.Blue, Color.Gray, Color.Black),
                ) {
                    Text("Next")
                }
            }

            if(isTypingMode) {
                QuizDisplay(testBlock, totalBlock, testIter, testNumber, testList[testIter][0], height = topMargin)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()

                                        if (event.changes.size > 1) {
                                            val change = event.changes[1]
                                            when {
                                                change.changedToDown() -> {
                                                    onSelect(selectedIndex)
                                                }
                                            }
                                            change.consume()
                                        }

                                        // Get the first pointer event change
                                        val pointerChange = event.changes.first()
                                        if (pointerChange.pressed) {
                                            // Get the current pointer position
                                            val position = pointerChange.position

                                            // Check if the pointer is over the current box
                                            val x_index = position.x
                                                .toDp()
                                                .div(boxWidth)
                                                .toInt()
                                            //val y_index = (position.y.toDp()).div(boxHeight).toInt()
                                            val index = x_index// + y_index * 3

                                            val isPointerInCurrentBox =
                                                index < options.size && index >= 0
                                            if (isPointerInCurrentBox && (
                                                        !pointerChange.previousPressed || selectedIndex != index
                                                        )
                                            ) {
                                                // Update selectedIndex when pointer enters a new box
                                                //if (selectedIndex != index && !isShowAnswer)
                                                //    soundManager.speakOut((index + 1).toString())
                                                onSelect(index)
                                                selectedIndex = index
                                            }
                                            // Consume the event to prevent further handling
                                            pointerChange.consume()
                                        }
                                        // Handle pointer up event
//                                        if (pointerChange.changedToUp() && selectedIndex >= 0)
//                                            onSelect(selectedIndex)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { },
                                )
                            },
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        options.forEachIndexed { index, alphabet ->
                            Box(
                                modifier = Modifier
                                    .width(boxWidth)
                                    .height(boxHeight)
                                    .align(Alignment.CenterVertically)
                                    .background(
                                        if (index == selectedIndex) Color.Blue else Color.White
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isShowAnswer) alphabet.toUpperCase() else (index + 1).toString(),
                                    color = (
                                            if (isShowAnswer) {
                                                if (options[index] == testList[testIter]) {
                                                    Color.Green
                                                } else if (index == selectedAnswer) {
                                                    Color.Red
                                                } else if (index == selectedIndex) {
                                                    Color.White
                                                } else {
                                                    Color.Blue
                                                }
                                            } else {
                                                if (index == selectedIndex) Color.White else Color.Blue
                                            }
                                            ),
                                    fontSize = 40.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            } else {
                val inputKey = if (selectedAnswer == -1) "" else options[selectedAnswer]
                val isCorrect = if (selectedAnswer == -1) true else options[selectedAnswer] == testList[testIter]

                QuizDisplay(testBlock, totalBlock, testIter, testNumber, testList[testIter][0], height = topMargin,
                    inputKey = inputKey, isCorrect = isCorrect)

            }
        }
    }
}



fun getAllowGroup(group: String, isAlphabet: Boolean = false): List<String> {
    var allow = emptyList<String>().toMutableList()
//    if (group.contains("A")) allow += listOf("q", "w", "e", "a", "s", "d", "z", "x")
//    if (group.contains("B")) allow += listOf("e", "d", "x", "r", "t", "f", "g", "c", "v")
//    if (group.contains("C")) allow += listOf("t", "y", "u", "g", "h", "j", "v", "b", "n")
//    if (group.contains("D")) allow += listOf("u", "i", "o", "p", "j", "k", "l", "n", "m")

    if (group.contains("1")) allow += listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    if (group.contains("2")) allow +=listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    if (group.contains("3")) allow +=listOf("z", "x", "c", "v", "b", "n", "m")
    if (group == "123" && !isAlphabet) allow += listOf("Space", "delete", "Shift")
    allow = allow.toSet().toMutableList()
    return allow
}

fun delay(function: () -> Unit, delayMillis: Long, handler: Handler? = null): Runnable {
    val runnable = Runnable {
        function()
    }
    if (handler == null) {
        Handler(Looper.getMainLooper()).postDelayed(
            runnable,
            delayMillis
        )
    } else {
        handler.postDelayed(
            runnable,
            delayMillis
        )
    }

    return runnable
}



@Composable
fun QuizDisplay(testBlock: Int, blockNumber: Int, testIter: Int, testNumber: Int, testLetter: Char, height: Dp = 200.dp, inputKey: String = "", isCorrect: Boolean = true) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(height)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Block : ${testBlock} / ${blockNumber}", fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth().padding(30.dp),
            contentAlignment = getAlignment(testLetter)
        ) {
            Text(
                text = testLetter.uppercase(), fontSize =120.sp, fontWeight = FontWeight.Bold,
                color =  if (isCorrect) Color.Black else Color.Red
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = inputKey.uppercase(), fontSize =120.sp, fontWeight = FontWeight.Bold,
                color =  if (isCorrect) Color.Green else Color.Red
            )
        }
    }
}


fun getAlignment(testLetter: Char): Alignment {
    return when(getLocation(testLetter.toString())){
        Location.LEFT -> Alignment.TopStart
        Location.RIGHT -> Alignment.TopEnd
        else -> Alignment.Center
    }
}