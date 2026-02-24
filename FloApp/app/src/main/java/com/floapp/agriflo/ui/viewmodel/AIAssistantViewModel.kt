package com.floapp.agriflo.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.ai.AIManager
import com.floapp.agriflo.ai.AIQueryType
import com.floapp.agriflo.ui.screens.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiManager: AIManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isOnline: Boolean
        get() {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            return cm.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        }

    fun sendMessage(text: String) {
        val userMsg = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMsg
        viewModelScope.launch {
            _isLoading.value = true
            val response = aiManager.query(text, AIQueryType.AGRONOMIC_TEXT)
            val aiMsg = ChatMessage(text = response.text, isUser = false, tier = response.tier)
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}
