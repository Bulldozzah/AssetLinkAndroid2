package com.example.assetlinkandroid.ui.myloans

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.assetlinkandroid.data.model.Loan
import com.example.assetlinkandroid.data.model.LoanStatus
import com.example.assetlinkandroid.data.model.PaymentProof
import com.example.assetlinkandroid.data.model.ProofKind
import com.example.assetlinkandroid.data.model.ProofStatus
import com.example.assetlinkandroid.ui.AppViewModel
import com.example.assetlinkandroid.ui.common.DateFmt
import com.example.assetlinkandroid.ui.common.Money
import com.example.assetlinkandroid.ui.common.StatusChip
import com.example.assetlinkandroid.ui.common.StatusColors
import com.example.assetlinkandroid.ui.common.label
import com.example.assetlinkandroid.ui.theme.AppBorder
import com.example.assetlinkandroid.ui.theme.AppDestructive
import com.example.assetlinkandroid.ui.theme.AppPrimary

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
                title = { Text("My Loans", fontWeight = FontWeight.Bold) },
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
                .padding(padding),
        ) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = {
                        BadgedBox(badge = {
                            if (!state.loading && state.borrowed.isNotEmpty()) {
                                Badge { Text("${state.borrowed.size}") }
                            }
                        }) { Text("Borrowed") }
                    },
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = {
                        BadgedBox(badge = {
                            if (!state.loading && state.lent.isNotEmpty()) {
                                Badge { Text("${state.lent.size}") }
                            }
                        }) { Text("Lent") }
                    },
                )
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    val list = if (tab == 0) state.borrowed else state.lent
                    val side = if (tab == 0) Side.BORROWER else Side.LENDER
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (tab == 0) "No borrowing activity yet." else "No lending activity yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(list, key = { it.loan.id }) { lwi ->
                                LoanCard(
                                    lwi = lwi,
                                    side = side,
                                    uploading = state.uploadingFor == lwi.loan.id,
                                    onUpload = { uri, kind, amount, note ->
                                        vm.uploadProof(
                                            loanId = lwi.loan.id,
                                            kind = kind,
                                            amount = amount,
                                            uri = uri,
                                            note = note,
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
    onUpload: (Uri, String, Double, String?) -> Unit,
) {
    val loan = lwi.loan
    val item = lwi.item

    val latestFunding = lwi.proofs.firstOrNull { it.kind == ProofKind.FUNDING }
    val latestRepayment = lwi.proofs.firstOrNull { it.kind == ProofKind.REPAYMENT }

    val canFund = side == Side.LENDER && loan.status == LoanStatus.PENDING &&
        (latestFunding == null || latestFunding.status == ProofStatus.REJECTED)
    val canRepay = side == Side.BORROWER && loan.status == LoanStatus.ACTIVE &&
        (latestRepayment == null || latestRepayment.status == ProofStatus.REJECTED)

    var showDialog by remember { mutableStateOf(false) }
    var dialogKind by remember { mutableStateOf(ProofKind.FUNDING) }

    if (showDialog) {
        val defaultAmount = if (dialogKind == ProofKind.FUNDING) loan.principal else loan.totalRepayment
        UploadProofDialog(
            kind = dialogKind,
            defaultAmount = defaultAmount,
            uploading = uploading,
            onSubmit = { uri, amount, note ->
                onUpload(uri, dialogKind, amount, note)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, AppBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header row: thumbnail + title + status
            Row(verticalAlignment = Alignment.Top) {
                val photo = item?.photos?.firstOrNull()
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center,
                    ) { Text("📦", fontSize = 22.sp) }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item?.title ?: "Item ${loan.itemId.take(6)}",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    StatusChip(text = loan.status.label(), color = loanStatusColor(loan.status))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Stage label
            val label = stageLabel(loan, side, lwi.proofs)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = stageLabelColor(loan, side, lwi.proofs),
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppBorder)
            Spacer(Modifier.height(8.dp))

            // Loan info
            InfoLine("Principal", Money.format(loan.principal))
            InfoLine("Total repayment", Money.format(loan.totalRepayment))
            InfoLine("Interest", "${(loan.interestRate * 100).toInt()}% / ${loan.durationDays}d")
            loan.dueAt?.let { InfoLine("Due", DateFmt.format(it)) }
            loan.fundedAt?.let { InfoLine("Funded", DateFmt.format(it)) }

            // Proof blocks
            val fundingProof = lwi.proofs.firstOrNull { it.kind == ProofKind.FUNDING }
            val repaymentProof = lwi.proofs.firstOrNull { it.kind == ProofKind.REPAYMENT }

            if (fundingProof != null || repaymentProof != null) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = AppBorder)
                Spacer(Modifier.height(8.dp))
                fundingProof?.let { ProofBlock(proof = it, label = "Funding proof") }
                if (fundingProof != null && repaymentProof != null) Spacer(Modifier.height(6.dp))
                repaymentProof?.let { ProofBlock(proof = it, label = "Repayment proof") }
            }

            // Upload button
            if (canFund || canRepay) {
                Spacer(Modifier.height(12.dp))
                if (uploading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppPrimary,
                    )
                } else {
                    Button(
                        onClick = {
                            dialogKind = if (canFund) ProofKind.FUNDING else ProofKind.REPAYMENT
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(if (canFund) "Upload funding proof" else "Upload repayment proof")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProofBlock(proof: PaymentProof, label: String) {
    val uriHandler = LocalUriHandler.current
    val icon = when (proof.status) {
        ProofStatus.APPROVED -> "✅"
        ProofStatus.REJECTED -> "❌"
        else -> "🕐"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(icon, fontSize = 14.sp)
            Text(
                "$label: ${proof.status.replaceFirstChar(Char::uppercase)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "View full size",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AppPrimary,
                    textDecoration = TextDecoration.Underline,
                ),
                modifier = Modifier.noRippleClickable { uriHandler.openUri(proof.proofUrl) },
            )
        }
        // Inline image preview
        AsyncImage(
            model = proof.proofUrl,
            contentDescription = "$label image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { uriHandler.openUri(proof.proofUrl) },
        )
        Text(
            "${Money.format(proof.amount)} · submitted ${DateFmt.format(proof.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        proof.note?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        if (proof.status == ProofStatus.REJECTED && proof.reviewerNote != null) {
            Text(
                "Rejected: ${proof.reviewerNote}",
                style = MaterialTheme.typography.bodySmall,
                color = AppDestructive,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun UploadProofDialog(
    kind: String,
    defaultAmount: Double,
    uploading: Boolean,
    onSubmit: (Uri, Double, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf(defaultAmount.toInt().toString()) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedName = uri.lastPathSegment ?: "image selected"
        }
    }

    val title = if (kind == ProofKind.FUNDING) "Upload funding proof" else "Upload repayment proof"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount sent ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = AppBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectedName ?: "Choose proof image",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("Bank, ref. number, channel…") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = AppBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            val uri = selectedUri
            val amount = amountText.toDoubleOrNull()?.takeIf { it > 0 }
            Button(
                onClick = {
                    if (uri != null && amount != null) {
                        onSubmit(uri, amount, noteText.ifBlank { null })
                    }
                },
                enabled = !uploading && selectedUri != null && amountText.toDoubleOrNull()?.takeIf { it > 0 } != null,
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun stageLabel(loan: Loan, side: Side, proofs: List<PaymentProof>): String {
    val pendingFunding = proofs.any { it.kind == ProofKind.FUNDING && it.status == ProofStatus.PENDING }
    val pendingRepayment = proofs.any { it.kind == ProofKind.REPAYMENT && it.status == ProofStatus.PENDING }
    val latestFunding = proofs.firstOrNull { it.kind == ProofKind.FUNDING }
    return when (loan.status) {
        LoanStatus.PENDING -> when {
            pendingFunding -> "Awaiting loan-officer review of funding proof"
            side == Side.LENDER &&
                (latestFunding == null || latestFunding.status == ProofStatus.REJECTED) ->
                "Action needed: upload proof of payment"
            else -> "Waiting for the lender to send funds"
        }
        LoanStatus.ACTIVE -> when {
            pendingRepayment -> "Awaiting loan-officer review of repayment"
            side == Side.BORROWER -> {
                val due = loan.dueAt?.let { " by ${DateFmt.format(it)}" } ?: ""
                "Active — repay ${Money.format(loan.totalRepayment)}$due"
            }
            else -> "Active — borrower has until the due date to repay"
        }
        LoanStatus.REPAID -> "Repaid"
        LoanStatus.DEFAULTED -> "Defaulted"
        LoanStatus.CANCELLED -> "Cancelled"
    }
}

private fun stageLabelColor(loan: Loan, side: Side, proofs: List<PaymentProof>): Color {
    val pendingFunding = proofs.any { it.kind == ProofKind.FUNDING && it.status == ProofStatus.PENDING }
    val pendingRepayment = proofs.any { it.kind == ProofKind.REPAYMENT && it.status == ProofStatus.PENDING }
    val latestFunding = proofs.firstOrNull { it.kind == ProofKind.FUNDING }
    return when (loan.status) {
        LoanStatus.PENDING -> when {
            pendingFunding -> Color(0xFFD97706)
            side == Side.LENDER &&
                (latestFunding == null || latestFunding.status == ProofStatus.REJECTED) ->
                AppDestructive
            else -> Color(0xFF737373)
        }
        LoanStatus.ACTIVE -> when {
            pendingRepayment -> Color(0xFFD97706)
            else -> Color(0xFF16A34A)
        }
        LoanStatus.REPAID -> Color(0xFF16A34A)
        LoanStatus.DEFAULTED -> AppDestructive
        LoanStatus.CANCELLED -> Color(0xFF737373)
    }
}

private fun loanStatusColor(status: LoanStatus) = when (status) {
    LoanStatus.PENDING -> StatusColors.Pending
    LoanStatus.ACTIVE -> StatusColors.Active
    LoanStatus.REPAID -> StatusColors.Repaid
    LoanStatus.DEFAULTED -> StatusColors.Defaulted
    LoanStatus.CANCELLED -> StatusColors.Neutral
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    )
