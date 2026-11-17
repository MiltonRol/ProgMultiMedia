package com.example.diariiviatges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.diariiviatges.navigation.AppNavigation
import com.example.diariiviatges.ui.theme.DiariViatgesTheme

// FIX: quitada @ExperimentalMaterial3Api de la Activity: las pantallas ya hacen @OptIn,
// así que la Activity no necesita declararse "experimental".
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiariViatgesTheme {
                AppNavigation()
            }
        }
    }
}
