package com.taehokimmm.hapticvboard_android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taehokimmm.hapticvboard_android.layout.intro.GroupIntro
import com.taehokimmm.hapticvboard_android.layout.intro.IntroInit
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.VibrationTestEnd
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.VibrationTestInit
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.Study1VibrationQuiz
import com.taehokimmm.hapticvboard_android.layout.textentry.Study2End
import com.taehokimmm.hapticvboard_android.layout.textentry.Study2Init
import com.taehokimmm.hapticvboard_android.layout.textentry.Study3
import com.taehokimmm.hapticvboard_android.layout.typingtest.TypingTestFreePlay
import com.taehokimmm.hapticvboard_android.layout.typingtest.TypingTest
import com.taehokimmm.hapticvboard_android.layout.typingtest.TypingTestEnd
import com.taehokimmm.hapticvboard_android.layout.typingtest.TypingTestInit
import com.taehokimmm.hapticvboard_android.manager.HapticManager
//import com.taehokimmm.hapticvboard_android.manager.SerialMonitorScreen
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import com.taehokimmm.hapticvboard_android.ui.theme.HapticVBoardAndroidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val soundManager = SoundManager(this)
        val hapticManager = HapticManager(this)
        setContent {
            HapticVBoardAndroidTheme {
                MainScreen(soundManager, hapticManager)
            }
        }
    }
}

enum class HapticMode {
    VOICE, PHONEME, TICK, NONE, VOICEPHONEME, VOICETICK, VOICEPHONEMETICK
}

@Composable
fun MainScreen(soundManager: SoundManager?, hapticManager: HapticManager?) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentScreen by rememberSaveable { mutableStateOf("freeType") }
    var hapticMode by rememberSaveable { mutableStateOf(HapticMode.NONE) }

    if (hapticMode == HapticMode.NONE) {
        AlertDialog(onDismissRequest = { },
            title = { Text("Serial Connection Not Found", fontSize = 20.sp) },
            text = { Text("Please connect the serial device and press Retry. If you are not using a serial device, press Ignore.") },
            confirmButton = {
                Button(onClick = {
                    hapticManager?.connect()
                    if (hapticManager?.isOpen() == true) hapticMode = HapticMode.PHONEME
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { hapticMode = HapticMode.VOICE },
                ) {
                    Text("Ignore")
                }
            })
    }

    val window = LocalContext.current.findActivity()?.window!!
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    insetsController.apply {
        hide(WindowInsetsCompat.Type.navigationBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController = navController, onItemClicked = {
                scope.launch { drawerState.close() }
            })
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        Scaffold(
            topBar = { DrawTopAppBar(currentScreen, scope, drawerState, navController) },
            contentWindowInsets = WindowInsets(0.dp),
            content = { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "freeType",
                ) {
                    composable("freeType") {
                        currentScreen = "freeType"
                        FreeTypeMode(innerPadding, soundManager, hapticManager, hapticMode)
                    }
                    //------ Introduction -----//
                    composable("intro/init") {
                        currentScreen = "intro/init"
                        IntroInit(navController)
                    }
                    composable("intro/intro/{Category}/{Group}") {
                        currentScreen = "intro/intro/"
                        val category = it.arguments?.getString("Category")!!
                        val group = it.arguments?.getString("Group")!!
                        GroupIntro(innerPadding, soundManager, hapticManager, category, group)
                    }

                    //------ Vibration Test -----//
                    composable("vibrationTest/init") {
                        currentScreen = "vibrationTest/init"
                        VibrationTestInit(navController)
                    }
                    composable("vibrationTest/train/{subject}/{Group}") {
                        currentScreen = "vibrationTest/train"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1VibrationQuiz(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode
                        )
                    }
                    composable("vibrationTest/end/{subject}") {
                        currentScreen = "vibrationTest/end"
                        val subject = it.arguments?.getString("subject")!!
                        VibrationTestEnd(subject, navController)
                    }

                    //------ Typing Test -----//

                    composable("typingTest/init") {
                        currentScreen = "typingTest/init"
                        TypingTestInit(navController)
                    }

                    composable("typingTest/freeplay/{subject}/{option}/{block}") {
                        currentScreen = "typingTest"
                        val subject = it.arguments?.getString("subject")!!
                        val option = it.arguments?.getString("option")!!
                        val block = it.arguments?.getString("block")!!

                        TypingTestFreePlay(
                            innerPadding,
                            subject,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            option,
                            block.toInt()
                        )
                    }

                    composable("typingTest/train/{subject}/{option}/{block}") {
                        currentScreen = "typingTest"
                        val subject = it.arguments?.getString("subject")!!
                        val option = it.arguments?.getString("option")!!
                        val block = it.arguments?.getString("block")!!

                        TypingTest(
                            block.toInt(),
                            innerPadding,
                            subject,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            option
                        )
                    }

                    composable("typingTest/end/{subject}") {
                        currentScreen = "typingTest/end"
                        val subject = it.arguments?.getString("subject")!!
                        TypingTestEnd(subject, navController)
                    }

                    //------ Text Entry -----//
                    composable("textEntry/init") {
                        currentScreen = "textEntry/init"
                        Study2Init(navController)
                    }
                    composable("textEntry/{subject}/{feedback}/{isPractice}/{testBlock}") {
                        currentScreen = "textEntry"
                        val subject = it.arguments?.getString("subject")!!
                        val feedback = it.arguments?.getString("feedback")!!
                        val isPractice = it.arguments?.getString("isPractice")!!.toBoolean()
                        val testBlock = it.arguments?.getString("testBlock")!!.toInt()
                        var hapticMode = HapticMode.NONE
                        if (feedback == "audio") hapticMode = HapticMode.VOICE
                        else if(feedback == "phoneme") hapticMode = HapticMode.PHONEME
                        else if(feedback == "audiophoneme") hapticMode = HapticMode.VOICEPHONEME
                        else if(feedback == "vibration") hapticMode = HapticMode.TICK

                        Study3(
                            innerPadding,
                            subject,
                            isPractice,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode,
                            testBlock
                        )
                    }
                    composable("textEntry/end/{subject}") {
                        currentScreen = "textEntry/end"
                        val subject = it.arguments?.getString("subject")!!
                        Study2End(subject, navController)
                    }
                    composable("setting") {
                        currentScreen = "setting"
                        SettingScreen(hapticManager)
                    }
                }
            },
        )
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onItemClicked: () -> Unit) {
    var selectedItem by rememberSaveable { mutableStateOf("freeType") }

    Box(
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        ModalDrawerSheet {
            Text(
                "HapticVBoard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            NavigationDrawerItem(label = { Text("Free Type") },
                selected = selectedItem == "freeType",
                onClick = {
                    navController.navigate("freeType")
                    selectedItem = "freeType"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Introduction") },
                selected = selectedItem == "intro/init",
                onClick = {
                    navController.navigate("intro/init")
                    selectedItem = "intro/init"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("1. Vibration Test") },
                selected = selectedItem == "vibrationTest",
                onClick = {
                    navController.navigate("vibrationTest/init")
                    selectedItem = "vibrationTest"
                    onItemClicked()
                })
//            NavigationDrawerItem(label = { Text("Study 1 Test") },
//                selected = selectedItem == "study1/test",
//                onClick = {
//                    navController.navigate("study1/test/init")
//                    selectedItem = "study1/test"
//                    onItemClicked()
//                })
            NavigationDrawerItem(label = { Text("2. Typing Test") },
                selected = selectedItem == "typingTest",
                onClick = {
                    navController.navigate("typingTest/init")
                    selectedItem = "typingTest"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("3. Text Entry") },
                selected = selectedItem == "textEntry",
                onClick = {
                    navController.navigate("textEntry/init")
                    selectedItem = "textEntry"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Setting") },
                selected = selectedItem == "setting",
                onClick = {
                    navController.navigate("setting")
                    selectedItem = "setting"
                    onItemClicked()
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawTopAppBar(
    currentScreen: String,
    scope: CoroutineScope,
    drawerState: DrawerState,
    navController: NavHostController
) {
    val displayText = when (currentScreen) {
        "freeType" -> "Free Type"
        "intro/init" -> "Introduction"
        "intro/intro/init" -> "Introduction"
        "train" -> "Train"
        "vibrationTest/init" -> "Vibration Test"
        "typingTest/init" -> "Typing Test"
        "textEntry/init" -> "Text Entry"
        "setting" -> "Setting"
        else -> ""
    }

    when (currentScreen) {

        "testEnd" -> {}

        else -> TopAppBar(title = { Text(displayText) }, navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerState.apply {
                        if (isClosed) open() else close()
                    }
                }
            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        })
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

@Preview
@Composable
fun DefaultPreview() {
    HapticVBoardAndroidTheme {
        MainScreen(null, null)
    }
}