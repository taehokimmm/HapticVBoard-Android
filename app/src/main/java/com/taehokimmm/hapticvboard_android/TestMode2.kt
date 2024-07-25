package com.taehokimmm.hapticvboard_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Test2Init(navController: NavHostController) {

    var testSubjectIdentifier by remember { mutableStateOf("") }
    var testQuestions by remember { mutableIntStateOf(26) }
    var testQuestionString by remember { mutableStateOf("26") }
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
                    testQuestions = it.toIntOrNull() ?: 26
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
                            testQuestions = 26
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
                        navController.navigate("test2/${testSubjectIdentifier}/${testQuestions}")
                    } else if (testSubjectIdentifier.isEmpty()) {
                        errorMessage = "Please enter a test subject"
                    } else if (testQuestions > 26) {
                        errorMessage = "Number of questions must be less than 26"
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
fun Test2End(
    subject: String, correct: Int, testNumber: Int,
    wrongAnswers: List<Char>, correctAnswers: List<Char>,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Correct: $correct / $testNumber, Accuracy: ${correct * 100 / testNumber}%",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                navController.navigate("test2Init")
            }
        ) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Display the wrong answers and the correct answers
        Column {
            Text("Wrong Answers: ", fontSize = 20.sp)
            for (i in wrongAnswers.indices) {
                Text(
                    text = "Q: ${wrongAnswers[i]}          A: ${correctAnswers[i]}",
                    fontSize = 20.sp
                )
            }
        }
    }
}