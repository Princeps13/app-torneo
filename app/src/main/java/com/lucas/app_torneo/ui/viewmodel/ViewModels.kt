package com.lucas.app_torneo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lucas.app_torneo.data.local.MatchEntity
import com.lucas.app_torneo.data.local.TeamEntity
import com.lucas.app_torneo.data.local.TournamentEntity
import com.lucas.app_torneo.data.local.TournamentStatus
import com.lucas.app_torneo.data.local.TournamentType
import com.lucas.app_torneo.domain.StandingRow
import com.lucas.app_torneo.domain.computeStandings
import com.lucas.app_torneo.repository.MatchRepository
import com.lucas.app_torneo.repository.TeamRepository
import com.lucas.app_torneo.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreateTournamentUiState(
    val step: Int = 1,
    val nombre: String = "",
    val tipo: TournamentType = TournamentType.LIGA,
    val tournamentId: Long? = null,
    val teams: List<TeamEntity> = emptyList(),
    val partidosPorCruce: String = "1",
    val error: String? = null
)

data class TournamentDetailUiState(
    val tournament: TournamentEntity? = null,
    val teams: List<TeamEntity> = emptyList(),
    val matches: List<MatchEntity> = emptyList(),
    val standings: List<StandingRow> = emptyList(),
    val error: String? = null
)

class HomeViewModel(private val repo: TournamentRepository) : ViewModel() {
    val tournaments = repo.tournamentsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteTournament(tournament: TournamentEntity) {
        viewModelScope.launch { repo.deleteTournament(tournament) }
    }
}

class CreateTournamentViewModel(
    private val tournamentRepo: TournamentRepository,
    private val teamRepo: TeamRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(CreateTournamentUiState())
    val ui: StateFlow<CreateTournamentUiState> = _ui

    fun setNombre(nombre: String) { _ui.value = _ui.value.copy(nombre = nombre, error = null) }
    fun setTipo(tipo: TournamentType) { _ui.value = _ui.value.copy(tipo = tipo, error = null) }

    fun setPartidosPorCruce(value: String) {
        _ui.value = _ui.value.copy(partidosPorCruce = value.filter(Char::isDigit), error = null)
    }

    fun createTournamentStep() {
        viewModelScope.launch {
            if (_ui.value.nombre.isBlank()) {
                _ui.value = _ui.value.copy(error = "Ingresá un nombre de torneo")
                return@launch
            }
            val id = tournamentRepo.createTournament(_ui.value.nombre.trim(), _ui.value.tipo)
            _ui.value = _ui.value.copy(step = 2, tournamentId = id, error = null)
            refreshTeams()
        }
    }

    fun refreshTeams() {
        val id = _ui.value.tournamentId ?: return
        viewModelScope.launch { _ui.value = _ui.value.copy(teams = teamRepo.getTeams(id)) }
    }

    fun addTeam(nombreEquipo: String, nombrePersona: String) {
        val id = _ui.value.tournamentId ?: return
        viewModelScope.launch {
            if (nombreEquipo.isBlank() || nombrePersona.isBlank()) {
                _ui.value = _ui.value.copy(error = "Completá equipo y persona")
                return@launch
            }
            teamRepo.addTeam(id, nombreEquipo.trim(), nombrePersona.trim())
            _ui.value = _ui.value.copy(error = null)
            refreshTeams()
        }
    }

    fun deleteTeam(team: TeamEntity) {
        viewModelScope.launch { teamRepo.deleteTeam(team); refreshTeams() }
    }

    fun startTournament(onStarted: (Long) -> Unit) {
        val id = _ui.value.tournamentId ?: return
        viewModelScope.launch {
            val teams = teamRepo.getTeams(id)
            val min = if (_ui.value.tipo == TournamentType.LIGA) 3 else 2
            if (teams.size < min) {
                _ui.value = _ui.value.copy(error = "Cantidad mínima de equipos: $min")
                return@launch
            }
            val tournament = tournamentRepo.getTournament(id)
            if (tournament != null) {
                val partidosPorCruce = if (_ui.value.tipo == TournamentType.LIGA) {
                    _ui.value.partidosPorCruce.toIntOrNull()?.coerceAtLeast(1) ?: run {
                        _ui.value = _ui.value.copy(error = "Ingresá una cantidad válida de partidos por cruce")
                        return@launch
                    }
                } else 1

                tournamentRepo.startTournament(tournament, teams, partidosPorCruce)
                onStarted(id)
            }
        }
    }
}

class TournamentDetailViewModel(
    tournamentId: Long,
    private val tournamentRepo: TournamentRepository,
    private val teamRepo: TeamRepository,
    private val matchRepo: MatchRepository
) : ViewModel() {
    val ui: StateFlow<TournamentDetailUiState> = combine(
        tournamentRepo.tournamentFlow(tournamentId),
        teamRepo.teamsFlow(tournamentId),
        matchRepo.matchesFlow(tournamentId)
    ) { t, teams, matches ->
        TournamentDetailUiState(
            tournament = t,
            teams = teams,
            matches = matches,
            standings = if (t?.tipo == TournamentType.LIGA) computeStandings(matches, teams) else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TournamentDetailUiState())

    fun saveResult(match: MatchEntity, gl: Int, gv: Int, winnerOnDraw: Long? = null) {
        viewModelScope.launch {
            if (gl < 0 || gv < 0) return@launch
            val tournament = ui.value.tournament ?: return@launch
            val winner = if (tournament.tipo == TournamentType.LLAVES) {
                when {
                    gl > gv -> match.localTeamId
                    gv > gl -> match.visitanteTeamId
                    else -> winnerOnDraw
                }
            } else null
            if (tournament.tipo == TournamentType.LLAVES && winner == null) return@launch
            matchRepo.updateMatch(match.copy(golesLocal = gl, golesVisitante = gv, jugado = true, ganadorTeamId = winner))
            if (tournament.tipo == TournamentType.LLAVES) advanceKnockout(tournament)
            if (tournament.tipo == TournamentType.LIGA && ui.value.matches.all { it.jugado }) {
                tournamentRepo.updateStatus(tournament, TournamentStatus.FINALIZADO)
            }
        }
    }

    private suspend fun advanceKnockout(tournament: TournamentEntity) {
        val allMatches = matchRepo.getMatches(tournament.id)
        val currentRound = allMatches.maxOfOrNull { it.round } ?: 1
        val roundMatches = allMatches.filter { it.round == currentRound }
        if (roundMatches.isEmpty() || roundMatches.any { !it.jugado || it.ganadorTeamId == null }) return

        val winners = roundMatches.mapNotNull { it.ganadorTeamId }
        if (winners.size == 1) {
            tournamentRepo.updateStatus(tournament, TournamentStatus.FINALIZADO)
            return
        }
        if (allMatches.any { it.round > currentRound }) return

        val next = winners.chunked(2).mapIndexedNotNull { idx, pair ->
            if (pair.size == 2) {
                MatchEntity(
                    tournamentId = tournament.id,
                    round = currentRound + 1,
                    localTeamId = pair[0],
                    visitanteTeamId = pair[1],
                    metadata = "Ronda ${currentRound + 1} - ${idx + 1}"
                )
            } else {
                MatchEntity(
                    tournamentId = tournament.id,
                    round = currentRound + 1,
                    localTeamId = pair[0],
                    visitanteTeamId = pair[0],
                    golesLocal = 0,
                    golesVisitante = 0,
                    jugado = true,
                    ganadorTeamId = pair[0],
                    metadata = "Bye"
                )
            }
        }
        matchRepo.insertAll(next)
    }

    fun resetTournament() {
        viewModelScope.launch {
            ui.value.tournament?.let { tournamentRepo.resetTournament(it) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val container: AppContainer,
    private val tournamentId: Long? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.tournamentRepository) as T
            modelClass.isAssignableFrom(CreateTournamentViewModel::class.java) ->
                CreateTournamentViewModel(container.tournamentRepository, container.teamRepository) as T
            modelClass.isAssignableFrom(TournamentDetailViewModel::class.java) ->
                TournamentDetailViewModel(
                    tournamentId = tournamentId ?: error("tournamentId requerido"),
                    tournamentRepo = container.tournamentRepository,
                    teamRepo = container.teamRepository,
                    matchRepo = container.matchRepository
                ) as T
            else -> error("ViewModel no soportado")
        }
    }
}
