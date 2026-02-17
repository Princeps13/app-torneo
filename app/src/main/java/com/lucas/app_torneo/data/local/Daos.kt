package com.lucas.app_torneo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY creadoEn DESC")
    fun getAllFlow(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TournamentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tournament: TournamentEntity): Long

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getById(id: Long): TournamentEntity?

    @Update
    suspend fun update(tournament: TournamentEntity)

    @Delete
    suspend fun delete(tournament: TournamentEntity)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId ORDER BY orden ASC, id ASC")
    fun getByTournamentFlow(tournamentId: Long): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId ORDER BY id ASC")
    suspend fun getByTournament(tournamentId: Long): List<TeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: TeamEntity): Long

    @Update
    suspend fun update(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY round ASC, id ASC")
    fun getByTournamentFlow(tournamentId: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY round ASC, id ASC")
    suspend fun getByTournament(tournamentId: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId AND round = :round ORDER BY id ASC")
    suspend fun getByTournamentAndRound(tournamentId: Long, round: Int): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(matches: List<MatchEntity>)

    @Update
    suspend fun update(match: MatchEntity)

    @Query("DELETE FROM matches WHERE tournamentId = :tournamentId")
    suspend fun deleteByTournament(tournamentId: Long)
}
