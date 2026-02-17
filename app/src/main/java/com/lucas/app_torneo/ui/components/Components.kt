package com.lucas.app_torneo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text("$localName vs $visitName", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = gl,
                    onValueChange = { gl = it.filter(Char::isDigit) },
                    label = { Text("Goles local") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = gv,
                    onValueChange = { gv = it.filter(Char::isDigit) },
                    label = { Text("Goles visitante") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f)
                )
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
    val scrollState = rememberScrollState()
    val columns = listOf(
        "Equipo" to 2.4f,
        "PJ" to 0.8f,
        "PG" to 0.8f,
        "PE" to 0.8f,
        "PP" to 0.8f,
        "GF" to 0.9f,
        "GC" to 0.9f,
        "DG" to 0.9f,
        "Pts" to 1f
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEach { (title, weight) ->
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = if (title == "Equipo") TextAlign.Start else TextAlign.Center,
                            modifier = Modifier.weight(weight)
                        )
                    }
                }
            }

            rows.forEachIndexed { index, row ->
                val rowModifier = if (index % 2 == 0) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                } else {
                    Modifier
                }

                Row(
                    modifier = rowModifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.team.nombreEquipo, modifier = Modifier.weight(2.4f), fontWeight = FontWeight.Medium)
                    Text(row.pj.toString(), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(row.pg.toString(), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(row.pe.toString(), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(row.pp.toString(), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(row.gf.toString(), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                    Text(row.gc.toString(), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                    Text(row.dg.toString(), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                    Text(row.pts.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
