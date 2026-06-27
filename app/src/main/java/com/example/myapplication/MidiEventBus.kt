// MidiEventBus.kt
package com.example.myapplication

object MidiEventBus {

    var onPadHit: ((Int) -> Unit)? = null

    // ── NEW: Control Change callback — ccNumber (0-127), ccValue (0-127) ───────
    var onControlChange: ((Int, Int) -> Unit)? = null

    fun triggerPad(
        pad: Int
    ) {
        onPadHit?.invoke(pad)
    }

    fun triggerControlChange(
        ccNumber: Int,
        ccValue: Int
    ) {
        onControlChange?.invoke(ccNumber, ccValue)
    }
}