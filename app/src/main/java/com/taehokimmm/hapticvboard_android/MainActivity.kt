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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.taehokimmm.hapticvboard_android.layout.study1.intro.GroupIntro
import com.taehokimmm.hapticvboard_android.layout.study1.intro.IntroInit
import com.taehokimmm.hapticvboard_android.layout.study1.test.Study1Test
import com.taehokimmm.hapticvboard_android.layout.study1.test.Study1TestEnd
import com.taehokimmm.hapticvboard_android.layout.study1.test.Study1TestInit
import com.taehokimmm.hapticvboard_android.layout.study1.train.Study1TrainEnd
import com.taehokimmm.hapticvboard_android.layout.study1.train.Study1TrainInit
import com.taehokimmm.hapticvboard_android.layout.study1.train.Study1FreePlay
import com.taehokimmm.hapticvboard_android.layout.study1.train.Study1IdentiQuiz
import com.taehokimmm.hapticvboard_android.layout.study1.train.Study1TypingQuiz
import com.taehokimmm.hapticvboard_android.layout.study2.Study2End
import com.taehokimmm.hapticvboard_android.layout.study2.Study2Init
import com.taehokimmm.hapticvboard_android.layout.study2.Study2Test
import com.taehokimmm.hapticvboard_android.layout.study2.train.Study2Train
import com.taehokimmm.hapticvboard_android.layout.study2.train.Study2TrainEnd
import com.taehokimmm.hapticvboard_android.layout.study2.train.Study2TrainInit
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
    VOICE, PHONEME, TICK, NONE, VOICEPHONEME, VOICETICK
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
                    composable("study1/train/init") {
                        currentScreen = "study1/train/init"
                        Study1TrainInit(navController)
                    }
                    composable("study1/train/phase1/{subject}/{Group}") {
                        currentScreen = "study1/train/phase1"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1IdentiQuiz(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode
                        )
                    }
                    composable("study1/train/phase2/{subject}/{Group}") {
                        currentScreen = "study1/train/phase2"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1FreePlay(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode
                        )
                    }
                    composable("study1/train/phase3/{subject}/{Group}") {
                        currentScreen = "study1/train/phase3"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1TypingQuiz(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            HapticMode.PHONEME
                        )
                    }
                    composable("study1/train/end/{subject}") {
                        currentScreen = "study1/train/end"
                        val subject = it.arguments?.getString("subject")!!
                        Study1TrainEnd(subject, navController)
                    }
                    composable("study1/test/init") {
                        currentScreen = "study1/test/init"
                        Study1TestInit(navController)
                    }
                    composable("study1/test/{subject}/{Group}") {
                        currentScreen = "study1/test"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1Test(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            HapticMode.PHONEME
                        )
                    }
                    composable("study1/test/end/{subject}") {
                        currentScreen = "study1/test/end"
                        val subject = it.arguments?.getString("subject")!!
                        Study1TestEnd(subject, navController)
                    }

                    composable("study2/train/init") {
                        currentScreen = "study2/train/init"
                        Study2TrainInit(navController)
                    }
                    composable("study2/train/{subject}") {
                        currentScreen = "study2/test"
                        val subject = it.arguments?.getString("subject")!!

                        Study2Train(
                            innerPadding,
                            subject,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                        )
                    }
                    composable("study2/train/end/{subject}") {
                        currentScreen = "study2/end"
                        val subject = it.arguments?.getString("subject")!!
                        Study2TrainEnd(subject, navController)
                    }


                    composable("study2/init") {
                        currentScreen = "study2/init"
                        Study2Init(navController)
                    }
                    composable("study2/{subject}/{feedback}/{isPractice}") {
                        currentScreen = "study2/test"
                        val subject = it.arguments?.getString("subject")!!
                        val feedback = it.arguments?.getString("feedback")!!
                        val isPractice = it.arguments?.getString("isPractice")!!.toBoolean()
                        var hapticMode = HapticMode.NONE
                        if (feedback == "audio") hapticMode = HapticMode.VOICE
                        else if(feedback == "phoneme") hapticMode = HapticMode.PHONEME
                        else if(feedback == "audiophoneme") hapticMode = HapticMode.VOICEPHONEME
                        else if(feedback == "vibration") hapticMode = HapticMode.TICK

                        Study2Test(
                            innerPadding,
                            subject,
                            isPractice,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode
                        )
                    }
                    composable("study2/end/{subject}") {
                        currentScreen = "study2/end"
                        val subject = it.arguments?.getString("subject")!!
                        Study2End(subject, navController)
                    }
                    composable("setting") {
                        currentScreen = "setting"
                        SettingScreen()
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
            NavigationDrawerItem(label = { Text("Study 1 Train") },
                selected = selectedItem == "study1/train",
                onClick = {
                    navController.navigate("study1/train/init")
                    selectedItem = "study1/train"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 1 Test") },
                selected = selectedItem == "study1/test",
                onClick = {
                    navController.navigate("study1/test/init")
                    selectedItem = "study1/test"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 2 Train") },
                selected = selectedItem == "study2/train",
                onClick = {
                    navController.navigate("study2/train/init")
                    selectedItem = "study2/train"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 2 Test") },
                selected = selectedItem == "study2/test",
                onClick = {
                    navController.navigate("study2/init")
                    selectedItem = "study2/test"
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
        "study1/train/init" -> "Study 1 Train"
        "study1/train/phase1" -> "Identification Quiz"
        "study1/train/phase2" -> "Free Play"
        "study1/train/phase3" -> "Typing Quiz"
        "study1/test/init" -> "Study 1 Test"
        "study2/train/init" -> "Study 2 Train"
        "study2/init" -> "Study 2 Test"
        "setting" -> "Setting"
        else -> ""
    }

    when (currentScreen) {
        "study1/test" -> CenterAlignedTopAppBar(
            title = {
                Button(
                    onClick = { navController.navigate("study1/test/init") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3B30), contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = "End Test")
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("End Test")
                    }
                }
            }, modifier = Modifier.padding(top = 20.dp)
        )

        "study2/test" -> CenterAlignedTopAppBar(
            title = {
                Button(
                    onClick = { navController.navigate("study2/init") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3B30), contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = "End Test")
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("End Test")
                    }
                }
            }, modifier = Modifier.padding(top = 20.dp)
        )

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