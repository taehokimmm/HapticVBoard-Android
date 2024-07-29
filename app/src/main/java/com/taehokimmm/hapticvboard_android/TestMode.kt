package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun TestInit(navController: NavHostController) {

    var testSubjectIdentifier by remember { mutableStateOf("") }
    var testQuestions by remember { mutableIntStateOf(10) }
    var testQuestionString by remember { mutableStateOf("10") }
    var errorMessage by remember { mutableStateOf("") }

    val subjectFocusRequester = FocusRequester()
    val questionsFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // Test subject identifier
            TextField(
                value = testSubjectIdentifier,
                onValueChange = { testSubjectIdentifier = it.trim() },
                maxLines = 1,
                label = { Text(text = "Test Subject", fontSize = 16.sp) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(subjectFocusRequester),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { questionsFocusRequester.requestFocus() })
            )

            // Number of questions
            TextField(
                value = testQuestionString,
                onValueChange = {
                    testQuestionString = it
                    testQuestions = it.toIntOrNull() ?: 10
                },
                maxLines = 1,
                label = { Text(text = "Number of Questions", fontSize = 16.sp) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(questionsFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            testQuestionString = ""
                        }
                        if (!focusState.isFocused && testQuestionString.isEmpty()) {
                            testQuestions = 10
                            testQuestionString = testQuestions.toString()
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (testSubjectIdentifier.isNotEmpty() && testQuestions > 0) {
                        navController.navigate("test/${testSubjectIdentifier}/${testQuestions}")
                    } else if (testSubjectIdentifier.isEmpty()) {
                        errorMessage = "Please enter a test subject"
                    } else {
                        errorMessage = "Number must be a positive integer"
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Test")
            }
        }
    }
}

@Composable
fun TestText(
    innerPadding: PaddingValues,
    testName: String,
    testNumber: Int,
    navController: NavHostController?,
    soundManager: SoundManager?,
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
//            accuracy = calculateAccuracy(inputText, testList[testIter - 1])
//            val wpm = calculateWPM(startTime, endTime, wordCount)
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
            navController!!.navigate("testEnd")
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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

@Composable
fun TestLetter(
    innerPadding: PaddingValues,
    testName: String,
    testNumber: Int,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current
    var testIter by remember { mutableIntStateOf(0) }

    var correct by remember { mutableIntStateOf(0) }

    // Record the wrong answers and the respective correct answers
    val wrongAnswers = remember { mutableStateListOf<Char>() }
    val correctAnswers = remember { mutableStateListOf<Char>() }

    // Create a list of a-z characters, shuffled
    val testList = remember { ('a'..'z').shuffled() }

    if (testIter >= testNumber) {
        // Navigate to the Test End Screen
        Test2End(
            subject = testName,
            correct = correct,
            testNumber = testNumber,
            wrongAnswers = wrongAnswers,
            correctAnswers = correctAnswers,
            navController = navController!!
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            TestDisplay(testIter, testNumber, testList[testIter], soundManager)

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->
                            if (key == testList[testIter].toString()) {
                                correct++
                            } else {
                                wrongAnswers.add(key[0])
                                correctAnswers.add(testList[testIter])
                            }
                            testIter++
                        },
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = hapticMode
                    )
                    AndroidView(
                        modifier = Modifier
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

@Composable
fun TestDisplay(testIter: Int, testNumber: Int, testLetter: Char, soundManager: SoundManager) {
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

        Button(
            onClick = {
                soundManager.speakOut(testLetter.toString())
            },
            modifier = Modifier.fillMaxWidth().height(420.dp),
            shape = RoundedCornerShape(corner = CornerSize(0)),
            colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
        ) {
            Text(
                text = testLetter.uppercase(), fontSize = 60.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TestEnd(navController: NavHostController) {
    var countDown by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) {
        while (countDown > 0) {
            delay(1000L) // 1 second delay
            countDown--
        }
        navController.navigate("testInit") // Navigate back to the initial screen
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test Completed!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Returning to the main screen in $countDown seconds...", fontSize = 16.sp
        )
    }
}

fun readTxtFile(context: Context, resId: Int): List<String> {
    val inputStream = context.resources.openRawResource(resId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val lines = reader.readLines()
    reader.close()
    return lines
}

@Preview
@Composable
fun TestInitPreview() {
    TestInit(NavHostController(LocalContext.current))
}

@Preview
@Composable
fun TestModePreview() {
    TestText(PaddingValues(0.dp),"Test", 10, NavHostController(LocalContext.current), null, null, HapticMode.NONE)
}

@Preview
@Composable
fun TestEndPreview() {
    TestEnd(NavHostController(LocalContext.current))
}
