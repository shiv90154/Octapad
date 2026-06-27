package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MusicPanel(
    onImport: () -> Unit,
    onAudios: () -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onClose: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(170.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B1B1B))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "SETTINGS",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        MenuItem("IMPORT", Color.Cyan) {
            onImport()
        }

        MenuItem("AUDIOS", Color.White) {
            onAudios()
        }

        MenuItem("EXPORT", Color.Yellow) {
            onExport()
        }

        MenuItem("RENAME KIT", Color.Green) {
            onRename()
        }

        Spacer(modifier = Modifier.weight(1f))

        MenuItem("CLOSE", Color.Red) {
            onClose()
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    color: Color,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A))
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

    }
}