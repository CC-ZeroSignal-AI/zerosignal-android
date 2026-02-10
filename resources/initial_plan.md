ZeroSignal AI: Architectural Blueprint and Implementation Strategy for Offline-First Intelligence
1. Executive Summary
   1.1 Project Vision and Technical Imperative
   ZeroSignal AI represents a divergence from the prevailing cloud-centric paradigms of artificial intelligence. By mandating a fully offline, on-device architecture, the project addresses critical reliability gaps in environments devoid of connectivity—disaster zones, remote wilderness, and secure facilities. The technical objective is to engineer an Android-first assistant capable of running quantized Large Language Models (LLMs) on Qualcomm Snapdragon Neural Processing Units (NPUs) while leveraging a local Retrieval-Augmented Generation (RAG) pipeline.
   The complexity of this undertaking necessitates a rigorous division of labor. To prevent codebase entropy and integration bottlenecks, the development lifecycle is bifurcated into three distinct engineering roles: the AI Engine Architect, the Data & RAG Systems Engineer, and the UI/UX Experience Lead. This report provides a comprehensive blueprint for this division, detailing the Sovereign Territory of each developer, the immutable interfaces that bind them, and the implementation logic for the system's most critical components: the NPU inference engine, the vector search mechanism, and the "Game-Style" immersive model installer.
   1.2 Architectural Philosophy: Modular Clean Architecture
   The system adopts a strict Multi-Module Clean Architecture. This is not merely a preference but a structural requirement to allow three senior engineers to work in parallel without merge conflicts. The application is dissected into feature and core layers, where the "Domain" layer acts as the demilitarized zone (DMZ) containing shared contracts (Interfaces) and data models. The implementation details—whether using Nexa SDK for AI or ObjectBox for storage—are hidden behind these interfaces, adhering to the Dependency Inversion Principle.
   Table 1: Developer Responsibility Matrix
   Role
   Primary Module Focus
   Key Technologies
   Responsibility
   Dev 1: AI Architect
   :core:ai
   Nexa SDK, Qualcomm Hexagon NPU, TensorFlow Lite delegates
   Model lifecycle, inference optimization, token streaming, memory management.
   Dev 2: Data/RAG
   :core:data, :core:database
   ObjectBox, HNSW Indexing, Coroutines, WorkManager
   Context Pack ingestion, vector embedding, similarity search, file I/O.
   Dev 3: UI/UX
   :feature:chat, :feature:installer
   Jetpack Compose, Canvas, Lottie, StateFlow
   Immersive "Game-Style" UI, chat mechanics, markdown rendering, state management.

2. Structural Blueprint: The Distributed Monolith
   2.1 The Philosophy of Developer Isolation
   To facilitate high-velocity parallel development, the codebase is structured to minimize "surface area" contact between developers. Each developer owns a set of Gradle modules. Code collisions are physically prevented by isolating implementations into separate module directories. The only shared space is the :core:domain module, which contains Kotlin interfaces and data classes. Changes to the Domain module require a "treaty" (consensus) among all three developers, while changes within a specific core module (e.g., :core:ai) are the sole prerogative of the assigned engineer.
   2.2 Detailed File Structure and Package Hierarchy
   The following directory tree illustrates the physical separation of concerns. This structure supports the requirement that the Backend (Context Pack generation) is ready, focusing entirely on the Android client consumption.
   ZeroSignalAI/
   ├── build.gradle.kts (Project Level)
   ├── settings.gradle.kts (Include: :app, :core:domain, :core:ai, :core:data, :core:database, :feature:chat, :feature:installer)
   │
   ├── core/
   │ ├── domain/ (SHARED TERRITORY - STRICTLY CONTROLLED)
   │ │ ├── build.gradle.kts
   │ │ └── src/main/java/com/zerosignal/core/domain/
   │ │ ├── model/
   │ │ │ ├── AiMessage.kt (Data class for chat UI)
   │ │ │ ├── ContextNode.kt (Data class for RAG retrieval)
   │ │ │ ├── ModelDownloadState.kt (Sealed interface for installer)
   │ │ │ └── ContextPackManifest.kt
   │ │ ├── repository/
   │ │ │ ├── LlmRepository.kt (Interface defined by UI, implemented by AI Dev)
   │ │ │ ├── VectorStoreRepository.kt (Interface defined by AI/UI, implemented by Data Dev)
   │ │ │ └── PackInstallerRepository.kt
   │ │ └── usecase/
   │ │ ├── StreamResponseUseCase.kt (Orchestrator)
   │ │ └── IngestPackUseCase.kt
   │ │
   │ ├── ai/ (TERRITORY: DEVELOPER 1)
   │ │ ├── build.gradle.kts (Implementation: ai.nexa:core:0.0.19)
   │ │ └── src/main/java/com/zerosignal/core/ai/
   │ │ ├── di/
   │ │ │ └── AiModule.kt (Hilt provides LlmRepository)
   │ │ ├── internal/
   │ │ │ ├── NexaEngineWrapper.kt (Low-level JNI bindings)
   │ │ │ ├── NpuConfigStrategy.kt (Snapdragon optimization logic)
   │ │ │ └── Tokenizer.kt
   │ │ └── repository/
   │ │ └── NexaLlmRepository.kt (Implementation of Domain Interface)
   │ │
   │ ├── database/ (TERRITORY: DEVELOPER 2 - STORAGE)
   │ │ ├── build.gradle.kts (Implementation: io.objectbox:objectbox-kotlin)
   │ │ └── src/main/java/com/zerosignal/core/database/
   │ │ ├── entity/
   │ │ │ ├── VectorChunkEntity.kt (@Entity with @HnswIndex)
   │ │ │ └── PackMetadataEntity.kt
   │ │ ├── dao/
   │ │ │ └── ContextDao.kt
   │ │ └── di/
   │ │ └── DatabaseModule.kt
   │ │
   │ └── data/ (TERRITORY: DEVELOPER 2 - PIPELINE)
   │ ├── src/main/java/com/zerosignal/core/data/
   │ │ ├── worker/
   │ │ │ ├── ModelDownloadWorker.kt (Long-running Foreground Service)
   │ │ │ └── ContextIngestionWorker.kt
   │ │ ├── network/
   │ │ │ └── ManifestDownloader.kt
   │ │ └── repository/
   │ │ └── RealPackInstallerRepository.kt
   │
   └── feature/ (TERRITORY: DEVELOPER 3)
   ├── installer/
   │ └── src/main/java/com/zerosignal/feature/installer/
   │ ├── components/
   │ │ ├── GlitchEffectText.kt
   │ │ └── HexagonProgressBar.kt
   │ ├── viewmodel/
   │ │ └── InstallerViewModel.kt
   │ └── InstallerScreen.kt
   │
   └── chat/
   └── src/main/java/com/zerosignal/feature/chat/
   ├── components/
   │ ├── MessageBubble.kt
   │ └── ContextSourceCard.kt
   └── ChatScreen.kt
   2.3 Integration Mechanics
   The isolation is enforced via Gradle dependency rules. The :feature modules depend on :core:domain. They do not depend on :core:ai or :core:data directly. Instead, Hilt (Dependency Injection) binds the implementations (e.g., NexaLlmRepository) to the interfaces (e.g., LlmRepository) at runtime in the :app module. This prevents the UI developer from accidentally accessing low-level NPU configuration methods, keeping the architecture pure.1
3. Developer 1: The AI Engine Architect
   3.1 Objective
   The AI Architect's mandate is to implement the "Brain" of ZeroSignal. The core challenge is running a 3B+ parameter model (specifically the Llama3.2-3B-NPU-Turbo) on a mobile device without freezing the UI or draining the battery instantly. This requires deep integration with the Qualcomm Hexagon NPU via the Nexa SDK.
   3.2 The Nexa SDK Implementation
   The Nexa SDK serves as the bridge between the Android application layer and the underlying hardware acceleration. The AI developer must encapsulate this complexity within the NexaEngineWrapper class.
   3.2.1 NPU Initialization Strategy
   To utilize the Qualcomm Hexagon NPU, the initialization logic must explicitly request the npu plugin. The analysis of the Nexa SDK documentation indicates that simply loading the model is insufficient; the ModelConfig must be tuned for the specific hardware capabilities.3

Kotlin


// File: core/ai/src/main/java/com/zerosignal/core/ai/internal/NexaEngineWrapper.kt

package com.zerosignal.core.ai.internal

import com.nexa.sdk.llm.LlmWrapper
import com.nexa.sdk.model.ModelConfig
import com.nexa.sdk.model.VlmCreateInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NexaEngineWrapper @Inject constructor() {

    private var llmInstance: LlmWrapper? = null

    /**
     * Initializes the LLM engine targeting the NPU.
     * This is a heavy operation and must be called from a background thread.
     */
    fun loadModel(modelPath: String) {
        val config = ModelConfig(
            maxTokens = 4096, // Context window size
            temperature = 0.6f, // Slightly deterministic for factual RAG
            pluginId = "npu", // CRITICAL: Targets Qualcomm Hexagon [3, 4]
            enableThinking = false
        )

        // Builder pattern as per Nexa SDK docs [5]
        LlmWrapper.builder()
           .vlmCreateInput(
                VlmCreateInput(
                    model_name = "Llama3.2-3B-NPU-Turbo",
                    model_path = modelPath,
                    config = config,
                    plugin_id = "npu"
                )
            )
           .build()
           .onSuccess { wrapper ->
                llmInstance = wrapper
            }
           .onFailure { error ->
                throw IllegalStateException("Failed to initialize NPU engine: ${error.message}")
            }
    }

    /**
     * Streams tokens one by one for the "Typewriter" effect.
     */
    fun generateStream(prompt: String): Flow<String> = flow {
        val instance = llmInstance?: throw IllegalStateException("Engine not ready")
        
        // Nexa SDK provides a Flow/Stream mechanism [6]
        instance.generateStreamFlow(prompt, GenerationConfig(max_tokens = 512))
           .collect { token ->
                emit(token)
            }
    }
}


3.3 Prompt Engineering for RAG
The AI developer is also responsible for the "Prompt Assembly" logic within the NexaLlmRepository. The raw model does not know about the user's downloaded Context Packs. The Repository must take the context retrieved by Developer 2 and inject it into a rigorous system prompt.
Strategic Insight: Using a "System" prompt structure is crucial for instruction-tuned models like Llama 3.2. The prompt template acts as a firewall, preventing the model from hallucinating information not present in the provided context.

Kotlin


// File: core/ai/src/main/java/com/zerosignal/core/ai/repository/NexaLlmRepository.kt

override fun streamResponse(userQuery: String, retrievedContext: List<String>): Flow<String> {
val contextBlock = retrievedContext.joinToString("\n\n")

    // Strict RAG Prompt Template
    val fullPrompt = """
        <|begin_of_text|><|start_header_id|>system<|end_header_id|>
        You are ZeroSignal, an offline AI assistant optimized for field operations.
        Use ONLY the provided context below to answer the user's question. 
        If the answer is not in the context, state that you do not know.
        
        CONTEXT DATA:
        $contextBlock
        <|eot_id|><|start_header_id|>user<|end_header_id|>
        $userQuery
        <|eot_id|><|start_header_id|>assistant<|end_header_id|>
    """.trimIndent()

    return engineWrapper.generateStream(fullPrompt)
}


4. Developer 2: The Data & RAG Engineer
   4.1 Objective
   The Data Engineer constructs the "Memory" of the system. This involves two massive components: the Vector Database (using ObjectBox) for semantic search, and the Data Pipeline for downloading and ingesting Context Packs. This role requires managing persistent storage and background processing.
   4.2 The ObjectBox Vector Store
   ObjectBox is selected for its high-performance on-device vector search capabilities, specifically its implementation of the HNSW (Hierarchical Navigable Small Worlds) indexing algorithm. This allows for millisecond-level retrieval of context chunks even among millions of entries.7
   4.2.1 Entity Definition with HNSW Index
   The Data Developer defines the VectorChunkEntity in the :core:database module. This entity represents a snippet of knowledge from a Context Pack.

Kotlin


// File: core/database/src/main/java/com/zerosignal/core/database/entity/VectorChunkEntity.kt

package com.zerosignal.core.database.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Property
import io.objectbox.annotation.VectorDistanceType

@Entity
data class VectorChunkEntity(
@Id
var id: Long = 0,

    // The actual text content (e.g., "Yosemite bears are active in spring...")
    val textContent: String,

    // Metadata for citation (e.g., "Yosemite_Guide_v2.pdf")
    val sourceId: String,

    /**
     * The Vector Embedding.
     * Dimensions: 768 is standard for mobile-optimized embedding models (e.g., Nomic/Gemma).
     * DistanceType: COSINE is preferred for semantic text similarity.
     * [9, 8, 10]
     */
    @HnswIndex(dimensions = 768, distanceType = VectorDistanceType.COSINE)
    @Property(type = PropertyType.FloatVector)
    var embedding: FloatArray? = null
)


Technical Nuance: The @HnswIndex annotation triggers ObjectBox to build a graph-based index. The dimensions parameter must strictly match the output of the embedding model used (e.g., 768 for embedding-gemma-300m-npu or 384 for smaller models). A mismatch here will cause runtime crashes or nonsensical search results.10
4.3 Context Pack Management: The Ingestion Pipeline
A "Context Pack" is conceptually a ZIP file containing text data and a manifest. The Data Developer implements a ContextIngestionWorker using Android WorkManager to process this in the background.
The Pipeline Flow:
Download: Fetch the pack zip from the backend.
Unzip & Parse: Extract text files.
Chunking: Split text into 512-token segments with 20% overlap (sliding window) to preserve context across boundaries.
Embedding Generation: Pass chunks to the embedding model (likely embedding-gemma running on NPU via Nexa SDK).
Indexing: Bulk insert chunks into ObjectBox.

Kotlin


// File: core/data/src/main/java/com/zerosignal/core/data/repository/VectorSearchRepositoryImpl.kt

class VectorSearchRepositoryImpl @Inject constructor(
private val boxStore: BoxStore,
private val embeddingService: EmbeddingService // Interface to AI module
) : VectorStoreRepository {

    override suspend fun findRelevantContext(query: String): List<String> {
        // 1. Vectorize the user's query
        val queryVector = embeddingService.getEmbedding(query) // Returns FloatArray

        // 2. Perform ANN Search using ObjectBox
        val box = boxStore.boxFor(VectorChunkEntity::class.java)
        
        // HNSW Query Syntax 
        val results = box.query(
            VectorChunkEntity_.embedding.nearestNeighbors(queryVector, 5) // Top 5 neighbors
        ).build().findWithScores()

        // 3. Extract text
        return results.map { result -> result.get().textContent }
    }
}


4.4 Large File Download Strategy (The "Game-Style" Backend)
Downloading the 2GB+ AI model requires robustness against network flakiness. The Data Developer implements a ModelDownloadWorker that utilizes a Foreground Service to prevent the OS from killing the download when the screen is off.11
Implementation Details:
WorkManager with setForegroundAsync: This is mandatory for long-running tasks (>10 min).
Range Headers: The downloader must support Range: bytes= HTTP headers to resume interrupted downloads.
Checksum Verification: Before the AI engine attempts to load the model, the Data layer must calculate the SHA-256 hash of the downloaded file and compare it against the nexa.manifest.

Kotlin


// File: core/data/src/main/java/com/zerosignal/core/data/worker/ModelDownloadWorker.kt

override suspend fun doWork(): Result {
val modelUrl = inputData.getString("URL")?: return Result.failure()

    // Promote to Foreground Service 
    setForeground(createForegroundInfo(0))

    return try {
        //... Download logic with buffer streams...
        // Update progress every 1%
        setProgress(workDataOf("PROGRESS" to progress))
        
        Result.success()
    } catch (e: Exception) {
        if (e is IOException) Result.retry() else Result.failure()
    }
}


5. Developer 3: The UI/UX Architect
   5.1 Objective
   The UI Developer constructs the "Face" of ZeroSignal. The design brief calls for a "Game-Style" model installer and a highly responsive chat interface. This role operates exclusively in the :feature modules, consuming StateFlows provided by the ViewModels.
   5.2 The "Game-Style" First-Run Model Download
   Standard progress bars are boring. To mask the waiting time for a 2GB download, the UI Developer builds an immersive "Cyberpunk/Sci-Fi" installer screen using Jetpack Compose and Canvas.
   Visual Concept:
   Background: A scrolling hex-grid or particle system (using Canvas).
   Progress: A central "reactor core" that fills up, or a terminal-style text log scrolling rapidly ("Allocating Tensors...", "Verifying Neural Weights...").
   Metrics: Real-time display of download speed and NPU activation status.
   5.2.1 UI State Modeling
   The UI state must capture the granularity of the download process.

Kotlin


// File: feature/installer/src/main/java/com/zerosignal/feature/installer/state/InstallerUiState.kt

sealed interface InstallerUiState {
object Idle : InstallerUiState

    data class Downloading(
        val progress: Float,       // 0.0 to 1.0
        val downloadSpeed: String,  // e.g. "15 MB/s"
        val currentFile: String,    // e.g. "weights-shard-001.nexa"
        val logs: List<String>      // "Terminal" logs
    ) : InstallerUiState
    
    object VerifyingIntegrity : InstallerUiState
    object InitializingNpu : InstallerUiState
    object Complete : InstallerUiState
}


5.2.2 The Immersive Installer Composable
The UI developer uses WorkManager's getWorkInfoByIdLiveData (converted to Flow) to drive this UI.

Kotlin


// File: feature/installer/src/main/java/com/zerosignal/feature/installer/InstallerScreen.kt

@Composable
fun InstallerScreen(viewModel: InstallerViewModel = hiltViewModel()) {
val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Background Animation
        ParticleBackground(speed = 2f)

        // 2. Central HUD
        when (val s = state) {
            is InstallerUiState.Downloading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlitchText(text = "ACQUIRING INTELLIGENCE")
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Custom 'Reactor' Progress Bar
                    ReactorProgressIndicator(progress = s.progress)
                    
                    // Terminal Log Window
                    TerminalLog(logs = s.logs, modifier = Modifier.height(100.dp))
                }
            }
            //... handle other states
        }
    }
}


5.3 The Context Chat Interface
The chat interface must handle the asynchronous nature of token streaming. Unlike a standard REST chat where the full message arrives at once, tokens arrive every ~50-100ms.
Key Implementation Details:
Auto-Scroll: The LazyColumn must detect when the user is at the bottom and auto-scroll as new tokens arrive, but lock scroll if the user has scrolled up to read history.
Markdown: The AI output will contain headers, lists, and code blocks. The UI developer integrates a Markdown rendering library (e.g., compose-markdown) to render this on the fly.
RAG Visualization: To build trust, the UI should show which context chunks were used. "Answered using context from 'Yosemite Guide.pdf' (Page 12)".
6. Detailed Logic: The "Game-Style" First-Run Model Download
   This section synthesizes the work of the Data Developer (Backend logic) and the UI Developer (Visuals). The download process is the critical first impression.
   6.1 The Nexa Model Structure
   The ZeroSignal AI does not download a single blob. Based on Nexa SDK architecture 12, the download involves a specific directory structure that must be replicated on the device:
   /data/data/com.zerosignal/files/models/Llama3.2-3B/
   ├── config.json (Tokenizer settings)
   ├── nexa.manifest (Model metadata)
   ├── tokenizer.model (Token mapping)
   └── files-1-1.nexa (Quantized weights - The large file)
   6.2 The Logic Flow
   Manifest Fetch: The app first downloads nexa.manifest (JSON) from the backend. This file contains the URL and checksum of the massive files-1-1.nexa blob.14
   Space Check: The logic checks for ~4GB of free space. If insufficient, the UI prompts the user to clear space.
   WorkManager Enqueue: A OneTimeWorkRequest is enqueued with the Expedited flag to prioritize it.11
   Foreground Elevation: The Worker immediately calls setForegroundAsync() with a persistent notification ("Downloading Neural Core...").
   Chunked Streaming: The Data Layer streams the file to disk. It calculates a running hash (SHA-256) during the download to avoid reading the 3GB file twice.
   Progress Emission: The Worker emits progress updates via setProgressAsync().
   UI Consumption: The InstallerViewModel observes the WorkInfo via a Flow. It maps the progress (0-100) to the "Reactor Core" animation.
   Completion: Upon success, the Worker moves the files from cache to the no_backup directory (to prevent cloud backup of 3GB files) and signals Success.
7. Deep Dive: Context Pack Management
   The "Context Pack" system allows the user to augment the AI's knowledge base.
   7.1 Context Pack Specification
   A Context Pack is a ZIP file structured as follows:
   manifest.json: Title, Version, Author.
   data/: A folder containing .txt or .md files.
   7.2 The Ingestion Algorithm (RAG)
   This is the most computationally intensive operation in the app. It must be carefully optimized to prevent the UI from stuttering (ANR).
   Sliding Window Chunking: The Data Developer implements a "Sliding Window" algorithm. Simply splitting text every 500 words cuts sentences in half. The algorithm splits by 500 tokens but overlaps the last 50 tokens of the previous chunk with the next. This ensures that semantic meaning at the boundaries is preserved.
   Embedding Batching: The embedding model (Nexa SDK) runs on the NPU. However, loading the embedding model takes time. The system batches chunks (e.g., process 10 chunks at a time) to maximize throughput.
   Transactional Write: ObjectBox supports ACID transactions. Writing 1,000 vectors individually is slow. The Data Developer wraps the box.put(chunks) call in a store.runInTx {... } block for near-instant insertion.9
8. Conclusion
   The ZeroSignal AI architecture is designed for resilience. By rigidly isolating the AI Engine, Data Pipeline, and UI Layer, the project mitigates the risks associated with rapid, parallel development. The use of Nexa SDK ensures that the 3B parameter model runs efficiently on the Snapdragon NPU, unlocking true offline capability. ObjectBox provides the necessary speed for real-time RAG, and WorkManager guarantees that the massive assets required for intelligence are delivered reliably.
   This blueprint transforms the ambitious goal of an "Offline-First AI" from a theoretical concept into a strictly engineered, production-ready reality. The result is an application that does not merely act as a chatbot, but as a robust, autonomous intelligence system capable of operating in the most constrained environments on Earth.
   Works cited
   MVVM Clean Architecture in Android | by Anand Gaur - Medium, accessed February 9, 2026, https://medium.com/@anandgaur2207/mvvm-clean-architecture-in-android-be5ef3f05330
   Android Clean Architecture project structure | by Davide Cirillo - Medium, accessed February 9, 2026, https://medium.com/@dvd.ciri/android-clean-architecture-project-structure-7a6a0569a495
   NexaSDK for Android: a simple way to bring on-device AI to smartphones with Snapdragon, accessed February 9, 2026, https://www.qualcomm.com/developer/blog/2025/11/nexa-ai-for-android-simple-way-to-bring-on-device-ai-to-smartphones-with-snapdragon
   Llama3.2-3B-NPU-Turbo - Nexa SDK | Ship Any AI Model to Any Device in Minutes, accessed February 9, 2026, https://sdk.nexa.ai/model/Llama3.2-3B-NPU-Turbo
   Quickstart - Documentations - Overview - Nexa AI, accessed February 9, 2026, https://docs.nexa.ai/en/nexa-sdk-android/quickstart
   On-device vector database for Android (Java, Kotlin) - ObjectBox, accessed February 9, 2026, https://objectbox.io/the-on-device-vector-database-for-android-and-java/
   On-device Vector Database for Dart/Flutter - ObjectBox, accessed February 9, 2026, https://objectbox.io/on-device-vector-database-for-dart-flutter/
   On-Device Vector Search - ObjectBox Docs, accessed February 9, 2026, https://docs.objectbox.io/on-device-vector-search
   HnswIndex (ObjectBox Java 5.0.0 API), accessed February 9, 2026, https://objectbox.io/docfiles/java/current/io/objectbox/annotation/HnswIndex.html
   Support for long-running workers | Background work - Android Developers, accessed February 9, 2026, https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/long-running
   Model create() failed, error code -1143883480 · Issue #954 · NexaAI/nexa-sdk - GitHub, accessed February 9, 2026, https://github.com/NexaAI/nexa-sdk/issues/954
   NexaAI/OmniNeural-4B at main - Hugging Face, accessed February 9, 2026, https://huggingface.co/NexaAI/OmniNeural-4B/tree/main
   nexa.manifest · NexaAI/DeepSeek-OCR-GGUF-CUDA at main - Hugging Face, accessed February 9, 2026, https://huggingface.co/NexaAI/DeepSeek-OCR-GGUF-CUDA/blob/main/nexa.manifest
   add nexa.manifest · NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile at 4a89b10, accessed February 9, 2026, https://huggingface.co/NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile/commit/4a89b10973ebd4efd391480d764643e74d4f6f11
   Define work requests | Background work - Android Developers, accessed February 9, 2026, https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work
