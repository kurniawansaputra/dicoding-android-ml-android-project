package com.dicoding.asclepius.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.asclepius.data.local.Asclepius
import com.dicoding.asclepius.data.local.AsclepiusDao
import com.dicoding.asclepius.data.local.AsclepiusRoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AsclepiusRepository(application: Application) {
    private val mAsclepiusDao: AsclepiusDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = AsclepiusRoomDatabase.AsclepiusRoomDatabase.getDatabase(application)
        mAsclepiusDao = db.asclepiusDao()
    }

    fun getAllAsclepius(): LiveData<List<Asclepius>> = mAsclepiusDao.getAllAsclepius()

    fun insert(asclepius: Asclepius) {
        executorService.execute { mAsclepiusDao.insert(asclepius) }
    }

    fun delete(asclepius: Asclepius) {
        executorService.execute { mAsclepiusDao.delete(asclepius) }
    }

    fun update(asclepius: Asclepius) {
        executorService.execute { mAsclepiusDao.update(asclepius) }
    }

    fun deleteAll() {
        executorService.execute { mAsclepiusDao.deleteAll() }
    }
}