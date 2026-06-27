package com.example.myapplication.ui.audio

import android.net.Uri

/**
 * AudioItem — represents one imported audio clip.
 *
 * @param id        Unique id (System.currentTimeMillis())
 * @param name      Display name (filename without extension)
 * @param uri       Content URI pointing to the audio file in app storage
 * @param durationMs Duration in milliseconds (max 30_000)
 * @param assignedPad  Which pad index (0-7) this audio is assigned to, -1 = unassigned
 */
data class AudioItem(
    val id: Long,
    val name: String,
    val uri: Uri,
    val durationMs: Long,
    var assignedPad: Int = -1,
    var assignedKit: Int = 0
)

/**
 * AudioRepository — single in-memory store shared across all audio screens.
 *
 * Kept as an object (singleton) so ImportScreen, AudioListScreen,
 * and ExportScreen all see the same list without passing it through
 * every composable.
 */
object AudioRepository {
    val audios = mutableListOf<AudioItem>()

    fun add(item: AudioItem) {
        audios.add(item)
    }

    fun remove(id: Long) {
        audios.removeAll { it.id == id }
    }

    fun assignPadToKit(
        id: Long,
        padIndex: Int,
        kitIndex: Int
    ) {
        audios.find { it.id == id }?.apply {
            assignedPad = padIndex
            assignedKit = kitIndex
        }
    }

    /** Returns the AudioItem assigned to [padIndex], or null. */
    fun audioForPad(
        kitIndex: Int,
        padIndex: Int
    ): AudioItem? =
        audios.find {
            it.assignedKit == kitIndex &&
                    it.assignedPad == padIndex
        }

    fun getAll(): List<AudioItem> = audios.toList()
}
