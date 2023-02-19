package com.eywa.projectclava.main.common

import android.content.Context
import android.media.MediaPlayer
import com.eywa.projectclava.R

interface ClavaMediaPlayer {
    fun playMatchFinishedSound()
}

class ClavaMediaPlayerImpl(context: Context) : ClavaMediaPlayer {
    private val player = MediaPlayer.create(context, R.raw.notification_sound)

    override fun playMatchFinishedSound() = player.start()
}
