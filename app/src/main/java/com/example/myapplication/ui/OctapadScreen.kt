package com.example.myapplication.ui

import com.example.myapplication.LatencyTracker
import com.example.myapplication.MidiEventBus
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.kit.KitListScreen
import com.example.myapplication.ui.pads.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.ui.drag.DragPadOverlay
import com.example.myapplication.ui.drag.PadActionMenu

// ─── Global Color Tokens ───────────────────────────────────────────────────────
val PadDark     = Color(0xFF2A2A2A)
val PadPressed  = Color(0xFF3A3A3A)
val PanelBg     = Color(0xFF111111)
val LedActive   = Color(0xFF00E5FF)
val LedInactive = Color(0xFF444444)
val BtnBg       = Color(0xFF2A2A2A)
val BtnActive   = Color(0xFF00E5FF)
val LcdBg       = Color(0xFFB8D4E8)
val NavRed      = Color(0xFFC0392B)

// ─── Data Model ───────────────────────────────────────────────────────────────
data class Kit(var name: String)

// ─── Main Screen ──────────────────────────────────────────────────────────────
@Composable
fun OctapadScreen(soundPool: SoundPool, sounds: List<Int>) {



    var dragVisible by remember { mutableStateOf(false) }
    var dragPad by remember { mutableStateOf(-1) }

    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }

    var pad1X by remember { mutableStateOf(0f) }
    var pad1Y by remember { mutableStateOf(0f) }

    var pad2X by remember { mutableStateOf(0f) }
    var pad2Y by remember { mutableStateOf(0f) }

    var pad3X by remember { mutableStateOf(0f) }
    var pad3Y by remember { mutableStateOf(0f) }

    var pad4X by remember { mutableStateOf(0f) }
    var pad4Y by remember { mutableStateOf(0f) }

    var pad5X by remember { mutableStateOf(0f) }
    var pad5Y by remember { mutableStateOf(0f) }

    var pad6X by remember { mutableStateOf(0f) }
    var pad6Y by remember { mutableStateOf(0f) }

    var pad7X by remember { mutableStateOf(0f) }
    var pad7Y by remember { mutableStateOf(0f) }

    var pad8X by remember { mutableStateOf(0f) }
    var pad8Y by remember { mutableStateOf(0f) }

    var showPadMenu by remember { mutableStateOf(false) }

    var sourcePad by remember { mutableStateOf(-1) }
    var targetPad by remember { mutableStateOf(-1) }

    var volume      by remember { mutableStateOf(1f) }
    var pitch       by remember { mutableStateOf(1f) }
    val pressedPads  = remember {
        mutableStateListOf(false, false, false, false, false, false, false, false)
    }

    val kits         = remember { mutableStateListOf(Kit("KIT 001")) }
    var currentKit   by remember { mutableStateOf(0) }
    var showKitList  by remember { mutableStateOf(false) }
    val scope        = rememberCoroutineScope()

    val padSounds = remember {
        sounds.toMutableList()
    }

    fun onPadHit(index: Int) {
        scope.launch {
            pressedPads[index] = true
            val latency =
                (System.nanoTime() -
                        LatencyTracker.midiTime) / 1_000_000.0

            android.util.Log.d(
                "LATENCY",
                "Latency = $latency ms"
            )

            soundPool.play(padSounds[index], volume, volume, 1, 0, pitch)
            delay(100)
            pressedPads[index] = false
        }
    }

    LaunchedEffect(Unit) {

        MidiEventBus.onPadHit = { pad ->

            when (pad) {

                1 -> onPadHit(0)
                2 -> onPadHit(1)
                3 -> onPadHit(2)
                4 -> onPadHit(3)
                5 -> onPadHit(4)
                6 -> onPadHit(5)
                7 -> onPadHit(6)
                8 -> onPadHit(7)
            }
        }
    }

    // Delete kit logic — adjust currentKit so it never goes out of bounds
    fun deleteKit(index: Int) {
        if (kits.size <= 1) return
        kits.removeAt(index)
        currentKit = when {
            index < currentKit         -> currentKit - 1
            currentKit >= kits.size    -> kits.lastIndex
            else                       -> currentKit
        }
    }

    fun swapPads(source: Int, target: Int) {

        val temp = padSounds[source - 1]

        padSounds[source - 1] =
            padSounds[target - 1]

        padSounds[target - 1] =
            temp
    }

    fun detectTargetPad(): Int {

        val threshold = 250f

        return when {

            kotlin.math.abs(dragX - pad1X) < threshold &&
                    kotlin.math.abs(dragY - pad1Y) < threshold -> 1

            kotlin.math.abs(dragX - pad2X) < threshold &&
                    kotlin.math.abs(dragY - pad2Y) < threshold -> 2

            kotlin.math.abs(dragX - pad3X) < threshold &&
                    kotlin.math.abs(dragY - pad3Y) < threshold -> 3

            kotlin.math.abs(dragX - pad4X) < threshold &&
                    kotlin.math.abs(dragY - pad4Y) < threshold -> 4

            kotlin.math.abs(dragX - pad5X) < threshold &&
                    kotlin.math.abs(dragY - pad5Y) < threshold -> 5

            kotlin.math.abs(dragX - pad6X) < threshold &&
                    kotlin.math.abs(dragY - pad6Y) < threshold -> 6

            kotlin.math.abs(dragX - pad7X) < threshold &&
                    kotlin.math.abs(dragY - pad7Y) < threshold -> 7

            kotlin.math.abs(dragX - pad8X) < threshold &&
                    kotlin.math.abs(dragY - pad8Y) < threshold -> 8

            else -> -1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD0D0D0)),
        contentAlignment = Alignment.Center

    )
    {
        // ── Main layout ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE2E2E2))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Left: 8 Drum Pads ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1 — Pad 1..4
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Pad1(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[0],
                            onPress = { onPadHit(0) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 1
                                sourcePad = 1

                                dragX = pad1X
                                dragY = pad1Y

                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },



                            onPadPositionChanged = { x, y ->
                                pad1X = x
                                pad1Y = y
                            }

                        )

                        Pad2(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[1],
                            onPress = { onPadHit(1) },

                            onDragStart = {
                                dragVisible = true

                                dragPad = 2
                                sourcePad = 2
                                dragX = pad2X
                                dragY = pad2Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad2X = x
                                pad2Y = y
                            }
                        )

                        Pad3(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[2],
                            onPress = { onPadHit(2) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 3
                                sourcePad = 3
                                dragX = pad3X
                                dragY = pad3Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad3X = x
                                pad3Y = y
                            }
                        )
                        Pad4(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[3],
                            onPress = { onPadHit(3) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 4
                                sourcePad = 4
                                dragX = pad4X
                                dragY = pad4Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad4X = x
                                pad4Y = y
                            }
                        )


                    }

                    // Row 2 — Pad 5..8
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Pad5(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[4],
                            onPress = { onPadHit(4) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 5
                                sourcePad = 5
                                dragX = pad5X
                                dragY = pad5Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad5X = x
                                pad5Y = y
                            }
                        )

                        Pad6(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[5],
                            onPress = { onPadHit(5) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 6
                                sourcePad = 6
                                dragX = pad6X
                                dragY = pad6Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad6X = x
                                pad6Y = y
                            }
                        )


                        Pad7(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[6],
                            onPress = { onPadHit(6) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 7
                                sourcePad = 7
                                dragX = pad7X
                                dragY = pad7Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad7X = x
                                pad7Y = y
                            }
                        )

                        Pad8(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[7],
                            onPress = { onPadHit(7) },

                            onDragStart = {
                                dragVisible = true
                                dragPad = 8
                                sourcePad = 8
                                dragX = pad8X
                                dragY = pad8Y
                            },

                            onDragMove = { dx, dy ->
                                dragX += dx
                                dragY += dy
                            },

                            onDragEnd = {

                                targetPad = detectTargetPad()

                                dragVisible = false

                                if (
                                    targetPad != -1 &&
                                    targetPad != sourcePad
                                ) {
                                    showPadMenu = true
                                }
                            },

                            onPadPositionChanged = { x, y ->
                                pad8X = x
                                pad8Y = y
                            }
                        )


                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Right: Control Panel ──────────────────────────────────────────
            RightPanel(
                kits          = kits,
                currentKit    = currentKit,
                onKitAdd      = {
                    if (kits.size < 200) {
                        kits.add(Kit("KIT %03d".format(kits.size + 1)))
                        currentKit = kits.lastIndex
                    }
                },
                onKitDelete   = { deleteKit(currentKit) },
                onKitPrev     = { if (currentKit > 0) currentKit-- },
                onKitNext     = { if (currentKit < kits.lastIndex) currentKit++ },
                onOpenKitList = { showKitList = true }
            )
        }

        // ── KitListScreen overlay (shown on top when triggered) ───────────────
        if (showKitList) {
            KitListScreen(
                kits       = kits,
                currentKit = currentKit,
                onSelect   = { index ->
                    currentKit  = index
                    showKitList = false
                },
                onAdd      = {
                    if (kits.size < 200) {
                        kits.add(Kit("KIT %03d".format(kits.size + 1)))
                        currentKit = kits.lastIndex
                    }
                },
                onDelete   = { index -> deleteKit(index) },
                onClose    = { showKitList = false }
            )

        }
        DragPadOverlay(
            visible = dragVisible,
            padNumber = dragPad,
            x = dragX,
            y = dragY
        )

    }
    PadActionMenu(
        visible = showPadMenu,

        onMix = {
            showPadMenu = false
        },

        onAddToEnd = {
            showPadMenu = false
        },

        onSwap = {

            swapPads(
                sourcePad,
                targetPad
            )

            showPadMenu = false
        },

        onCancel = {
            showPadMenu = false
        }
    )
}
