package io.github.jamesmyatt.randomatizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.jamesmyatt.randomatizer.ui.RollerRoute
import io.github.jamesmyatt.randomatizer.ui.theme.RandomatizerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomatizerTheme {
                RollerRoute()
            }
        }
    }
}
