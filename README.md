# CognitEdge Android MVP

Offline Retrieval-Augmented Generation (RAG) prototype prepared for the Nexa SDK bounty hackathon. The project focuses on the data and native layer so UI is intentionally minimal. Model notes mirror the [Nexa SDK Android overview](https://docs.nexa.ai/en/nexa-sdk-android/overview).

## What works today
- Loads an embedded Context Pack (`context_pack.json`) that mimics the `.cpack` download flow.
- Performs vector search locally via `ContextRetriever` using cosine similarity.
- Builds prompts that separate retrieved snippets from the final question.
- Streams responses from a `FakeNexaInferenceEngine` so the pipeline runs on any device/emulator.

## Nexa models and SDK integration
`rag/NexaModels.kt` lists the Snapdragon-ready binaries surfaced in the Nexa SDK overview (OmniNeural 4B Q8, Granite 4.0 Micro 3B INT4, Llama 3.2 3B Q4, and the Jina v2 reranker). Each entry captures quantization, context window, and bundle names so you can download the matching `*.bin` artifacts when you get SDK access.

1. Implement `NexaRuntime` (see `rag/NexaRuntime.kt`) in C++/JNI per the Nexa docs so it can `loadModel(config)` and `streamCompletion(prompt)`.
2. Wrap that runtime with `NexaBridgeEngine` and hand it to `RagViewModelFactory`. Thanks to the factory signature you can select the desired model config per device tier.
3. Swap out `FakeNexaInferenceEngine` only when the JNI bridge is ready—the fake keeps everything testable today.
4. Optional: run `jina-v2-rerank-npu` by letting `ContextRetriever` take the top-N cosine hits, reranking them using the Nexa runtime before prompt assembly.
5. When you flip the system to on-device embedding generation, replace `QueryVectorizer` with a Nexa embedder or continue consuming downloaded context vectors as the bounty brief describes.

## Project layout
```
app/src/main/java/com/zerosignal/cognitedge
├── MainActivity.kt                # minimal shell to interact with the pipeline
└── rag
    ├── ContextModels.kt           # ContextPack data classes
    ├── ContextPackLoader.kt       # loads pack from assets/local storage
    ├── ContextRetriever.kt        # cosine similarity search
    ├── NexaInference.kt           # Nexa bridge abstraction + streaming fake engine
    ├── NexaModels.kt              # Catalog of Snapdragon-ready Nexa models
    ├── NexaRuntime.kt             # Contract for the JNI runtime
    ├── NexaBridgeEngine.kt        # Loads models via NexaRuntime
    ├── PromptBuilder.kt           # builds final prompt string
    ├── QueryVectorizer.kt         # placeholder query encoder
    └── RagController.kt/.kt       # orchestrates retrieval + generation
```

## Running it
1. Open the folder in Android Studio (Giraffe+).
2. Sync Gradle (Gradle 8.7 / Android Gradle Plugin 8.5.2).
3. Launch the default `app` configuration – MainActivity lets you type a question and shows the streaming answer.

## Replacing the fake engine
```kotlin
val runtime = YourNexaRuntime(applicationContext)
val factory = RagViewModelFactory(
    appContext = applicationContext,
    modelConfig = NexaModelCatalog.byId("omninneural-4b-q8") ?: NexaModelCatalog.defaultLlm()
) { config ->
    NexaBridgeEngine(runtime, config)
}
```
Return `factory` from `MainActivity`'s `viewModels` delegate (or a `Service`). Nothing else in the pipeline changes.

## Tests
`./gradlew test` runs a small unit test that verifies the retrieval stack stays deterministic.
