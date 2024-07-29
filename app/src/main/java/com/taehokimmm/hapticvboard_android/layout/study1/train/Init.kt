package com.taehokimmm.hapticvboard_android.layout.study1.train

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.taehokimmm.hapticvboard_android.database.deleteDatabaseByName
import com.taehokimmm.hapticvboard_android.layout.study1.test.CheckboxWithLabel
import com.taehokimmm.hapticvboard_android.layout.study1.test.Spinner


@Composable
fun Study1TrainInit(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("test") }
    var errorMessage by remember { mutableStateOf("") }

    var checkboxLeftState by remember { mutableStateOf(false) }
    var checkboxCenterState by remember { mutableStateOf(false) }
    var checkboxRightState by remember { mutableStateOf(false) }

    val subjectFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var subjects = listOf("test")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 5) {
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
                CheckboxWithLabel(
                    checked = checkboxLeftState,
                    onCheckedChange = { checkboxLeftState = it },
                    label = "Left"
                )
                CheckboxWithLabel(
                    checked = checkboxCenterState,
                    onCheckedChange = { checkboxCenterState = it },
                    label = "Center"
                )
                CheckboxWithLabel(
                    checked = checkboxRightState,
                    onCheckedChange = { checkboxRightState = it },
                    label = "Right"
                )
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
                    when {
                        testSubjectIdentifier.isEmpty() -> errorMessage =
                            "Please enter a test subject"

                        !checkboxLeftState && !checkboxCenterState && !checkboxRightState -> errorMessage =
                            "Please select a test group"

                        else -> {
                            val group = buildString {
                                if (checkboxLeftState) append("L")
                                if (checkboxCenterState) append("C")
                                if (checkboxRightState) append("R")
                            }
                            deleteDatabaseByName(context, testSubjectIdentifier)
                            navController.navigate("study1/train/phase1/${testSubjectIdentifier}/${group}")
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Train")
            }
        }
    }
}