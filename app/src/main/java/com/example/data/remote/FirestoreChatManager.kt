package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FirestoreChatManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "FirestoreChatManager"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                val app = FirebaseApp.getInstance()
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore for Chat with custom DB ID", e)
            null
        }
    }

    // In-memory cache for instant real-time UI response and offline fallback
    private val _threadsState = MutableStateFlow<List<ChatThread>>(emptyList())
    val threadsState: Flow<List<ChatThread>> = _threadsState.asStateFlow()

    private val _messagesByThread = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    private var activeMessagesListener: ListenerRegistration? = null
    private var activeThreadIdListening: String? = null

    init {
        listenToThreadsFromFirestore()
    }

    private fun listenToThreadsFromFirestore() {
        val db = firestore ?: return
        try {
            db.collection("chat_threads")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to threads error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val threads = snapshot.documents.mapNotNull { doc ->
                            try {
                                ChatThread(
                                    id = doc.id,
                                    craftsmanId = doc.getLong("craftsmanId") ?: 0L,
                                    craftsmanName = doc.getString("craftsmanName") ?: "",
                                    craftsmanCraft = doc.getString("craftsmanCraft") ?: "",
                                    craftsmanPhone = doc.getString("craftsmanPhone") ?: "",
                                    clientId = doc.getString("clientId") ?: "client_user",
                                    clientName = doc.getString("clientName") ?: "الزبون",
                                    clientLocation = doc.getString("clientLocation") ?: "طرابلس",
                                    lastMessage = doc.getString("lastMessage") ?: "",
                                    lastMessageTime = doc.getLong("lastMessageTime") ?: System.currentTimeMillis(),
                                    unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (threads.isNotEmpty()) {
                            _threadsState.value = threads
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to chat_threads", e)
        }
    }

    fun startListeningToMessages(threadId: String) {
        if (activeThreadIdListening == threadId) return
        activeMessagesListener?.remove()
        activeThreadIdListening = threadId

        val db = firestore ?: return
        try {
            activeMessagesListener = db.collection("chat_threads")
                .document(threadId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to messages error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                ChatMessage(
                                    id = doc.id,
                                    threadId = doc.getString("threadId") ?: threadId,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "",
                                    senderRole = doc.getString("senderRole") ?: ChatMessage.ROLE_CLIENT,
                                    text = doc.getString("text") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isRead = doc.getBoolean("isRead") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (messages.isNotEmpty()) {
                            val currentMap = _messagesByThread.value.toMutableMap()
                            currentMap[threadId] = messages
                            _messagesByThread.value = currentMap
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to listen to messages for thread $threadId", e)
        }
    }

    fun stopListeningToMessages() {
        activeMessagesListener?.remove()
        activeMessagesListener = null
        activeThreadIdListening = null
    }

    fun getMessagesFlow(threadId: String): Flow<List<ChatMessage>> {
        startListeningToMessages(threadId)
        return _messagesByThread.map { map -> map[threadId] ?: emptyList() }
    }

    fun sendMessage(
        thread: ChatThread,
        messageText: String,
        senderRole: String = ChatMessage.ROLE_CLIENT,
        senderName: String = "الزبون",
        onComplete: (() -> Unit)? = null
    ) {
        if (messageText.isBlank()) return

        val messageId = "msg_${System.currentTimeMillis()}_${(100..999).random()}"
        val message = ChatMessage(
            id = messageId,
            threadId = thread.id,
            senderId = if (senderRole == ChatMessage.ROLE_CLIENT) thread.clientId else "craftsman_${thread.craftsmanId}",
            senderName = senderName,
            senderRole = senderRole,
            text = messageText.trim(),
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        // 1. Optimistic instant local update
        val currentMessages = _messagesByThread.value[thread.id]?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        val updatedMap = _messagesByThread.value.toMutableMap()
        updatedMap[thread.id] = currentMessages
        _messagesByThread.value = updatedMap

        val updatedThread = thread.copy(
            lastMessage = message.text,
            lastMessageTime = message.timestamp
        )
        val threadList = _threadsState.value.filter { it.id != thread.id }.toMutableList()
        threadList.add(0, updatedThread)
        _threadsState.value = threadList

        // 2. Push to Firestore
        scope.launch(Dispatchers.IO) {
            val db = firestore
            if (db != null) {
                try {
                    // Update thread metadata
                    val threadData = hashMapOf(
                        "craftsmanId" to thread.craftsmanId,
                        "craftsmanName" to thread.craftsmanName,
                        "craftsmanCraft" to thread.craftsmanCraft,
                        "craftsmanPhone" to thread.craftsmanPhone,
                        "clientId" to thread.clientId,
                        "clientName" to thread.clientName,
                        "clientLocation" to thread.clientLocation,
                        "lastMessage" to message.text,
                        "lastMessageTime" to message.timestamp
                    )
                    db.collection("chat_threads").document(thread.id)
                        .set(threadData, SetOptions.merge())

                    // Insert message document
                    val msgData = hashMapOf(
                        "threadId" to thread.id,
                        "senderId" to message.senderId,
                        "senderName" to message.senderName,
                        "senderRole" to message.senderRole,
                        "text" to message.text,
                        "timestamp" to message.timestamp,
                        "isRead" to message.isRead
                    )
                    db.collection("chat_threads").document(thread.id)
                        .collection("messages").document(messageId)
                        .set(msgData)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to push message to Firestore: ${e.message}")
                }
            }

            // 3. Realistic smart coordination reply from craftsman if message was from client
            if (senderRole == ChatMessage.ROLE_CLIENT) {
                simulateCraftsmanReply(thread, messageText)
            }

            onComplete?.invoke()
        }
    }

    private fun simulateCraftsmanReply(thread: ChatThread, clientMsg: String) {
        scope.launch(Dispatchers.IO) {
            delay(1200) // Brief realistic pause

            val replyText = when {
                clientMsg.contains("موقع") || clientMsg.contains("وين") ->
                    "وعليكم السلام مرحبتين بيك، عرفت الموقع في ${thread.clientLocation}، قريب منك واصل في غضون 25 دقيقة إن شاء الله."
                clientMsg.contains("تكلفة") || clientMsg.contains("سعر") || clientMsg.contains("كم") ->
                    "الكشفية والمعاينة مبدئياً 20 د.ل، وبعد المعاينة نحدد لك التكلفة الإجمالية بدقة وبأفضل سعر مناسب يرضيك."
                clientMsg.contains("متى") || clientMsg.contains("وقت") || clientMsg.contains("تبدأ") ->
                    "نقدر نمر عليك اليوم العشية بعد العصر أو غدوة الصبح في التوقيت اللي يساعدك، شنو الأنسب لك؟"
                clientMsg.contains("صورة") || clientMsg.contains("قطع") || clientMsg.contains("مواد") ->
                    "تمام، وفرت القطع الأصلية ومعاي كامل العدة والمعدات اللازمة لشغل متقن ومضمون."
                clientMsg.contains("تمام") || clientMsg.contains("متفقين") || clientMsg.contains("أكد") ->
                    "بارك الله فيك، تم تسجيل الموعد والتنسيق كامل، توكلنا على الله."
                else ->
                    "مرحبتين بيك خوي ${thread.clientName}! قريت رسالتك وجاهز لخدمتك على أكمل وجه. تقدر تأكد الطلب وتحدد وقت المعاينة."
            }

            val replyMsgId = "reply_${System.currentTimeMillis()}"
            val replyMessage = ChatMessage(
                id = replyMsgId,
                threadId = thread.id,
                senderId = "craftsman_${thread.craftsmanId}",
                senderName = thread.craftsmanName,
                senderRole = ChatMessage.ROLE_CRAFTSMAN,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Update in-memory state
            val currentList = _messagesByThread.value[thread.id]?.toMutableList() ?: mutableListOf()
            currentList.add(replyMessage)
            val updatedMap = _messagesByThread.value.toMutableMap()
            updatedMap[thread.id] = currentList
            _messagesByThread.value = updatedMap

            val updatedThread = thread.copy(
                lastMessage = replyMessage.text,
                lastMessageTime = replyMessage.timestamp
            )
            val threadList = _threadsState.value.filter { it.id != thread.id }.toMutableList()
            threadList.add(0, updatedThread)
            _threadsState.value = threadList

            // Also persist reply to Firestore
            val db = firestore
            if (db != null) {
                try {
                    val msgData = hashMapOf(
                        "threadId" to thread.id,
                        "senderId" to replyMessage.senderId,
                        "senderName" to replyMessage.senderName,
                        "senderRole" to replyMessage.senderRole,
                        "text" to replyMessage.text,
                        "timestamp" to replyMessage.timestamp,
                        "isRead" to false
                    )
                    db.collection("chat_threads").document(thread.id)
                        .collection("messages").document(replyMsgId)
                        .set(msgData)

                    db.collection("chat_threads").document(thread.id)
                        .set(hashMapOf(
                            "lastMessage" to replyMessage.text,
                            "lastMessageTime" to replyMessage.timestamp
                        ), SetOptions.merge())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to persist automated reply: ${e.message}")
                }
            }
        }
    }
}
