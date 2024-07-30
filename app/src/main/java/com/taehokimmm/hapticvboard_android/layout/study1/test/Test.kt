package com.taehokimmm.hapticvboard_android.layout.study1.test

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
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
import com.taehokimmm.hapticvboard_android.database.addStudy1Answer
import com.taehokimmm.hapticvboard_android.database.study1.Study1Answer
import com.taehokimmm.hapticvboard_android.layout.study1.train.TestDisplay
import com.taehokimmm.hapticvboard_android.layout.study1.train.getAllowGroup
import com.taehokimmm.hapticvboard_android.layout.study1.train.getSuppressGroup
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


@Composable
fun Study1Test(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val context = LocalContext.current

    val suppress = getSuppressGroup(group)
    val allowlist = getAllowGroup(group)
    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }

    var testList = remember { allowlist.shuffled() }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }
    val timer = Timer()
    var timerTask: TimerTask = object: TimerTask() {
        override fun run(){
            return
        }
    }


    var startTime by remember { mutableStateOf(0L) }
    var isSpeakingDone by remember {mutableStateOf(false)}
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        // Initiate TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeakingDone = true
                        Log.e("phase3", "speech done")
                        startTime = System.currentTimeMillis()
                    }

                    override fun onError(utteranceId: String?) {
                    }
                })
            }
        }
    }
    fun speak() {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.speak("Press : "+testList[testIter], TextToSpeech.QUEUE_FLUSH, params, "utteranceId")
    }

    if (testIter == -1) {
        // Audio Explanation
        soundManager.speakOut("Tap to start Block " + testBlock)
        // Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    testIter = 0
                    speak()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(text="Tap to Start \n Block : " + testBlock,
                    fontSize = 20.sp)
            }
        }
    }
    else if (testIter >= testList.size) {
        testBlock++
        testList = allowlist.shuffled()
        if (testBlock > 3) {
            navController.navigate("study1/test/end/${subject}")
        } else {
            testIter = -1
        }
    }
    else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TestDisplay(testIter, testList.size, testList[testIter][0], soundManager)

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {

                    if (isSpeakingDone) {
                        KeyboardLayout(
                            touchEvents = keyboardTouchEvents,
                            onKeyRelease = { key ->
                                //--- Append Data to Database ---//
                                val curTime = System.currentTimeMillis()
                                val data = Study1Answer(
                                    answer = testList[testIter],
                                    perceived = key,
                                    iter = testIter,
                                    block = testBlock,
                                    duration = curTime - startTime
                                )
                                addStudy1Answer(context, subject, group, data)
                                // ------------------------------//
                                testIter++
                                if (testIter < testList.size) speak()
                            },
                            soundManager = soundManager,
                            hapticManager = hapticManager,
                            hapticMode = hapticMode,
                            suppress = suppress
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

        timer.schedule(timerTask, 0, 100)
    }
}