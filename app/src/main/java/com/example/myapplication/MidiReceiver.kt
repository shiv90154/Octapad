// MidiReceiverHandler.kt
package com.example.myapplication

import android.media.midi.MidiReceiver
import android.util.Log
import com.example.myapplication.MidiEventBus
class MidiReceiverHandler : MidiReceiver() {

    override fun onSend(
        data: ByteArray,
        offset: Int,
        count: Int,
        timestamp: Long
    ) {

        if (count < 3) return

        val status =
            data[offset].toInt() and 0xFF

        val note =
            data[offset + 1].toInt() and 0xFF

        val velocity =
            data[offset + 2].toInt() and 0xFF

        val command =
            status and 0xF0

        val channel =
            (status and 0x0F) + 1

        when (command) {

            0x90 -> {
                Log.d("MIDI_DEBUG", "MIDI RECEIVED note=$note")

                LatencyTracker.midiTime =
                    System.nanoTime()

                Log.d(
                    "MIDI_NOTE",
                    "NOTE ON : note=$note velocity=$velocity"
                )

                when(note) {
                    27 -> MidiEventBus.onPadHit?.invoke(1)
                    22 -> MidiEventBus.onPadHit?.invoke(2)
                    21 -> MidiEventBus.onPadHit?.invoke(3)
                    20 -> MidiEventBus.onPadHit?.invoke(4)
                    23 -> MidiEventBus.onPadHit?.invoke(5)
                    24 -> MidiEventBus.onPadHit?.invoke(6)
                    25 -> MidiEventBus.onPadHit?.invoke(7)
                    26 -> MidiEventBus.onPadHit?.invoke(8)
                }

                NativeBridge.sendMidiMessage(
                    channel,
                    note,
                    velocity
                )
            }

            0x80 -> {

                Log.d(
                    "MIDI_NOTE",
                    "NOTE OFF : note=$note"
                )

                NativeBridge.sendMidiMessage(
                    channel,
                    note,
                    0
                )
            }

            // ── NEW: Control Change (knobs / sliders) — now routed through C++ ──
            0xB0 -> {

                val ccNumber = note      // data[offset+1] = controller number
                val ccValue  = velocity  // data[offset+2] = controller value (0-127)

                // Quick raw confirmation log — safe to remove later once confirmed working
                Log.d(
                    "MIDI_CC_DEBUG",
                    "CONTROL CHANGE (raw) : cc=$ccNumber value=$ccValue channel=$channel"
                )

                NativeBridge.sendControlChange(
                    channel,
                    ccNumber,
                    ccValue
                )
            }
        }
    }
}