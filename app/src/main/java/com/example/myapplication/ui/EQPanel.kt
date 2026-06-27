package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EqBg        = Color(0xFF111111)
private val EqPanelBg   = Color(0xFF1A1A1A)
private val EqAccent    = Color(0xFF00E5FF)
private val EqGreen     = Color(0xFF00C853)
private val EqDivider   = Color(0xFF2A2A2A)
private val EqTextMuted = Color(0xFF888888)
private val EqTextWht   = Color(0xFFEEEEEE)

@Composable
fun EQPanel(
    visible: Boolean,
    loopEnabled: Boolean,
    exclusiveMode: Boolean,
    onLoopChange: (Boolean) -> Unit,
    onExclusiveChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(220)) + fadeIn(tween(220)),
        exit    = slideOutHorizontally(targetOffsetX  = { -it }, animationSpec = tween(180)) + fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(EqBg)
                .border(
                    width = 1.dp,
                    color = EqDivider,
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text          = "PAD SETTINGS",
                        color         = EqAccent,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2A2A2A))
                            // ✅ FIX: clickable instead of pointerInput — no recomposition bug
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = EqTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                EqDividerLine()

                SectionLabel("PLAYBACK")

                // ── LOOP toggle ───────────────────────────────────────────────
                EqToggleRow(
                    icon = "",
                    title = "LOOP",
                    subtitle = if (loopEnabled) "Audio will loop continuously" else "Loop is disabled",
                    enabled = loopEnabled,
                    activeColor = EqGreen,
                    onToggle = {
                        android.util.Log.d("EQ_TEST", "Loop = $it")
                        onLoopChange(it)
                    }
                )

                EqDividerLine()

                SectionLabel("PAD BEHAVIOUR")

                // ── EXCLUSIVE MODE toggle ─────────────────────────────────────
                EqToggleRow(
                    icon        = "",
                    title       = "EXCLUSIVE MODE",
                    subtitle = if (exclusiveMode) "Single Pad Playback" else "Multi Pad Playback",                    enabled     = exclusiveMode,
                    activeColor = EqAccent,
                    onToggle    = onExclusiveChange
                )

                EqDividerLine()

                Text(
                    text      = "More settings coming soon…",
                    color     = EqTextMuted,
                    fontSize  = 8.sp,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────

@Composable
private fun EqToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    activeColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EqPanelBg)
            // ✅ FIX: whole row bhi tappable — zyada easy for user
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle(!enabled) }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(icon, fontSize = 16.sp)
            Column {
                Text(
                    text          = title,
                    color         = EqTextWht,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text     = subtitle,
                    color    = if (enabled) activeColor else EqTextMuted,
                    fontSize = 8.sp
                )
            }
        }
        PillToggle(enabled = enabled, activeColor = activeColor)
    }
}

// ── Pill toggle — DISPLAY ONLY, click handled by parent Row ──────────────────

@Composable
private fun PillToggle(
    enabled: Boolean,
    activeColor: Color
) {
    // ✅ FIX: animateFloatAsState key = enabled — force recompose on state change
    val thumbOffset by animateFloatAsState(
        targetValue   = if (enabled) 1f else 0f,
        animationSpec = tween(160),
        label         = "pillThumb"
    )

    Box(
        modifier = Modifier
            .width(38.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) activeColor.copy(alpha = 0.25f) else Color(0xFF2A2A2A))
            .border(
                width = 1.dp,
                color = if (enabled) activeColor.copy(alpha = 0.6f) else Color(0xFF3A3A3A),
                shape = RoundedCornerShape(50)
            )
        // ✅ FIX: NO pointerInput/clickable here — parent Row handles it
        // pointerInput(enabled) was causing stale lambda capture = toggle nahi karta tha
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .offset(x = (2f + thumbOffset * 18f).dp, y = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(if (enabled) activeColor else Color(0xFF555555))
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        color         = EqTextMuted,
        fontSize      = 8.sp,
        fontWeight    = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier      = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun EqDividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(EqDivider)
    )
}