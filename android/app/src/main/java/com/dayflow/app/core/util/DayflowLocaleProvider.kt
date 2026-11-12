package com.dayflow.app.core.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun DayflowLocaleProvider(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val baseContext = LocalContext.current
    val localeConfiguration = remember(configuration, languageCode) {
        Configuration(configuration).apply {
            setLocale(
                when (languageCode) {
                    "de" -> Locale.GERMAN
                    "es" -> Locale("es")
                    else -> Locale.ENGLISH
                }
            )
        }
    }
    val localizedContext = remember(baseContext, localeConfiguration) {
        baseContext.createConfigurationContext(localeConfiguration)
    }

    // Overriding LocalContext with a configuration-wrapped context breaks
    // androidx.activity's fallback that unwraps LocalContext to find the
    // hosting ActivityResultRegistryOwner (the Activity). Remember the
    // owner from the original context and provide it explicitly, so
    // rememberLauncherForActivityResult keeps working inside localized
    // content (e.g. permission launchers in the Settings screen).
    val registryOwner = baseContext.findActivityResultRegistryOwner()

    if (registryOwner != null) {
        CompositionLocalProvider(
            LocalConfiguration provides localeConfiguration,
            LocalContext provides localizedContext,
            LocalActivityResultRegistryOwner provides registryOwner,
            content = content
        )
    } else {
        CompositionLocalProvider(
            LocalConfiguration provides localeConfiguration,
            LocalContext provides localizedContext,
            content = content
        )
    }
}

private tailrec fun Context.findActivityResultRegistryOwner(): ActivityResultRegistryOwner? {
    if (this is ActivityResultRegistryOwner) return this
    val unwrapped = (this as? ContextWrapper)?.baseContext ?: return null
    return unwrapped.findActivityResultRegistryOwner()
}
