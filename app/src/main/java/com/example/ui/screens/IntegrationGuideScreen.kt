package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IntegrationGuideScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val phpCodeSample = """
<?php
// PHP Webhook Handler for bKash / Nagad / Rocket Automation
header('Content-Type: application/json');

${"$"}secretApiKey = "sec_live_your_secret_key_here";
${"$"}headers = array_change_key_case(getallheaders(), CASE_LOWER);

${"$"}clientToken = str_replace('Bearer ', '', ${"$"}headers['authorization'] ?? '');
${"$"}timestamp = ${"$"}headers['x-timestamp'] ?? '';
${"$"}signature = ${"$"}headers['x-signature'] ?? '';

// 1. Verify Secret API Token
if (${"$"}clientToken !== ${"$"}secretApiKey) {
    http_response_code(401);
    echo json_encode(["status" => "error", "message" => "Unauthorized API key"]);
    exit;
}

// 2. Read incoming JSON Payload & Validate HMAC-SHA256 Signature
${"$"}rawBody = file_get_contents('php://input');
${"$"}expectedSignature = 'sha256=' . hash_hmac('sha256', ${"$"}timestamp . '.' . ${"$"}rawBody, ${"$"}secretApiKey);

if (!empty(${"$"}signature) && !hash_equals(${"$"}expectedSignature, ${"$"}signature)) {
    http_response_code(403);
    echo json_encode(["status" => "error", "message" => "Invalid HMAC Signature"]);
    exit;
}

${"$"}data = json_decode(${"$"}rawBody, true);
${"$"}event = ${"$"}data['event'] ?? '';
${"$"}trxId = ${"$"}data['trx_id'] ?? '';
${"$"}amount = ${"$"}data['amount'] ?? 0;
${"$"}provider = ${"$"}data['provider'] ?? '';

if (${"$"}event === 'DEPOSIT_RECEIVED') {
    ${"$"}senderNumber = ${"$"}data['sender_number'] ?? '';
    
    // TODO: 1. Check database for duplicate TrxID
    // TODO: 2. Match deposit request with user account
    // TODO: 3. Credit user's wallet balance on website
    
    echo json_encode([
        "status" => "success",
        "message" => "Deposit of ৳${"$"}amount confirmed for TrxID: ${"$"}trxId"
    ]);
} elseif (${"$"}event === 'WITHDRAW_EXECUTED') {
    ${"$"}destinationNumber = ${"$"}data['destination_number'] ?? '';
    ${"$"}ussdCommand = ${"$"}data['ussd_command'] ?? '';
    
    // TODO: Mark pending withdrawal order as COMPLETED in DB
    
    echo json_encode([
        "status" => "success",
        "message" => "Withdrawal confirmed for TrxID: ${"$"}trxId"
    ]);
}
?>
    """.trimIndent()

    val nodeJsCodeSample = """
// Node.js (Express) Webhook Integration Handler
const express = require('express');
const crypto = require('crypto');
const app = express();

// Use raw body for HMAC signature verification
app.use(express.json());

const SECRET_API_KEY = "sec_live_your_secret_key_here";

app.post('/api/mfs-webhook', (req, res) => {
  const authHeader = req.headers['authorization'] || '';
  const token = authHeader.replace('Bearer ', '');

  if (token !== SECRET_API_KEY) {
    return res.status(401).json({ status: 'error', message: 'Unauthorized API Key' });
  }

  const timestamp = req.headers['x-timestamp'] || '';
  const signature = req.headers['x-signature'] || '';
  const rawBody = JSON.stringify(req.body);

  if (signature) {
    const expected = 'sha256=' + crypto.createHmac('sha256', SECRET_API_KEY)
      .update(timestamp + '.' + rawBody)
      .digest('hex');

    if (signature !== expected) {
      return res.status(403).json({ status: 'error', message: 'Invalid HMAC Signature' });
    }
  }

  const { event, trx_id, amount, provider, sender_number, destination_number } = req.body;

  if (event === 'DEPOSIT_RECEIVED') {
    console.log(`[DEPOSIT] ${"$"}{provider}: ৳${"$"}{amount} received from ${"$"}{sender_number}. TrxID: ${"$"}{trx_id}`);
    return res.json({ status: 'success', message: 'Deposit processed' });
  } else if (event === 'WITHDRAW_EXECUTED') {
    console.log(`[WITHDRAW] ${"$"}{provider}: ৳${"$"}{amount} sent to ${"$"}{destination_number}. TrxID: ${"$"}{trx_id}`);
    return res.json({ status: 'success', message: 'Withdrawal completed' });
  }
});

app.listen(3000, () => console.log('MFS Webhook server running on port 3000'));
    """.trimIndent()

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "ইউজার ম্যানুয়াল ও ওয়েবসাইট ইন্টিগ্রেশন",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "এপস ডাউনলোড, সেটআপ ও ওয়েবসাইটে অটোমেশন যুক্ত করার সম্পূর্ণ নির্দেশিকা",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("এপস ডাউনলোড ও সেটআপ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("সাইটে এড করার কোড", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // App Download & Setup Guide Tab
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("১. এপস কিভাবে ডাউনলোড ও ইনস্টল করবেন?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "• AI Studio স্ক্রিনের উপরে ডানপাশে **Settings** বা **Export / Share** মেনুতে যান।\n" +
                                "• সেখান থেকে **'Download APK'** অথবা **'Export Project ZIP'** বাটনে ক্লিক করুন।\n" +
                                "• APK ডাউনলোড সম্পন্ন হলে অ্যান্ড্রয়েড ফোনে ইনস্টল করুন (ইনস্টলের সময় 'Install from Unknown Sources' অন রাখুন)।",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("২. মোবাইল এপে অটোমেশন সেটআপ করার নিয়ম", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "১. **Settings স্ক্রিনে যান:** এপস ওপেন করে নিচের 'Settings' ট্যাবে ক্লিক করুন।\n" +
                                "২. **Live SMS Mode চালু করুন:** 'Live SMS Listening Mode' সুইচে ক্লিক করে এটি অন করুন।\n" +
                                "৩. **Permission মঞ্জুর করুন:** স্ক্রিনে আসা `RECEIVE_SMS` এবং `CALL_PHONE` পারমিশন 'Allow' করুন।\n" +
                                "৪. **Website Webhook URL বসান:** আপনার ওয়েবসাইটের API Endpoint (যেমন: `https://your-site.com/api/mfs-webhook`) এবং Secret API Key ফিল্ডে লিখে সেভ করুন।\n" +
                                "৫. **Auto-Withdraw Rule তৈরি করুন:** 'Rules' ট্যাবে গিয়ে bKash, Nagad ও Rocket এর জন্য আলাদা আলাদা সর্বনিম্ন ডিপোজিট থ্রেশহোল্ড এবং এজেন্ট/ব্যাংক ক্যাশআউট নাম্বার সেটআপ করুন।",
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thunderstorm, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("৩. কাজের ফ্লো (How Workflow Works)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "• **ডিপোজিট প্রসেস:** কাস্টমার বিকাশ/নগদ/রকেটে টাকা পাঠালে ফোনে SMS আসার সাথে সাথে এপস সেটি পার্স করবে এবং আপনার ওয়েবসাইটে জমার মেসেজ Webhook এর মাধ্যমে পাঠাবে।\n" +
                                "• **উইথড্র প্রসেস:** সাইট থেকে উইথড্র রিকুয়েস্ট আসলে বা নির্দিষ্ট ব্যালেন্স জমা হলে এপস নিজে থেকেই ক্যাশআউট USSD (*247# / *167# / *322#) ডায়াল করে টাকা পাঠাবে এবং সাইটে কনফার্ম করবে।",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // Website Integration Code Samples Tab
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
                        Text("PHP Webhook Integration Code", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { copyToClipboard(phpCodeSample, "PHP Code") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy PHP Code", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = phpCodeSample,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

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
                        Text("Node.js (Express) Webhook Code", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { copyToClipboard(nodeJsCodeSample, "Node.js Code") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Node.js Code", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = nodeJsCodeSample,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("API JSON Payload Specifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Deposit Event Payload:\n" +
                                "{\n" +
                                "  \"event\": \"DEPOSIT_RECEIVED\",\n" +
                                "  \"trx_id\": \"9B8A7C6D\",\n" +
                                "  \"amount\": 5500.0,\n" +
                                "  \"sender_number\": \"01712345678\",\n" +
                                "  \"provider\": \"BKASH\",\n" +
                                "  \"timestamp\": 1785631680000\n" +
                                "}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
