package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ChatMessage
import com.example.models.Sender
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import com.example.utils.glassmorphic
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(cornerRadius = 0.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isAiProcessing) GlowingGreen else NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JARVIS NEURAL CHAT",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.clearChatHistory() },
                    modifier = Modifier.testTag("btn_clear_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = TextSecondary
                    )
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty() && streamingText.isEmpty()) {
                    item {
                        EmptyChatState()
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(msg = msg, onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Message", msg.content))
                        Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                    })
                }

                if (streamingText.isNotEmpty()) {
                    item {
                        ChatMessageItem(
                            msg = ChatMessage(
                                sender = Sender.JARVIS,
                                content = streamingText,
                                isStreaming = true
                            ),
                            onCopy = {}
                        )
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .glassmorphic(cornerRadius = 24.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Input Button
                IconButton(
                    onClick = {
                        if (isListening) viewModel.voiceEngine.stopListening()
                        else viewModel.voiceEngine.startListening()
                    },
                    modifier = Modifier.testTag("chat_voice_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) DangerRed else NeonCyan
                    )
                }

                // Text Input
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening..." else "Ask JARVIS...",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendUserMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.testTag("chat_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank()) NeonCyan else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Neural Network",
                tint = NeonCyan.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NEURAL LINK STANDBY",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask questions, analyze code, or control device hardware.",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage, onCopy: () -> Unit) {
    val isUser = msg.sender == Sender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .glassmorphic(
                    cornerRadius = 16.dp,
                    backgroundColor = if (isUser) Color(0xFF0D253F) else DarkGlassSurface,
                    borderColor = if (isUser) ElectricBlue else GlassBorder
                )
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "USER" else "JARVIS",
                        color = if (isUser) ElectricBlue else NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Message",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCopy() }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                SelectionContainer {
                    Text(
                        text = msg.content,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = if (msg.content.contains("```")) FontFamily.Monospace else FontFamily.Default,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
