package com.example.diariiviatges.util

import com.example.diariiviatges.model.RouteEntry

/**
 * Formata una crònica a Markdown per exportar-la amb Storage Access Framework.
 * També la fa servir el botó "Compartir" (Intent.ACTION_SEND).
 */
fun formatAsMarkdown(entry: RouteEntry): String = buildString {
    appendLine("# Crònica de viatge: ${entry.routeName}")
    appendLine()
    appendLine("- **Data:** ${entry.date}")
    appendLine("- **Destí:** ${entry.destination}")
    appendLine("- **Dificultat:** ${entry.difficulty.label}")
    appendLine("- **Pressupost estimat:** ${entry.estimatedBudget} €")
    appendLine("- **Viatge:** ${if (entry.isSolo) "en solitari" else "acompanyat"}")
    appendLine()
    appendLine("## Relat")
    appendLine(entry.notes.ifBlank { "Sense notes." })
}
