package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import org.json.JSONArray
import org.json.JSONObject

data class CustomDeviceTone(
    val id: String,
    val displayName: String,
    val uriString: String,
    val isSystemRingtone: Boolean = false,
    val dateAddedMillis: Long = System.currentTimeMillis()
)

object CustomToneManager {
    private const val PREF_NAME = "aurora_custom_tones"
    private const val KEY_CUSTOM_TONES = "saved_custom_tones"

    fun getSavedCustomTones(context: Context): List<CustomDeviceTone> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CUSTOM_TONES, null) ?: return emptyList()
        val list = mutableListOf<CustomDeviceTone>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomDeviceTone(
                        id = obj.getString("id"),
                        displayName = obj.getString("displayName"),
                        uriString = obj.getString("uriString"),
                        isSystemRingtone = obj.optBoolean("isSystemRingtone", false),
                        dateAddedMillis = obj.optLong("dateAddedMillis", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("CustomToneManager", "Error parsing custom tones", e)
        }
        return list
    }

    fun addCustomTone(context: Context, tone: CustomDeviceTone) {
        val current = getSavedCustomTones(context).filter { it.uriString != tone.uriString }.toMutableList()
        current.add(0, tone)
        saveList(context, current)
    }

    fun removeCustomTone(context: Context, toneId: String) {
        val current = getSavedCustomTones(context).toMutableList()
        val toneToRemove = current.find { it.id == toneId }
        current.removeAll { it.id == toneId }
        saveList(context, current)

        // If it was a copied file, delete the file from storage
        toneToRemove?.let { tone ->
            if (!tone.isSystemRingtone && tone.uriString.startsWith("file://")) {
                try {
                    val file = File(Uri.parse(tone.uriString).path ?: "")
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
        }
    }

    private fun saveList(context: Context, list: List<CustomDeviceTone>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("displayName", item.displayName)
            obj.put("uriString", item.uriString)
            obj.put("isSystemRingtone", item.isSystemRingtone)
            obj.put("dateAddedMillis", item.dateAddedMillis)
            array.put(obj)
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_TONES, array.toString())
            .apply()
    }

    /**
     * Imports an audio file chosen by the user into the app's internal files directory,
     * ensuring that alarm playback works reliably even after reboots or permission expiration.
     */
    fun importAudioFile(context: Context, sourceUri: Uri): CustomDeviceTone? {
        return try {
            val contentResolver = context.contentResolver
            var fileName = "Custom_Tone_${System.currentTimeMillis()}"

            // Query file display name
            try {
                contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            fileName = name
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("CustomToneManager", "Could not query display name, using fallback", e)
            }

            val tonesDir = File(context.filesDir, "custom_tones")
            if (!tonesDir.exists()) {
                tonesDir.mkdirs()
            }

            // Sanitize filename and create destination file
            val safeName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(tonesDir, "${System.currentTimeMillis()}_$safeName")

            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val tone = CustomDeviceTone(
                id = "tone_${System.currentTimeMillis()}",
                displayName = fileName,
                uriString = Uri.fromFile(targetFile).toString(),
                isSystemRingtone = false
            )
            addCustomTone(context, tone)
            tone
        } catch (e: Exception) {
            Log.e("CustomToneManager", "Failed to import custom audio file", e)
            null
        }
    }

    /**
     * Registers a system ringtone/alarm selected via RingtoneManager
     */
    fun registerSystemRingtone(context: Context, ringtoneUri: Uri, title: String): CustomDeviceTone {
        val tone = CustomDeviceTone(
            id = "sys_${System.currentTimeMillis()}",
            displayName = title,
            uriString = ringtoneUri.toString(),
            isSystemRingtone = true
        )
        addCustomTone(context, tone)
        return tone
    }
}
