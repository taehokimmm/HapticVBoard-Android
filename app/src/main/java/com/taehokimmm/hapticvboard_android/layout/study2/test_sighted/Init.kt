package com.taehokimmm.hapticvboard_android.layout.study2.test_sighted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.taehokimmm.hapticvboard_android.database.closeAllDatabases
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.database.closeStudy2TrainDatabase
import com.taehokimmm.hapticvboard_android.layout.study1.test.CheckboxWithLabel
import com.taehokimmm.hapticvboard_android.layout.study1.test.Spinner


@Composable
fun Study2Init(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val subjectFocusRequester = FocusRequester()
    val questionsFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var options = listOf("audio", "phoneme", "vibration")
    var selectedOption by remember { mutableStateOf("audio") }

    var totalBlocks by remember{mutableStateOf(2)}
    var selectedBlock by remember {
        mutableStateOf("0")
    }

    var subjects = listOf("test")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 8) {
        subjects += listOf("Pilot" + i)
    }

    var isPractice by remember {
        mutableStateOf(false)
    }

    fun setTotalBlocks() {
        if (isPractice) {
            totalBlocks = 1
        } else {
            when (selectedOption) {
                "audio" -> totalBlocks = 2
                "phoneme" -> totalBlocks = 4
                else -> totalBlocks = 1
            }
        }
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
                Column {
                    options.forEach{option -> (
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = {
                                    selectedOption = option
                                    setTotalBlocks()
                                }
                            )
                            Text(text = option)
                        }
                    )}
                }
            }

            // Test subject identifier
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Block",
                fontSize = 16.sp
            )

            Spinner(
                options = (0 until totalBlocks).map({i -> i.toString()}),
                onOptionSelected = { selectedOption ->
                    selectedBlock = selectedOption
                    setTotalBlocks()
                }
            )


            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Session",
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CheckboxWithLabel(
                    checked = isPractice,
                    onCheckedChange = {
                        isPractice = it
                    },
                    label = "Is Practice Session?"
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    closeAllDatabases()
                    if (testSubjectIdentifier.isNotEmpty()) {
                        navController.navigate("study2/test/$testSubjectIdentifier/$selectedOption/$isPractice/$selectedBlock")
                    } else if (testSubjectIdentifier.isEmpty()) {
                        errorMessage = "Please enter a test subject"
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Test")
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