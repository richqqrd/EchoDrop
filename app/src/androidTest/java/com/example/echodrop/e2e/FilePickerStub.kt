package com.example.echodrop.e2e

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.Intents.intending

object FilePickerStub {
    fun register() {
        val dummyUri = Uri.parse("content://echodrop/dummy.txt")
        val result   = Intent().apply { putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(dummyUri)) }
        val res = Instrumentation.ActivityResult(Activity.RESULT_OK, result)
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(res)
    }

    fun unregister() {
    }
} 