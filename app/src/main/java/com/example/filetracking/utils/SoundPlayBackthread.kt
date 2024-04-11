package com.example.filetracking.utils

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.example.filetracking.R

class SoundPlaybackThread : Thread() {
    private lateinit var handler: Handler
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private  var context:Context?=null

    override fun run() {
        // Initialize the Looper for this thread
        Looper.prepare()

        // Create a Handler associated with this Looper
        handler = Handler()

        // Initialize the SoundPool and load sound resources
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool?.load(context, R.raw.scankey, 1) ?: 0

        // Start the Looper message loop
        Looper.loop()
    }

    fun playSound() {
        // Post a task to the handler to play the sound
        handler.post {
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }

    fun releaseResources() {
        // Release resources when done
        soundPool?.release()
        Looper.myLooper()?.quit()
    }
}
