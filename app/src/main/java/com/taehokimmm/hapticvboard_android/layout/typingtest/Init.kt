package com.taehokimmm.hapticvboard_android.layout.typingtest

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase
import com.taehokimmm.hapticvboard_android.layout.textentry.Spinner


@Composable
fun TypingTestInit(navController: NavHostController) {
    var testSubjectIdentifier by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var options = listOf("1", "2", "3", "123")
    var selectedOption by remember { mutableStateOf("") }

    var subjects = listOf("practice")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 8) {
        subjects += listOf("Pilot" + i)
    }

    var blocks = listOf("1", "2", "3", "4", "5")
    var selectedBlock by remember { mutableStateOf("1") }

    var modes = listOf("train", "test")
    var selectedMode by remember { mutableStateOf("train") }

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

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Block identifier
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Group",
                fontSize = 16.sp
            )

            // Select Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed{index, option -> (
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = {
                                selectedOption = option
                            }
                        )
                        Text(text = option)
                    }
                )}
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Select Block
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Block",
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                blocks.forEachIndexed{index, option -> (
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedBlock == option,
                                onClick = {
                                    selectedBlock = option
                                }
                            )
                            Text(text = option)
                        }
                        )}
            }


            // Select Mode
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Mode",
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                modes.forEachIndexed{index, option -> (
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedMode == option,
                                onClick = {
                                    selectedMode = option
                                }
                            )
                            Text(text = option)
                        }
                        )}
            }

            Button(
                onClick = {
                    closeStudyDatabase()
                    if (testSubjectIdentifier.isNotEmpty() && selectedOption.isNotEmpty()) {
                        navController.navigate("typingTest/freeplay/$testSubjectIdentifier/$selectedOption/$selectedBlock/$selectedMode")
                    } else {
                        errorMessage = "Please enter a test subject"
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