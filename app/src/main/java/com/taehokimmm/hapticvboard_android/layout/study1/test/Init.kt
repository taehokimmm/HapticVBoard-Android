package com.taehokimmm.hapticvboard_android.layout.study1.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.taehokimmm.hapticvboard_android.database.resetStudy1Data


@Composable
fun Study1TestInit(navController: NavHostController) {
    val context = LocalContext.current
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
                text = "Select Test Group",
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
                            resetStudy1Data(context, testSubjectIdentifier, group)
                            navController.navigate("study1/test/${testSubjectIdentifier}/${group}")
                        }
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
fun Spinner(options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }

    Box {
        Text(
            text = selectedOption,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .background(
                    color = Color.LightGray
                )
                .padding(16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        onOptionSelected(option)
                    })
            }
        }
    }
}



@Composable
fun CheckboxWithLabel(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, fontSize = 16.sp)
    }
}