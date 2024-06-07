@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun App(isBrowser: Boolean = false) {
    MaterialTheme {
        Navigator(HomeScreen(isBrowser)) { navigator ->
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Connectivity Demo") },
                        navigationIcon = {
                            if (navigator.canPop) {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                    )
                                }
                            }
                        },
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    CurrentScreen()
                }
            }
        }
    }
}