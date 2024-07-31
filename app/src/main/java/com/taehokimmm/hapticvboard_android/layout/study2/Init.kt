package com.taehokimmm.hapticvboard_android.layout.study2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.resetData
import com.taehokimmm.hapticvboard_android.layout.study1.test.CheckboxWithLabel
import com.taehokimmm.hapticvboard_android.layout.study1.test.Spinner


@Composable
fun Study2Init(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("test") }
    var testQuestions by remember { mutableIntStateOf(10) }
    var testQuestionString by remember { mutableStateOf("10") }
    var errorMessage by remember { mutableStateOf("") }

    val subjectFocusRequester = FocusRequester()
    val questionsFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var checkboxLeftState by remember { mutableStateOf(false) }
    var checkboxCenterState by remember { mutableStateOf(false) }
    var checkboxRightState by remember { mutableStateOf(false) }

    var subjects = listOf("test")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 5) {
        subjects += listOf("Pilot" + i)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // Test subject identifier
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Subject",
                fontSize = 16.sp
            )

            Spinner(
                options = subjects,
                onOptionSelected = { selectedOption ->
                    testSubjectIdentifier = selectedOption.trim()
                }
            )

            // Select Modality
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CheckboxWithLabel(
                    checked = checkboxLeftState,
                    onCheckedChange = { checkboxLeftState = it },
                    label = "Audio"
                )
                CheckboxWithLabel(
                    checked = checkboxCenterState,
                    onCheckedChange = { checkboxCenterState = it },
                    label = "Phoneme"
                )
                CheckboxWithLabel(
                    checked = checkboxRightState,
                    onCheckedChange = { checkboxRightState = it },
                    label = "Vibration"
                )
            }

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
                        val feedback = buildString {
                            if (checkboxLeftState) append("audio")
                            if (checkboxCenterState) append("phoneme")
                            if (checkboxRightState) append("vibration")
                        }
                        navController.navigate("study2/$testSubjectIdentifier/$testQuestions/$feedback")
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


            Button(onClick = {
                val feedback = buildString {
                    if (checkboxLeftState) append("audio")
                    if (checkboxCenterState) append("phoneme")
                    if (checkboxRightState) append("vibration")
                }
                resetData(context, testSubjectIdentifier, feedback)
            }, colors = ButtonColors(Color.Red, Color.White, Color.White, Color.White)
            )
            {
                Text("DELETE DATABASE",
                    color = Color.White)
            }
        }
    }
}