package com.example.myapplication

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.ui.OctapadScreen

class MainActivity : ComponentActivity() {

    private lateinit var soundPool: SoundPool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // maxStreams raised so many pads can overlap without cutting each other off
        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attributes)
            .build()

        val sounds = listOf(
            soundPool.load(this, R.raw.pad1, 1),
            soundPool.load(this, R.raw.pad2, 1),
            soundPool.load(this, R.raw.pad3, 1),
            soundPool.load(this, R.raw.pad4, 1),
            soundPool.load(this, R.raw.pad5, 1),
            soundPool.load(this, R.raw.pad6, 1),
            soundPool.load(this, R.raw.pad7, 1),
            soundPool.load(this, R.raw.pad8, 1)
        )

        // ── Restore MIDI device connection — this was missing, which is why
        //    no MIDI_TEST logs appeared and the device was never connected ──
        MidiManagerHelper(this).listDevices()

        setContent { OctapadScreen(soundPool, sounds) }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}