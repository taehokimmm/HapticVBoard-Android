package com.taehokimmm.hapticvboard_android

import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    VOICE, SERIAL, NONE
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
                    if (hapticManager?.isOpen() == true) hapticMode = HapticMode.SERIAL
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
                    composable("freeType2") {
                        currentScreen = "freeType2"
                        FreeTypeWithGroup(innerPadding, soundManager, hapticManager, hapticMode)
                    }
                    composable("study1/train/init") {
                        currentScreen = "study1/train/init"
                        Study1TrainInit(navController)
                    }
                    composable("study1/train/phase1/{subject}/{Group}") {
                        currentScreen = "study1/train/phase1"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1TrainPhase1(
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
                        Study1TrainPhase2(
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
                        currentScreen = "study3/train"
                        val subject = it.arguments?.getString("subject")!!
                        val group = it.arguments?.getString("Group")!!
                        Study1TrainPhase3(
                            innerPadding,
                            subject,
                            group,
                            navController,
                            soundManager!!,
                            hapticManager!!,
                            hapticMode
                        )
                    }
                    composable("study1/train/end/{subject}") {
                        currentScreen = "study1/train"
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
                            hapticMode
                        )
                    }
                    composable("study1/test/end/{subject}") {
                        currentScreen = "study1/test"
                        val subject = it.arguments?.getString("subject")!!
                        Study1TestEnd(subject, navController)
                    }
                    composable("testInit") {
                        currentScreen = "testInit"
                        TestInit(navController)
                    }
                    composable("test2Init") {
                        currentScreen = "test2Init"
                        Test2Init(navController)
                    }
                    composable("testEnd") {
                        currentScreen = "testEnd"
                        TestEnd(navController)
                    }
//                    composable("serial") {
//                        currentScreen = "serial"
//                        SerialMonitorScreen()
//                    }
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
            NavigationDrawerItem(label = { Text("Free Type 2") },
                selected = selectedItem == "freeType2",
                onClick = {
                    navController.navigate("freeType2")
                    selectedItem = "freeType2"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Train") },
                selected = selectedItem == "train",
                onClick = {
                    navController.navigate("train")
                    selectedItem = "train"
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
                    navController.navigate("study1/test")
                    selectedItem = "study1/test"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 2 Train") },
                selected = selectedItem == "study2/train",
                onClick = {
                    navController.navigate("study2/train")
                    selectedItem = "study2/train"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 2 Test") },
                selected = selectedItem == "study2/test",
                onClick = {
                    navController.navigate("study2/test")
                    selectedItem = "study2/test"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Serial Monitor") },
                selected = selectedItem == "serial",
                onClick = {
                    navController.navigate("serial")
                    selectedItem = "serial"
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
        "freeType2" -> "Free Type with Group"
        "train" -> "Train"
        "study1/train" -> "Study 1 Train"
        "study1/train/phase1" -> "Phase 1 — Free Play"
        "study1/train/phase2" -> "Phase 2 — Identification Test"
        "study1/train/phase3" -> "Phase 3 — Typing Test"
        "study1/test" -> "Study 1 Test"
        "study2/train" -> "Study 2 Train"
        "study2/test" -> "Study 2 Test"
        "serial" -> "Serial Monitor"
        else -> ""
    }

    when (currentScreen) {
        "test" -> CenterAlignedTopAppBar(
            title = {
                Button(
                    onClick = { navController.navigate("testInit") },
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
            },
        )

        "test2" -> CenterAlignedTopAppBar(
            title = {
                Button(
                    onClick = { navController.navigate("test2Init") },
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
            },
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

@Preview
@Composable
fun DefaultPreview() {
    HapticVBoardAndroidTheme {
        MainScreen(null, null)
    }
}