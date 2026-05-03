package com.example.assetlinkandroid.ui.itemsforsale

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.assetlinkandroid.data.model.SaleListing
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.theme.AppBorder
import com.example.assetlinkandroid.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsForSaleScreen(
    appVm: AppViewModel,
    onMenuClick: () -> Unit,
    vm: ItemsForSaleViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val session by appVm.session.collectAsStateWithLifecycle()
    val userId = session?.userId
    val snackbar = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }
    var confirmSale by remember { mutableStateOf<SaleListing?>(null) }
    var uploadSale by remember { mutableStateOf<SaleListing?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Items for Sale", fontWeight = FontWeight.Bold)
                        Text(
                            "Browse auctioned items available for purchase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Filled.Close else Icons.Filled.Tune,
                            contentDescription = "Toggle filters",
                            tint = if (state.hasActiveFilters) AppPrimary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter bar
            if (showFilters) {
                FilterBar(
                    query = state.query,
                    minPrice = state.minPrice,
                    maxPrice = state.maxPrice,
                    onQueryChange = vm::updateQuery,
                    onMinChange = vm::updateMinPrice,
                    onMaxChange = vm::updateMaxPrice,
                )
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        if (state.hasActiveFilters) "No results match your filters."
                        else "No items for sale right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.filtered, key = { it.id }) { sale ->
                        SaleCard(
                            sale = sale,
                            userId = userId,
                            busy = state.busy,
                            onAccept = { confirmSale = sale },
                            onUploadPop = { uploadSale = sale },
                        )
                    }
                }
            }
        }

        // Confirm accept dialog
        confirmSale?.let { sale ->
            AlertDialog(
                onDismissRequest = { confirmSale = null },
                title = { Text("Accept this item?") },
                text = {
                    Text(
                        "You are accepting \"${sale.itemTitle}\" for ${Money.format(sale.salePrice)}. " +
                                "You will then need to upload proof of payment."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.acceptSale(sale)
                            confirmSale = null
                        },
                        enabled = !state.busy,
                    ) { Text("Accept") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { confirmSale = null }) { Text("Cancel") }
                },
            )
        }

        // Upload POP dialog
        uploadSale?.let { sale ->
            UploadPopDialog(
                sale = sale,
                busy = state.busy,
                onDismiss = { uploadSale = null },
                onSubmit = { uri, note ->
                    vm.submitProof(sale, uri, note)
                    uploadSale = null
                },
            )
        }
    }
}

// ── Filter bar ────────────────────────────────────────────────────────────────

@Composable
private fun FilterBar(
    query: String,
    minPrice: String,
    maxPrice: String,
    onQueryChange: (String) -> Unit,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by title or description…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = { onMinChange(it.filter(Char::isDigit)) },
                modifier = Modifier.weight(1f),
                label = { Text("Min price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { onMaxChange(it.filter(Char::isDigit)) },
                modifier = Modifier.weight(1f),
                label = { Text("Max price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }
}

// ── Sale card ─────────────────────────────────────────────────────────────────

@Composable
private fun SaleCard(
    sale: SaleListing,
    userId: String?,
    busy: Boolean,
    onAccept: () -> Unit,
    onUploadPop: () -> Unit,
) {
    val isListed = sale.status == "listed"
    val isUnderOffer = sale.status == "under_offer"
    val isBuyer = userId != null && sale.buyerId == userId

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            // Photo
            val photo = sale.itemPhotos.firstOrNull()
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = sale.itemTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                )
            }

            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        sale.itemTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip(
                        text = if (isListed) "For Sale" else "Under Offer",
                        color = if (isListed) AppPrimary else Color(0xFFF59E0B),
                    )
                }
                sale.itemType?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                sale.itemDescription?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    Money.format(sale.salePrice),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimary,
                )

                // Action area
                when {
                    userId == null -> {
                        Text(
                            "Sign in to purchase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    isListed -> {
                        Button(
                            onClick = onAccept,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Accept — ${Money.format(sale.salePrice)}")
                        }
                    }
                    isUnderOffer && isBuyer -> {
                        Button(
                            onClick = onUploadPop,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Upload proof of payment")
                        }
                    }
                    isUnderOffer -> {
                        Text(
                            "This item is under offer by another buyer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Upload POP dialog ─────────────────────────────────────────────────────────

@Composable
private fun UploadPopDialog(
    sale: SaleListing,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Uri, String?) -> Unit,
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var note by remember { mutableStateOf("") }
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedUri = it } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Proof of Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Item: ${sale.itemTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "Amount: ${Money.format(sale.salePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                )

                OutlinedButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppBorder),
                ) {
                    Text(if (selectedUri != null) "Image selected ✓" else "Choose image…")
                }

                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Proof preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (optional)") },
                    singleLine = false,
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedUri?.let { onSubmit(it, note.ifBlank { null }) } },
                enabled = selectedUri != null && !busy,
            ) { Text(if (busy) "Uploading…" else "Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
