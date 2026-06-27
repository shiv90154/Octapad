package com.example.myapplication.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.audio.WaveformGenerator
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun RightPanel(
    selectedPad: Int,
    padVolume: Float,
    padPitch: Float,
    onVolumeChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    kits: List<Kit>,
    currentKit: Int,
    onKitAdd: () -> Unit,
    onKitDelete: () -> Unit,
    onKitPrev: () -> Unit,
    onKitNext: () -> Unit,
    onOpenKitList: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenAudios: () -> Unit,
    onOpenExport: () -> Unit,
    onRenameKit: () -> Unit,
    playingPadUri: Uri? = null,
    playbackPositionMs: Long = 0L,
    playbackDurationMs: Long = 0L,
    // ── NEW: EQ panel state ───────────────────────────────────────────────────
    loopEnabled: Boolean = false,
    exclusiveMode: Boolean = false,
    onLoopChange: (Boolean) -> Unit = {},
    onExclusiveChange: (Boolean) -> Unit = {}
) {
    var activeBtn by remember { mutableStateOf("") }

    var showEqPanel  by remember { mutableStateOf(false) }
    var showMusicPanel by remember { mutableStateOf(false) }

    // Wrap everything in a Row so EQPanel slides in to the LEFT of RightPanel
    Row(modifier = Modifier.fillMaxHeight()) {

        // ── EQ Panel — animates in from the left side ─────────────────────────
        AnimatedVisibility(
            visible = showEqPanel,
            enter   = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(220)) + fadeIn(tween(220)),
            exit    = slideOutHorizontally(targetOffsetX  = { -it }, animationSpec = tween(180)) + fadeOut(tween(180))
        ) {
            EQPanel(
                visible           = showEqPanel,
                loopEnabled       = loopEnabled,
                exclusiveMode     = exclusiveMode,
                onLoopChange      = onLoopChange,
                onExclusiveChange = onExclusiveChange,
                onClose           = { showEqPanel = false }
            )
        }

        AnimatedVisibility(
            visible = showMusicPanel,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(220)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(180)
            ) + fadeOut()
        ) {

            MusicPanel(
                onImport = onOpenImport,
                onAudios = onOpenAudios,
                onExport = onOpenExport,
                onRename = onRenameKit,
                onClose = {
                    showMusicPanel = false
                }
            )
        }

        // ── Main right column (unchanged width, same layout) ──────────────────
        Column(
            modifier = Modifier
                .width(190.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
//                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ── PATCH LIST / EDITxx PADS / REC ──────────────────────────────────
            CtrlBtnRow(
                labels   = listOf("PATCH LIST"),
                active   = activeBtn,
                onSelect = { label ->
                    activeBtn = label
                    if (label == "PATCH LIST") onOpenKitList()
                }
            )

            // ── EQ / MIDI / MUSIC ─────────────────────────────────────────────
            // EQ button: toggle EQPanel; other buttons: close EQPanel
            CtrlBtnRow(
                labels = listOf("EQ", "MIDI", "SETTINGS"),
                active = activeBtn,
                onSelect = { label ->
                    activeBtn = label

                    when(label){

                        "EQ" ->{
                            showEqPanel = !showEqPanel
                            showMusicPanel = false
                        }

                        "SETTINGS" ->{
                            showMusicPanel = !showMusicPanel
                            showEqPanel = false
                        }

                        else->{
                            showEqPanel = false
                            showMusicPanel = false
                        }
                    }
                }
            )


            // ── Pad volume / pitch knobs ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PAD ${selectedPad + 1}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))


                    Spacer(Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "VOL",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${(padVolume * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 8.sp
                            )

                            Spacer(Modifier.height(2.dp))

                            SmallKnob(
                                title = "",
                                value = padVolume,
                                onValueChange = onVolumeChange,
                                min = 0f,
                                max = 1f
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "PITCH",
                                color = Color(0xFFFFB74D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${"%.2f".format(padPitch)}x",
                                color = Color.White,
                                fontSize = 8.sp
                            )

                            Spacer(Modifier.height(2.dp))

                            SmallKnob(
                                title = "",
                                value = padPitch,
                                onValueChange = onPitchChange,
                                min = 0.5f,
                                max = 2f
                            )
                        }
                    }
                }
            }



            // ── LCD Screen — real waveform + live timer ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LcdBg)
                    .padding(6.dp)
            ) {
                Column {
                    Text(
                        text       = kits[currentKit].name,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF001A33),
                        modifier   = Modifier.fillMaxWidth(),
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    WaveformDisplay(
                        audioUri = playingPadUri,
                        progress = if (playbackDurationMs > 0)
                            (playbackPositionMs.toFloat() / playbackDurationMs.toFloat()).coerceIn(0f, 1f)
                        else 0f
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text      = "${playbackPositionMs.toTimerStr()} / ${
                            if (playbackDurationMs > 0L) playbackDurationMs.toTimerStr() else "--:--"
                        }",
                        color     = Color(0xFF001A33),
                        fontSize  = 8.sp,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Navigation Row  < PATCH > ──────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NavRed)
                        .pointerInput(Unit) { detectTapGestures { onKitPrev() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text("<", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("PATCH", color = Color(0xFF888888), fontSize = 8.sp, letterSpacing = 1.sp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NavRed)
                        .pointerInput(Unit) { detectTapGestures { onKitNext() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(">", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Real waveform composable ──────────────────────────────────────────────────

@Composable
private fun WaveformDisplay(
    audioUri: Uri?,
    progress: Float
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var amplitudes by remember(audioUri) { mutableStateOf<List<Float>>(emptyList()) }

    LaunchedEffect(audioUri) {
        if (audioUri != null) {
            scope.launch {
                amplitudes = WaveformGenerator.extractAmplitudes(context, audioUri, barCount = 90)
            }
        } else {
            amplitudes = emptyList()
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val midY = size.height / 2f
        if (amplitudes.isNotEmpty()) {
            val barCount  = amplitudes.size
            val barWidth  = (size.width / barCount) * 0.55f
            val gap       = (size.width / barCount) * 0.45f
            val activeIdx = (progress * barCount).toInt()
            amplitudes.forEachIndexed { i, amp ->
                val barHeight = (amp * size.height * 0.92f).coerceAtLeast(1.5f)
                val x = i * (barWidth + gap)
                drawRoundRect(
                    color        = if (i <= activeIdx) Color(0xFF001A33) else Color(0xFF001A33).copy(alpha = 0.3f),
                    topLeft      = Offset(x, midY - barHeight / 2f),
                    size         = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(1f, 1f)
                )
            }
            val seekX = (progress * size.width).coerceIn(0f, size.width)
            drawLine(color = Color(0xFFD9534F), start = Offset(seekX, 0f), end = Offset(seekX, size.height), strokeWidth = 1.5f)
        } else {
            drawLine(color = Color(0xFF001A33).copy(alpha = 0.25f), start = Offset(0f, midY), end = Offset(size.width, midY), strokeWidth = 1.5f)
        }
    }
}

// ── Timer formatting ──────────────────────────────────────────────────────────

private fun Long.toTimerStr(): String {
    val totalSec = this / 1000
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}

// ── Menu option ───────────────────────────────────────────────────────────────

@Composable
private fun MenuOption(icon: String, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures { onClick() } }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
fun CtrlBtnRow(labels: List<String>, active: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        labels.forEach { label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (label == active) BtnActive else BtnBg)
                    .pointerInput(Unit) { detectTapGestures { onSelect(label) } }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color      = if (label == active) Color.Black else Color(0xFFCCCCCC),
                    fontSize   = 7.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LcdRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 8.sp, color = Color(0xFF001A33))
        Text(value, fontSize = 8.sp, color = Color(0xFF001A33))
    }
}

// ── SmallKnob ─────────────────────────────────────────────────────────────────

@Composable
fun SmallKnob(title: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = Color.White, fontSize = 8.sp)
        Text(text = "%.2f".format(value), color = Color.Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)

        val targetAngle = ((value - min) / (max - min)) * 270f - 135f
        val animatedAngle by androidx.compose.animation.core.animateFloatAsState(
            targetValue   = targetAngle,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
            label         = "knobAngle"
        )

        var dragStartAngle by remember {
            mutableStateOf(0f)
        }

        var dragStartValue by remember(value) {
            mutableStateOf(value)
        }

        var isDragging     by remember { mutableStateOf(false) }
        var canvasSizePx   by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

        val displayAngle = if (isDragging) targetAngle else animatedAngle

        Canvas(
            modifier = Modifier
                .size(46.dp)
                .onSizeChanged { intSize ->
                    canvasSizePx = androidx.compose.ui.geometry.Size(intSize.width.toFloat(), intSize.height.toFloat())
                }
                .pointerInput(min, max) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val center = Offset(canvasSizePx.width / 2f, canvasSizePx.height / 2f)
                            dragStartAngle = Math.toDegrees(kotlin.math.atan2((offset.x - center.x).toDouble(), -(offset.y - center.y).toDouble())).toFloat()
                            dragStartValue = value
                        },
                        onDragEnd    = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(canvasSizePx.width / 2f, canvasSizePx.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            val currentAngle = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), -dy.toDouble())).toFloat()
                            var angleDelta = currentAngle - dragStartAngle
                            if (angleDelta > 180f) angleDelta -= 360f
                            if (angleDelta < -180f) angleDelta += 360f
                            onValueChange(((dragStartValue + (angleDelta / 540f) * (max - min)).coerceIn(min, max)))
                        }
                    )
                }
        ) {
            drawCircle(color = Color(0xFF666666), radius = size.minDimension / 2)
            drawCircle(color = Color(0xFF333333), radius = size.minDimension / 2.8f)
            rotate(displayAngle) {
                drawLine(color = Color.White, start = center, end = Offset(center.x, center.y - size.minDimension / 2.1f), strokeWidth = 3.5f)
            }
        }
    }
}