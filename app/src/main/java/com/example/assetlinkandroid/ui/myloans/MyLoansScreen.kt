package com.example.assetlinkandroid.ui.myloans

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assetlinkandroid.data.model.LoanStatus
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.DateFmt
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.StatusColors
import com.example.assetlinkandroid.ui.common.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    appVm: AppViewModel,
    onMenuClick: () -> Unit,
    vm: MyLoansViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Loans") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Borrowed") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Lent") })
            }
            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    val list = if (tab == 0) state.borrowed else state.lent
                    val side = if (tab == 0) Side.BORROWER else Side.LENDER
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(if (tab == 0) "No borrowed loans yet." else "No lent loans yet.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(list, key = { it.loan.id }) { lwi ->
                                LoanCard(
                                    lwi = lwi,
                                    side = side,
                                    uploading = state.uploadingFor == lwi.loan.id,
                                    onUpload = { uri, kind ->
                                        vm.uploadProof(
                                            loanId = lwi.loan.id,
                                            kind = kind,
                                            amount = if (kind == MyLoansViewModel.FUNDING) lwi.loan.principal
                                            else lwi.loan.totalRepayment,
                                            uri = uri,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class Side { BORROWER, LENDER }

@Composable
private fun LoanCard(
    lwi: LoanWithItem,
    side: Side,
    uploading: Boolean,
    onUpload: (Uri, String) -> Unit,
) {
    val loan = lwi.loan
    val item = lwi.item

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val kind = when {
                side == Side.LENDER && loan.status == LoanStatus.PENDING -> MyLoansViewModel.FUNDING
                side == Side.BORROWER && loan.status == LoanStatus.ACTIVE -> MyLoansViewModel.REPAYMENT
                else -> return@rememberLauncherForActivityResult
            }
            onUpload(it, kind)
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item?.title ?: "Item ${loan.itemId.take(6)}",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(text = loan.status.label(), color = loanStatusColor(loan.status))
            }
            Spacer(Modifier.height(6.dp))
            InfoLine("Principal", Money.format(loan.principal))
            InfoLine("Total repayment", Money.format(loan.totalRepayment))
            InfoLine("Duration", "${loan.durationDays} days")
            loan.dueAt?.let { InfoLine("Due", DateFmt.format(it)) }
            loan.fundedAt?.let { InfoLine("Funded", DateFmt.format(it)) }

            // Action: upload PoP based on side + status.
            val canFund = side == Side.LENDER && loan.status == LoanStatus.PENDING &&
                lwi.proofs.none { it.kind == MyLoansViewModel.FUNDING && it.status != "rejected" }
            val canRepay = side == Side.BORROWER && loan.status == LoanStatus.ACTIVE &&
                lwi.proofs.none { it.kind == MyLoansViewModel.REPAYMENT && it.status != "rejected" }

            if (canFund || canRepay) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        pickImage.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    enabled = !uploading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            uploading -> "Uploading..."
                            canFund -> "Upload funding proof"
                            else -> "Upload repayment proof"
                        }
                    )
                }
            }

            if (lwi.proofs.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                Text(
                    "Submitted proofs",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                lwi.proofs.forEach { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${p.kind.replaceFirstChar(Char::uppercase)} · ${Money.format(p.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip(text = p.status, color = proofStatusColor(p.status))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun loanStatusColor(status: LoanStatus) = when (status) {
    LoanStatus.PENDING -> StatusColors.Pending
    LoanStatus.ACTIVE -> StatusColors.Active
    LoanStatus.REPAID -> StatusColors.Repaid
    LoanStatus.DEFAULTED -> StatusColors.Defaulted
    LoanStatus.CANCELLED -> StatusColors.Neutral
}

private fun proofStatusColor(status: String) = when (status) {
    "approved" -> StatusColors.Repaid
    "rejected" -> StatusColors.Rejected
    else -> StatusColors.Pending
}
