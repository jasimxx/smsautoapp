package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun SettingsAndLiveSetupScreen(
    isLiveMode: Boolean,
    webhookUrl: String,
    apiKey: String,
    autoPinVault: String,
    bkashSimSlot: Int,
    nagadSimSlot: Int,
    rocketSimSlot: Int,
    onToggleLiveMode: (Boolean) -> Unit,
    onUpdateWebhookUrl: (String) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onUpdateAutoPinVault: (String) -> Unit,
    onUpdateBkashSimSlot: (Int) -> Unit,
    onUpdateNagadSimSlot: (Int) -> Unit,
    onUpdateRocketSimSlot: (Int) -> Unit,
    onTestUssdDial: (String, Int) -> Unit
) {
    val context = LocalContext.current
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] == true || permissions[Manifest.permission.READ_SMS] == true
    }

    var webhookUrlInput by remember(webhookUrl) { mutableStateOf(webhookUrl) }
    var apiKeyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var pinVaultInput by remember(autoPinVault) { mutableStateOf(autoPinVault) }
    var bkashFeeInput by remember { mutableStateOf("1.85") }
    var nagadFeeInput by remember { mutableStateOf("1.25") }
    var rocketFeeInput by remember { mutableStateOf("1.80") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Live Mode, Webhook & Auto-Dial Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure website confirmation webhooks and MFS USSD auto-dial strings",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Mode Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            if (isLiveMode) Icons.Default.Sensors else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isLiveMode) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (isLiveMode) "Live SMS Listening Mode" else "Safe Demo Sandbox Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isLiveMode) "Auto-parses real SMS from bKash/Nagad/Rocket & Auto-dials" else "Simulates triggers without calling dialer",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isLiveMode,
                        onCheckedChange = { onToggleLiveMode(it) },
                        modifier = Modifier.testTag("live_mode_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Permission Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Android SMS & Phone Call Permissions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = if (hasSmsPermission) "RECEIVE_SMS & READ_SMS Granted" else "SMS Permission Required for Live Mode",
                            fontSize = 11.sp,
                            color = if (hasSmsPermission) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }

                    if (!hasSmsPermission) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS,
                                        Manifest.permission.CALL_PHONE
                                    )
                                )
                            },
                            modifier = Modifier.testTag("request_permission_button")
                        ) {
                            Text("Grant Permission", fontSize = 12.sp)
                        }
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = Color(0xFF10B981))
                    }
                }
            }
        }

        // 24/7 Uninterrupted Background Mode & Doze Exemption Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Power, contentDescription = null, tint = Color(0xFF059669))
                    Text(
                        text = "⚡ ২৪/৭ পাওয়ারফুল ব্যাকগ্রাউন্ড মোড (No Missed Deposit)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF065F46)
                    )
                }

                Text(
                    text = "ফোন স্ক্রিন লক থাকলেও বা ব্যাকগ্রাউন্ডে থাকলেও SMS আসার সাথে সাথে ইনস্ট্যান্ট ডিপোজিট প্রসেস ও ওয়েব হুক পোস্ট হবে। ডিভাইস রিবুট হলেও সার্ভিস অটো-স্টার্ট হবে।",
                    fontSize = 12.sp,
                    color = Color(0xFF047857)
                )

                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent().apply {
                                action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(fallbackIntent)
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "ফোন সেটিংস থেকে অ্যাপের Battery Optimization Off করুন", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_opt_button")
                ) {
                    Icon(Icons.Default.BatterySaver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ব্যাটারি অপটিমাইজেশন বন্ধ করুন (Ignore Battery Opt)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Website Webhook Integration Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Http, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Website Deposit & Withdraw Webhook API", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(
                    text = "Incoming SMS triggers a POST confirmation to your website endpoint in real-time.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = webhookUrlInput,
                    onValueChange = {
                        webhookUrlInput = it
                        onUpdateWebhookUrl(it)
                    },
                    label = { Text("Website Webhook Endpoint URL") },
                    placeholder = { Text("https://your-website.com/api/mfs-webhook") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        onUpdateApiKey(it)
                    },
                    label = { Text("Authorization Secret API Key") },
                    placeholder = { Text("sec_token_998877") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "HMAC-SHA256 Signed Headers Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val newKey = com.example.network.WebhookManager.generateSecureApiKey()
                            apiKeyInput = newKey
                            onUpdateApiKey(newKey)
                        },
                        modifier = Modifier.testTag("regenerate_key_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate Key", fontSize = 11.sp)
                    }
                }
            }
        }

        // Dual SIM Slot Assignment Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Dual SIM Slot Routing Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(
                    text = "Specify which SIM slot (SIM 1 or SIM 2) has your bKash, Nagad, or Rocket account for automatic USSD dialing.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // bKash SIM Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("bKash SIM Slot:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = bkashSimSlot == 0,
                            onClick = { onUpdateBkashSimSlot(0) },
                            label = { Text("SIM 1") }
                        )
                        FilterChip(
                            selected = bkashSimSlot == 1,
                            onClick = { onUpdateBkashSimSlot(1) },
                            label = { Text("SIM 2") }
                        )
                    }
                }

                // Nagad SIM Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nagad SIM Slot:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = nagadSimSlot == 0,
                            onClick = { onUpdateNagadSimSlot(0) },
                            label = { Text("SIM 1") }
                        )
                        FilterChip(
                            selected = nagadSimSlot == 1,
                            onClick = { onUpdateNagadSimSlot(1) },
                            label = { Text("SIM 2") }
                        )
                    }
                }

                // Rocket SIM Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rocket SIM Slot:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = rocketSimSlot == 0,
                            onClick = { onUpdateRocketSimSlot(0) },
                            label = { Text("SIM 1") }
                        )
                        FilterChip(
                            selected = rocketSimSlot == 1,
                            onClick = { onUpdateRocketSimSlot(1) },
                            label = { Text("SIM 2") }
                        )
                    }
                }
            }
        }

        // USSD Auto-Dial & PIN Vault
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("USSD Auto-Dialer & MFS PIN Config", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(
                    text = "Enter your PIN below to append it automatically to the USSD dial command (*247*1*1*Number*Amount*PIN#). Leave blank if you prefer typing PIN manually on phone screen.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pinVaultInput,
                    onValueChange = {
                        pinVaultInput = it
                        onUpdateAutoPinVault(it)
                    },
                    label = { Text("MFS PIN (Optional for Auto-Confirm)") },
                    placeholder = { Text("Leave blank to enter PIN manually") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_vault_input"),
                    singleLine = true
                )

                val sampleUssd = if (pinVaultInput.isNotBlank()) "*247*1*1*01711223344*2500*$pinVaultInput#" else "*247*1*1*01711223344*2500#"
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Sample Auto-Dial Code Preview:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(sampleUssd, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { onTestUssdDial(sampleUssd, bkashSimSlot) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhoneCallback, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Auto-Dial USSD Code")
                }
            }
        }

        // MFS Fee Configuration
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MFS Cash-Out Fee Configurator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "Standard cash-out charges in Bangladesh for calculating net payouts",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bkashFeeInput,
                        onValueChange = { bkashFeeInput = it },
                        label = { Text("bKash %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nagadFeeInput,
                        onValueChange = { nagadFeeInput = it },
                        label = { Text("Nagad %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rocketFeeInput,
                        onValueChange = { rocketFeeInput = it },
                        label = { Text("Rocket %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Production Setup Info
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Production Integration & MFS Automation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    text = "• When an SMS arrives, deposit status is immediately posted to your website URL.\n" +
                            "• Withdrawal rules trigger instant USSD dialing (*247# / *167# / *322#) and notify your website.\n" +
                            "• Ensure appropriate daily and per-transaction limits are configured for safety.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
