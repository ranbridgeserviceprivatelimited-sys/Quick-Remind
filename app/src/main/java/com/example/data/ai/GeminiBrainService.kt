package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GeminiBrainService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY.ifEmpty { "" }

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun executePrompt(
        prompt: String,
        systemInstruction: String? = null,
        bitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val currentKey = apiKey
        if (currentKey.isBlank() || currentKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚠️ Gemini API Key not configured. Please set your key in the AI Studio Secrets panel. (Offline fallback mode active)"
        }

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Text part
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)

            // Image part if provided
            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                val imagePart = JSONObject().put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                })
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysContent = JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                }
                root.put("systemInstruction", sysContent)
            }

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("topP", 0.95)
            }
            root.put("generationConfig", genConfig)

            val url = "$baseUrl?key=$currentKey"
            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    return@withContext "API error (${response.code}): $errorBody"
                }
                val responseStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                    }
                }
                "No valid response received from Gemini."
            }
        } catch (e: Exception) {
            "Error contacting Gemini AI: ${e.localizedMessage}"
        }
    }

    suspend fun answerAssistantQuery(
        query: String,
        memories: List<MemoryItem>,
        reminders: List<ReminderItem>,
        projects: List<ProjectItem>,
        notes: List<NoteItem>,
        events: List<CalendarEventItem>
    ): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val contextBuilder = StringBuilder()
        contextBuilder.append("Current Local Date/Time: $todayStr\n\n")

        contextBuilder.append("=== USER STORED MEMORIES ===\n")
        memories.forEach {
            contextBuilder.append("- [${it.category}] [Importance: ${it.importance}] (Permanent: ${it.isPermanent}) ${it.title}: ${it.content} (Tags: ${it.tags.joinToString(",")})\n")
        }

        contextBuilder.append("\n=== ACTIVE PROJECTS ===\n")
        projects.forEach {
            val deadline = it.deadlineMillis?.let { d -> dateFormat.format(Date(d)) } ?: "No deadline"
            contextBuilder.append("- Project: ${it.name} (${it.status}, Category: ${it.category}, Deadline: $deadline) Description: ${it.description} | Next Step: ${it.nextStepSuggestion}\n")
        }

        contextBuilder.append("\n=== SMART REMINDERS ===\n")
        reminders.forEach {
            val due = dateFormat.format(Date(it.dueDateMillis))
            val status = if (it.isCompleted) "COMPLETED" else "PENDING"
            contextBuilder.append("- [$status] [Priority: ${it.priority}] ${it.title} Due: $due | Notes: ${it.notes}\n")
        }

        contextBuilder.append("\n=== QUICK NOTES & IDEAS ===\n")
        notes.forEach {
            contextBuilder.append("- [${it.category}] ${it.title}: ${it.content} (Tags: ${it.tags.joinToString(",")})\n")
        }

        contextBuilder.append("\n=== CALENDAR EVENTS ===\n")
        events.forEach {
            val start = dateFormat.format(Date(it.startMillis))
            contextBuilder.append("- Event: ${it.title} at $start (${it.location})\n")
        }

        val systemPrompt = """
            You are 'Quick Remind AI Brain', the ultimate personal memory assistant and knowledge recall companion.
            The user relies on you with the philosophy: "If I tell Quick Remind something once, it should help me remember it later."
            
            Guidelines:
            1. Answer directly, concisely, and accurately based on the user's stored memories, projects, notes, reminders, and events.
            2. Support Telugu, English, and Telugu-English mixed prompts (Tanglish) seamlessly.
               For example, if user asks in Telugu/Tanglish ("Flight controller lo em microcontroller vaduthunna?"), reply in friendly helpful Tanglish or Telugu matching the user's tone.
            3. If the user asks about something not in memory, kindly clarify what is known and suggest saving it.
            4. Be proactive: if relevant, mention related deadlines, pending subtasks, or connected facts.
            5. Keep answers crisp and formatted with clear bullet points.
        """.trimIndent()

        val prompt = """
            User Database Context:
            $contextBuilder

            User Question/Command:
            "$query"

            Please provide the most helpful, accurate, and memory-aware answer:
        """.trimIndent()

        executePrompt(prompt, systemPrompt)
    }

    data class ParsedNLResult(
        val intent: String, // "REMINDER", "MEMORY", "NOTE", "PROJECT", "QUERY"
        val title: String,
        val details: String,
        val category: String,
        val priority: String, // "HIGH", "MEDIUM", "LOW"
        val dueDateTimeMillis: Long? = null,
        val recurrence: String = "NONE", // "DAILY", "WEEKLY", "MONTHLY", "NONE"
        val tags: List<String> = emptyList(),
        val isPermanent: Boolean = true,
        val conversationalReply: String
    )

    suspend fun parseNaturalLanguageVoiceOrText(input: String): ParsedNLResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE", Locale.getDefault()).format(Date(now))

        val systemPrompt = """
            You are Quick Remind's NLP Parser. You parse natural language in English, Telugu, or Telugu-English mixed dialect (Tanglish).
            Examples:
            - "Repu morning 8 ki college ki vellali ani remind cheyyi" -> REMINDER, title: "College ki vellali", time: Tomorrow at 08:00
            - "Remember that my flight controller project uses STM32 microcontroller" -> MEMORY, title: "Flight Controller Microcontroller", details: "Uses STM32 microcontroller", category: "PROJECT", isPermanent: true
            - "Future idea: AI based agriculture drone for crop health" -> NOTE, category: "IDEA", title: "AI based agriculture drone"
            - "Create project Flight Controller with PCB design and sensor integration" -> PROJECT, title: "Flight Controller"
            - "What microcontroller am I using?" -> QUERY

            Current Reference Time: $nowFormatted (Epoch millis: $now)

            Output strictly JSON matching this structure:
            {
              "intent": "REMINDER" | "MEMORY" | "NOTE" | "PROJECT" | "QUERY",
              "title": "Short title",
              "details": "Details or notes",
              "category": "PROJECT" | "PERSONAL" | "IDEA" | "WORK" | "STUDY" | "GENERAL",
              "priority": "HIGH" | "MEDIUM" | "LOW",
              "dueDateTimeMillis": <number in milliseconds since epoch or null if not a timed reminder>,
              "recurrence": "NONE" | "DAILY" | "WEEKLY" | "MONTHLY",
              "tags": ["tag1", "tag2"],
              "isPermanent": true,
              "conversationalReply": "Short friendly confirmation in the language of the prompt"
            }
        """.trimIndent()

        val prompt = "Input: \"$input\"\nReturn JSON only."

        val rawResponse = executePrompt(prompt, systemPrompt)
        try {
            val jsonStart = rawResponse.indexOf('{')
            val jsonEnd = rawResponse.lastIndexOf('}')
            if (jsonStart != -1 && jsonEnd != -1) {
                val jsonStr = rawResponse.substring(jsonStart, jsonEnd + 1)
                val obj = JSONObject(jsonStr)
                val tagsList = mutableListOf<String>()
                val tagsArr = obj.optJSONArray("tags")
                if (tagsArr != null) {
                    for (i in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(i))
                    }
                }

                val dueMillis = if (obj.has("dueDateTimeMillis") && !obj.isNull("dueDateTimeMillis")) {
                    val v = obj.optLong("dueDateTimeMillis", 0L)
                    if (v > now - 86400000L) v else null
                } else null

                return@withContext ParsedNLResult(
                    intent = obj.optString("intent", "MEMORY"),
                    title = obj.optString("title", input.take(40)),
                    details = obj.optString("details", input),
                    category = obj.optString("category", "GENERAL"),
                    priority = obj.optString("priority", "MEDIUM"),
                    dueDateTimeMillis = dueMillis,
                    recurrence = obj.optString("recurrence", "NONE"),
                    tags = tagsList,
                    isPermanent = obj.optBoolean("isPermanent", true),
                    conversationalReply = obj.optString("conversationalReply", "Saved successfully to your Quick Remind!")
                )
            }
        } catch (e: Exception) {
            // Fallback parsing
        }

        // Local fallback if offline or parse error
        val isReminder = input.contains("remind", ignoreCase = true) || input.contains("repu", ignoreCase = true) || input.contains("tomorrow", ignoreCase = true)
        val isNote = input.contains("note", ignoreCase = true) || input.contains("idea", ignoreCase = true)
        val isProject = input.contains("project", ignoreCase = true)

        val intent = when {
            isReminder -> "REMINDER"
            isNote -> "NOTE"
            isProject -> "PROJECT"
            else -> "MEMORY"
        }

        val defaultDue = if (isReminder) now + 3600000L * 2 else null

        ParsedNLResult(
            intent = intent,
            title = input.take(45),
            details = input,
            category = "GENERAL",
            priority = "MEDIUM",
            dueDateTimeMillis = defaultDue,
            recurrence = "NONE",
            tags = listOf("quick-entry"),
            isPermanent = true,
            conversationalReply = "Noted! Saved to Quick Remind."
        )
    }

    suspend fun generateDailyBriefing(
        reminders: List<ReminderItem>,
        projects: List<ProjectItem>,
        tasks: List<ProjectTaskItem>,
        memories: List<MemoryItem>
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Generate an inspiring, structured 'Good Morning / Daily Focus Plan' for the user.
            
            Pending Reminders:
            ${reminders.filter { !it.isCompleted }.joinToString("\n") { "- [Priority ${it.priority}] ${it.title}" }}

            Active Projects:
            ${projects.filter { it.status == ProjectStatus.ACTIVE }.joinToString("\n") { p ->
                val pTasks = tasks.filter { it.projectId == p.id }
                val comp = pTasks.count { it.isCompleted }
                val total = pTasks.size
                "- ${p.name} ($comp/$total tasks done): Next Step -> ${p.nextStepSuggestion}"
            }}

            Key Memories:
            ${memories.filter { it.importance == MemoryImportance.HIGH }.take(5).joinToString("\n") { "- ${it.title}: ${it.content}" }}

            Format the plan with:
            1. 🔥 High Priority (Do First)
            2. ⚡ Medium Priority (Keep Momentum)
            3. 🟢 Low / Routine Tasks
            4. 💡 Proactive AI Tip for today
            Keep it actionable and motivating.
        """.trimIndent()

        executePrompt(prompt, "You are a world-class executive productivity planner.")
    }

    suspend fun generateEveningReview(
        completedReminders: List<ReminderItem>,
        pendingReminders: List<ReminderItem>,
        projects: List<ProjectItem>
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Create a constructive 'Daily Review & Evening Wrap-up':
            
            Completed Today:
            ${completedReminders.joinToString("\n") { "✓ ${it.title}" }.ifEmpty { "None recorded today" }}

            Still Pending:
            ${pendingReminders.joinToString("\n") { "✗ ${it.title}" }.ifEmpty { "All clear!" }}

            Active Projects Status:
            ${projects.joinToString("\n") { "- ${it.name}" }}

            Provide:
            - Summary of wins
            - Carried-forward action items
            - 2 top focus areas for tomorrow morning
        """.trimIndent()

        executePrompt(prompt, "You are an empathetic, insightful daily review coach.")
    }

    suspend fun extractFromImageMemory(bitmap: Bitmap): ParsedNLResult = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze this image (whiteboard, document, receipt, handwritten note, or object).
            Extract all key facts, actionable tasks, formulas, or project notes.
            
            Return strictly a JSON object:
            {
              "intent": "MEMORY" | "NOTE" | "REMINDER",
              "title": "Clear descriptive title of the image content",
              "details": "Detailed extracted facts and text from the image",
              "category": "PROJECT" | "STUDY" | "WORK" | "IDEA" | "GENERAL",
              "priority": "HIGH" | "MEDIUM" | "LOW",
              "tags": ["extracted-tag1", "extracted-tag2"],
              "conversationalReply": "Summary of what was extracted into memory"
            }
        """.trimIndent()

        val raw = executePrompt(prompt, "You are an OCR and smart visual memory indexing expert.", bitmap)
        try {
            val jsonStart = raw.indexOf('{')
            val jsonEnd = raw.lastIndexOf('}')
            if (jsonStart != -1 && jsonEnd != -1) {
                val jsonStr = raw.substring(jsonStart, jsonEnd + 1)
                val obj = JSONObject(jsonStr)
                val tagsList = mutableListOf<String>()
                val tagsArr = obj.optJSONArray("tags")
                if (tagsArr != null) {
                    for (i in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(i))
                    }
                }
                return@withContext ParsedNLResult(
                    intent = obj.optString("intent", "MEMORY"),
                    title = obj.optString("title", "Image Memory"),
                    details = obj.optString("details", raw),
                    category = obj.optString("category", "STUDY"),
                    priority = obj.optString("priority", "NORMAL"),
                    tags = tagsList,
                    isPermanent = true,
                    conversationalReply = obj.optString("conversationalReply", "Image information extracted into memory!")
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        ParsedNLResult(
            intent = "MEMORY",
            title = "Scanned Visual Memory",
            details = raw,
            category = "GENERAL",
            priority = "MEDIUM",
            tags = listOf("image-scan"),
            isPermanent = true,
            conversationalReply = "Saved photo memory successfully."
        )
    }

    suspend fun generateProactiveInsight(
        memories: List<MemoryItem>,
        reminders: List<ReminderItem>,
        projects: List<ProjectItem>,
        tasks: List<ProjectTaskItem>
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the user's workload and habits across these items and generate 1 short, high-value proactive suggestion (under 40 words):
            - Projects: ${projects.map { "${it.name} (${tasks.count { t -> t.projectId == it.id && t.isCompleted }}/${tasks.count { t -> t.projectId == it.id }} done)" }}
            - Pending reminders: ${reminders.filter { !it.isCompleted }.map { it.title }}
            - High priority memories: ${memories.filter { it.importance == MemoryImportance.HIGH }.map { it.title }}
            
            Example output style:
            "Your Flight Controller testing task has been pending for 3 days. Dedicate 30 mins this afternoon to test IMU communication."
        """.trimIndent()

        executePrompt(prompt, "You are a proactive AI assistant watching for bottlenecks.")
    }
}
