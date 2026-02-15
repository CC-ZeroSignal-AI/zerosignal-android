# ZeroSignal AI - Offline Android AI Assistant

Offline Retrieval-Augmented Generation (RAG) assistant for Android, built for the Nexa SDK bounty hackathon. Downloads knowledge packs from a remote server, stores embeddings in ObjectBox with HNSW indexing, and runs inference on Qualcomm Snapdragon NPU. Fully functional on emulator via a fake inference engine.

## Features

- **Chat** - Conversational AI with source attribution, streaming responses, and Llama 3.2 chat template
- **Pack Store** - Browse, search, and download knowledge packs from the ZeroSignal web server
- **Offline RAG** - All inference and retrieval runs locally after packs are downloaded
- **NPU Acceleration** - Snapdragon NPU inference with CPU/GPU fallback

## Architecture

4-module Clean Architecture with Hilt dependency injection:

```
:app                  Compose UI, navigation, ViewModels
:core:domain          Shared contracts (models, repository interfaces, use cases)
:core:ai              Inference engines, model catalog, Nexa SDK integration
:core:data            ObjectBox, networking (Retrofit), vector search, prompt building
```

### Navigation Flow

```
SetupScreen ──> MainScreen (Bottom Navigation)
                  ├── Tab 0: ChatScreen
                  └── Tab 1: PackStoreScreen
```

### Key Components

| Layer | Component | Description |
|-------|-----------|-------------|
| **Domain** | `AskQuestionUseCase` | Orchestrates retrieval + inference, returns streaming `AskResult` with source attribution |
| **Domain** | `RemotePack` / `TopicStat` | Domain models for pack metadata from the server |
| **Domain** | `PackDownloadProgress` | Sealed class: `Idle`, `Downloading(progress, downloaded, total)`, `Completed`, `Error(message)` |
| **Domain** | `PackStoreRepository` | Interface for fetching packs, downloading, tracking download state |
| **Domain** | `InferenceRepository` / `ContextRepository` | Clean interfaces for swapping implementations |
| **AI** | `FakeNexaInferenceEngine` | Streams fake responses for emulator/testing |
| **AI** | `NexaSdkEngine` | Real Nexa SDK wrapper (stub until SDK artifact available) |
| **AI** | `NexaModelRepository` | Manages model download lifecycle with `ModelDownloadState` |
| **Data** | `ObjectBoxContextRepository` | HNSW-indexed vector search via ObjectBox (768-dim, cosine) |
| **Data** | `RemotePackStoreRepository` | Retrofit-based pack fetching + paginated download with cursor |
| **Data** | `ZeroSignalApi` | Retrofit interface matching the ZS-web FastAPI endpoints |
| **Data** | `PromptBuilder` | Llama 3.2 chat template (`<\|begin_of_text\|>` format) |
| **App** | `PackStoreViewModel` | Pack listing, client-side search, download orchestration |
| **App** | `PackStoreScreen` | Browse packs with gradient cards, topic chips, download progress |
| **App** | `MainScreen` | Bottom navigation scaffold (Chat / Pack Store) |
| **App** | `SetupScreen` | First-boot model download with progress UI |
| **App** | `ChatScreen` | Chat interface with message bubbles, auto-scroll, source attribution |

### NPU Fallback Strategy

```
Snapdragon device    -> plugin_id = "npu"     (real NPU inference)
Non-Snapdragon       -> plugin_id = "cpu_gpu"  (CPU/GPU fallback)
Emulator             -> FakeNexaInferenceEngine (skip download, demo mode)
```

## Server Integration (ZS-web)

The Android app connects to the **ZS-web** FastAPI server for pack management. The server uses Qdrant for vector storage and sentence-transformers for embeddings.

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/packs` | List all packs with metadata, topics, document counts |
| `GET` | `/packs/{pack_id}` | Get single pack metadata |
| `GET` | `/packs/{pack_id}/download?limit=50&offset=` | Paginated download with cursor-based pagination (`next_offset`) |
| `POST` | `/packs/{pack_id}/search` | Semantic search within a pack (body: `{ "query": "...", "top_k": 5 }`) |

### Base URL Configuration

The server base URL is configured in `DataModule.kt`:

```kotlin
// For Android emulator (maps localhost to host machine)
private const val BASE_URL = "http://10.0.2.2:8000/"

// For physical device on same network
// private const val BASE_URL = "http://<your-ip>:8000/"
```

### Download Flow

Pack downloads use cursor-based pagination:

1. App calls `GET /packs/{id}/download?limit=50`
2. Server returns a batch of documents with embeddings + `next_offset`
3. App stores each batch in ObjectBox
4. Loop continues with `offset=next_offset` until `next_offset` is `null`
5. Progress emitted as `Flow<PackDownloadProgress>` to the UI

### Data Mapping

```
Server (Python)              ->  Android (Kotlin)
─────────────────────────────────────────────────
PackMetadata                 ->  PackMetadataDto  ->  RemotePack
TopicStat                    ->  TopicStatDto     ->  TopicStat
DownloadResponse             ->  DownloadResponseDto
DownloadItem                 ->  DownloadItemDto  ->  VectorChunkEntity (ObjectBox)
SearchResult                 ->  SearchResultDto
```

## Tech Stack

- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose + Material3 (dark-only cyberpunk theme)
- **DI**: Hilt 2.51.1 + KSP
- **Database**: ObjectBox 4.0.3 with HNSW vector indexing (768-dim, cosine)
- **Networking**: Retrofit2 + OkHttp3 + Moshi (JSON)
- **Navigation**: Compose Navigation + Bottom NavigationBar
- **Build**: Gradle 8.7, AGP 8.5.2, min SDK 26, target SDK 34, Java 17

## UI Theme

Dark-only theme with cyberpunk-inspired accents:

| Color | Hex | Usage |
|-------|-----|-------|
| Neon Cyan | `#00E5FF` | Primary, icons, active indicators |
| Electric Purple | `#BB86FC` | Secondary, domain badges, topic chip borders |
| Gradient Start | `#00E5FF` | Gradient pairs for text, buttons, borders |
| Gradient End | `#BB86FC` | Gradient pairs for text, buttons, borders |
| Downloading Amber | `#FFB300` | Download progress bars |
| Downloaded Green | `#00E676` | Completed download status |
| Surface Dark | `#0A0A0F` | App background |
| Surface Card | `#12121A` | Card backgrounds |
| Nav Bar Surface | `#08080D` | Bottom navigation bar |

## Project Layout

```
app/src/main/java/com/zerosignal/cognitedge/
├── MainActivity.kt                    ComponentActivity + Compose setContent
├── ZeroSignalApp.kt                   @HiltAndroidApp
├── RagViewModel.kt                    @HiltViewModel, chat state + streaming
└── ui/
    ├── theme/
    │   ├── Color.kt                   Cyberpunk color palette
    │   ├── Theme.kt                   Material3 dark color scheme
    │   └── Type.kt                    Typography with letter spacing
    ├── navigation/
    │   ├── NavGraph.kt                Setup -> Main route
    │   └── MainScreen.kt             Bottom nav scaffold (Chat | Packs)
    ├── setup/
    │   ├── SetupScreen.kt             Model download UI with progress
    │   └── SetupViewModel.kt          Download state management
    ├── chat/
    │   ├── ChatScreen.kt              LazyColumn chat with gradient empty state
    │   └── components/
    │       ├── MessageBubble.kt        Gradient-bordered assistant bubbles
    │       └── ChatInputBar.kt         Gradient-bordered input + send button
    └── store/
        ├── PackStoreScreen.kt          Pack listing with search, loading/error states
        ├── PackStoreViewModel.kt       Pack fetching, filtering, download management
        └── components/
            ├── PackCard.kt             Gradient-bordered cards with topic chips
            └── SearchBar.kt            Gradient-bordered search input

core/domain/src/main/java/.../domain/
├── model/
│   ├── ResponseChunk.kt               Streaming inference chunks
│   ├── ContextChunk.kt                Vector search results
│   ├── ChatMessage.kt                 Chat message model
│   └── RemotePack.kt                  RemotePack, TopicStat, PackDownloadProgress
├── repository/
│   ├── InferenceRepository.kt         Inference engine interface
│   ├── ContextRepository.kt           Vector storage interface
│   └── PackStoreRepository.kt         Pack store operations interface
└── usecase/
    └── AskQuestionUseCase.kt          RAG orchestrator

core/ai/src/main/java/.../ai/
├── engine/                            NexaInferenceEngine, FakeEngine, NexaSdkEngine, NexaRuntime
├── catalog/NexaModelCatalog.kt        Snapdragon-ready model configs
├── repository/                        NexaInferenceRepository, NexaModelRepository
└── di/AiModule.kt                     Hilt bindings + NPU detection

core/data/src/main/java/.../data/
├── database/
│   ├── entity/VectorChunkEntity.kt    @Entity with @HnswIndex(768-dim)
│   └── ObjectBoxProvider.kt           Singleton BoxStore
├── remote/
│   ├── ZeroSignalApi.kt               Retrofit interface (GET /packs, download, POST search)
│   └── dto/ApiDtos.kt                 Moshi DTOs (PackMetadataDto, DownloadResponseDto, etc.)
├── repository/
│   ├── ObjectBoxContextRepository.kt  HNSW vector search implementation
│   ├── LocalContextRepository.kt      Local context fallback
│   └── RemotePackStoreRepository.kt   Network pack store with paginated download
├── loader/                            ContextPackLoader, ContextPackParser, ContextPackIngester
├── search/                            ContextRetriever, QueryVectorizer, Similarity
├── prompt/PromptBuilder.kt            Llama 3.2 chat template
└── di/DataModule.kt                   Hilt bindings + Retrofit/OkHttp/Moshi providers
```

## Building & Running

### Prerequisites

- Android Studio (Hedgehog+)
- JDK 17
- Android SDK 34
- ZS-web server running (for Pack Store features)

### Build

```bash
# Build debug APK
./gradlew :app:assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk

# Run tests
./gradlew test
```

### Run

1. Open in Android Studio, sync Gradle.
2. Start the ZS-web server: `cd ../ZS-web && uvicorn server.app.main:app --reload`
3. Run on emulator — Setup screen auto-detects emulator and offers "Continue (Demo Mode)".
4. Navigate to **Pack Store** tab to browse and download packs from the server.
5. On Qualcomm Cloud device — Setup screen shows model download progress, then navigates to Chat.

### Server Setup (ZS-web)

The Android app expects the ZS-web FastAPI server to be running:

```bash
cd /path/to/ZS-web
pip install -r requirements.txt
uvicorn server.app.main:app --host 0.0.0.0 --port 8000 --reload
```

The server requires Qdrant running (default: `localhost:6333`). See ZS-web README for full setup.

## Context Pack

The app ships with a **Yosemite National Park visitor guide** (`app/src/main/assets/context_pack.json`):
- 13 chunks covering trails, safety, wildlife, campgrounds, emergencies, and more
- 768-dimensional embeddings pre-computed with the same `QueryVectorizer` hash used at query time
- Topics: Park Overview, El Capitan, Half Dome, Yosemite Falls, Bear Safety, Campgrounds, Weather/Seasons, Wilderness Permits, Emergency Procedures, Firefall, Giant Sequoias, Rock Fall Hazards, Transportation

### Sample Test Questions

- "How do I hike Half Dome?"
- "What are the bear safety rules?"
- "When do Yosemite Falls have the most water?"
- "How do I get a wilderness permit?"
- "What should I do in an emergency in the park?"
- "Tell me about the giant sequoia trees"

## Extending the App

### Adding a New Pack Source

1. Ingest documents into ZS-web via `POST /packs/{pack_id}/documents`
2. The pack automatically appears in the Android Pack Store
3. Users can download it — documents + embeddings stored locally in ObjectBox

### Replacing the Fake Engine with Real Nexa SDK

When the `ai.nexa:core` artifact is available:

1. Uncomment the dependency in `core/ai/build.gradle.kts`
2. Implement real calls in `NexaSdkEngine.kt` (replace the stub with `LlmWrapper.builder()`)
3. Implement real download in `NexaModelRepository.kt` (replace simulated progress)
4. The `AiModule` already handles NPU detection — no other changes needed

### Changing the Server URL

Edit `BASE_URL` in `core/data/.../di/DataModule.kt`:

```kotlin
private const val BASE_URL = "http://your-server:8000/"
```

For physical devices, use your machine's LAN IP instead of `10.0.2.2`.

## Tests

```bash
./gradlew test
```

`AskQuestionUseCaseTest` verifies the retrieval + inference pipeline with mock repositories, including source attribution.
