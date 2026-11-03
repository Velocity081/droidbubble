package com.droidbubble.app.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.core.content.ContextCompat

/**
 * Transparent activity that requests MediaProjection permission to start screen recording.
 */
class ScreenRecordPromptActivity : Activity() {

    companion object {
        private const val REQUEST_MEDIA_PROJECTION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mpm != null) {
            @Suppress("DEPRECATION")
            startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        } else {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                action = ScreenRecordingService.ACTION_START
                putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenRecordingService.EXTRA_RESULT_DATA, data)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
        }
        finish()
    }
}

