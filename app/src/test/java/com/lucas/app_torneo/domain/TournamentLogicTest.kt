package com.lucas.app_torneo.domain

import com.lucas.app_torneo.data.local.MatchEntity
import com.lucas.app_torneo.data.local.TeamEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentLogicTest {
    @Test
    fun computeStandings_ok() {
        val teams = listOf(
            TeamEntity(id = 1, tournamentId = 1, nombreEquipo = "A", nombrePersona = "P1"),
            TeamEntity(id = 2, tournamentId = 1, nombreEquipo = "B", nombrePersona = "P2"),
            TeamEntity(id = 3, tournamentId = 1, nombreEquipo = "C", nombrePersona = "P3")
        )
        val matches = listOf(
            MatchEntity(tournamentId = 1, round = 1, localTeamId = 1, visitanteTeamId = 2, golesLocal = 2, golesVisitante = 0, jugado = true),
            MatchEntity(tournamentId = 1, round = 2, localTeamId = 1, visitanteTeamId = 3, golesLocal = 1, golesVisitante = 1, jugado = true)
        )
        val table = computeStandings(matches, teams)
        assertEquals(4, table.first().pts)
        assertEquals("A", table.first().team.nombreEquipo)
    }

    @Test
    fun generateLeagueSchedule_countMatches() {
        val teams = (1L..4L).map {
            TeamEntity(id = it, tournamentId = 1, nombreEquipo = "E$it", nombrePersona = "P$it")
        }
        val matches = generateLeagueSchedule(1, teams, 123)
        assertEquals(6, matches.size)
    }

    @Test
    fun generateKnockoutBracket_handlesByes() {
        val teams = (1L..6L).map {
            TeamEntity(id = it, tournamentId = 1, nombreEquipo = "E$it", nombrePersona = "P$it")
        }
        val matches = generateKnockoutBracket(1, teams, 10)
        val byeMatches = matches.count { it.localTeamId == it.visitanteTeamId && it.jugado }
        assertTrue(byeMatches > 0)
    }
}
