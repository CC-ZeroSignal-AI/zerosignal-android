# ZeroSignal AI - Offline Android AI Assistant

Offline Retrieval-Augmented Generation (RAG) assistant for Android, built for the Nexa SDK bounty hackathon. Downloads the Llama 3.2 3B NPU-Turbo model on first boot, stores embeddings in ObjectBox with HNSW indexing, and runs inference on Qualcomm Snapdragon NPU. Fully functional on emulator via a fake inference engine.

## Architecture

4-module structure with Hilt dependency injection:

```
:app                  Compose UI, navigation, ViewModels
:core:domain          Shared contracts (models, repository interfaces, use cases)
:core:ai              Inference engines, model catalog, Nexa SDK integration
:core:data            Context loading, vector search, ObjectBox, prompt building
```

### Key components

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain** | `AskQuestionUseCase` | Orchestrates retrieval + inference, returns streaming `AskResult` with source attribution |
| **Domain** | `InferenceRepository` / `ContextRepository` | Clean interfaces for swapping implementations |
| **AI** | `FakeNexaInferenceEngine` | Streams fake responses for emulator/testing |
| **AI** | `NexaSdkEngine` | Real Nexa SDK wrapper (stub until SDK artifact available) |
| **AI** | `NexaModelRepository` | Manages model download lifecycle with `ModelDownloadState` |
| **Data** | `ObjectBoxContextRepository` | HNSW-indexed vector search via ObjectBox (768-dim, cosine) |
| **Data** | `PromptBuilder` | Llama 3.2 chat template (`<\|begin_of_text\|>` format) |
| **App** | `SetupScreen` | First-boot model download with progress UI |
| **App** | `ChatScreen` | Chat interface with message bubbles, auto-scroll, source attribution |

### NPU fallback strategy

```
Snapdragon device    -> plugin_id = "npu"     (real NPU inference)
Non-Snapdragon       -> plugin_id = "cpu_gpu"  (CPU/GPU fallback)
Emulator             -> FakeNexaInferenceEngine (skip download, demo mode)
```

## Context Pack

The app ships with a **Yosemite National Park visitor guide** (`app/src/main/assets/context_pack.json`):
- 13 chunks covering trails, safety, wildlife, campgrounds, emergencies, and more
- 768-dimensional embeddings pre-computed with the same `QueryVectorizer` hash used at query time
- Topics: Park Overview, El Capitan, Half Dome, Yosemite Falls, Bear Safety, Campgrounds, Weather/Seasons, Wilderness Permits, Emergency Procedures, Firefall, Giant Sequoias, Rock Fall Hazards, Transportation

### Sample test questions

- "How do I hike Half Dome?"
- "What are the bear safety rules?"
- "When do Yosemite Falls have the most water?"
- "How do I get a wilderness permit?"
- "What should I do in an emergency in the park?"
- "Tell me about the giant sequoia trees"

## Tech Stack

- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose + Material3 (dark theme)
- **DI**: Hilt + KSP
- **Database**: ObjectBox 4.0.3 with HNSW vector indexing
- **Navigation**: Compose Navigation
- **Build**: Gradle 8.7, AGP 8.5.2, min SDK 26, target SDK 34

## Project Layout

```
app/src/main/java/com/zerosignal/cognitedge/
├── MainActivity.kt                    ComponentActivity + Compose setContent
├── ZeroSignalApp.kt                   @HiltAndroidApp
├── RagViewModel.kt                    @HiltViewModel, chat state + streaming
└── ui/
    ├── theme/                         Dark theme (Color, Type, Theme)
    ├── navigation/NavGraph.kt         Setup -> Chat routes
    ├── setup/
    │   ├── SetupScreen.kt             Model download UI with progress
    │   └── SetupViewModel.kt          Download state management
    └── chat/
        ├── ChatScreen.kt              LazyColumn chat with auto-scroll
        └── components/
            ├── MessageBubble.kt        User/assistant bubbles + markdown
            └── ChatInputBar.kt         Text input + send button

core/domain/src/main/java/.../domain/
├── model/                             ResponseChunk, ContextChunk, ChatMessage, etc.
├── repository/                        InferenceRepository, ContextRepository, ModelRepository
└── usecase/AskQuestionUseCase.kt      RAG orchestrator

core/ai/src/main/java/.../ai/
├── engine/                            NexaInferenceEngine, FakeEngine, NexaSdkEngine, NexaRuntime
├── catalog/NexaModelCatalog.kt        Snapdragon-ready model configs
├── repository/                        NexaInferenceRepository, NexaModelRepository
└── di/AiModule.kt                     Hilt bindings + NPU detection

core/data/src/main/java/.../data/
├── database/entity/VectorChunkEntity  @Entity with @HnswIndex(768-dim)
├── database/ObjectBoxProvider.kt      Singleton BoxStore
├── loader/                            ContextPackLoader, ContextPackParser, ContextPackIngester
├── search/                            ContextRetriever, QueryVectorizer, Similarity
├── prompt/PromptBuilder.kt            Llama 3.2 chat template
├── repository/                        ObjectBoxContextRepository, LocalContextRepository
└── di/DataModule.kt                   Hilt bindings
```

## Building & Running

```bash
# Build debug APK
./gradlew :app:assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk

# Run tests
./gradlew test
```

1. Open in Android Studio (Hedgehog+).
2. Sync Gradle.
3. Run on emulator — Setup screen auto-detects emulator and offers "Continue (Demo Mode)".
4. On Qualcomm Cloud device — Setup screen shows model download progress, then navigates to Chat.

## Replacing the Fake Engine with Real Nexa SDK

When the `ai.nexa:core` artifact is available:

1. Uncomment the dependency in `core/ai/build.gradle.kts`
2. Implement real calls in `NexaSdkEngine.kt` (replace the stub with `LlmWrapper.builder()`)
3. Implement real download in `NexaModelRepository.kt` (replace simulated progress)
4. The `AiModule` already handles NPU detection — no other changes needed

## Tests

```bash
./gradlew test
```

`AskQuestionUseCaseTest` verifies the retrieval + inference pipeline with mock repositories, including source attribution.
