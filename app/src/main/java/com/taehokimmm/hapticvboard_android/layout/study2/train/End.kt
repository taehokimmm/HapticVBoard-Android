package com.taehokimmm.hapticvboard_android.layout.study2.train

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Study2TrainEnd(
    subject: String, navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Study2 Train Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("study2/train/init")
        }) {
            Text("Return to Study2 Train Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}