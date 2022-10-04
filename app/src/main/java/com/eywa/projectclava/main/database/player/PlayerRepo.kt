package com.eywa.projectclava.main.database.player

class PlayerRepo(private val playerDao: PlayerDao) {
    fun getAll() = playerDao.getAll()
    suspend fun insertAll(vararg players: DatabasePlayer) = playerDao.insertAll(*players)
    suspend fun delete(player: DatabasePlayer) = playerDao.delete(player)
    suspend fun update(vararg players: DatabasePlayer) = playerDao.update(*players)
}