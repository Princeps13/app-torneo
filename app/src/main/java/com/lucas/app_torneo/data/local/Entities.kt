package com.lucas.app_torneo.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TournamentType,
    val creadoEn: Long,
    val estado: TournamentStatus,
    val seedRandom: Long
)

enum class TournamentType { LIGA, LLAVES }
enum class TournamentStatus { CONFIGURANDO, EN_CURSO, FINALIZADO }

@Entity(
    tableName = "teams",
    foreignKeys = [ForeignKey(
        entity = TournamentEntity::class,
        parentColumns = ["id"],
        childColumns = ["tournamentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tournamentId")]
)
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val nombreEquipo: String,
    val nombrePersona: String,
    val orden: Int = -1
)

@Entity(
    tableName = "matches",
    foreignKeys = [ForeignKey(
        entity = TournamentEntity::class,
        parentColumns = ["id"],
        childColumns = ["tournamentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tournamentId")]
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val round: Int,
    val localTeamId: Long,
    val visitanteTeamId: Long,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val jugado: Boolean = false,
    val ganadorTeamId: Long? = null,
    val metadata: String? = null
)
