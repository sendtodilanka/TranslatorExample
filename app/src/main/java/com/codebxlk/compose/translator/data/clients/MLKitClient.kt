package com.codebxlk.compose.translator.data.clients

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.LruCache
import com.codebxlk.compose.translator.MainActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MLKitClient(application: Application) : Application.ActivityLifecycleCallbacks {

    private val languageIdentifier by lazy { LanguageIdentification.getClient() }
    private val remoteModelManager by lazy { RemoteModelManager.getInstance() }
    private val translator by lazy {
        object : LruCache<TranslatorOptions, Translator>(3) {
            override fun create(options: TranslatorOptions): Translator {
                return Translation.getClient(options)
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions,
                oldValue: Translator,
                newValue: Translator?,
            ) {
                oldValue.close()
            }
        }
    }


    val offlineLanguages: List<String> by lazy {
        TranslateLanguage.getAllLanguages()
    }

    suspend fun getDownloadedModelIds(): Set<String> {
        return try {
            remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .await()
                .map { it.language }
                .toSet()
        } catch (exception: Exception) {
            emptySet()
        }
    }

    fun downloadModel(
        languageId: String,
        isWiFiRequired: Boolean,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    ) {
        val model = TranslateRemoteModel.Builder(languageId).build()

        val conditions = DownloadConditions.Builder()
        if (isWiFiRequired) conditions.requireWifi()

        remoteModelManager.download(model, conditions.build())
            .addOnSuccessListener(onSuccessListener)
            .addOnFailureListener(onFailureListener)
    }

    fun removeLanguageModel(
        languageId: String,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    ) {
        val model = TranslateRemoteModel.Builder(languageId).build()

        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    remoteModelManager.deleteDownloadedModel(model)
                        .addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(onFailureListener)
                } else {
                    // Handle the case when the model is not downloaded
                    onFailureListener.onFailure(
                        Exception("Model with ID $languageId is not downloaded.")
                    )
                }
            }.addOnFailureListener(onFailureListener)
    }

    /** ActivityLifecycleCallbacks */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (activity is MainActivity) {
            runCatching {
                languageIdentifier.close()
                translator.evictAll()
                Timber.e("MLKit cleared when MainActivity on destroyed")
            }.onFailure {
                Timber.e(it, "Error while clearing MLKit resources")
            }
        }
    }
}