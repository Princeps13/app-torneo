package com.lucas.app_torneo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lucas.app_torneo.data.local.MatchEntity
import com.lucas.app_torneo.data.local.TeamEntity
import com.lucas.app_torneo.domain.StandingRow

@Composable
fun TeamRow(team: TeamEntity, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${team.nombreEquipo} (${team.nombrePersona})")
        Button(onClick = onDelete) { Text("Eliminar") }
    }
}

@Composable
fun MatchCard(
    match: MatchEntity,
    localName: String,
    visitName: String,
    onSave: (Int, Int, Boolean) -> Unit
) {
    var gl by remember { mutableStateOf(match.golesLocal?.toString() ?: "") }
    var gv by remember { mutableStateOf(match.golesVisitante?.toString() ?: "") }
    var winnerLocal by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$localName vs $visitName")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = gl, onValueChange = { gl = it.filter(Char::isDigit) }, label = { Text("Goles local") })
                OutlinedTextField(value = gv, onValueChange = { gv = it.filter(Char::isDigit) }, label = { Text("Goles visitante") })
            }
            if (gl.isNotBlank() && gv.isNotBlank() && gl == gv) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { winnerLocal = true }) { Text("Gana local") }
                    Button(onClick = { winnerLocal = false }) { Text("Gana visitante") }
                }
            }
            Button(onClick = {
                val a = gl.toIntOrNull() ?: return@Button
                val b = gv.toIntOrNull() ?: return@Button
                onSave(a, b, winnerLocal)
            }) { Text("Guardar resultado") }
        }
    }
}

@Composable
fun StandingsTable(rows: List<StandingRow>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach {
            Text("${it.team.nombreEquipo}: PJ ${it.pj} | PG ${it.pg} | PE ${it.pe} | PP ${it.pp} | GF ${it.gf} | GC ${it.gc} | DG ${it.dg} | Pts ${it.pts}")
        }
    }
}
