package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * RightPanel — right-side control bar.
 *
 * @param kits          Full kit list
 * @param currentKit    Currently active kit index
 * @param onKitAdd      "+" tapped → add new kit
 * @param onKitDelete   "-" tapped → delete current kit
 * @param onKitPrev     "<" tapped → go to previous kit
 * @param onKitNext     ">" tapped → go to next kit
 * @param onOpenKitList "PATCH LIST" tapped → open KitListScreen
 */
@Composable
fun RightPanel(
    kits: List<Kit>,
    currentKit: Int,
    onKitAdd: () -> Unit,
    onKitDelete: () -> Unit,
    onKitPrev: () -> Unit,
    onKitNext: () -> Unit,
    onOpenKitList: () -> Unit
) {
    var activeBtn by remember { mutableStateOf("REC") }
    var activeAbc by remember { mutableStateOf("C") }

    Column(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        // ── + and - buttons row ──────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PATCH",
                color = Color(0xFF888888),
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "+" Add kit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF003333))
                        .pointerInput(Unit) { detectTapGestures { onKitAdd() } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        color = BtnActive,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // "-" Delete kit (disabled if only 1 kit)
                val canDelete = kits.size > 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (canDelete) Color(0xFF330000) else Color(0xFF1A1A1A))
                        .pointerInput(Unit) {
                            detectTapGestures { if (canDelete) onKitDelete() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "−",
                        color = if (canDelete) Color(0xFFFF4444) else Color(0xFF444444),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Control Button Rows ──────────────────────────────────────────────
        // "PATCH LIST" button opens the KitListScreen
        CtrlBtnRow(
            labels   = listOf("PATCH LIST", "EDIT PADS", "REC"),
            active   = activeBtn,
            onSelect = { label ->
                activeBtn = label
                if (label == "PATCH LIST") onOpenKitList()
            }
        )
        CtrlBtnRow(
            labels   = listOf("EQ", "MIDI", "MUSIC"),
            active   = activeBtn,
            onSelect = { activeBtn = it }
        )

        // ── A / B / C Selector ───────────────────────────────────────────────
        AbcRow(active = activeAbc, onSelect = { activeAbc = it })

        // ── Patch Chain badge ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "PATCH CHAIN",
                color = Color(0xFFAAAAAA),
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        }

        // ── Brand label ──────────────────────────────────────────────────────
        Text(
            "REAL OCTAPAD",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // ── LCD Screen ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(LcdBg)
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = kits[currentKit].name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001A33),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                LcdRow("REC",   "00.00.00")
                LcdRow("MUSIC", "00.00.00")
                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("A", "B", "C").forEach { letter ->
                        val isActive = letter == activeAbc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isActive) Color(0xFF001A33) else Color.Transparent)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                letter,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) LcdBg else Color(0xFF001A33)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Navigation Row  < PATCH > ────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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

// ─── Helper Composables ───────────────────────────────────────────────────────

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
                    color = if (label == active) Color.Black else Color(0xFFCCCCCC),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AbcRow(active: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        listOf("A", "B", "C").forEach { letter ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (letter == active) BtnActive else BtnBg)
                    .pointerInput(Unit) { detectTapGestures { onSelect(letter) } }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letter,
                    color = if (letter == active) Color.Black else Color(0xFFCCCCCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LcdRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 8.sp, color = Color(0xFF001A33))
        Text(value,  fontSize = 8.sp, color = Color(0xFF001A33))
    }
}