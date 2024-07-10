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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taehokimmm.hapticvboard_android.ui.theme.HapticVBoardAndroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val soundManager = SoundManager(this)
        setContent {
            HapticVBoardAndroidTheme {
                MainScreen(soundManager = soundManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(soundManager: SoundManager) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var showTopAppBar by rememberSaveable { mutableStateOf(true) }

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
            topBar = {
                if (showTopAppBar) {
                    TopAppBar(title = { }, navigationIcon = {
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
                } else {
                    CenterAlignedTopAppBar(
                        title = {
                            Button(
                                onClick = { navController.navigate("testInit") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF3B30),
                                    contentColor = Color.White
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
                }
            },
            content = {
                NavHost(
                    navController = navController,
                    startDestination = "freeType",
                ) {
                    composable("freeType") {
                        showTopAppBar = true
                        FreeTypeMode(soundManager)
                    }
                    composable("train") {
                        showTopAppBar = true
                        TrainMode( soundManager)
                    }
                    composable("hapticTest") {
                        showTopAppBar = true
                        HapticTest(soundManager)
                    }
                    composable("testInit") {
                        showTopAppBar = true
                        TestInit(soundManager, navController)
                    }
                    composable("test/{subject}/{questions}") { backStackEntry ->
                        val subject = backStackEntry.arguments?.getString("subject")
                        val questions = backStackEntry.arguments?.getString("questions")?.toInt()
                        if (subject != null && questions != null) {
                            showTopAppBar = false
                            TestMode(subject, questions, navController, soundManager)
                        }
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
                selected = selectedItem == "testInit",
                onClick = {
                    navController.navigate("testInit")
                    selectedItem = "testInit"
                    onItemClicked()
                })
        }
    }
}