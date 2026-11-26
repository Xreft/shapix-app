package com.xreft.shapix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ads.init(this)
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = TEAL, onPrimary = Color(0xFF041C29), background = BG, surface = PANEL
                )
            ) {
                var code by rememberSaveable { mutableStateOf(defaultLang().code) }
                val lang = Lang.values().first { it.code == code }
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    Surface(Modifier.fillMaxSize(), color = BG, contentColor = TEXT) {
                        EditorScreen(lang) { code = it.code }
                    }
                }
            }
        }
    }
}

private fun defaultLang() = when (Locale.getDefault().language) {
    "ar" -> Lang.Ar
    "fa" -> Lang.Fa
    else -> Lang.En
}
