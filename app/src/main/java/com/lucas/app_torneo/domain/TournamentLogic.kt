package com.lucas.app_torneo.domain

import com.lucas.app_torneo.data.local.MatchEntity
import com.lucas.app_torneo.data.local.TeamEntity
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.random.Random

data class StandingRow(
    val team: TeamEntity,
    val pj: Int,
    val pg: Int,
    val pe: Int,
    val pp: Int,
    val gf: Int,
    val gc: Int,
    val dg: Int,
    val pts: Int
)

fun generateLeagueSchedule(
    tournamentId: Long,
    teams: List<TeamEntity>,
    seed: Long,
    matchesPerPair: Int = 1
): List<MatchEntity> {
    val shuffled = teams.shuffled(Random(seed)).toMutableList()
    if (shuffled.size % 2 != 0) {
        shuffled.add(TeamEntity(id = -1L, tournamentId = tournamentId, nombreEquipo = "DESCANSA", nombrePersona = "-"))
    }
    val n = shuffled.size
    if (n < 2) return emptyList()
    val rounds = n - 1
    val matches = mutableListOf<MatchEntity>()
    val rotation = shuffled.toMutableList()

    val safeMatchesPerPair = matchesPerPair.coerceAtLeast(1)

    repeat(safeMatchesPerPair) { cycle ->
        for (r in 0 until rounds) {
            for (i in 0 until n / 2) {
                val a = rotation[i]
                val b = rotation[n - 1 - i]
                if (a.id != -1L && b.id != -1L) {
                    val isEvenCycle = cycle % 2 == 1
                    val local = if (isEvenCycle) b else a
                    val visitante = if (isEvenCycle) a else b
                    val roundNumber = (cycle * rounds) + r + 1

                    matches.add(
                        MatchEntity(
                            tournamentId = tournamentId,
                            round = roundNumber,
                            localTeamId = local.id,
                            visitanteTeamId = visitante.id,
                            metadata = "Jornada $roundNumber"
                        )
                    )
                }
            }

            val fixed = rotation.first()
            val rest = rotation.drop(1).toMutableList()
            rest.add(0, rest.removeAt(rest.lastIndex))
            rotation.clear()
            rotation.add(fixed)
            rotation.addAll(rest)
        }
    }
    return matches
}

fun generateKnockoutBracket(tournamentId: Long, teams: List<TeamEntity>, seed: Long): List<MatchEntity> {
    if (teams.size < 2) return emptyList()
    val shuffled = teams.shuffled(Random(seed))
    val size = nextPowerOfTwo(teams.size)
    val byes = size - teams.size
    val directAdvance = shuffled.take(byes)
    val playIn = shuffled.drop(byes)

    val matches = mutableListOf<MatchEntity>()
    var idx = 0
    while (idx < playIn.size) {
        val local = playIn[idx]
        val visitante = playIn[idx + 1]
        matches.add(
            MatchEntity(
                tournamentId = tournamentId,
                round = 1,
                localTeamId = local.id,
                visitanteTeamId = visitante.id,
                metadata = "Ronda 1"
            )
        )
        idx += 2
    }
    directAdvance.forEachIndexed { index, team ->
        matches.add(
            MatchEntity(
                tournamentId = tournamentId,
                round = 1,
                localTeamId = team.id,
                visitanteTeamId = team.id,
                golesLocal = 0,
                golesVisitante = 0,
                jugado = true,
                ganadorTeamId = team.id,
                metadata = "Bye ${index + 1}"
            )
        )
    }
    return matches
}

fun computeStandings(matches: List<MatchEntity>, teams: List<TeamEntity>): List<StandingRow> {
    val played = matches.filter { it.jugado && it.golesLocal != null && it.golesVisitante != null }
    return teams.map { team ->
        var pj = 0; var pg = 0; var pe = 0; var pp = 0; var gf = 0; var gc = 0
        played.forEach { m ->
            if (m.localTeamId == team.id || m.visitanteTeamId == team.id) {
                pj++
                val golesTeam: Int
                val golesRival: Int
                if (m.localTeamId == team.id) {
                    golesTeam = m.golesLocal!!; golesRival = m.golesVisitante!!
                } else {
                    golesTeam = m.golesVisitante!!; golesRival = m.golesLocal!!
                }
                gf += golesTeam; gc += golesRival
                when {
                    golesTeam > golesRival -> pg++
                    golesTeam == golesRival -> pe++
                    else -> pp++
                }
            }
        }
        val pts = pg * 3 + pe
        StandingRow(team, pj, pg, pe, pp, gf, gc, gf - gc, pts)
    }.sortedWith(compareByDescending<StandingRow> { it.pts }.thenByDescending { it.dg }.thenByDescending { it.gf })
}

fun nextPowerOfTwo(n: Int): Int {
    if (n <= 1) return 1
    val exp = ceil(log2(n.toDouble())).toInt()
    return 1 shl exp
}
