package com.floapp.agriflo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.ai.AITier
import com.floapp.agriflo.ui.viewmodel.AIAssistantViewModel
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val tier: AITier? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(viewModel: AIAssistantViewModel = hiltViewModel()) {
    var inputText by remember { mutableStateOf("") }
    val messages  by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Quick-ask shortcuts — English only
    val quickQuestions = listOf(
        "When should I plant rice?",
        "How do I fertilize properly?",
        "How do I identify a pest?",
        "What is my estimated profit?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Flo AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            if (viewModel.isOnline) "Online — Cloud AI Active" else "Offline — Local AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (viewModel.isOnline) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = { Icon(Icons.Filled.SmartToy, null, tint = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {

            // Quick question chips — shown only when no messages yet
            if (messages.isEmpty()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Quick Questions:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    quickQuestions.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { question ->
                                AssistChip(
                                    onClick = { viewModel.sendMessage(question) },
                                    label = { Text(question, style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message -> ChatBubble(message) }
                if (isLoading) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Card(shape = RoundedCornerShape(16.dp)) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Flo AI is thinking…", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    coroutineScope.launch { listState.animateScrollToItem(messages.size - 1) }
                }
            }

            // Message input row
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Flo anything…", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(Modifier.size(32.dp).align(Alignment.Bottom), contentAlignment = Alignment.Center) {
                Text("🌾", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.width(8.dp))
        }
        Card(
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 4.dp,
                topEnd   = if (message.isUser) 4.dp  else 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                )
                message.tier?.let { tier ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (tier == AITier.LOCAL) "📱 Offline AI" else "☁️ Cloud AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
