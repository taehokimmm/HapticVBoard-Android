package com.taehokimmm.hapticvboard_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val soundManager = SoundManager(this)
        setContent {
            MainScreen(soundManager = soundManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(soundManager: SoundManager) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController = navController, onItemClicked = {
                scope.launch { drawerState.close() }
            })
        },
        gesturesEnabled = false,
    ) {
        Scaffold(
            topBar = {
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
            },
            content = { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "freetype",
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable("freetype") { FreeTypeMode(soundManager = soundManager) }
                    composable("test") { TestInit(soundManager = soundManager) }
                }
            },
        )
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onItemClicked: () -> Unit) {
    var selectedItem by rememberSaveable { mutableStateOf("freetype") }

    Box(
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        ModalDrawerSheet {
            Text(
                "HapticVBoard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            NavigationDrawerItem(label = { Text("Free Type") }, selected = selectedItem == "freetype", onClick = {
                navController.navigate("freetype")
                selectedItem = "freetype"
                onItemClicked()
            })
            NavigationDrawerItem(label = { Text("Test") }, selected = selectedItem == "test", onClick = {
                navController.navigate("test")
                selectedItem = "test"
                onItemClicked()
            })
        }
    }
}