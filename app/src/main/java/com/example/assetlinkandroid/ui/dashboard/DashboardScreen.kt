package com.example.assetlinkandroid.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.model.ItemStatus
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.StatusColors
import com.example.assetlinkandroid.ui.theme.AppBorder
import com.example.assetlinkandroid.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appVm: AppViewModel,
    onItemClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onNavToMyItems: () -> Unit = {},
    onNavToMyLoans: () -> Unit = {},
    onNavToNotifications: () -> Unit = {},
    onNavToBrowse: () -> Unit = {},
    vm: DashboardViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val session by appVm.session.collectAsStateWithLifecycle()
    val needsRoleSetup = session != null && !session!!.isBorrower && !session!!.isLender

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Role badges
            session?.let { s ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (s.roles.isEmpty()) {
                            StatusChip(text = "No role yet", color = Color(0xFF9CA3AF))
                        } else {
                            s.roles.sortedBy { it.name }.forEach { role ->
                                StatusChip(text = role.display, color = roleColor(role))
                            }
                        }
                    }
                }
            }

            // Role setup card — shown when user has no borrower or lender role
            if (needsRoleSetup) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    RoleSelectionCard(
                        assigning = state.assigningRole,
                        onBorrow = { vm.assignRole(AppRole.BORROWER) { appVm.refreshSession() } },
                        onLend   = { vm.assignRole(AppRole.LENDER)   { appVm.refreshSession() } },
                    )
                }
            }

            // Stats row
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label   = "My Items",
                        count   = state.myItemsCount,
                        icon    = Icons.Filled.Inventory2,
                        loading = state.statsLoading,
                        onClick = onNavToMyItems,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label   = "My Loans",
                        count   = state.myLoansCount,
                        icon    = Icons.Filled.AccountBalance,
                        loading = state.statsLoading,
                        onClick = onNavToMyLoans,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label   = "Alerts",
                        count   = state.unreadCount,
                        icon    = Icons.Filled.Notifications,
                        loading = state.statsLoading,
                        onClick = onNavToNotifications,
                    )
                }
            }

            // Quick actions based on role
            session?.let { s ->
                if (s.isBorrower || s.isLender) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (s.isBorrower) {
                                Button(
                                    onClick = onNavToMyItems,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        "Pawn a new item",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            if (s.isLender) {
                                OutlinedButton(
                                    onClick = onNavToBrowse,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AppBorder),
                                ) {
                                    Icon(
                                        Icons.Filled.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        "Browse items",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Browse CTA card
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    onClick = onNavToBrowse,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AppBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "Browse Listings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Discover items to fund",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = AppPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    assigning: Boolean,
    onBorrow: () -> Unit,
    onLend: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Get started with AssetLink",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Choose your role to unlock features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onBorrow,
                    enabled = !assigning,
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppBorder),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "I want to borrow",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Button(
                    onClick = onLend,
                    enabled = !assigning,
                    modifier = Modifier.weight(1f).height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", fontSize = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "I want to lend",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                        )
                    }
                }
            }
            if (assigning) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    icon: ImageVector,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(6.dp))
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = AppPrimary,
                )
            } else {
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
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
