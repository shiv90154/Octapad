// OctapadScreen.kt
package com.example.myapplication.ui
import com.example.myapplication.LatencyTracker
import com.example.myapplication.MidiEventBus
import android.media.SoundPool
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.audio.AudioListScreen
import com.example.myapplication.ui.audio.AudioRepository
import com.example.myapplication.ui.audio.ExportScreen
import com.example.myapplication.ui.audio.ImportScreen
import com.example.myapplication.ui.kit.KitListScreen
import com.example.myapplication.ui.pads.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.ui.drag.DragPadOverlay
import com.example.myapplication.ui.drag.PadActionMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
inline fun <T> Iterable<T>.anyIndexed(
    predicate: (Int, T) -> Boolean
): Boolean {

    forEachIndexed { index, item ->

        if (predicate(index, item)) {
            return true
        }
    }

    return false
}

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
data class Kit(
    var name: String,

    // NEW: changed from plain MutableList to mutableStateListOf so that
    // mutating volumes[i]/pitches[i] (from MIDI CC or manual knob drag)
    // reliably triggers Compose recomposition — this is what makes the
    // knob actually rotate in real time when a MIDI CC value comes in.
    val volumes: MutableList<Float> =
        mutableStateListOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f),

    val pitches: MutableList<Float> =
        mutableStateListOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f),

    val sounds: MutableList<Int> =
        MutableList(8) { -1 }
)

// ── NEW: MIDI CC numbers for the volume/pitch knobs on your hardware ──────────
// Rotate each hardware knob once, check Logcat tag "MIDI_CC_DEBUG" for the
// "cc=" number it prints, then put the correct numbers here.
const val VOLUME_KNOB_CC = 11   // TODO: replace with your real CC number
const val PITCH_KNOB_CC  = 12   // TODO: replace with your real CC number

// ─── Main Screen ──────────────────────────────────────────────────────────────
@Composable
fun OctapadScreen(soundPool: SoundPool, sounds: List<Int>) {

    var loopEnabled by remember { mutableStateOf(false) }
    var exclusiveMode by remember { mutableStateOf(false) }
    var currentStreamId by remember { mutableStateOf(0) }
    val context = LocalContext.current

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


    val padVolumes = remember {
        mutableStateListOf(
            1f,1f,1f,1f,
            1f,1f,1f,1f
        )
    }

    val padPitches = remember {
        mutableStateListOf(
            1f,1f,1f,1f,
            1f,1f,1f,1f
        )
    }

    var selectedPad by remember {
        mutableStateOf(0)
    }
    val pressedPads  = remember {
        mutableStateListOf(false, false, false, false, false, false, false, false)
    }

    val kits         = remember { mutableStateListOf(Kit("KIT 001")) }
    var currentKit   by remember { mutableStateOf(0) }

    var showKitList  by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newKitName by remember { mutableStateOf("") }

    // ── NEW: Audio screen states ───────────────────────────────────────────────
    var topPanel by remember { mutableStateOf("") }

    // ── NEW: Live waveform/timer tracking for the LCD screen ──────────────────
    var playingPadUri      by remember { mutableStateOf<android.net.Uri?>(null) }
    var playbackPositionMs by remember { mutableStateOf(0L) }
    var playbackDurationMs by remember { mutableStateOf(0L) }
    // Each pad hit gets a unique token; only the MOST RECENT token is allowed
    // to update/clear the waveform — older hits become "background" sounds
    // that keep playing but no longer own the LCD display.
    var latestHitToken      by remember { mutableStateOf(0L) }

    val scope        = rememberCoroutineScope()

    val padSounds = remember {
        sounds.toMutableList()
    }

    // ── NEW: Default pad sound resource IDs (R.raw.pad1..pad8) ─────────────────
    // Used to build a URI + real duration when no custom audio is assigned,
    // so the LCD waveform/timer always has something to show.
    val defaultPadResIds = remember {
        listOf(
            com.example.myapplication.R.raw.pad1,
            com.example.myapplication.R.raw.pad2,
            com.example.myapplication.R.raw.pad3,
            com.example.myapplication.R.raw.pad4,
            com.example.myapplication.R.raw.pad5,
            com.example.myapplication.R.raw.pad6,
            com.example.myapplication.R.raw.pad7,
            com.example.myapplication.R.raw.pad8
        )
    }

    // URI for each default pad sound (android.resource://package/id)
    val defaultPadUris = remember {
        defaultPadResIds.map { resId ->
            Uri.parse("android.resource://${context.packageName}/$resId")
        }
    }

    // Cache durations so we don't re-query MediaMetadataRetriever every tap
    val defaultPadDurations = remember { mutableStateMapOf<Int, Long>() }

    fun getDefaultDuration(index: Int): Long {
        defaultPadDurations[index]?.let { return it }
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, defaultPadUris[index])
            val d = retriever.extractMetadata(
                android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 500L
            defaultPadDurations[index] = d
            d
        } catch (e: Exception) {
            500L
        } finally {
            retriever.release()
        }
    }

    fun onPadHit(index: Int) {
        android.util.Log.d("PADHIT", "onPadHit called for index=$index")
        scope.launch {
            try {
                pressedPads[index] = true
                val latency =
                    (System.nanoTime() -
                            LatencyTracker.midiTime) / 1_000_000.0

                android.util.Log.d(
                    "LATENCY",
                    "Latency = $latency ms"
                )

                // ── Drive the LCD waveform/timer — assigned audio takes priority,
                //    otherwise fall back to this pad's default raw sound ──────────
                val assigned = AudioRepository.audioForPad(
                    currentKit,
                    index
                )
                val uriToShow: Uri
                val durationToShow: Long
                if (exclusiveMode && currentStreamId != 0) {
                    soundPool.stop(currentStreamId)
                }

                if (assigned != null) {
                    uriToShow      = assigned.uri
                    durationToShow = assigned.durationMs
                } else {
                    uriToShow      = defaultPadUris[index]
                    durationToShow = getDefaultDuration(index)
                }

                val myToken = System.nanoTime()
                latestHitToken     = myToken
                playingPadUri      = uriToShow
                playbackDurationMs = durationToShow
                playbackPositionMs = 0L

                if (assigned != null) {
                    // ── Custom audio is assigned — play via a fresh, independent
                    //    MediaPlayer instance each time so multiple hits overlap
                    //    instead of cutting each other off ──────────────────────
                    try {
                        val mp = android.media.MediaPlayer()
                        mp.setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        mp.setDataSource(context, assigned.uri)
                        mp.setOnPreparedListener { player -> player.start() }
                        mp.setOnCompletionListener { player -> player.release() }
                        mp.setOnErrorListener { player, _, _ -> player.release(); true }
                        mp.prepareAsync()
                    } catch (e: Exception) {
                        android.util.Log.e("PADHIT", "Custom audio play failed: ${e.message}")
                        currentStreamId = soundPool.play(
                            padSounds[index],
                            kits[currentKit].volumes[index],
                            kits[currentKit].volumes[index],
                            1,
                            if (loopEnabled) -1 else 0,
                            kits[currentKit].pitches[index]
                        )
                    }
                } else {
                    // ── No custom audio — SoundPool already overlaps correctly
                    //    as long as maxStreams is high enough ───────────────────
                    currentStreamId = soundPool.play(
                        padSounds[index],
                        kits[currentKit].volumes[index],
                        kits[currentKit].volumes[index],
                        1,
                        if (loopEnabled) -1 else 0,
                        kits[currentKit].pitches[index]
                    )
                }

                // Drive a lightweight progress timer while the clip plays —
                // but ONLY if this hit is still the most recent one.
                val startTime = System.currentTimeMillis()
                var stillLatest = true
                while (stillLatest && System.currentTimeMillis() - startTime < durationToShow) {
                    if (latestHitToken != myToken) {
                        stillLatest = false
                    } else {
                        playbackPositionMs = System.currentTimeMillis() - startTime
                        delay(50)
                    }
                }

                if (latestHitToken == myToken) {
                    playbackPositionMs = durationToShow
                    delay(150)
                    if (latestHitToken == myToken) {
                        playingPadUri = null
                        playbackPositionMs = 0L
                        playbackDurationMs = 0L
                    }
                }

                delay(100)
                pressedPads[index] = false

            } catch (e: Exception) {
                android.util.Log.e("PADHIT", "onPadHit FAILED for index=$index: ${e.message}", e)
                pressedPads[index] = false
            }
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

    // ── NEW: MIDI knob (Control Change) → live volume/pitch on selected pad ───
    LaunchedEffect(Unit) {
        MidiEventBus.onControlChange = { ccNumber, ccValue ->
            val normalized = (ccValue / 127f).coerceIn(0f, 1f)

            when (ccNumber) {

                VOLUME_KNOB_CC -> {
                    val newVolume = normalized // knob range is 0f..1f
                    kits[currentKit].volumes[selectedPad] = newVolume

                    // Live update if a SoundPool-based sound is currently playing.
                    // NOTE: custom imported audio (MediaPlayer path) doesn't get
                    // this live update — it picks up the new value on its NEXT hit.
                    if (currentStreamId != 0) {
                        soundPool.setVolume(currentStreamId, newVolume, newVolume)
                    }
                }

                PITCH_KNOB_CC -> {
                    val newPitch = 0.5f + normalized * 1.5f // maps 0f..1f -> 0.5x..2.0x
                    kits[currentKit].pitches[selectedPad] = newPitch

                    if (currentStreamId != 0) {
                        soundPool.setRate(currentStreamId, newPitch)
                    }
                }
            }
        }
    }

    fun generateNextKitName(): String {

        var nextNumber = 1

        while (
            kits.any {
                it.name.equals(
                    "KIT %03d".format(nextNumber),
                    ignoreCase = true
                )
            }
        ) {
            nextNumber++
        }

        return "KIT %03d".format(nextNumber)
    }

    fun deleteKit(index: Int) {

        if (kits.size <= 1) return

        kits.removeAt(index)

        if (currentKit >= kits.size) {
            currentKit = kits.lastIndex
        }
    }




    fun swapPads(source: Int, target: Int) {
        val temp = padSounds[source - 1]
        padSounds[source - 1] = padSounds[target - 1]
        padSounds[target - 1] = temp
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
    ) {
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
                            onPress = {
                                selectedPad = 0
                                onPadHit(0)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad1X = x
                                pad1Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad1 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad1 Stop")
                            }
                        )

                        Pad2(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[1],
                            onPress = {
                                selectedPad = 1
                                onPadHit(1)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad2X = x
                                pad2Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad2 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad2 Stop")
                            }
                        )

                        Pad3(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[2],
                            onPress = {
                                selectedPad = 2
                                onPadHit(2)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad3X = x
                                pad3Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad3 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad3 Stop")
                            }
                        )


                        Pad4(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[3],
                            onPress = {
                                selectedPad = 3
                                onPadHit(3)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad4X = x
                                pad4Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad4 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad4 Stop")
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
                            onPress = {
                                selectedPad = 4
                                onPadHit(4)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad5X = x
                                pad5Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad5 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad5 Stop")
                            }
                        )

                        Pad6(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[5],
                            onPress = {
                                selectedPad = 5
                                onPadHit(5)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad6X = x
                                pad6Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad6 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad6 Stop")
                            }
                        )

                        Pad7(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[6],
                            onPress = {
                                selectedPad = 6
                                onPadHit(6)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad7X = x
                                pad7Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad7 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad7 Stop")
                            }
                        )

                        Pad8(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            pressed = pressedPads[7],
                            onPress = {
                                selectedPad = 7
                                onPadHit(7)
                            },
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
                                if (targetPad != -1 && targetPad != sourcePad) {
                                    showPadMenu = true
                                }
                            },
                            onPadPositionChanged = { x, y ->
                                pad8X = x
                                pad8Y = y
                            },

                            onRecordStart = {
                                android.util.Log.d("REC", "Pad8 Start")
                            },

                            onRecordStop = {
                                android.util.Log.d("REC", "Pad8 Stop")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Right: Control Panel ──────────────────────────────────────────
            RightPanel(
                loopEnabled = loopEnabled,
                exclusiveMode = exclusiveMode,
                onLoopChange = {
                    loopEnabled = it

                    if (!it && currentStreamId != 0) {
                        soundPool.stop(currentStreamId)
                        currentStreamId = 0
                    }
                },
                onExclusiveChange = { exclusiveMode = it },
                selectedPad = selectedPad,
                padVolume = kits[currentKit].volumes[selectedPad],
                padPitch = kits[currentKit].pitches[selectedPad],

                onVolumeChange = {
                    kits[currentKit].volumes[selectedPad] = it
                },

                onPitchChange = {
                    kits[currentKit].pitches[selectedPad] = it
                },
                kits          = kits,
                currentKit    = currentKit,
                onKitAdd = {
                    if (kits.size < 200) {

                        kits.add(
                            Kit(generateNextKitName())
                        )

                        currentKit = kits.lastIndex
                    }
                },
                onKitDelete   = { deleteKit(currentKit) },
                onKitPrev     = { if (currentKit > 0) currentKit-- },
                onKitNext     = { if (currentKit < kits.lastIndex) currentKit++ },
                onOpenKitList = { showKitList = true },
                // ── NEW callbacks ──────────────────────────────────────────────
                onOpenImport = { topPanel = "IMPORT" },
                onOpenAudios = { topPanel = "AUDIOS" },
                onOpenExport = { topPanel = "EXPORT" },

                onRenameKit = {
                    newKitName = kits[currentKit].name
                    showRenameDialog = true
                },
                // ── NEW: live waveform/timer ────────────────────────────────────
                playingPadUri       = playingPadUri,
                playbackPositionMs  = playbackPositionMs,
                playbackDurationMs  = playbackDurationMs
            )
        }

        // ── KitListScreen overlay ─────────────────────────────────────────────
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
                        kits.add(
                            Kit(generateNextKitName())
                        )
                        currentKit = kits.lastIndex
                    }
                },
                onDelete   = { index -> deleteKit(index) },
                onClose    = { showKitList = false }
            )
        }

        if (showRenameDialog) {

            AlertDialog(
                onDismissRequest = {
                    showRenameDialog = false
                },

                title = {
                    androidx.compose.material3.Text("Rename Kit")
                },

                text = {
                    OutlinedTextField(
                        value = newKitName,
                        onValueChange = {
                            newKitName = it
                        },
                        label = {
                            androidx.compose.material3.Text("Kit Name")
                        }
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {

                            if (newKitName.isNotBlank()) {

                                val alreadyExists = kits.anyIndexed { index, kit ->
                                    index != currentKit &&
                                            kit.name.equals(
                                                newKitName.trim(),
                                                ignoreCase = true
                                            )
                                }

                                if (alreadyExists) {

                                    android.widget.Toast.makeText(
                                        context,
                                        "Kit name must be unique",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    return@TextButton
                                }

                                kits[currentKit] = kits[currentKit].copy(
                                    name = newKitName.trim()
                                )
                            }

                            showRenameDialog = false

                            showRenameDialog = false
                        }
                    ) {
                        androidx.compose.material3.Text("SAVE")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            showRenameDialog = false
                        }
                    ) {
                        androidx.compose.material3.Text("CANCEL")
                    }
                }
            )
        }

        // ── NEW: Audio overlay screens ────────────────────────────────────────
        when (topPanel) {

            "IMPORT" -> {
                ImportScreen(
                    onClose = { topPanel = "" }
                )
            }

            "AUDIOS" -> {
                AudioListScreen(
                    currentKit = currentKit,
                    onClose = { topPanel = "" }
                )
            }

            "EXPORT" -> {
                ExportScreen(
                    onClose = { topPanel = "" }
                )
            }
        }

        // ── DragPadOverlay (existing) ─────────────────────────────────────────
        DragPadOverlay(
            visible   = dragVisible,
            padNumber = dragPad,
            x         = dragX,
            y         = dragY
        )
    }

    // ── PadActionMenu (existing — outside Box) ────────────────────────────────
    PadActionMenu(
        visible = showPadMenu,
        onMix = {
            showPadMenu = false
        },
        onAddToEnd = {
            showPadMenu = false
        },
        onSwap = {
            swapPads(sourcePad, targetPad)
            showPadMenu = false
        },
        onCancel = {
            showPadMenu = false
        }
    )
}