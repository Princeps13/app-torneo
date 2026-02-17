package com.lucas.app_torneo.repository

import com.lucas.app_torneo.data.local.MatchDao
import com.lucas.app_torneo.data.local.MatchEntity
import com.lucas.app_torneo.data.local.TeamDao
import com.lucas.app_torneo.data.local.TeamEntity
import com.lucas.app_torneo.data.local.TournamentDao
import com.lucas.app_torneo.data.local.TournamentEntity
import com.lucas.app_torneo.data.local.TournamentStatus
import com.lucas.app_torneo.data.local.TournamentType
import com.lucas.app_torneo.domain.generateKnockoutBracket
import com.lucas.app_torneo.domain.generateLeagueSchedule
import kotlinx.coroutines.flow.Flow

class TournamentRepository(
    private val tournamentDao: TournamentDao,
    private val teamDao: TeamDao,
    private val matchDao: MatchDao
) {
    fun tournamentsFlow(): Flow<List<TournamentEntity>> = tournamentDao.getAllFlow()
    fun tournamentFlow(id: Long): Flow<TournamentEntity?> = tournamentDao.getByIdFlow(id)
    suspend fun getTournament(id: Long): TournamentEntity? = tournamentDao.getById(id)

    suspend fun createTournament(nombre: String, tipo: TournamentType): Long {
        return tournamentDao.insert(
            TournamentEntity(
                nombre = nombre,
                tipo = tipo,
                creadoEn = System.currentTimeMillis(),
                estado = TournamentStatus.CONFIGURANDO,
                seedRandom = 0L
            )
        )
    }

    suspend fun startTournament(tournament: TournamentEntity, teams: List<TeamEntity>) {
        val seed = System.currentTimeMillis()
        val randomized = teams.shuffled(kotlin.random.Random(seed))
        randomized.forEachIndexed { index, team -> teamDao.update(team.copy(orden = index + 1)) }

        val savedTeams = teamDao.getByTournament(tournament.id)
        val matches = if (tournament.tipo == TournamentType.LIGA) {
            generateLeagueSchedule(tournament.id, savedTeams, seed)
        } else {
            generateKnockoutBracket(tournament.id, savedTeams, seed)
        }
        matchDao.deleteByTournament(tournament.id)
        matchDao.insertAll(matches)
        tournamentDao.update(tournament.copy(seedRandom = seed, estado = TournamentStatus.EN_CURSO))
    }

    suspend fun updateStatus(tournament: TournamentEntity, status: TournamentStatus) {
        tournamentDao.update(tournament.copy(estado = status))
    }

    suspend fun resetTournament(tournament: TournamentEntity) {
        matchDao.deleteByTournament(tournament.id)
        tournamentDao.update(tournament.copy(estado = TournamentStatus.CONFIGURANDO, seedRandom = 0L))
    }
}

class TeamRepository(private val dao: TeamDao) {
    fun teamsFlow(tournamentId: Long): Flow<List<TeamEntity>> = dao.getByTournamentFlow(tournamentId)
    suspend fun getTeams(tournamentId: Long): List<TeamEntity> = dao.getByTournament(tournamentId)
    suspend fun addTeam(tournamentId: Long, nombreEquipo: String, nombrePersona: String) {
        dao.insert(TeamEntity(tournamentId = tournamentId, nombreEquipo = nombreEquipo, nombrePersona = nombrePersona))
    }
    suspend fun deleteTeam(team: TeamEntity) = dao.delete(team)
}

class MatchRepository(private val dao: MatchDao) {
    fun matchesFlow(tournamentId: Long): Flow<List<MatchEntity>> = dao.getByTournamentFlow(tournamentId)
    suspend fun getMatches(tournamentId: Long): List<MatchEntity> = dao.getByTournament(tournamentId)
    suspend fun updateMatch(match: MatchEntity) = dao.update(match)
    suspend fun insertAll(matches: List<MatchEntity>) = dao.insertAll(matches)
    suspend fun getByRound(tournamentId: Long, round: Int): List<MatchEntity> = dao.getByTournamentAndRound(tournamentId, round)
}
