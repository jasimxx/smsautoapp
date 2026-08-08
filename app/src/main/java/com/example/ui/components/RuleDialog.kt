package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MfsProvider
import com.example.data.WithdrawRule
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.RocketPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDialog(
    ruleToEdit: WithdrawRule?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        provider: String,
        triggerType: String,
        thresholdAmount: Double,
        smsKeyword: String,
        destinationType: String,
        destinationDetail: String,
        maxPerTx: Double,
        dailyLimit: Double,
        requiresPinConfirmation: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(ruleToEdit?.title ?: "") }
    var selectedProvider by remember { mutableStateOf(ruleToEdit?.provider ?: "BKASH") }
    var selectedTrigger by remember { mutableStateOf(ruleToEdit?.triggerType ?: "SMS_MATCH") }
    var thresholdText by remember { mutableStateOf((ruleToEdit?.thresholdAmount ?: 1000.0).toString()) }
    var smsKeyword by remember { mutableStateOf(ruleToEdit?.smsKeyword ?: "Received") }
    var destinationType by remember { mutableStateOf(ruleToEdit?.destinationType ?: "AGENT") }
    var destinationDetail by remember { mutableStateOf(ruleToEdit?.destinationDetail ?: "01711223344 (Agent)") }
    var maxPerTxText by remember { mutableStateOf((ruleToEdit?.maxPerTx ?: 25000.0).toString()) }
    var dailyLimitText by remember { mutableStateOf((ruleToEdit?.dailyLimit ?: 100000.0).toString()) }
    var requiresPin by remember { mutableStateOf(ruleToEdit?.requiresPinConfirmation ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (ruleToEdit == null) "Create Auto-Withdraw Rule" else "Edit Auto-Withdraw Rule",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Rule Title") },
                    placeholder = { Text("e.g. bKash Merchant Cash-Out") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_title_input"),
                    singleLine = true
                )

                // Provider Selection
                Text("Target MFS Provider", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "BKASH" to ("bKash" to BkashPink),
                        "NAGAD" to ("Nagad" to NagadOrange),
                        "ROCKET" to ("Rocket" to RocketPurple)
                    ).forEach { (code, pair) ->
                        val (label, brandColor) = pair
                        val isSelected = selectedProvider.equals(code, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) brandColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedProvider = code }
                                .testTag("provider_chip_$code")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Trigger Type
                Text("Trigger Condition", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SMS_MATCH" to "SMS Match",
                        "THRESHOLD" to "Min Balance",
                        "SCHEDULED" to "Batch Daily"
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = selectedTrigger == code,
                            onClick = { selectedTrigger = code },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Threshold amount
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it },
                    label = { Text("Trigger Threshold (BDT)") },
                    placeholder = { Text("1000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("threshold_input"),
                    singleLine = true
                )

                if (selectedTrigger == "SMS_MATCH") {
                    OutlinedTextField(
                        value = smsKeyword,
                        onValueChange = { smsKeyword = it },
                        label = { Text("SMS Matching Keyword") },
                        placeholder = { Text("e.g. Received, Cash In, Deposit") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Destination
                Text("Destination Target", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = destinationType == "AGENT",
                        onClick = { destinationType = "AGENT" },
                        label = { Text("Agent Cash-Out", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = destinationType == "BANK",
                        onClick = { destinationType = "BANK" },
                        label = { Text("Bank Account", fontSize = 12.sp) }
                    )
                }

                OutlinedTextField(
                    value = destinationDetail,
                    onValueChange = { destinationDetail = it },
                    label = { Text(if (destinationType == "AGENT") "Agent Number / Name" else "Bank Name & Account No") },
                    placeholder = { Text(if (destinationType == "AGENT") "01711XXXXXX (Main Agent)" else "Dutch-Bangla Bank A/C 104...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_input"),
                    singleLine = true
                )

                // Safety Limits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = maxPerTxText,
                        onValueChange = { maxPerTxText = it },
                        label = { Text("Max/Tx (BDT)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dailyLimitText,
                        onValueChange = { dailyLimitText = it },
                        label = { Text("Daily Limit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // PIN Confirmation Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Require PIN Confirmation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Holds transaction in PENDING status until user PIN is entered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = requiresPin,
                        onCheckedChange = { requiresPin = it },
                        modifier = Modifier.testTag("pin_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a rule title"
                        return@Button
                    }
                    val thresh = thresholdText.toDoubleOrNull()
                    if (thresh == null || thresh <= 0) {
                        errorMessage = "Please enter a valid threshold amount"
                        return@Button
                    }
                    val maxTx = maxPerTxText.toDoubleOrNull() ?: 25000.0
                    val daily = dailyLimitText.toDoubleOrNull() ?: 100000.0

                    onSave(
                        title.trim(),
                        selectedProvider,
                        selectedTrigger,
                        thresh,
                        smsKeyword.trim(),
                        destinationType,
                        destinationDetail.trim(),
                        maxTx,
                        daily,
                        requiresPin
                    )
                },
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
