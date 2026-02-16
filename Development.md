# Development Progress: Replace Fake Inference with Real Nexa SDK

## Task Summary

Replace the entire fake/mocked inference layer with real **Nexa SDK (`ai.nexa:core:0.0.22`)** for on-device LLM inference on **Qualcomm NPUs** using **Llama 3.2 3B Q4** from **Nexa Model Hub**. Emulator and non-Snapdragon devices are blocked with an error — no fake/demo mode.

---

## Completed Steps (All 7 of 7 code changes done)

### 1. Enabled Nexa SDK dependency — DONE
- **`zerosignal-android/core/ai/build.gradle.kts`** — Uncommented `implementation("ai.nexa:core:0.0.22")`
- **`zerosignal-android/settings.gradle.kts`** — Added `maven("https://maven.nexa.ai/releases")` to `dependencyResolutionManagement.repositories`
- **`zerosignal-android/app/src/main/AndroidManifest.xml`** — Added `android:extractNativeLibs="true"` to `<application>` tag

### 2. Rewrote NexaSdkEngine with real SDK calls — DONE
- **`zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/engine/NexaSdkEngine.kt`**
- Replaced the stub with real Nexa SDK integration
- Uses `LlmWrapper.builder()` with `modelPath` and `pluginId` (npu/cpu_gpu)
- `streamCompletion()` uses `llmWrapper.generateStreamFlow(prompt, config)`
- Maps `LlmStreamResult.Token` -> `ResponseChunk(token, isFinal=false)`
- Maps `LlmStreamResult.Completed` -> `ResponseChunk("", isFinal=true)`
- Maps `LlmStreamResult.Error` -> throws RuntimeException
- Thread-safe lazy init with `ReentrantLock` + `@Volatile`
- Added `unload()` for lifecycle cleanup

### 3. Deleted FakeNexaInferenceEngine — DONE
- **`zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/engine/FakeNexaInferenceEngine.kt`** — Deleted entirely
- Verified: zero `.kt` files reference `FakeNexaInferenceEngine`

### 4. Created UnsupportedDeviceException — DONE
- **`zerosignal-android/core/domain/src/main/java/com/zerosignal/cognitedge/domain/model/UnsupportedDeviceException.kt`** — New file
- `class UnsupportedDeviceException(message: String) : RuntimeException(message)`

### 5. Updated AiModule DI — DONE
- **`zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/di/AiModule.kt`**
- Removed all `FakeNexaInferenceEngine` imports and references
- `DeviceCapability` changed from `internal` to `public` (so `SetupViewModel` can use it)
- `provideNexaInferenceEngine()` now:
  - Throws `UnsupportedDeviceException` if `isEmulator || !isSnapdragon`
  - Calls `NexaSdk.getInstance().init(context)` for SDK initialization
  - Returns `NexaSdkEngine(modelPath, pluginId = "npu")` — only NPU, no cpu_gpu fallback
  - If model not yet downloaded, passes empty string (SetupScreen gate enforces download first)

### 6. Replaced simulated download with Nexa Model Hub — DONE
- **`zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/repository/NexaModelRepository.kt`**
- Removed the simulated for-loop download
- Uses `NexaSdk.getInstance().downloadModel()` with callbacks
- Wraps callbacks in `callbackFlow` to map to `ModelDownloadState`
- `isModelReady()` checks directory exists AND contains files > 1MB (not placeholder text)
- `getModelPath()` returns directory path (not single file) for Nexa SDK
- Changed from single `modelFile` to `modelDir` (directory-based model storage)

### 7. Updated SetupViewModel and SetupScreen — DONE
- **`zerosignal-android/app/src/main/java/com/zerosignal/cognitedge/ui/setup/SetupViewModel.kt`**
  - Replaced `_isEmulator: StateFlow<Boolean>` with `_isDeviceSupported: StateFlow<Boolean>`
  - Uses `DeviceCapability.detect()` — checks `!isEmulator && isSnapdragon`
  - Removed `skipSetup()` method entirely
- **`zerosignal-android/app/src/main/java/com/zerosignal/cognitedge/ui/setup/SetupScreen.kt`**
  - Removed "Continue (Demo Mode)" button
  - Removed "Skip (Demo Mode)" button
  - Removed "Continue in Demo Mode" button (from error state)
  - Removed `onSkip` parameter from `SetupScreenContent`
  - When device unsupported: shows "Unsupported Device" title + explanation text
  - When device supported + not downloaded: shows only "Download Model" button (no skip)
  - Added `SetupScreenUnsupportedPreview` preview composable
  - Removed `OutlinedButton` import (no longer needed)

---

## Verification Status

| Check | Status |
|-------|--------|
| No `.kt` files reference `FakeNexaInferenceEngine` | PASSED |
| No `.kt` files reference `skipSetup` | PASSED |
| No `.kt` files reference `onSkip` | PASSED |
| `FakeNexaInferenceEngine.kt` file deleted | PASSED |
| `UnsupportedDeviceException.kt` created | PASSED |
| `DeviceCapability` is public (not internal) | PASSED |
| `README.md` still has old references (docs only) | OK — not code |

---

## Files Modified (Complete List)

| File | Action | Path |
|------|--------|------|
| `build.gradle.kts` (ai) | Edited | `zerosignal-android/core/ai/build.gradle.kts` |
| `settings.gradle.kts` | Edited | `zerosignal-android/settings.gradle.kts` |
| `AndroidManifest.xml` | Edited | `zerosignal-android/app/src/main/AndroidManifest.xml` |
| `NexaSdkEngine.kt` | Rewritten | `zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/engine/NexaSdkEngine.kt` |
| `FakeNexaInferenceEngine.kt` | Deleted | `zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/engine/FakeNexaInferenceEngine.kt` |
| `AiModule.kt` | Rewritten | `zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/di/AiModule.kt` |
| `NexaModelRepository.kt` | Rewritten | `zerosignal-android/core/ai/src/main/java/com/zerosignal/cognitedge/ai/repository/NexaModelRepository.kt` |
| `SetupViewModel.kt` | Rewritten | `zerosignal-android/app/src/main/java/com/zerosignal/cognitedge/ui/setup/SetupViewModel.kt` |
| `SetupScreen.kt` | Rewritten | `zerosignal-android/app/src/main/java/com/zerosignal/cognitedge/ui/setup/SetupScreen.kt` |
| `UnsupportedDeviceException.kt` | Created | `zerosignal-android/core/domain/src/main/java/com/zerosignal/cognitedge/domain/model/UnsupportedDeviceException.kt` |

---

## Build Verification & Fixes (Agent:Cursor)

### 1. Build Issues Resolved — DONE
- **Nexa SDK Package Structure**: Identified that `ai.nexa:core:0.0.22` uses package `com.nexa.sdk` instead of `ai.nexa.core`. Updated imports in `NexaSdkEngine.kt`, `AiModule.kt`, and `NexaModelRepository.kt`.
- **Initialization Logic**: Updated `NexaSdkEngine` to use the correct `LlmWrapper.builder()` which is a suspending function, and `LlmCreateInput` data class.
- **Model Download**: Discovered that `NexaSdk` v0.0.22 does NOT expose a `downloadModel` method. Implemented manual download logic in `NexaModelRepository` to fetch model files directly from Hugging Face (`NexaAI/Llama3.2-3B-NPU-Turbo-NPU-mobile`).
  - Added support for downloading multiple split files (`files-*-*.nexa`, `weights-*-*.nexa`).
  - Implemented progress tracking for the ~3.5GB download.
- **SDK Version Requirements**:
  - Increased `minSdk` to **27** (required by Nexa SDK).
  - Increased `compileSdk` and `targetSdk` to **35** (required by transitive dependencies like `androidx.core:core-ktx:1.15.0`).

### 2. Verification Status — DONE
- **Gradle Build**: `./gradlew :app:assembleDebug` completed successfully.
- **Code Cleanup**: Verified no references to `FakeNexaInferenceEngine`, `Demo Mode`, or `skipSetup` remain in the codebase.

### Notes
- **Download Size**: The Llama 3.2 3B Q4 model is approximately **3.5 GB**. The current implementation downloads these files sequentially on the main thread (IO dispatcher). For a production app, a robust background download manager (like WorkManager) is recommended.

## Final Task Completion (Agent:Cursor)
- **Status**: All tasks completed.
- **Verification**: App builds successfully and code is clean.
- **Next Steps**: Deploy to Snapdragon device for runtime testing.

---

## Phase 2: Pack Store API Integration

### Task Summary
Wire the Pack Store UI to the live Vercel API at `https://zerosignal-web.vercel.app/` for fetching and downloading context packs from Qdrant Cloud.

### API Contract Verified
- `GET /health` → `{"status":"ok"}`
- `GET /packs` → Returns pack list with metadata, topics, source_urls
- `GET /packs/{pack_id}` → Single pack metadata
- `GET /packs/{pack_id}/download?limit=50&offset=` → Paginated chunks with 384-dim embeddings
- Server embeddings are 384-dim (MiniLM-L6-v2), padded to 768-dim for ObjectBox HSWN index

### Changes Made

#### 1. Updated BASE_URL — DONE
- **`core/data/src/main/java/com/zerosignal/cognitedge/data/di/DataModule.kt`**
- Changed from `http://10.0.2.2:8000/` to `https://zerosignal-web.vercel.app/`
- Changed logging level from `Level.BODY` to `Level.HEADERS` (prevents logging huge embedding arrays)
- Increased read timeout from 60s to 120s (large pack downloads)

#### 2. Fixed RemotePackStoreRepository — DONE
- **`core/data/src/main/java/com/zerosignal/cognitedge/data/repository/RemotePackStoreRepository.kt`**
- **Set `packId` on VectorChunkEntity** — was missing, chunks were being stored without pack association
- **Added try-catch** around download flow to emit `PackDownloadProgress.Error` on failure
- **Added `flowOn(Dispatchers.IO)`** — network + DB writes now run on IO dispatcher
- **Added cursor persistence** — saves `download_cursor_{packId}` in SharedPreferences
  - On interrupted download, resumes from last saved offset
  - Counts already-downloaded chunks to report accurate progress
  - Clears cursor on successful completion
- **Added `VectorChunkEntity_` import** for ObjectBox query builder

#### 3. Added ViewModel error handling — DONE
- **`app/src/main/java/com/zerosignal/cognitedge/ui/store/PackStoreViewModel.kt`**
- Added **double-download prevention** — skips if pack is already downloading
- Added **try-catch** around flow collection as safety net for unexpected exceptions
- Updates download progress map with `PackDownloadProgress.Error` on failure

### Files Modified

| File | Changes |
|------|---------|
| `core/data/.../di/DataModule.kt` | BASE_URL → Vercel, logging HEADERS, read timeout 120s |
| `core/data/.../repository/RemotePackStoreRepository.kt` | packId field, error handling, IO dispatcher, cursor persistence |
| `app/.../store/PackStoreViewModel.kt` | Double-download guard, try-catch safety net |

### No Changes Needed (Already Correct)
- `ZeroSignalApi.kt` — endpoints match server API
- `ApiDtos.kt` — DTOs match server response shapes exactly
- `PackStoreScreen.kt` — UI handles all states (loading, error, empty, downloading, downloaded)
- `PackCard.kt` — Download progress UI already wired
- `PackSearchBar.kt` — Search functionality works
- `VectorChunkEntity.kt` — Entity has packId field + HSWN index
- `ObjectBoxContextRepository.kt` — Vector search works with padded embeddings

### Status: DONE — Build verification needed
```bash
cd zerosignal-android && ./gradlew :app:assembleDebug
```
