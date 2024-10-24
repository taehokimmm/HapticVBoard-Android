package com.taehokimmm.hapticvboard_android.layout.typingtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.closeStudy2TrainDatabase

@Composable
fun TypingTestEnd(
    subject: String, navController: NavHostController
) {
    LaunchedEffect(Unit) {
        closeStudy2TrainDatabase()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Typing Test Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("typingTest/init")
        }) {
            Text("Return to Typing Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}