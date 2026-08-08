package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SmsLog
import com.example.data.WithdrawActivity
import com.example.ui.SimulationUiState
import com.example.ui.components.ProviderBadge
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy

@Composable
fun SimulatorAndLogsScreen(
    simulationState: SimulationUiState,
    smsLogs: List<SmsLog>,
    activities: List<WithdrawActivity>,
    onUpdateInputs: (sender: String, message: String) -> Unit,
    onRunSimulation: () -> Unit,
    onClearLogs: () -> Unit,
    onExecuteManualWithdrawal: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMS Simulator & Logs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Test SMS trigger evaluation and view full activity logs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("SMS Workbench", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Execution Logs (${activities.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        if (selectedTab == 0) {
            // SMS Workbench Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Quick SMS Presets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "bKash ৳5,500" to ("bKash" to "You have received Tk 5,500.00 from 01712345678. Fee Tk 0.00. Balance Tk 24,000.00. TrxID 9B8A7C6D at 01/08/2026 15:40"),
                            "Nagad ৳3,200" to ("NAGAD" to "Nagad Received Tk 3,200.00 from 01811223344. TxnID 77A88BC. Balance Tk 15,400.00"),
                            "Rocket ৳7,000" to ("Rocket" to "DBBL Rocket: Received Tk 7,000.00 from 01999887766. Tx ID: 3345112. Balance: Tk 16,400.00")
                        ).forEach { (label, pair) ->
                            val (presetSender, presetMsg) = pair
                            SuggestionChip(
                                onClick = { onUpdateInputs(presetSender, presetMsg) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = simulationState.senderInput,
                        onValueChange = { onUpdateInputs(it, simulationState.messageInput) },
                        label = { Text("SMS Sender ID") },
                        placeholder = { Text("e.g. bKash, NAGAD, Rocket") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = simulationState.messageInput,
                        onValueChange = { onUpdateInputs(simulationState.senderInput, it) },
                        label = { Text("Simulated SMS Body") },
                        placeholder = { Text("Paste or type SMS text...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("message_input"),
                        maxLines = 4
                    )
                }

                item {
                    Button(
                        onClick = onRunSimulation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_simulation_button"),
                        enabled = !simulationState.isProcessing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (simulationState.isProcessing) "Parsing SMS..." else "Parse & Execute Auto-Withdraw Rule")
                    }
                }

                // Simulation Result
                simulationState.lastResult?.let { res ->
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (res.activity != null && res.activity.status == "COMPLETED")
                                    Color(0xFF064E3B) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulation_result_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SMS Engine Evaluation",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    ProviderBadge(providerStr = res.smsLog.detectedProvider)
                                }

                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                                Text(
                                    text = res.summaryMessage,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )

                                res.activity?.let { act ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Detected Amount", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                            Text("৳ ${act.incomingAmount}", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Column {
                                            Text("Cash-Out Fee", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                            Text("৳ ${String.format(Locale.US, "%.2f", act.feeAmount)}", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Net Payout", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                            Text("৳ ${String.format(Locale.US, "%.2f", act.netPayout)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Logs Tab
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recorded Activity History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = MaterialTheme.colorScheme.error)
                    }
                }

                if (activities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs recorded yet. Run a simulation above!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activities, key = { it.id }) { act ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProviderBadge(providerStr = act.provider)
                                        Text(
                                            text = act.status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (act.status == "COMPLETED") Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                    }

                                    Text(
                                        text = act.ruleTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = "Withdrawn ৳${act.withdrawnAmount} • Fee ৳${String.format(Locale.US, "%.2f", act.feeAmount)} • Net ৳${String.format(Locale.US, "%.2f", act.netPayout)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "Destination: ${act.destination} | TrxID: ${act.trxId}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = dateFormat.format(Date(act.timestamp)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (act.status == "PENDING_MANUAL" || act.status == "PENDING_PIN") {
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(act.destination))
                                                    android.widget.Toast.makeText(context, "নম্বর কপি হয়েছে: ${act.destination}", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("নম্বর কপি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onExecuteManualWithdrawal(act.id) },
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
                    }
                }
            }
        }
    }
}
