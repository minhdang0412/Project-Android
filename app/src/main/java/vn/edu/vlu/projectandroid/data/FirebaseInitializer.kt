package vn.edu.vlu.projectandroid.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"
    const val API_KEY = "AIzaSyBIn7NxwWYulPMBz3MvnrGfocInnWfqO3Y"
    const val APP_ID = "1:960420282449:android:096bc95449f92acab7b870"
    const val PROJECT_ID = "project-295e7"
    const val STORAGE_BUCKET = "project-295e7.firebasestorage.app"
    const val GCM_SENDER_ID = "960420282449"

    fun init(context: Context) {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(API_KEY)
                    .setApplicationId(APP_ID)
                    .setProjectId(PROJECT_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .setGcmSenderId(GCM_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "Firebase initialized with explicit options")
            } else {
                val currentApp = FirebaseApp.getInstance()
                val currentKey = currentApp.options.apiKey
                if (currentKey.isBlank() || currentKey.contains("Dummy", ignoreCase = true)) {
                    Log.w(TAG, "Current API key invalid ($currentKey), reinitializing...")
                    currentApp.delete()
                    val options = FirebaseOptions.Builder()
                        .setApiKey(API_KEY)
                        .setApplicationId(APP_ID)
                        .setProjectId(PROJECT_ID)
                        .setStorageBucket(STORAGE_BUCKET)
                        .setGcmSenderId(GCM_SENDER_ID)
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.d(TAG, "Firebase reinitialized successfully with valid key")
                } else {
                    Log.d(TAG, "Firebase already initialized with key: ${currentKey.take(8)}...")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }
}
