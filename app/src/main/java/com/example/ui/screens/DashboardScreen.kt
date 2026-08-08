package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WithdrawActivity
import com.example.data.WithdrawRule
import com.example.ui.components.ProviderBadge
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.RocketPurple
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    rules: List<WithdrawRule>,
    activities: List<WithdrawActivity>,
    isLiveMode: Boolean,
    bkashBalance: Double,
    nagadBalance: Double,
    rocketBalance: Double,
    onNavigateToRules: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenCreateRule: () -> Unit,
    onExecuteManualWithdrawal: (Int) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("bn", "BD")).apply {
        maximumFractionDigits = 2
    }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)

    val activeRulesCount = rules.count { it.isEnabled }
    val todayCompletedActivities = activities.filter { it.status == "COMPLETED" }
    val todayTotalWithdrawn = todayCompletedActivities.sumOf { it.withdrawnAmount }
    val pendingRequests = activities.filter { it.status == "PENDING_MANUAL" || it.status == "PENDING_PIN" }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Pending Withdrawal Requests (Manual Approval)
        if (pendingRequests.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_requests_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFD97706))
                                Text(
                                    text = "🚨 পেন্ডিং উইথড্র রিকোয়েস্ট (${pendingRequests.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = "ম্যানুয়াল ক্যাশ আউট",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }

                        pendingRequests.forEach { req ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProviderBadge(providerStr = req.provider)
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%,.2f", req.withdrawnAmount)}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("টার্গেট নম্বর (টাকা পাঠানোর নম্বর):", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(
                                                text = req.destination,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("TrxID:", fontSize = 11.sp, color = Color(0xFF64748B))
                                            Text(req.trxId, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(req.destination))
                                                android.widget.Toast.makeText(context, "নম্বর কপি হয়েছে: ${req.destination}", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("copy_number_btn_${req.id}")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("নম্বর কপি", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { onExecuteManualWithdrawal(req.id) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .testTag("accept_withdraw_btn_${req.id}")
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("টাকা পাঠানো সম্পন্ন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Status & Overview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overview_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveMode) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLiveMode) "LIVE SMS LISTENER ACTIVE" else "DEMO SANDBOX MODE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveMode) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onNavigateToSettings() }
                        ) {
                            Text(
                                text = "Config",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Automated MFS Withdrawals",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Today's Auto-Withdrawn",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳ ${String.format(Locale.US, "%,.2f", todayTotalWithdrawn)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Active Rules",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$activeRulesCount / ${rules.size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // MFS Balance & Threshold Status
        item {
            Text(
                text = "MFS Wallets & Cash-Out Rates",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // bKash Card
                MfsWalletCard(
                    providerName = "bKash",
                    balance = bkashBalance,
                    feeRate = "1.85%",
                    brandColor = BkashPink,
                    activeRulesCount = rules.count { it.provider == "BKASH" && it.isEnabled },
                    modifier = Modifier.weight(1f)
                )

                // Nagad Card
                MfsWalletCard(
                    providerName = "Nagad",
                    balance = nagadBalance,
                    feeRate = "1.25%",
                    brandColor = NagadOrange,
                    activeRulesCount = rules.count { it.provider == "NAGAD" && it.isEnabled },
                    modifier = Modifier.weight(1f)
                )

                // Rocket Card
                MfsWalletCard(
                    providerName = "Rocket",
                    balance = rocketBalance,
                    feeRate = "1.80%",
                    brandColor = RocketPurple,
                    activeRulesCount = rules.count { it.provider == "ROCKET" && it.isEnabled },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToSimulator,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_sms_button")
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test SMS", fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenCreateRule,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Rule", fontSize = 13.sp)
                }
            }
        }

        // Recent Activity List Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Withdrawal Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSimulator) {
                    Text("View All", fontSize = 12.sp)
                }
            }
        }

        // Recent Activity Items
        if (activities.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No withdrawal activities recorded yet.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onNavigateToSimulator) {
                            Text("Simulate an incoming SMS")
                        }
                    }
                }
            }
        } else {
            items(activities.take(5)) { act ->
                ActivityRowCard(activity = act, dateFormat = dateFormat, onExecuteManualWithdrawal = onExecuteManualWithdrawal)
            }
        }
    }
}

@Composable
fun MfsWalletCard(
    providerName: String,
    balance: Double,
    feeRate: String,
    brandColor: Color,
    activeRulesCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brandColor, shape = RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = providerName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "৳ ${String.format(Locale.US, "%,.0f", balance)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fee: $feeRate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$activeRulesCount Rules", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = brandColor)
            }
        }
    }
}

@Composable
fun ActivityRowCard(
    activity: WithdrawActivity,
    dateFormat: SimpleDateFormat,
    onExecuteManualWithdrawal: ((Int) -> Unit)? = null
) {
    val statusColor = when (activity.status) {
        "COMPLETED" -> Color(0xFF10B981)
        "PENDING_MANUAL", "PENDING_PIN" -> Color(0xFFD97706)
        "SKIPPED" -> Color(0xFF6B7280)
        else -> Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_item_${activity.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProviderBadge(providerStr = activity.provider)

                    Column {
                        Text(
                            text = activity.ruleTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ${activity.destination}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(Date(activity.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.2f", activity.withdrawnAmount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Net: ৳ ${String.format(Locale.US, "%,.2f", activity.netPayout)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (activity.status == "PENDING_MANUAL") "PENDING APPROVAL" else activity.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if ((activity.status == "PENDING_MANUAL" || activity.status == "PENDING_PIN") && onExecuteManualWithdrawal != null) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activity.destination))
                            android.widget.Toast.makeText(context, "নম্বর কপি হয়েছে: ${activity.destination}", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নম্বর কপি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onExecuteManualWithdrawal(activity.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("টাকা পাঠানো সম্পন্ন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
