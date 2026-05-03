package com.example.assetlinkandroid.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.assetlinkandroid.data.model.ItemStatus
import com.example.assetlinkandroid.data.model.LoanStatus
import com.example.assetlinkandroid.data.model.OfferStatus
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

object Money {
    private val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    fun format(amount: Double): String = nf.format(amount)
    fun format(amount: Number?): String = if (amount == null) "—" else nf.format(amount.toDouble())
}

object DateFmt {
    private val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.US)
        .withZone(ZoneId.systemDefault())
    fun format(iso: String?): String = iso?.let {
        runCatching { fmt.format(Instant.parse(it)) }.getOrDefault(it)
    } ?: "—"

    fun parseEpochMs(iso: String?): Long? = iso?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    }
}

@Composable
fun CountdownText(targetIso: String?, modifier: Modifier = Modifier) {
    val targetMs = remember(targetIso) { DateFmt.parseEpochMs(targetIso) }
    if (targetMs == null) {
        Text("—", modifier = modifier, style = MaterialTheme.typography.bodyMedium)
        return
    }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(targetMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val remaining = max(0L, targetMs - now)
    val text = if (remaining <= 0L) "Ended" else formatDuration(remaining)
    Text(text, modifier = modifier, style = MaterialTheme.typography.bodyMedium)
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return when {
        h > 0 -> "%dh %02dm %02ds".format(h, m, s)
        m > 0 -> "%dm %02ds".format(m, s)
        else -> "${s}s"
    }
}

fun ItemStatus.label(): String = name.lowercase().replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
fun LoanStatus.label(): String = name.lowercase().replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
fun OfferStatus.label(): String = name.lowercase().replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
