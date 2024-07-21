package com.taehokimmm.hapticvboard_android

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
        val serialManager = SerialManager(this)
        setContent {
            HapticVBoardAndroidTheme {
                MainScreen(soundManager, serialManager)
            }
        }
    }
}

enum class HapticMode {
    VOICE, SERIAL, NONE
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(soundManager: SoundManager?, serialManager: SerialManager?) {
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
                    serialManager?.connect()
                    if (serialManager?.isOpen() == true) hapticMode = HapticMode.SERIAL
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
            content = {
                NavHost(
                    navController = navController,
                    startDestination = "freeType",
                ) {
                    composable("freeType") {
                        currentScreen = "freeType"
                        FreeTypeMode(soundManager, serialManager, hapticMode)
                    }
                    composable("train") {
                        currentScreen = "train"
                        TrainMode(soundManager, serialManager, hapticMode)
                    }
                    composable("hapticTest") {
                        currentScreen = "hapticTest"
                        HapticTest(soundManager)
                    }
                    composable("testInit") {
                        currentScreen = "testInit"
                        TestInit(navController)
                    }
                    composable("test2Init") {
                        currentScreen = "test2Init"
                        Test2Init(navController)
                    }
                    composable("test/{subject}/{questions}") { backStackEntry ->
                        val subject = backStackEntry.arguments?.getString("subject")
                        val questions = backStackEntry.arguments?.getString("questions")?.toInt()
                        if (subject != null && questions != null) {
                            currentScreen = "test"
                            TestMode(
                                subject,
                                questions,
                                navController,
                                soundManager,
                                serialManager,
                                hapticMode
                            )
                        }
                    }
                    composable("test2/{subject}/{questions}") { backStackEntry ->
                        val subject = backStackEntry.arguments?.getString("subject")
                        val questions = backStackEntry.arguments?.getString("questions")?.toInt()
                        if (subject != null && questions != null) {
                            currentScreen = "test2"
                            Test2Mode(
                                subject,
                                questions,
                                navController,
                                soundManager,
                                serialManager,
                                hapticMode
                            )
                        }
                    }
                    composable("testEnd") {
                        currentScreen = "testEnd"
                        TestEnd(navController)
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
            NavigationDrawerItem(label = { Text("Train") },
                selected = selectedItem == "train",
                onClick = {
                    navController.navigate("train")
                    selectedItem = "train"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 1") },
                selected = selectedItem == "hapticTest",
                onClick = {
                    navController.navigate("hapticTest")
                    selectedItem = "hapticTest"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 2") },
                selected = selectedItem == "test2Init",
                onClick = {
                    navController.navigate("test2Init")
                    selectedItem = "test2Init"
                    onItemClicked()
                })
            NavigationDrawerItem(label = { Text("Study 3") },
                selected = selectedItem == "testInit",
                onClick = {
                    navController.navigate("testInit")
                    selectedItem = "testInit"
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
        "train" -> "Train"
        "hapticTest" -> "Study 1"
        "testInit" -> "Study 3"
        "test2Init" -> "Study 2"
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

        else -> TopAppBar(title = { Text(text = displayText) }, navigationIcon = {
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