package com.taehokimmm.hapticvboard_android.layout.study1.train

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.deleteDatabaseByName
import com.taehokimmm.hapticvboard_android.database.resetData
import com.taehokimmm.hapticvboard_android.layout.study1.test.CheckboxWithLabel
import com.taehokimmm.hapticvboard_android.layout.study1.test.Spinner


@Composable
fun Study1TrainInit(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("test") }
    var errorMessage by remember { mutableStateOf("") }

    var checkbox1State by remember { mutableStateOf(false) }
    var checkbox2State by remember { mutableStateOf(false) }
    var checkbox3State by remember { mutableStateOf(false) }
    var checkbox4State by remember { mutableStateOf(false) }

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
                    checked = checkbox1State,
                    onCheckedChange = { checkbox1State = it },
                    label = "1"
                )
                CheckboxWithLabel(
                    checked = checkbox2State,
                    onCheckedChange = { checkbox2State = it },
                    label = "2"
                )
                CheckboxWithLabel(
                    checked = checkbox3State,
                    onCheckedChange = { checkbox3State = it },
                    label = "3"
                )
                CheckboxWithLabel(
                    checked = checkbox4State,
                    onCheckedChange = { checkbox4State = it },
                    label = "4"
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

                        !checkbox1State && !checkbox2State && !checkbox3State && !checkbox4State -> errorMessage =
                            "Please select a test group"

                        else -> {
                            val group = buildString {
                                if (checkbox1State) append("1")
                                if (checkbox2State) append("2")
                                if (checkbox3State) append("3")
                                if (checkbox4State) append("4")
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


            Button(onClick = {
                val group = buildString {
                    if (checkbox1State) append("1")
                    if (checkbox2State) append("2")
                    if (checkbox3State) append("3")
                    if (checkbox4State) append("4")
                }
                resetData(context, testSubjectIdentifier, group)
            }, colors = ButtonColors(Color.Red, Color.White, Color.White, Color.White)
            )
            {
                Text("DELETE DATABASE",
                    color = Color.White)
            }
        }
    }
}