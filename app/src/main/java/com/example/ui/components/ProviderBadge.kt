package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MfsProvider
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.RocketPurple

@Composable
fun ProviderBadge(
    providerStr: String,
    modifier: Modifier = Modifier
) {
    val provider = MfsProvider.fromString(providerStr)
    val (bgColor, textColor, label) = when (provider) {
        MfsProvider.BKASH -> Triple(BkashPink, Color.White, "bKash")
        MfsProvider.NAGAD -> Triple(NagadOrange, Color.White, "Nagad")
        MfsProvider.ROCKET -> Triple(RocketPurple, Color.White, "Rocket")
    }

    Box(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
