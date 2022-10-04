package com.eywa.projectclava.main.database.match

class MatchRepo(
        private val matchDao: MatchDao,
        private val matchPlayerDao: MatchPlayerDao
) {
    fun getAll() = matchDao.getAll()
    suspend fun insert(vararg match: DatabaseMatch) = matchDao.insertAll(*match)
    suspend fun insert(vararg matchPlayers: DatabaseMatchPlayer) = matchPlayerDao.insertAll(*matchPlayers)
    suspend fun delete(match: DatabaseMatch) = matchDao.delete(match)
    suspend fun delete(matchPlayer: DatabaseMatchPlayer) = matchPlayerDao.delete(matchPlayer)
    suspend fun update(vararg matches: DatabaseMatch) = matchDao.update(*matches)
}