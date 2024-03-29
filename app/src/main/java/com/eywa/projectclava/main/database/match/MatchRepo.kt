package com.eywa.projectclava.main.database.match

class MatchRepo(
        private val matchDao: MatchDao,
        private val matchPlayerDao: MatchPlayerDao
) {
    fun getAll() = matchDao.getAll()
    suspend fun insert(match: DatabaseMatch) = matchDao.insert(match)
    suspend fun insert(vararg matchPlayers: DatabaseMatchPlayer) =
        matchPlayerDao.insertAll(*matchPlayers)

    suspend fun delete(match: DatabaseMatch) = matchDao.delete(match)
    suspend fun delete(matchPlayer: DatabaseMatchPlayer) = matchPlayerDao.delete(matchPlayer)
    suspend fun deleteAll() = matchDao.deleteAll()
    suspend fun update(vararg matches: DatabaseMatch) = matchDao.update(*matches)
    suspend fun updateSoundHappened(matches: List<DatabaseMatch>) =
        matchDao.updateSoundHappened(matches.map { it.id })
}
