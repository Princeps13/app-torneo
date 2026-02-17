package com.lucas.app_torneo.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTournamentType(value: TournamentType): String = value.name

    @TypeConverter
    fun toTournamentType(value: String): TournamentType = TournamentType.valueOf(value)

    @TypeConverter
    fun fromTournamentStatus(value: TournamentStatus): String = value.name

    @TypeConverter
    fun toTournamentStatus(value: String): TournamentStatus = TournamentStatus.valueOf(value)
}
