package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SimulationUiState(
    val senderInput: String = "bKash",
    val messageInput: String = "You have received Tk 5,500.00 from 01712345678. Fee Tk 0.00. Balance Tk 24,000.00. TrxID 9B8A7C6D at 01/08/2026 15:40",
    val lastResult: ProcessResult? = null,
    val isProcessing: Boolean = false
)

class AutomationViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AutomationRepository(application.applicationContext)

    val rules: StateFlow<List<WithdrawRule>> = repository.rules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val smsLogs: StateFlow<List<SmsLog>> = repository.smsLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activities: StateFlow<List<WithdrawActivity>> = repository.activities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isLiveMode: StateFlow<Boolean> = repository.isLiveMode
    val webhookUrl: StateFlow<String> = repository.webhookUrl
    val apiKey: StateFlow<String> = repository.apiKey
    val autoPinVault: StateFlow<String> = repository.autoPinVault
    val bkashSimSlot: StateFlow<Int> = repository.bkashSimSlot
    val nagadSimSlot: StateFlow<Int> = repository.nagadSimSlot
    val rocketSimSlot: StateFlow<Int> = repository.rocketSimSlot
    val bkashBalance: StateFlow<Double> = repository.bkashBalance
    val nagadBalance: StateFlow<Double> = repository.nagadBalance
    val rocketBalance: StateFlow<Double> = repository.rocketBalance

    private val _simulationState = MutableStateFlow(SimulationUiState())
    val simulationState: StateFlow<SimulationUiState> = _simulationState.asStateFlow()

    private val _ruleToEdit = MutableStateFlow<WithdrawRule?>(null)
    val ruleToEdit: StateFlow<WithdrawRule?> = _ruleToEdit.asStateFlow()

    private val _showRuleDialog = MutableStateFlow(false)
    val showRuleDialog: StateFlow<Boolean> = _showRuleDialog.asStateFlow()

    fun toggleLiveMode(enabled: Boolean) {
        repository.setLiveMode(enabled)
    }

    fun updateWebhookUrl(url: String) {
        repository.setWebhookUrl(url)
    }

    fun updateApiKey(key: String) {
        repository.setApiKey(key)
    }

    fun updateAutoPinVault(pin: String) {
        repository.setAutoPinVault(pin)
    }

    fun updateBkashSimSlot(slot: Int) {
        repository.setBkashSimSlot(slot)
    }

    fun updateNagadSimSlot(slot: Int) {
        repository.setNagadSimSlot(slot)
    }

    fun updateRocketSimSlot(slot: Int) {
        repository.setRocketSimSlot(slot)
    }

    fun dialUssdCode(code: String, simSlot: Int = 0) {
        repository.triggerUssdDialIntent(code, simSlot)
    }

    fun openCreateRuleDialog() {
        _ruleToEdit.value = null
        _showRuleDialog.value = true
    }

    fun openEditRuleDialog(rule: WithdrawRule) {
        _ruleToEdit.value = rule
        _showRuleDialog.value = true
    }

    fun closeRuleDialog() {
        _showRuleDialog.value = false
        _ruleToEdit.value = null
    }

    fun saveRule(
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
    ) {
        viewModelScope.launch {
            val existing = _ruleToEdit.value
            val rule = if (existing != null) {
                existing.copy(
                    title = title,
                    provider = provider,
                    triggerType = triggerType,
                    thresholdAmount = thresholdAmount,
                    smsKeyword = smsKeyword,
                    destinationType = destinationType,
                    destinationDetail = destinationDetail,
                    maxPerTx = maxPerTx,
                    dailyLimit = dailyLimit,
                    requiresPinConfirmation = requiresPinConfirmation
                )
            } else {
                WithdrawRule(
                    title = title,
                    provider = provider,
                    triggerType = triggerType,
                    thresholdAmount = thresholdAmount,
                    smsKeyword = smsKeyword,
                    destinationType = destinationType,
                    destinationDetail = destinationDetail,
                    maxPerTx = maxPerTx,
                    dailyLimit = dailyLimit,
                    requiresPinConfirmation = requiresPinConfirmation,
                    isEnabled = true
                )
            }
            if (existing != null) {
                repository.updateRule(rule)
            } else {
                repository.addRule(rule)
            }
            closeRuleDialog()
        }
    }

    fun toggleRuleEnabled(rule: WithdrawRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(id: Int) {
        viewModelScope.launch {
            repository.deleteRule(id)
        }
    }

    fun updateSimulationInputs(sender: String, message: String) {
        _simulationState.value = _simulationState.value.copy(
            senderInput = sender,
            messageInput = message
        )
    }

    fun runSmsSimulation() {
        val currentState = _simulationState.value
        viewModelScope.launch {
            _simulationState.value = currentState.copy(isProcessing = true)
            val result = repository.processIncomingSms(currentState.senderInput, currentState.messageInput)
            _simulationState.value = currentState.copy(
                isProcessing = false,
                lastResult = result
            )
        }
    }

    fun executeManualWithdrawal(activityId: Int) {
        viewModelScope.launch {
            repository.executeManualWithdrawal(activityId)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogsAndActivities()
            _simulationState.value = _simulationState.value.copy(lastResult = null)
        }
    }
}
