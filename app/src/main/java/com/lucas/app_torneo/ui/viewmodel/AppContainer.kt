package com.lucas.app_torneo.ui.viewmodel

import android.content.Context
import com.lucas.app_torneo.data.local.AppDatabase
import com.lucas.app_torneo.repository.MatchRepository
import com.lucas.app_torneo.repository.TeamRepository
import com.lucas.app_torneo.repository.TournamentRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)
    val tournamentRepository = TournamentRepository(db.tournamentDao(), db.teamDao(), db.matchDao())
    val teamRepository = TeamRepository(db.teamDao())
    val matchRepository = MatchRepository(db.matchDao())
}
