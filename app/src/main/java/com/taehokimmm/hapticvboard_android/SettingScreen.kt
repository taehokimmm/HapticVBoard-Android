package com.taehokimmm.hapticvboard_android

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taehokimmm.hapticvboard_android.database.deleteDatabaseByName
import com.taehokimmm.hapticvboard_android.manager.HapticManager

@Composable
fun SettingScreen(
    hapticManager: HapticManager? = null
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            AndroidView(modifier = Modifier.fillMaxWidth(), factory = { ctx ->
                EditText(ctx).apply {
                    hint = "Enter text here"
                    textSize = 20f
                    isFocusable = true
                    isCursorVisible = true
                    isPressed = true
                    // Add a text change listener to update the inputText state and log it
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {
                            inputText = s.toString()
                            Log.d("Setting", inputText)
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })
                }
            })

            Button(
                onClick = {
                    Log.d("Setting", inputText)
                    deleteDatabaseByName(context, inputText)
                }, colors = ButtonColors(Color.Red, Color.White, Color.White, Color.White)
            ) {
                Text(
                    "DELETE DATABASE", color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(200.dp))
            Row (
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ){
                Text(text = "Volume",
                    modifier = Modifier.align(Alignment.CenterVertically))
                Button(
                    onClick = {
                        hapticManager?.setVolumeUp()
                    }, colors = ButtonColors(Color.Black, Color.White, Color.White, Color.White)
                ) {
                    Text(
                        "+", color = Color.White
                    )
                }
                Button(
                    onClick = {
                        hapticManager?.setVolumeDown()
                    }, colors = ButtonColors(Color.Black, Color.White, Color.White, Color.White)
                ) {
                    Text(
                        "-", color = Color.White
                    )
                }

            }
        }
    }

}


@Preview
@Composable
fun Setting() {
    SettingScreen()
}