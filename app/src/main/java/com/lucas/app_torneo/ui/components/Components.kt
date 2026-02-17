package com.lucas.app_torneo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (match.jugado) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$localName vs $visitName", style = MaterialTheme.typography.titleMedium)
            if (match.jugado) {
                Text(
                    text = "Resultado: ${match.golesLocal ?: 0} - ${match.golesVisitante ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
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
}

@Composable
fun BracketView(
    rounds: List<List<MatchEntity>>,
    roundLabels: List<String>,
    teamName: (Long) -> String
) {
    val scrollState = rememberScrollState()
    val columnWidth = 220.dp
    val matchCardHeight = 82.dp
    val verticalGap = 18.dp
    val spacingMultiplier = 1.8f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rounds.forEachIndexed { roundIndex, matches ->
            val spacing = verticalGap * (1f + roundIndex * spacingMultiplier)
            Column(
                modifier = Modifier.width(columnWidth),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = roundLabels.getOrElse(roundIndex) { "Ronda ${roundIndex + 1}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                    matches.forEach { match ->
                        BracketMatchBlock(match = match, teamName = teamName, height = matchCardHeight)
                    }
                }
            }
        }
    }
}

@Composable
private fun BracketMatchBlock(match: MatchEntity, teamName: (Long) -> String, height: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(height)
                .clip(RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(teamName(match.localTeamId), maxLines = 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray.copy(alpha = 0.35f))
                )
                Text(teamName(match.visitanteTeamId), maxLines = 1)
            }
        }
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        )
    }
}

@Composable
fun StandingsTable(rows: List<StandingRow>) {
    val scrollState = rememberScrollState()
    val teamColumnWidth = 180.dp
    val statColumnWidth = 56.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Equipo", teamColumnWidth, TextAlign.Start)
                listOf("PJ", "PG", "PE", "PP", "GF", "GC", "DG", "Pts").forEach { title ->
                    TableHeaderCell(title, statColumnWidth, TextAlign.Center)
                }
            }

            rows.forEachIndexed { index, row ->
                val rowModifier = if (index % 2 == 0) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
                Row(
                    modifier = rowModifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableValueCell(row.team.nombreEquipo, teamColumnWidth, TextAlign.Start, FontWeight.Medium)
                    TableValueCell(row.pj.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.pg.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.pe.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.pp.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.gf.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.gc.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.dg.toString(), statColumnWidth, TextAlign.Center)
                    TableValueCell(row.pts.toString(), statColumnWidth, TextAlign.Center, FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp, align: TextAlign) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = align,
        modifier = Modifier.width(width).padding(horizontal = 4.dp)
    )
}

@Composable
private fun TableValueCell(
    text: String,
    width: Dp,
    align: TextAlign,
    weight: FontWeight? = null
) {
    Text(
        text = text,
        textAlign = align,
        fontWeight = weight,
        modifier = Modifier.width(width).padding(horizontal = 4.dp)
    )
}
