package org.epoque.tandem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.epoque.tandem.ui.navigation.TandemNavHost
import org.epoque.tandem.ui.theme.TandemTheme

class MainActivity : ComponentActivity() {

    private var pendingInviteCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Handle deep link from initial launch
        handleDeepLink(intent)

        setContent {
            TandemTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TandemNavHost(
                        pendingInviteCode = pendingInviteCode,
                        onInviteCodeConsumed = { pendingInviteCode = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already running (singleTask)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return

        extractInviteCode(uri)?.let { code ->
            pendingInviteCode = code
        }
    }

    /**
     * Extracts invite code from deep link URI.
     * Supports:
     * - https://tandem.app/invite/{code}
     * - tandem://invite/{code}
     */
    private fun extractInviteCode(uri: Uri): String? {
        return when {
            // https://tandem.app/invite/{code}
            uri.host == "tandem.app" && uri.pathSegments.firstOrNull() == "invite" -> {
                uri.pathSegments.getOrNull(1)
            }
            // tandem://invite/{code}
            uri.scheme == "tandem" && uri.host == "invite" -> {
                uri.pathSegments.firstOrNull()
            }
            else -> null
        }
    }
}
