package com.taehokimmm.hapticvboard_android.layout.vibrationtest

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
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database


@Composable
fun VibrationTestEnd(subject: String, navController: NavHostController) {
    LaunchedEffect(Unit) {
        closeStudy1Database()
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vibration Test Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("vibrationTest/init")
        }) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
