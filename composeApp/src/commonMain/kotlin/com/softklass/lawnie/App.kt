package com.softklass.lawnie

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedTab by remember { mutableStateOf(LawnieTab.Dashboard) }
        Scaffold(
            topBar = { LawnieTopAppBar(selectedTab) },
            bottomBar = {
                LawnieBottomBar(
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
        ) { padding ->
            LawnieNavHost(
                tab = selectedTab,
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding
            )
        }
    }
}