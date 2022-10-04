package com.eywa.projectclava.main.database.court

class CourtRepo(private val courtDao: CourtDao) {
    fun getAll() = courtDao.getAll()
    suspend fun insertAll(vararg courts: DatabaseCourt) = courtDao.insertAll(*courts)
    suspend fun delete(court: DatabaseCourt) = courtDao.delete(court)
    suspend fun update(vararg courts: DatabaseCourt) = courtDao.update(*courts)
}