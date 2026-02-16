package com.zerosignal.cognitedge.ai.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Dynamically discovers model files from HuggingFace repos.
 * Follows the same pattern as Nexa SDK's official demo app.
 *
 * Instead of hardcoding file lists, we query the HuggingFace API
 * to get the actual file list, then cache it locally as a manifest.
 */
object ModelFileListingUtil {

    private const val TAG = "ModelFileListing"

    data class FileEntry(
        val name: String,
        val size: Long
    )

    /**
     * Lists all files in a HuggingFace repo by calling:
     * GET https://huggingface.co/api/models/{repoId}/tree/main
     *
     * Returns file names (excludes directories and .gitattributes/README).
     */
    suspend fun listFilesFromHuggingFace(repoId: String): List<FileEntry> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://huggingface.co/api/models/$repoId/tree/main"
            Log.d(TAG, "Listing HuggingFace repo: $apiUrl")

            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HuggingFace API returned ${connection.responseCode}")
                connection.disconnect()
                return@withContext emptyList()
            }

            val json = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val files = parseHuggingFaceResponse(json)
            Log.d(TAG, "Found ${files.size} files in $repoId")
            files
        } catch (e: Exception) {
            Log.e(TAG, "Error listing HuggingFace files: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Builds a download URL for a file in a HuggingFace repo.
     */
    fun getDownloadUrl(repoId: String, fileName: String): String {
        return "https://huggingface.co/$repoId/resolve/main/$fileName"
    }

    /**
     * Saves the discovered file list to a local manifest file.
     * Format: one filename per line.
     */
    fun saveManifest(manifestFile: File, files: List<FileEntry>) {
        manifestFile.parentFile?.mkdirs()
        manifestFile.writeText(files.joinToString("\n") { "${it.name}|${it.size}" })
        Log.d(TAG, "Saved manifest with ${files.size} entries to ${manifestFile.absolutePath}")
    }

    /**
     * Reads a previously saved manifest file.
     * Returns null if the manifest doesn't exist.
     */
    fun readManifest(manifestFile: File): List<FileEntry>? {
        if (!manifestFile.exists()) return null
        return try {
            manifestFile.readLines()
                .filter { it.isNotBlank() }
                .map { line ->
                    val parts = line.split("|", limit = 2)
                    FileEntry(
                        name = parts[0],
                        size = parts.getOrNull(1)?.toLongOrNull() ?: 0L
                    )
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read manifest: ${e.message}")
            null
        }
    }

    /**
     * Checks if all files from the manifest exist on disk with non-zero size.
     */
    fun allFilesExist(modelDir: File, files: List<FileEntry>): Boolean {
        return files.all { entry ->
            val file = File(modelDir, entry.name)
            file.exists() && file.length() > 0
        }
    }

    private fun parseHuggingFaceResponse(json: String): List<FileEntry> {
        val files = mutableListOf<FileEntry>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val type = item.optString("type", "")
                val path = item.optString("path", "")
                val size = item.optLong("size", 0L)

                // Only include actual model files, skip metadata
                if (type == "file" && path.isNotEmpty()
                    && !path.startsWith(".")
                    && path != "README.md"
                    && path != ".gitattributes"
                ) {
                    files.add(FileEntry(name = path, size = size))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing HuggingFace JSON: ${e.message}", e)
        }
        return files
    }
}
