package com.taehokimmm.hapticvboard_android.layout.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase


@Composable
fun IntroInit(navController: NavHostController) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }

    var options = listOf("1", "2", "3", "123")
    var selectedOption by remember { mutableStateOf(setOf("")) }

    var categories = listOf("phoneme", "location")
    var selectedCategory by remember { mutableStateOf("phoneme") }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Select Group Category
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Group Category",
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column {
                    categories.forEach{option -> (
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedCategory == option,
                                    onClick = { selectedCategory = option }
                                )
                                Text(text = option)
                            }
                            )}
                }
            }

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
                    options.forEach{option ->
                        CheckboxWithLabel(
                            checked = selectedOption.contains(option),
                            onCheckedChange = {
                                if (it) selectedOption = selectedOption.plus(option)
                                else selectedOption = selectedOption.minus(option)},
                            label = option
                        )
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
                        selectedOption.isEmpty() -> errorMessage =
                            "Please select a test group"

                        else -> {
                            navController.navigate("intro/intro/${selectedCategory}/${selectedOption.joinToString("")}")
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start")
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