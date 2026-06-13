package com.example.myapplication

object MidiEventBus {

    var onPadHit: ((Int) -> Unit)? = null

    fun triggerPad(
        pad: Int
    ) {
        onPadHit?.invoke(pad)
    }
}