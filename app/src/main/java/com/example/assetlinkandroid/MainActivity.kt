package com.example.assetlinkandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.assetlinkandroid.nav.AppNav
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.theme.AssetlinkAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeepLinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabase: SupabaseClient
    private val appVm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRecoveryIntent(intent)
        supabase.handleDeepLinks(intent)
        enableEdgeToEdge()
        setContent {
            AssetlinkAndroidTheme {
                AppNav()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRecoveryIntent(intent)
        supabase.handleDeepLinks(intent)
    }

    private fun handleRecoveryIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        // Supabase puts type=recovery in the fragment or query when it's a password reset
        val fragment = uri.fragment.orEmpty()
        val query = uri.query.orEmpty()
        if ("type=recovery" in fragment || "type=recovery" in query) {
            appVm.flagPasswordReset()
        }
    }
}