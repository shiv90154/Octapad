// NativeBridge.kt
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

    // NEW: forward Control Change (knob/slider) messages to native side
    external fun sendControlChange(
        channel: Int,
        ccNumber: Int,
        ccValue: Int
    )

    @JvmStatic
    fun onPadHitFromNative(
        pad: Int
    ) {
        MidiEventBus.triggerPad(pad)
    }

    // NEW: native calls this back once a CC message has been processed
    @JvmStatic
    fun onControlChangeFromNative(
        ccNumber: Int,
        ccValue: Int
    ) {
        MidiEventBus.triggerControlChange(ccNumber, ccValue)
    }
}