package com.taehokimmm.hapticvboard_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var inputText by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                        .padding(20.dp, 16.dp)
                        .height(30.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = inputText,
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                KeyboardLayout { key ->
                    when (key) {
                        "Backspace" -> if (inputText.isNotEmpty()) inputText = inputText.dropLast(1)
                        "Space" -> inputText += " "
                        "Shift" -> Unit
                        else -> inputText += key
                    }
                }
            }
        }
    }
}
