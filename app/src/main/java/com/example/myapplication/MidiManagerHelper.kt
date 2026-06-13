package com.example.myapplication

import android.content.Context
import android.media.midi.*
import android.os.Handler
import android.os.Looper
import android.util.Log

class MidiManagerHelper(
    private val context: Context
) {

    fun listDevices() {

        val midiManager =
            context.getSystemService(
                MidiManager::class.java
            )

        val devices =
            midiManager.devices

        Log.d(
            "MIDI_TEST",
            "Devices Found = ${devices.size}"
        )

        if (devices.isEmpty()) {
            Log.d(
                "MIDI_TEST",
                "No MIDI Device Found"
            )
            return
        }

        for (deviceInfo in devices) {

            Log.d(
                "MIDI_TEST",
                "Device = ${deviceInfo.properties}"
            )

            midiManager.openDevice(
                deviceInfo,
                { device ->

                    Log.d(
                        "MIDI_TEST",
                        "Device Opened"
                    )

                    val outputPort =
                        device.openOutputPort(0)

                    if (outputPort != null) {

                        outputPort.connect(
                            MidiReceiverHandler()
                        )

                        Log.d(
                            "MIDI_TEST",
                            "Receiver Connected"
                        )
                    }

                },
                Handler(
                    Looper.getMainLooper()
                )
            )
        }
    }
}