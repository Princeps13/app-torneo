package com.lucas.app_torneo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucas.app_torneo.data.local.TournamentType
import com.lucas.app_torneo.ui.components.MatchCard
import com.lucas.app_torneo.ui.components.StandingsTable
import com.lucas.app_torneo.ui.components.TeamRow
import com.lucas.app_torneo.ui.viewmodel.CreateTournamentViewModel
import com.lucas.app_torneo.ui.viewmodel.HomeViewModel
import com.lucas.app_torneo.ui.viewmodel.TournamentDetailViewModel

@Composable
fun HomeScreen(vm: HomeViewModel, onCreate: () -> Unit, onOpenTournament: (Long) -> Unit) {
    val tournaments by vm.tournaments.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onCreate) { Text("Crear torneo") }
        if (tournaments.isEmpty()) Text("No hay torneos cargados")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tournaments) { t ->
                Button(onClick = { onOpenTournament(t.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${t.nombre} - ${t.tipo} - ${t.estado}")
                }
            }
        }
    }
}

@Composable
fun CreateTournamentScreen(vm: CreateTournamentViewModel, onStarted: (Long) -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var equipo by remember { mutableStateOf("") }
    var persona by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Crear torneo")
        if (ui.step == 1) {
            TextField(value = ui.nombre, onValueChange = vm::setNombre, label = { Text("Nombre") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setTipo(TournamentType.LIGA) }) { Text("Liga") }
                Button(onClick = { vm.setTipo(TournamentType.LLAVES) }) { Text("Llaves") }
            }
            Button(onClick = vm::createTournamentStep) { Text("Siguiente") }
        } else {
            Text("Tipo: ${ui.tipo}")
            TextField(value = equipo, onValueChange = { equipo = it }, label = { Text("Nombre del equipo") })
            TextField(value = persona, onValueChange = { persona = it }, label = { Text("Nombre de la persona") })
            Button(onClick = { vm.addTeam(equipo, persona); equipo = ""; persona = "" }) { Text("Agregar equipo") }
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(ui.teams) { team -> TeamRow(team = team, onDelete = { vm.deleteTeam(team) }) }
            }
            Button(onClick = { vm.startTournament(onStarted) }) { Text("Iniciar torneo") }
        }
        ui.error?.let { Text(it) }
    }
}

@Composable
fun TournamentDetailScreen(vm: TournamentDetailViewModel) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = when (ui.tournament?.tipo) {
        TournamentType.LIGA -> listOf("Partidos", "Tabla")
        TournamentType.LLAVES -> listOf("Partidos", "Llaves")
        else -> listOf("Partidos")
    }
    val teamMap = ui.teams.associateBy { it.id }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(ui.tournament?.nombre ?: "Cargando...")
        Text("${ui.tournament?.tipo} - ${ui.tournament?.estado}")
        Button(onClick = vm::resetTournament) { Text("Reiniciar torneo") }
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, text ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(text) })
            }
        }
        when (tabs[selectedTab]) {
            "Partidos" -> LazyColumn {
                ui.matches.groupBy { it.round }.forEach { (round, matchesRound) ->
                    item { Text("Ronda/Jornada $round") }
                    items(matchesRound) { m ->
                        val local = teamMap[m.localTeamId]
                        val visit = teamMap[m.visitanteTeamId]
                        MatchCard(
                            match = m,
                            localName = "${local?.nombreEquipo} (${local?.nombrePersona})",
                            visitName = "${visit?.nombreEquipo} (${visit?.nombrePersona})"
                        ) { gl, gv, winnerLocal ->
                            vm.saveResult(m, gl, gv, if (gl == gv) if (winnerLocal) m.localTeamId else m.visitanteTeamId else null)
                        }
                    }
                }
            }
            "Tabla" -> StandingsTable(ui.standings)
            "Llaves" -> LazyColumn {
                ui.matches.groupBy { it.round }.forEach { (round, matchesRound) ->
                    item { Text("Ronda $round") }
                    items(matchesRound) { m ->
                        val local = teamMap[m.localTeamId]?.nombreEquipo ?: "-"
                        val visit = teamMap[m.visitanteTeamId]?.nombreEquipo ?: "-"
                        Text("$local vs $visit")
                    }
                }
            }
        }
        ui.error?.let { Text(it) }
    }
}
