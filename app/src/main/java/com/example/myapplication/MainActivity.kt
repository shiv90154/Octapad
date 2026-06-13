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
        android.util.Log.d("MIDI_TEST", "APP STARTED")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        val midiHelper =
            MidiManagerHelper(this)

        midiHelper.listDevices()

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



        setContent { OctapadScreen(soundPool, sounds) }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}