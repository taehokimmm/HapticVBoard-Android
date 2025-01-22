package com.taehokimmm.hapticvboard_android.layout.vibrationtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase
import com.taehokimmm.hapticvboard_android.layout.textentry.Spinner


@Composable
fun VibrationTestInit(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var options = listOf("1", "2", "3")
    var selectedOption by remember { mutableStateOf("") }

    val subjectFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var subjects = listOf("test", "practice")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 6) {
        subjects += listOf("VP" + i)
    }
    for(i in 1 until 8) {
        subjects += listOf("Pilot" + i)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Test subject identifier
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

            // Select Test Group
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Train Group",
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column {
                    options.forEach{option -> (
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(text = option)
                        }
                    )}
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            Button(
                onClick = {
                    closeStudyDatabase()
                    when {
                        testSubjectIdentifier.isEmpty() -> errorMessage =
                            "Please enter a test subject"

                        selectedOption.isEmpty() -> errorMessage =
                            "Please select a test group"

                        else -> {
                            navController.navigate("vibrationTest/train/${testSubjectIdentifier}/${selectedOption}")
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Train")
            }

//            Button(onClick = {
//                resetData(context, testSubjectIdentifier, selectedOption)
//            }, colors = ButtonColors(Color.Red, Color.White, Color.White, Color.White)
//            )
//            {
//                Text("DELETE DATABASE",
//                    color = Color.White)
//            }
        }
    }
}