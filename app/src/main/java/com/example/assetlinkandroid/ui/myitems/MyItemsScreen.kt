package com.example.assetlinkandroid.ui.myitems

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assetlinkandroid.ui.theme.AppBorder
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.label
import com.example.assetlinkandroid.ui.dashboard.itemStatusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(
    appVm: AppViewModel,
    onItemClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    vm: MyItemsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Items") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)) {
            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                state.items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No items yet.")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Visit a verified intake point to pawn an asset.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        MyItemRow(item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MyItemRow(item: Item, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                val first = item.photos.firstOrNull()
                if (first != null) {
                    AsyncImage(
                        model = first,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text("📦", fontSize = 40.sp)
                }
            }

            HorizontalDivider(color = AppBorder, thickness = 1.dp)

            Column(Modifier.padding(10.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reserve ${Money.format(item.reserveAmount)} · ${item.loanDurationDays}d",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(text = item.status.label(), color = itemStatusColor(item.status))
            }
        }
    }
}
