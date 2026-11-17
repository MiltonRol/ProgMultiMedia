package com.example.diariiviatges.data

import android.content.Context
import android.net.Uri
import com.example.diariiviatges.model.RouteEntry
import com.example.diariiviatges.util.formatAsMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * CAPA DE DADES: aïlla tota l'E/S de fitxers del ViewModel i de la UI.
 *
 * - Intern (filesDir): històric privat en JSON -> s'esborra en desinstal·lar.
 * - Extern (SAF): s'escriu a la Uri que retorna el selector del sistema -> persisteix.
 */
class StorageRepository(private val context: Context) {

    private val fileName = "diari_viatges.json"
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /** Fitxer dins de /data/data/<paquet>/files/ (emmagatzematge intern privat). */
    private fun historyFile(): File = File(context.filesDir, fileName)

    /** Llegeix l'historial en JSON. Si no existeix o és invàlid, retorna una llista buida. */
    suspend fun loadHistory(): List<RouteEntry> = withContext(Dispatchers.IO) {
        val file = historyFile()
        if (!file.exists()) return@withContext emptyList()
        try {
            json.decodeFromString<List<RouteEntry>>(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Desa l'historial complet en JSON dins de filesDir. */
    suspend fun saveHistory(entries: List<RouteEntry>) = withContext(Dispatchers.IO) {
        try {
            historyFile().writeText(json.encodeToString(entries))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Escriu la crònica a la Uri obtinguda amb ActivityResultContracts.CreateDocument. */
    suspend fun writeToUri(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Retorna la crònica en format Markdown (text) per a la exportació. */
    fun formatEntry(entry: RouteEntry): String = formatAsMarkdown(entry)
}
