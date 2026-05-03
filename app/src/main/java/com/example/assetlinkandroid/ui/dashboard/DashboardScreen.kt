package com.example.assetlinkandroid.ui.dashboard

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.model.ItemStatus
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.CountdownText
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.StatusColors
import com.example.assetlinkandroid.ui.common.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appVm: AppViewModel,
    onItemClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val session by appVm.session.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AssetLink", fontWeight = FontWeight.Bold)
                        Text(
                            session?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            session?.let { s ->
                if (s.roles.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        s.roles.sortedBy { it.name }.forEach { role ->
                            StatusChip(text = role.display, color = roleColor(role))
                        }
                    }
                }
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Failed to load: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No listings yet — check back soon.")
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            topBid = state.topBids[item.id],
                            onClick = { onItemClick(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCard(item: Item, topBid: Double?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            ) {
                val first = item.photos.firstOrNull()
                if (first != null) {
                    AsyncImage(
                        model = first,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No photo", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reserve ${Money.format(item.reserveAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Interest ${(item.interestRate * 100).toInt()}% · ${item.loanDurationDays}d",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Top bid ${if (topBid != null) Money.format(topBid) else "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        text = item.status.label(),
                        color = itemStatusColor(item.status),
                    )
                    Spacer(Modifier.size(8.dp))
                    if (item.status == ItemStatus.LISTED) {
                        CountdownText(item.biddingEndsAt)
                    }
                }
            }
        }
    }
}

internal fun roleColor(role: AppRole): Color = when (role) {
    AppRole.ADMIN -> StatusColors.Defaulted
    AppRole.LOAN_OFFICER -> StatusColors.Active
    AppRole.INSPECTOR -> StatusColors.Closed
    AppRole.STORAGE_PARTNER -> StatusColors.Funded
    AppRole.BORROWER -> StatusColors.Pending
    AppRole.LENDER -> StatusColors.Listed
}

internal fun itemStatusColor(status: ItemStatus): Color = when (status) {
    ItemStatus.PENDING -> StatusColors.Pending
    ItemStatus.APPROVED -> StatusColors.Active
    ItemStatus.LISTED -> StatusColors.Listed
    ItemStatus.BID_CLOSED -> StatusColors.Closed
    ItemStatus.FUNDED -> StatusColors.Funded
    ItemStatus.REPAID -> StatusColors.Repaid
    ItemStatus.AUCTIONED -> StatusColors.Closed
    ItemStatus.REJECTED -> StatusColors.Rejected
}
