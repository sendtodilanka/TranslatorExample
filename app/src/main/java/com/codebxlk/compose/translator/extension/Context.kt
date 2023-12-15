package com.codebxlk.compose.translator.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.codebxlk.compose.translator.R
import timber.log.Timber

fun Context.toast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}

private const val TTS_PACKAGE_NAME = "com.svox.langpack.installer"
private const val PLAY_STORE_URL = "market://search?q=pname:$TTS_PACKAGE_NAME"

fun Context.openTTSVoiceData(onError: () -> Unit) {
    val resolveActivity = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
        .resolveActivity(packageManager)

    val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)

    if (resolveActivity != null) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error starting TTS data installation intent")
            onError()
        }
    } else {
        Timber.e("Play Store not available, opening browser URL")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)))
        } catch (e: Exception) {
            Timber.e(e, "Error opening browser URL")
            onError()
        }
    }
}

fun Context.openTTSSettings(onError: () -> Unit) {
    val intent = Intent("com.android.settings.TTS_SETTINGS")

    try {
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e.localizedMessage)
        onError()
    }
}

fun Context.openAppInPlayStore(packageName: String, onError: () -> Unit) {
    val url = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    val intent = Intent(Intent.ACTION_VIEW, url).apply {
        setPackage("com.android.vending")
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        openPlayStoreWeb(url) {
            onError()
        }
    }
}

fun Context.openPlayStoreWeb(uri: Uri, onError: () -> Unit) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        onError()
    }
}