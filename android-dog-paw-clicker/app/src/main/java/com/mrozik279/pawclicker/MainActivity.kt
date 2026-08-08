package com.mrozik279.pawclicker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mrozik279.pawclicker.ui.PawClickerApp
import com.mrozik279.pawclicker.ui.theme.PawClickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawClickerTheme {
                PawClickerApp()
            }
        }
    }
}
