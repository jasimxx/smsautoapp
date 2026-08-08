package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.MenuBook
import com.example.ui.components.RuleDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IntegrationGuideScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SettingsAndLiveSetupScreen
import com.example.ui.screens.SimulatorAndLogsScreen

enum class ScreenTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Dashboard),
    RULES("Rules", Icons.Default.Tune),
    SIMULATOR("Simulator", Icons.Default.Sms),
    SETTINGS("Settings", Icons.Default.Settings),
    GUIDE("Guide", Icons.Default.MenuBook)
}

@Composable
fun MainAppContainer(viewModel: AutomationViewModel) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val smsLogs by viewModel.smsLogs.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val isLiveMode by viewModel.isLiveMode.collectAsStateWithLifecycle()
    val webhookUrl by viewModel.webhookUrl.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val autoPinVault by viewModel.autoPinVault.collectAsStateWithLifecycle()
    val bkashSimSlot by viewModel.bkashSimSlot.collectAsStateWithLifecycle()
    val nagadSimSlot by viewModel.nagadSimSlot.collectAsStateWithLifecycle()
    val rocketSimSlot by viewModel.rocketSimSlot.collectAsStateWithLifecycle()
    val bkashBalance by viewModel.bkashBalance.collectAsStateWithLifecycle()
    val nagadBalance by viewModel.nagadBalance.collectAsStateWithLifecycle()
    val rocketBalance by viewModel.rocketBalance.collectAsStateWithLifecycle()
    val simulationState by viewModel.simulationState.collectAsStateWithLifecycle()
    val showRuleDialog by viewModel.showRuleDialog.collectAsStateWithLifecycle()
    val ruleToEdit by viewModel.ruleToEdit.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                ScreenTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                ScreenTab.DASHBOARD -> DashboardScreen(
                    rules = rules,
                    activities = activities,
                    isLiveMode = isLiveMode,
                    bkashBalance = bkashBalance,
                    nagadBalance = nagadBalance,
                    rocketBalance = rocketBalance,
                    onNavigateToRules = { selectedTab = ScreenTab.RULES },
                    onNavigateToSimulator = { selectedTab = ScreenTab.SIMULATOR },
                    onNavigateToSettings = { selectedTab = ScreenTab.SETTINGS },
                    onOpenCreateRule = { viewModel.openCreateRuleDialog() },
                    onExecuteManualWithdrawal = { viewModel.executeManualWithdrawal(it) }
                )
                ScreenTab.RULES -> RulesScreen(
                    rules = rules,
                    onToggleRule = { viewModel.toggleRuleEnabled(it) },
                    onEditRule = { viewModel.openEditRuleDialog(it) },
                    onDeleteRule = { viewModel.deleteRule(it) },
                    onOpenCreateRule = { viewModel.openCreateRuleDialog() }
                )
                ScreenTab.SIMULATOR -> SimulatorAndLogsScreen(
                    simulationState = simulationState,
                    smsLogs = smsLogs,
                    activities = activities,
                    onUpdateInputs = { s, m -> viewModel.updateSimulationInputs(s, m) },
                    onRunSimulation = { viewModel.runSmsSimulation() },
                    onClearLogs = { viewModel.clearLogs() },
                    onExecuteManualWithdrawal = { viewModel.executeManualWithdrawal(it) }
                )
                ScreenTab.SETTINGS -> SettingsAndLiveSetupScreen(
                    isLiveMode = isLiveMode,
                    webhookUrl = webhookUrl,
                    apiKey = apiKey,
                    autoPinVault = autoPinVault,
                    bkashSimSlot = bkashSimSlot,
                    nagadSimSlot = nagadSimSlot,
                    rocketSimSlot = rocketSimSlot,
                    onToggleLiveMode = { viewModel.toggleLiveMode(it) },
                    onUpdateWebhookUrl = { viewModel.updateWebhookUrl(it) },
                    onUpdateApiKey = { viewModel.updateApiKey(it) },
                    onUpdateAutoPinVault = { viewModel.updateAutoPinVault(it) },
                    onUpdateBkashSimSlot = { viewModel.updateBkashSimSlot(it) },
                    onUpdateNagadSimSlot = { viewModel.updateNagadSimSlot(it) },
                    onUpdateRocketSimSlot = { viewModel.updateRocketSimSlot(it) },
                    onTestUssdDial = { code, simSlot -> viewModel.dialUssdCode(code, simSlot) }
                )
                ScreenTab.GUIDE -> IntegrationGuideScreen()
            }
        }

        if (showRuleDialog) {
            RuleDialog(
                ruleToEdit = ruleToEdit,
                onDismiss = { viewModel.closeRuleDialog() },
                onSave = { title, provider, triggerType, threshold, keyword, destType, destDetail, maxTx, daily, reqPin ->
                    viewModel.saveRule(
                        title, provider, triggerType, threshold, keyword, destType, destDetail, maxTx, daily, reqPin
                    )
                }
            )
        }
    }
}
