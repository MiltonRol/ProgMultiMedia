package com.example.diariiviatges.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Nivell de dificultat d'una ruta (Fase 1 de l'enunciat).
 * S'emmagatzema amb kotlinx.serialization pel nom del valor (FACIL, MODERADA, EXIGENT).
 */
enum class Difficulty(val label: String) {
    FACIL("Fàcil"),
    MODERADA("Moderada"),
    EXIGENT("Exigent")
}

/**
 * MODEL immutable que representa una ruta / crònica de viatge.
 * @Serializable permet desar la llista directament com a JSON dins de filesDir.
 */
@Serializable
data class RouteEntry(
    val id: String = UUID.randomUUID().toString(),
    val routeName: String = "",
    val destination: String = "",
    val difficulty: Difficulty = Difficulty.FACIL,
    val estimatedBudget: Double = 0.0,
    val date: String = "",
    val isSolo: Boolean = false,
    val notes: String = ""
)
