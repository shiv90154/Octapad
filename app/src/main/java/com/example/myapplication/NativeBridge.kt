package com.example.myapplication

object NativeBridge {

    init {
        System.loadLibrary("myapplication")
    }

    external fun sendMidiMessage(
        channel: Int,
        note: Int,
        velocity: Int
    )

    @JvmStatic
    fun onPadHitFromNative(
        pad: Int
    ) {
        MidiEventBus.triggerPad(pad)
    }
}