package com.example.assetlinkandroid.ui.itemdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.model.ItemStatus
import com.example.assetlinkandroid.data.model.Offer
import com.example.assetlinkandroid.data.model.OfferStatus
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.CountdownText
import com.example.assetlinkandroid.ui.common.DateFmt
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.label
import com.example.assetlinkandroid.ui.dashboard.itemStatusColor
import com.example.assetlinkandroid.ui.theme.AppBorder
import com.example.assetlinkandroid.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    appVm: AppViewModel,
    onBack: () -> Unit,
    vm: ItemDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val session by appVm.session.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showBidDialog by remember { mutableStateOf(false) }
    var pendingAcceptOfferId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.title ?: "Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            state.loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
            state.item != null -> {
                val item = state.item!!
                val isOwner = session?.userId == item.borrowerId
                val isLender = session?.has(AppRole.LENDER) == true && !isOwner
                val biddingOpen = item.status == ItemStatus.LISTED
                val biddingClosed = item.status == ItemStatus.BID_CLOSED
                val canPlaceBid = isLender && biddingOpen
                val canSelectBid = isOwner && (biddingOpen || biddingClosed) && state.offers.isNotEmpty()

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    PhotoCarousel(item.photos)
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        item.description?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(text = item.status.label(), color = itemStatusColor(item.status))
                            when {
                                biddingClosed -> {
                                    Spacer(Modifier.width(8.dp))
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Bidding closed", style = MaterialTheme.typography.bodySmall) },
                                    )
                                }
                                biddingOpen && item.biddingEndsAt != null -> {
                                    Spacer(Modifier.width(12.dp))
                                    Text("Closes in: ", style = MaterialTheme.typography.bodySmall)
                                    CountdownText(item.biddingEndsAt)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        InfoRow("Reserve", Money.format(item.reserveAmount))
                        InfoRow("Interest", "${(item.interestRate * 100).toInt()}%")
                        InfoRow("Duration", "${item.loanDurationDays} days")
                        state.borrowerProfile?.fullName?.let { InfoRow("Borrower", it) }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // Current bid card — mirrors web's trophy card
                        CurrentBidCard(
                            topBid = state.topBid,
                            currentTopBid = state.currentTopBid,
                            reserveAmount = item.reserveAmount,
                            pendingCount = state.offers.count { it.status == OfferStatus.PENDING },
                            isOwner = isOwner,
                            onAccept = if (isOwner && state.topBid != null)
                                { pendingAcceptOfferId = state.topBid!!.id } else null,
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Bids",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            if (canSelectBid) {
                                val selected = state.selectedOffer
                                Button(
                                    onClick = { selected?.let { pendingAcceptOfferId = it.id } },
                                    enabled = selected != null && !state.accepting,
                                ) {
                                    Text(
                                        when {
                                            state.accepting -> "Accepting..."
                                            selected != null -> "Accept ${Money.format(selected.amount)}"
                                            else -> "Select a bid"
                                        }
                                    )
                                }
                            }
                        }
                        if (canSelectBid && state.selectedOffer == null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Tap a bid below to select it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (state.offers.isEmpty()) {
                            Text("No bids yet.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            state.offers.sortedByDescending { it.amount }.forEach { offer ->
                                BidRow(
                                    offer = offer,
                                    lenderName = state.lenderProfiles[offer.lenderId]?.fullName,
                                    selectable = canSelectBid,
                                    selected = state.selectedOfferId == offer.id,
                                    onClick = if (canSelectBid) {
                                        {
                                            vm.selectOffer(
                                                if (state.selectedOfferId == offer.id) null else offer.id
                                            )
                                        }
                                    } else null,
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        if (canPlaceBid) {
                            Button(
                                onClick = { showBidDialog = true },
                                enabled = !state.placingBid,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Place bid") }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }

                if (showBidDialog) {
                    PlaceBidDialog(
                        minBid = state.minBidAmount,
                        currentTopBid = state.currentTopBid,
                        reserveAmount = item.reserveAmount,
                        onDismiss = { showBidDialog = false },
                        onConfirm = { amount ->
                            session?.userId?.let { uid ->
                                vm.placeBid(uid, amount, onDone = { showBidDialog = false })
                            }
                        },
                    )
                }

                pendingAcceptOfferId?.let { offerId ->
                    val offer = state.offers.firstOrNull { it.id == offerId }
                    if (offer == null) {
                        pendingAcceptOfferId = null
                    } else {
                        val totalRepayment = offer.amount * (1.0 + item.interestRate)
                        AlertDialog(
                            onDismissRequest = { pendingAcceptOfferId = null },
                            title = { Text("Accept this bid?") },
                            text = {
                                Column {
                                    Text(
                                        "Are you sure you want to accept this " +
                                            "${Money.format(offer.amount)} bid?",
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "You will receive ${Money.format(offer.amount)} now and " +
                                            "repay ${Money.format(totalRepayment)} " +
                                            "in ${item.loanDurationDays} days.",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Bidding will close and a loan will be created.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val id = offer.id
                                        pendingAcceptOfferId = null
                                        vm.acceptOffer(id, onDone = onBack)
                                    },
                                    enabled = !state.accepting,
                                ) { Text("Yes, accept") }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { pendingAcceptOfferId = null }) {
                                    Text("Cancel")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCarousel(photos: List<String>) {
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { Text("No photos") }
        return
    }
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(photos) { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BidRow(
    offer: Offer,
    lenderName: String?,
    selectable: Boolean,
    selected: Boolean,
    onClick: (() -> Unit)?,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
        else androidx.compose.ui.graphics.Color.Transparent
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 0.dp,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(lenderName ?: offer.lenderId.take(8), fontWeight = FontWeight.Medium)
                Text(offer.status.label(), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                Money.format(offer.amount),
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary
                    else androidx.compose.ui.graphics.Color.Unspecified,
            )
            if (selectable) {
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.RadioButton(
                    selected = selected,
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
private fun CurrentBidCard(
    topBid: Offer?,
    currentTopBid: Double?,
    reserveAmount: Double,
    pendingCount: Int,
    isOwner: Boolean,
    onAccept: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆", fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Current bid",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (currentTopBid != null) {
                Text(
                    Money.format(currentTopBid),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimary,
                )
                topBid?.let {
                    Text(
                        "@ ${(it.proposedInterest * 100).toInt()}% interest",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "$pendingCount active bid${if (pendingCount != 1) "s" else ""}" +
                        (topBid?.createdAt?.let { " • placed ${DateFmt.format(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isOwner && onAccept != null) {
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) { Text("Accept current bid") }
                }
            } else {
                Text(
                    "No bids yet. Reserve is ${Money.format(reserveAmount)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaceBidDialog(
    minBid: Int,
    currentTopBid: Double?,
    reserveAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember(minBid) { mutableStateOf(minBid.toString()) }
    val enteredAmount = text.toIntOrNull()
    val isValid = enteredAmount != null && enteredAmount >= minBid
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Place a bid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (currentTopBid != null) {
                    Text("Current top bid: ${Money.format(currentTopBid)}",
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Reserve: ${Money.format(reserveAmount)}",
                        style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Minimum next bid: ${Money.format(minBid)}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit) },
                    label = { Text("Your bid ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isValid && text.isNotEmpty(),
                    supportingText = if (!isValid && text.isNotEmpty()) {
                        { Text("Must be ≥ ${Money.format(minBid)}") }
                    } else null,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { enteredAmount?.let(onConfirm) },
                enabled = isValid,
            ) { Text("Submit") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

