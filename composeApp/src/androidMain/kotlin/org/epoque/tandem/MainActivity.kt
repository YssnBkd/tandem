package org.epoque.tandem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.epoque.tandem.ui.navigation.TandemNavHost
import org.epoque.tandem.ui.theme.TandemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TandemTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TandemNavHost()
                }
            }
        }
    }
}
