package com.example.assetlinkandroid.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = color,
    )
}

object StatusColors {
    val Pending = Color(0xFFB58900)
    val Active = Color(0xFF2563EB)
    val Listed = Color(0xFF059669)
    val Closed = Color(0xFF7C3AED)
    val Funded = Color(0xFF0E7490)
    val Repaid = Color(0xFF16A34A)
    val Defaulted = Color(0xFFDC2626)
    val Rejected = Color(0xFFDC2626)
    val Neutral = Color(0xFF6B7280)
}
