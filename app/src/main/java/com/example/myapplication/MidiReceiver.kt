package com.example.myapplication

import android.media.midi.MidiReceiver
import android.util.Log

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

                LatencyTracker.midiTime =
                    System.nanoTime()



                Log.d(
                    "MIDI_NOTE",
                    "NOTE ON : note=$note velocity=$velocity"
                )

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
        }
    }
}