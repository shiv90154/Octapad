package com.example.myapplication.ui.pads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.PadDark
import com.example.myapplication.ui.PadPressed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress


import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot


/**
 * DrumPad — base composable shared by all 8 individual pad wrappers.
 *
 * On press it shows a static RGB rainbow glow ring behind the pad.
 *
 * @param modifier  Standard Modifier (size/weight passed from parent)
 * @param pressed   Whether the pad is currently in a pressed/active state
 * @param onPress   Callback fired on tap
 */
@Composable
fun DrumPad(
    modifier: Modifier = Modifier,
    pressed: Boolean,
    padNumber: Int,
    onPress: () -> Unit,
    onDragStart: () -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onPadPositionChanged: (Float, Float) -> Unit
) {
    Box(
        modifier = modifier

            .onGloballyPositioned { coordinates ->


                val pos = coordinates.positionInRoot()

                onPadPositionChanged(
                    pos.x,
                    pos.y
                )
            }

            .pointerInput(Unit) {

                detectTapGestures(
                    onTap = {
                        onPress()
                    }
                )
            }

            .pointerInput(Unit) {

                detectDragGesturesAfterLongPress(

                    onDragStart = {
                        onDragStart()
                    },

                    onDragEnd = {
                        onDragEnd()
                    },

                    onDragCancel = {
                        onDragEnd()
                    }

                ) { change, dragAmount ->

                    change.consume()

                    onDragMove(
                        dragAmount.x,
                        dragAmount.y
                    )
                }
            }
    ) {
        // Layer 1 — RGB glow ring drawn BEHIND the pad surface
        Canvas(modifier = Modifier.matchParentSize()) {
            if (pressed) {
                val rgbBrush = Brush.sweepGradient(
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    )
                )
                // Soft outer glow
                drawRoundRect(
                    brush        = rgbBrush,
                    cornerRadius = CornerRadius(40f, 40f),
                    style        = Stroke(width = 60f),
                    alpha        = 0.25f
                )
                // Sharp inner ring
                drawRoundRect(
                    brush        = rgbBrush,
                    cornerRadius = CornerRadius(40f, 40f),
                    style        = Stroke(width = 20f),
                    alpha        = 1f
                )
            }
        }

        // Layer 2 — Actual pad surface ON TOP of the glow
        // 6.dp padding glow ko edges pe visible rakhti hai
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (pressed) PadPressed else PadDark)
        )
    }
}